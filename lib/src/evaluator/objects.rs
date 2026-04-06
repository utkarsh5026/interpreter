//! Runtime object system for the Mutant Lang interpreter.
//!
//! Every value that can exist at runtime is represented as an [`Object`] variant.
//! This mirrors the Java `exec/objects/` package but uses a single Rust enum
//! rather than a class hierarchy, leveraging pattern matching instead of
//! dynamic dispatch.
//!
//! # Object lifecycle
//!
//! Objects are owned values: they are cloned when bound into an environment and
//! when passed as function arguments. The deliberate use of `Clone` (rather than
//! `Rc`/`Arc` sharing) keeps the ownership model simple at the cost of some extra
//! allocations for large composites like arrays and hashes.
//!
//! # Control-flow sentinels
//!
//! [`Object::Return`], [`Object::Break`], and [`Object::Continue`] are not
//! "real" values — they are short-circuit signals propagated up the evaluator
//! call stack until a statement handler consumes them (e.g. a `return` statement
//! unwraps [`Object::Return`]; a loop body unwraps [`Object::Break`]).
//!
//! # Hash keys
//!
//! Only [`Object::Integer`], [`Object::Boolean`], and [`Object::Str`] can appear
//! as hash-literal keys. [`HashKey`] is the typed, `Hash`-able projection of
//! those three variants; conversion goes through [`TryFrom<Object>`] for
//! [`HashKey`].

use crate::ast::expression::Indentifier;
use crate::ast::statements::BlockStatement;
use crate::evaluator::env::{Env, Environment};
use std::collections::HashMap;
use std::fmt;

use super::EvalError;

/// Hashable key type for Mutant Lang hash literals.
///
/// Only integers, booleans, and strings are valid hash-literal keys in Mutant
/// Lang. Wrapping them in this enum lets the interpreter store them in a
/// [`HashMap`] without boxing, while keeping the type-level guarantee that
/// un-hashable objects (arrays, functions, …) can never appear as keys.
///
/// Convert an [`Object`] to a `HashKey` via [`HashKey::try_from`]; the
/// conversion fails with [`EvalError::TypeMismatch`] for any non-hashable
/// variant.
#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub enum HashKey {
    /// An integer key, e.g. `{1: "one"}`.
    Integer(i64),
    /// A boolean key, e.g. `{true: "yes"}`.
    Boolean(bool),
    /// A string key, e.g. `{"name": "Alice"}`.
    Str(String),
}

/// A first-class function value with a captured lexical environment.
///
/// `FunctionObject` is the runtime representation of a `fn(…) { … }`
/// literal. It bundles the parameter list, body AST, and the environment
/// *at the point of definition* so that closures work correctly — the body
/// is evaluated in a child of `env`, not in the caller's environment.
///
/// Cloning a `FunctionObject` is cheap for small closures but clones the
/// entire `env` chain, so deeply nested closures may carry non-trivial
/// allocation overhead.
#[derive(Debug, Clone)]
pub struct FunctionObject {
    /// Formal parameters in declaration order.
    ///
    /// At call time these are zipped with the argument list and the counts
    /// must match exactly — a mismatch raises [`EvalError::WrongArgCount`].
    pub params: Vec<Indentifier>,

    /// The function body, evaluated each time the function is called.
    pub body: BlockStatement,

    /// The lexical environment captured at the point of definition.
    ///
    /// Used as the outer (parent) scope when [`make_call_env`](Self::make_call_env)
    /// builds the call-site environment, implementing lexical scoping and
    /// closures.
    pub env: Env,
}

impl FunctionObject {
    /// Construct a `FunctionObject` from its constituent parts.
    ///
    /// Takes ownership of `params`, `body`, and `env`. Called by
    /// [`Object::function`] and the class-method evaluator when a `fn` literal
    /// or method definition is evaluated.
    pub const fn new(params: Vec<Indentifier>, body: BlockStatement, env: Env) -> Self {
        Self { params, body, env }
    }

    /// Create a call-site environment with arguments bound to parameters.
    ///
    /// Uses the function's captured `env` as the parent so that lexical
    /// scoping works correctly — the body sees variables from the closure's
    /// definition site, not the call site.
    ///
    /// Parameters and arguments are paired by position via `zip`. If the
    /// caller provides fewer arguments than parameters, the trailing parameters
    /// simply have no binding in the returned environment. Extra arguments are
    /// silently dropped.
    ///
    /// # Examples
    ///
    /// ```rust,ignore
    /// // Given: fn(x, y) { x + y } called with args [Object::Integer(1), Object::Integer(2)]
    /// let call_env = func.make_call_env(vec![Object::integer(1), Object::integer(2)]);
    /// // call_env now has x=1, y=2, with func.env as parent scope
    /// ```
    #[must_use]
    pub fn make_call_env(&self, args: Vec<Object>) -> Env {
        let call_env = Environment::new_child(&self.env, false);
        for (param, arg) in self.params.iter().zip(args) {
            call_env.borrow_mut().define(param.value(), arg);
        }
        call_env
    }
}

/// Formats as `fn(param1, param2, …)` — the signature without the body.
///
/// Useful for REPL output and error messages where printing the full AST
/// body would be too noisy.
impl fmt::Display for FunctionObject {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        let ps: Vec<String> = self
            .params
            .iter()
            .map(std::string::ToString::to_string)
            .collect();
        write!(f, "fn({})", ps.join(", "))
    }
}

/// A class definition with optional single inheritance.
///
/// `ClassObject` is produced when the evaluator processes a `class` statement.
/// It stores the class name, an optional single parent for inheritance, an
/// optional constructor function, and a map of named methods.
///
/// Method lookup traverses the inheritance chain via [`find_method`](Self::find_method),
/// implementing single-inheritance dispatch without virtual dispatch tables.
///
/// # Note on boxing
///
/// `parent` is `Box<Self>` to break the recursive size cycle that would
/// otherwise make `ClassObject` an unsized type on the stack.
#[derive(Debug, Clone)]
pub struct ClassObject {
    /// The class name as declared, e.g. `"Animal"`.
    pub name: String,

    /// The immediate parent class, if any.
    ///
    /// Boxed to avoid an infinite-size recursive struct. Walk the chain with
    /// [`find_method`](Self::find_method).
    pub parent: Option<Box<Self>>,

    /// The `constructor` method, if the class declared one.
    ///
    /// Called automatically when a `new ClassName(…)` expression is evaluated.
    /// If `None`, instances are created with an empty field map.
    pub constructor: Option<Box<FunctionObject>>,

    /// Named methods defined directly on this class (not inherited).
    ///
    /// Keyed by method name. Use [`find_method`](Self::find_method) for
    /// inheritance-aware lookup.
    pub methods: HashMap<String, FunctionObject>,

    /// The environment at the point the class was defined.
    ///
    /// Used as the outer scope when method call environments are constructed,
    /// so methods can close over variables visible at class-definition time.
    pub env: Env,
}

impl ClassObject {
    /// Construct a `ClassObject` from its parts.
    ///
    /// `parent` and `constructor` are wrapped in `Box` internally; callers
    /// pass plain (unboxed) values. `name` accepts anything that implements
    /// `Into<String>` (e.g. `&str` or `String`) for ergonomic use in the
    /// evaluator.
    pub fn new(
        name: impl Into<String>,
        parent: Option<Self>,
        constructor: Option<FunctionObject>,
        methods: HashMap<String, FunctionObject>,
        env: Env,
    ) -> Self {
        Self {
            name: name.into(),
            parent: parent.map(Box::new),
            constructor: constructor.map(Box::new),
            methods,
            env,
        }
    }

    /// Walk the inheritance chain to find a method by name.
    ///
    /// Checks this class first, then each ancestor in turn.
    /// Returns `None` if no class in the chain defines the method.
    ///
    /// The returned reference borrows from `self`, so it is tied to the
    /// lifetime of this `ClassObject`.
    ///
    /// # Examples
    ///
    /// ```rust,ignore
    /// if let Some(method) = class.find_method("speak") {
    ///     let call_env = method.make_call_env(args);
    ///     // evaluate method.body in call_env …
    /// }
    /// ```
    #[must_use]
    pub fn find_method(&self, name: &str) -> Option<&FunctionObject> {
        if let Some(method) = self.methods.get(name) {
            return Some(method);
        }
        self.parent.as_deref()?.find_method(name)
    }
}

/// Formats as `<class ClassName>`.
impl fmt::Display for ClassObject {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "<class {}>", self.name)
    }
}

/// A live instance of a [`ClassObject`] with its own field storage.
///
/// Created when a `new ClassName(…)` expression is evaluated. The instance
/// owns a clone of the class definition (so the class can be redefined without
/// affecting existing instances) and a mutable field map populated by the
/// constructor and by property-assignment statements.
#[derive(Debug, Clone)]
pub struct InstanceObject {
    /// The class this instance was created from.
    ///
    /// Boxed to avoid placing a potentially large `ClassObject` inline.
    /// Cloned from the class value at construction time — mutations to the
    /// original class after instantiation do not affect this instance.
    pub class: Box<ClassObject>,

    /// Instance fields set by the constructor or by property-assignment
    /// expressions (`instance.field = value`).
    ///
    /// Starts empty; fields are added lazily via [`set_field`](Self::set_field).
    pub fields: HashMap<String, Object>,

    /// The environment associated with this instance.
    ///
    /// Used to resolve `self` and instance-level bindings during method calls.
    pub env: Env,
}

impl InstanceObject {
    /// Create a new instance of `class` with an empty field map.
    ///
    /// The constructor is *not* invoked here; the evaluator is responsible for
    /// calling `constructor` (if present) after creating the instance.
    pub fn new(class: ClassObject, env: Env) -> Self {
        Self {
            class: Box::new(class),
            fields: HashMap::new(),
            env,
        }
    }

    /// Return a reference to the field named `name`, or `None` if unset.
    ///
    /// Does not traverse the class hierarchy — only instance fields set via
    /// [`set_field`](Self::set_field) are visible here.
    pub fn get_field(&self, name: &str) -> Option<&Object> {
        self.fields.get(name)
    }

    /// Set or overwrite the instance field named `name` to `val`.
    ///
    /// `name` accepts anything `Into<String>` for ergonomic use in the
    /// evaluator. Overwrites any previously stored value for the same key.
    pub fn set_field(&mut self, name: impl Into<String>, val: Object) {
        self.fields.insert(name.into(), val);
    }

    /// Look up a method by name via the class inheritance chain.
    ///
    /// Delegates to [`ClassObject::find_method`] on the instance's class.
    /// Returns `None` if neither the class nor any ancestor defines `name`.
    pub fn find_method(&self, name: &str) -> Option<&FunctionObject> {
        self.class.find_method(name)
    }
}

/// Formats as `<instance of ClassName>`.
impl fmt::Display for InstanceObject {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "<instance of {}>", self.class.name)
    }
}

/// Every value that can exist at runtime in a Mutant Lang program.
///
/// `Object` is the central type of the evaluator. Every expression reduces to
/// an `Object`, every variable binding stores an `Object`, and every built-in
/// or user-defined function takes and returns `Object` values.
///
/// # Numeric coercion
///
/// Arithmetic operators prefer integer arithmetic when both operands are
/// [`Integer`](Self::Integer) and fall back to float arithmetic (via `f64`)
/// when either operand is a [`Float`](Self::Float). See the `Add`, `Sub`,
/// `Mul`, `Div`, and `Rem` impl for precise coercion rules.
///
/// # Control-flow variants
///
/// [`Return`](Self::Return), [`Break`](Self::Break), and
/// [`Continue`](Self::Continue) are not user-visible values. They are produced
/// by the corresponding statements and consumed by the nearest enclosing
/// construct that handles them (function body, loop body). Propagating them as
/// `Object` variants — rather than as separate `Err` cases — avoids a second
/// error channel in the evaluator's `Result` type.
#[derive(Debug, Clone)]
pub enum Object {
    /// A 64-bit signed integer value, e.g. `42` or `-7`.
    Integer(i64),

    /// A 64-bit IEEE 754 floating-point value, e.g. `3.14`.
    ///
    /// Note: `f64` does not implement `Eq` or `Hash`, so `Object` itself
    /// cannot derive those traits. Hash-literal keys must use [`HashKey`]
    /// instead.
    Float(f64),

    /// A boolean value — `true` or `false`.
    Boolean(bool),

    /// A UTF-8 string value.
    Str(String),

    /// The null / absence-of-value sentinel, analogous to `null` in Java.
    ///
    /// Evaluates as falsy in boolean contexts. See [`is_truthy`](Self::is_truthy).
    Null,

    /// A dynamically-sized ordered list of objects, e.g. `[1, 2, 3]`.
    Array(Vec<Self>),

    /// A hash map from [`HashKey`] to `Object`, e.g. `{"a": 1, true: 2}`.
    ///
    /// Only [`Integer`](Self::Integer), [`Boolean`](Self::Boolean), and
    /// [`Str`](Self::Str) values are valid keys; others produce a
    /// [`EvalError::TypeMismatch`] at runtime.
    Hash(HashMap<HashKey, Self>),

    /// A user-defined function value (closure).
    ///
    /// See [`FunctionObject`] for the captured environment and parameter list.
    Function(FunctionObject),

    /// A built-in (native Rust) function.
    ///
    /// Stored as a bare function pointer `fn(Vec<Self>) -> Self` rather than
    /// a trait object, keeping the variant `Clone` and avoiding heap
    /// allocation for the function itself.
    Builtin(fn(Vec<Self>) -> Self),

    /// A return-statement sentinel wrapping the value to be returned.
    ///
    /// Produced by evaluating a `return <expr>;` statement. Propagated up
    /// the evaluator call stack until the enclosing function body unwraps it.
    Return(Box<Self>),

    /// A break-statement sentinel for exiting a loop.
    ///
    /// Produced by a bare `break;` statement and consumed by the nearest
    /// enclosing loop evaluator.
    Break,

    /// A continue-statement sentinel for skipping to the next loop iteration.
    ///
    /// Produced by `continue;` and consumed by the nearest enclosing loop
    /// evaluator.
    Continue,

    /// A class definition value.
    ///
    /// Produced by evaluating a `class` statement. Stored in the environment
    /// under the class name so that `new ClassName(…)` can look it up.
    Class(ClassObject),

    /// A live instance of a class.
    ///
    /// Produced by `new ClassName(args…)`. Carries its own field storage; see
    /// [`InstanceObject`].
    Instance(InstanceObject),

    /// An ephemeral context object used during class-body evaluation.
    ///
    /// Injected into the environment as `self` / the implicit receiver so that
    /// method definitions inside a class body can reference the class. Not a
    /// user-visible value; evaluates as falsy and displays as
    /// `<class context: Name>`.
    ClassContext(ClassObject),
}

impl Object {
    /// Wrap `value` in [`Object::Integer`].
    pub const fn integer(value: i64) -> Self {
        Self::Integer(value)
    }

    /// Wrap `value` in [`Object::Float`].
    pub const fn float(value: f64) -> Self {
        Self::Float(value)
    }

    /// Wrap `value` in [`Object::Boolean`].
    pub const fn boolean(value: bool) -> Self {
        Self::Boolean(value)
    }

    /// Wrap `value` in [`Object::Str`], accepting any `Into<String>` source.
    pub fn string(value: impl Into<String>) -> Self {
        Self::Str(value.into())
    }

    /// Return the [`Object::Null`] sentinel.
    pub const fn null() -> Self {
        Self::Null
    }

    /// Wrap `items` in [`Object::Array`].
    pub const fn array(items: Vec<Self>) -> Self {
        Self::Array(items)
    }

    /// Wrap `map` in [`Object::Hash`].
    pub const fn hash(map: HashMap<HashKey, Self>) -> Self {
        Self::Hash(map)
    }

    /// Construct a [`Object::Function`] (closure) from its AST components.
    ///
    /// Delegates to [`FunctionObject::new`].
    pub const fn function(params: Vec<Indentifier>, body: BlockStatement, env: Env) -> Self {
        Self::Function(FunctionObject::new(params, body, env))
    }

    /// Wrap a native Rust function pointer in [`Object::Builtin`].
    pub const fn builtin(f: fn(Vec<Self>) -> Self) -> Self {
        Self::Builtin(f)
    }

    /// Wrap `obj` in a [`Object::Return`] sentinel.
    ///
    /// The returned value is boxed to keep the `Object` enum size bounded.
    /// The enclosing function evaluator must unwrap this sentinel before
    /// returning control to the caller.
    #[must_use]
    pub fn return_val(obj: Self) -> Self {
        Self::Return(Box::new(obj))
    }

    /// Construct a [`Object::Class`] from its component parts.
    ///
    /// Delegates to [`ClassObject::new`].
    pub fn class(
        name: impl Into<String>,
        parent: Option<ClassObject>,
        constructor: Option<FunctionObject>,
        methods: HashMap<String, FunctionObject>,
        env: Env,
    ) -> Self {
        Self::Class(ClassObject::new(name, parent, constructor, methods, env))
    }

    /// Construct a new [`Object::Instance`] of `class`.
    ///
    /// Delegates to [`InstanceObject::new`]. The constructor is not invoked;
    /// callers are responsible for running it separately.
    pub fn instance(class: ClassObject, env: Env) -> Self {
        Self::Instance(InstanceObject::new(class, env))
    }

    /// Determine whether `self` is truthy under Mutant Lang semantics.
    ///
    /// Truthiness rules:
    ///
    /// | Variant | Truthy when |
    /// |---|---|
    /// | `Boolean(b)` | `b` is `true` |
    /// | `Null` | never |
    /// | `Integer(n)` | `n != 0` |
    /// | `Float(f)` | `f != 0.0` **and** `f` is finite |
    /// | `Str(s)` | `s` is non-empty |
    /// | `Array(a)` | `a` is non-empty |
    /// | `ClassContext(_)` | never |
    /// | everything else | always |
    ///
    /// The `Float` rule treats `NaN` and `±Inf` as falsy because
    /// `f64::is_finite()` returns `false` for those values.
    #[must_use]
    pub fn is_truthy(&self) -> bool {
        match self {
            Self::Boolean(b) => *b,
            Self::Null | Self::ClassContext(_) => false,
            Self::Integer(n) => *n != 0,
            Self::Float(f) => *f != 0.0 && f.is_finite(),
            Self::Str(s) => !s.is_empty(),
            Self::Array(a) => !a.is_empty(),
            _ => true,
        }
    }

    /// Perform integer (floor) division: `self // rhs`.
    ///
    /// Behaves differently from the `/` operator ([`Div`](std::ops::Div)):
    ///
    /// - For two integers, performs truncating integer division (same as `i64 / i64`).
    /// - For mixed integer/float operands, coerces both to `f64`, divides, and
    ///   floors the result back to `i64`.
    /// - Returns [`EvalError`] on division by zero or on non-numeric operands.
    ///
    /// # Errors
    ///
    /// - [`EvalError::Runtime`] — if the right-hand side is zero (integer or float).
    /// - [`EvalError::TypeMismatch`] — if the operands cannot be coerced to numbers.
    #[allow(clippy::cast_possible_truncation)]
    pub fn int_div(self, rhs: &Self) -> Result<Self, EvalError> {
        match (&self, &rhs) {
            (Self::Integer(l), Self::Integer(r)) => {
                if *r == 0 {
                    return Err(EvalError::runtime("division by zero"));
                }
                Ok((*l / *r).into())
            }
            _ => match coerce_to_floats(&self, rhs) {
                Some((l, r)) => {
                    if r == 0.0 {
                        return Err(EvalError::runtime("division by zero"));
                    }
                    Ok(((l / r).floor() as i64).into())
                }
                None => Err(EvalError::type_mismatch(
                    "numeric",
                    format!("{self} // {rhs}"),
                )),
            },
        }
    }
}

/// Formats each variant for REPL output and error messages.
///
/// Control-flow sentinels ([`Break`](Object::Break), [`Continue`](Object::Continue))
/// render as their keyword spellings; [`Return`](Object::Return) renders as the
/// inner value (transparent pass-through). Hash values render as `<hash>`
/// rather than attempting to iterate the map, for simplicity.
impl fmt::Display for Object {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            Self::Integer(n) => write!(f, "{n}"),
            Self::Float(fl) => write!(f, "{fl}"),
            Self::Boolean(b) => write!(f, "{b}"),
            Self::Str(s) => write!(f, "{s}"),
            Self::Null => write!(f, "null"),
            Self::Array(a) => {
                let items: Vec<String> = a.iter().map(std::string::ToString::to_string).collect();
                write!(f, "[{}]", items.join(", "))
            }
            Self::Return(v) => write!(f, "{v}"),
            Self::Break => write!(f, "break"),
            Self::Continue => write!(f, "continue"),
            Self::Function(func) => write!(f, "{func}"),
            Self::Builtin(_) => write!(f, "<builtin>"),
            Self::Class(cls) => write!(f, "{cls}"),
            Self::Instance(inst) => write!(f, "{inst}"),
            Self::Hash(_) => write!(f, "<hash>"),
            Self::ClassContext(cls) => write!(f, "<class context: {}>", cls.name),
        }
    }
}

/// Converts an `i64` into [`Object::Integer`] without allocation.
impl From<i64> for Object {
    fn from(value: i64) -> Self {
        Self::Integer(value)
    }
}

/// Converts an `f64` into [`Object::Float`] without allocation.
impl From<f64> for Object {
    fn from(value: f64) -> Self {
        Self::Float(value)
    }
}

/// Converts a `bool` into [`Object::Boolean`] without allocation.
impl From<bool> for Object {
    fn from(value: bool) -> Self {
        Self::Boolean(value)
    }
}

/// Converts an owned `String` into [`Object::Str`].
impl From<String> for Object {
    fn from(value: String) -> Self {
        Self::Str(value)
    }
}

/// Converts a `&str` slice into [`Object::Str`] by copying the bytes.
impl From<&str> for Object {
    fn from(value: &str) -> Self {
        Self::Str(value.to_owned())
    }
}

/// Converts a `Vec<Object>` into [`Object::Array`], taking ownership of the vec.
impl From<Vec<Self>> for Object {
    fn from(items: Vec<Self>) -> Self {
        Self::Array(items)
    }
}

/// Converts a `HashMap<HashKey, Object>` into [`Object::Hash`].
impl From<HashMap<HashKey, Self>> for Object {
    fn from(map: HashMap<HashKey, Self>) -> Self {
        Self::Hash(map)
    }
}

/// Extracts a [`HashKey`] from an [`Object`], failing for non-hashable types.
///
/// Only [`Object::Integer`], [`Object::Boolean`], and [`Object::Str`] can be
/// used as hash-literal keys. All other variants produce
/// [`EvalError::TypeMismatch`].
///
/// Takes ownership of the `Object` so that `Str` values can be moved into the
/// [`HashKey::Str`] variant without an extra clone.
///
/// # Errors
///
/// Returns [`EvalError::TypeMismatch`] when `obj` is any variant other than
/// `Integer`, `Boolean`, or `Str`.
impl TryFrom<Object> for HashKey {
    type Error = EvalError;

    fn try_from(obj: Object) -> Result<Self, Self::Error> {
        match obj {
            Object::Integer(n) => Ok(Self::Integer(n)),
            Object::Boolean(b) => Ok(Self::Boolean(b)),
            Object::Str(s) => Ok(Self::Str(s)),
            other => Err(EvalError::TypeMismatch {
                expected: "Integer, Boolean, or String".into(),
                got: format!("{other}"),
            }),
        }
    }
}

/// Implements the prefix `!` operator.
///
/// `!true` → `false`, `!false` → `true`, `!null` → `true`.
/// All other variants produce [`EvalError::TypeMismatch`] because logical
/// negation is not defined for numbers, strings, arrays, etc.
///
/// # Errors
///
/// Returns [`EvalError::TypeMismatch`] for any variant other than `Boolean`
/// or `Null`.
impl std::ops::Not for Object {
    type Output = Result<Self, EvalError>;

    fn not(self) -> Self::Output {
        match self {
            Self::Boolean(b) => Ok((!b).into()),
            Self::Null => Ok(true.into()),
            other => Err(EvalError::type_mismatch("Boolean or Null", &other)),
        }
    }
}

/// Implements the prefix `-` (unary negation) operator.
///
/// `-42` → `Integer(-42)`, `-3.14` → `Float(-3.14)`.
/// All other variants produce [`EvalError::TypeMismatch`].
///
/// # Errors
///
/// Returns [`EvalError::TypeMismatch`] for any variant other than `Integer`
/// or `Float`.
impl std::ops::Neg for Object {
    type Output = Result<Self, EvalError>;

    fn neg(self) -> Self::Output {
        match self {
            Self::Integer(i) => Ok((-i).into()),
            Self::Float(f) => Ok((-f).into()),
            other => Err(EvalError::type_mismatch("Integer or Float", &other)),
        }
    }
}

/// Implements the `+` operator with string concatenation support.
///
/// Precedence:
/// 1. `Integer + Integer` → `Integer`
/// 2. `Str + Str` → `Str` (concatenation)
/// 3. `Str + Integer` or `Integer + Str` → `Str` (coerced concatenation)
/// 4. Any two numeric-coercible values → `Float` (via `f64`)
///
/// # Errors
///
/// Returns [`EvalError::TypeMismatch`] when neither operand can be coerced to
/// a common type.
impl std::ops::Add for Object {
    type Output = Result<Self, EvalError>;

    fn add(self, rhs: Self) -> Self::Output {
        match (&self, &rhs) {
            (Self::Integer(l), Self::Integer(r)) => Ok((*l + *r).into()),
            (Self::Str(l), Self::Str(r)) => Ok(Self::Str(l.clone() + r)),
            (Self::Str(l), Self::Integer(r)) => Ok(Self::Str(format!("{l}{r}"))),
            (Self::Integer(l), Self::Str(r)) => Ok(Self::Str(format!("{l}{r}"))),
            _ => match (to_f64(&self), to_f64(&rhs)) {
                (Some(l), Some(r)) => Ok((l + r).into()),
                _ => Err(EvalError::type_mismatch(
                    "matching types for +",
                    format!("{self} + {rhs}"),
                )),
            },
        }
    }
}

/// Implements the `-` operator.
///
/// `Integer - Integer` stays integer; any mix involving a `Float` promotes to
/// `Float`.
///
/// # Errors
///
/// Returns [`EvalError::TypeMismatch`] for non-numeric operands.
impl std::ops::Sub for Object {
    type Output = Result<Self, EvalError>;

    fn sub(self, rhs: Self) -> Self::Output {
        match (&self, &rhs) {
            (Self::Integer(l), Self::Integer(r)) => Ok((*l - *r).into()),
            _ => match (to_f64(&self), to_f64(&rhs)) {
                (Some(l), Some(r)) => Ok((l - r).into()),
                _ => Err(EvalError::type_mismatch(
                    "numeric",
                    format!("{self} - {rhs}"),
                )),
            },
        }
    }
}

/// Implements the `*` operator.
///
/// `Integer * Integer` stays integer; any mix involving a `Float` promotes to
/// `Float`.
///
/// # Errors
///
/// Returns [`EvalError::TypeMismatch`] for non-numeric operands.
impl std::ops::Mul for Object {
    type Output = Result<Self, EvalError>;

    fn mul(self, rhs: Self) -> Self::Output {
        match (&self, &rhs) {
            (Self::Integer(l), Self::Integer(r)) => Ok((*l * *r).into()),
            _ => match (to_f64(&self), to_f64(&rhs)) {
                (Some(l), Some(r)) => Ok((l * r).into()),
                _ => Err(EvalError::type_mismatch(
                    "numeric",
                    format!("{self} * {rhs}"),
                )),
            },
        }
    }
}

/// Implements the `/` (true division) operator.
///
/// `Integer / Integer` performs truncating integer division and returns an
/// `Integer`. Mixed or float operands return a `Float`; `f64` division by
/// zero yields `±Inf` rather than an error (IEEE 754 behavior) — only the
/// pure-integer path raises [`EvalError::Runtime`] for division by zero.
///
/// # Errors
///
/// - [`EvalError::Runtime`] — for `Integer / 0`.
/// - [`EvalError::TypeMismatch`] — for non-numeric operands.
impl std::ops::Div for Object {
    type Output = Result<Self, EvalError>;

    fn div(self, rhs: Self) -> Self::Output {
        match (&self, &rhs) {
            (Self::Integer(l), Self::Integer(r)) => {
                if *r == 0 {
                    return Err(EvalError::runtime("division by zero"));
                }
                Ok((*l / *r).into())
            }
            _ => match (to_f64(&self), to_f64(&rhs)) {
                (Some(l), Some(r)) => Ok((l / r).into()),
                _ => Err(EvalError::type_mismatch(
                    "numeric",
                    format!("{self} / {rhs}"),
                )),
            },
        }
    }
}

/// Implements the `%` (remainder) operator.
///
/// `Integer % Integer` stays integer and raises an error on modulo-by-zero.
/// Float operands use `f64 % f64` (IEEE 754 remainder).
///
/// # Errors
///
/// - [`EvalError::Runtime`] — for `Integer % 0`.
/// - [`EvalError::TypeMismatch`] — for non-numeric operands.
impl std::ops::Rem for Object {
    type Output = Result<Self, EvalError>;

    fn rem(self, rhs: Self) -> Self::Output {
        match (&self, &rhs) {
            (Self::Integer(l), Self::Integer(r)) => {
                if *r == 0 {
                    return Err(EvalError::runtime("modulo by zero"));
                }
                Ok((*l % *r).into())
            }
            _ => match (to_f64(&self), to_f64(&rhs)) {
                (Some(l), Some(r)) => Ok((l % r).into()),
                _ => Err(EvalError::type_mismatch(
                    "numeric",
                    format!("{self} % {rhs}"),
                )),
            },
        }
    }
}

/// Extract an `f64` from a numeric [`Object`], or `None` for all other variants.
#[allow(clippy::cast_precision_loss)]
const fn to_f64(obj: &Object) -> Option<f64> {
    match obj {
        Object::Integer(i) => Some(*i as f64),
        Object::Float(f) => Some(*f),
        _ => None,
    }
}

/// Coerce two [`Object`]s to `(f64, f64)` only when at least one is a `Float`.
///
/// Returns `None` if both are integers (handled by the integer fast-path) or
/// if either is a non-numeric type. Used by [`Object::int_div`] to implement
/// the mixed integer/float floor-division path.
#[allow(clippy::cast_precision_loss)]
const fn coerce_to_floats(l: &Object, r: &Object) -> Option<(f64, f64)> {
    match (l, r) {
        (Object::Float(a), Object::Float(b)) => Some((*a, *b)),
        (Object::Float(a), Object::Integer(b)) => Some((*a, *b as f64)),
        (Object::Integer(a), Object::Float(b)) => Some((*a as f64, *b)),
        _ => None,
    }
}
