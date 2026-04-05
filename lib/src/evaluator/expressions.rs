//! Expression evaluation for the Mutant Lang tree-walking interpreter.
//!
//! This module is an `impl` block on [`Evaluator`] that handles every
//! [`Expression`] variant the parser can produce. It is the hot path of
//! the interpreter: nearly every runtime value flows through
//! [`Evaluator::eval_expression`] at some point.
//!
//! # Dispatch model
//!
//! [`eval_expression`](Evaluator::eval_expression) is a thin pattern-match
//! dispatcher. Each arm delegates immediately to a focused private helper,
//! keeping the top-level function readable and each helper independently
//! testable.
//!
//! # Class system conventions
//!
//! Two special keys are injected into call environments to support the OOP
//! features of the language:
//!
//! - `"this"` — the current [`Object::Instance`] inside a method or
//!   constructor body.
//! - `"__class_context__"` — an [`Object::ClassContext`] that records *which
//!   class is currently executing*. This is the mechanism that makes chained
//!   `super()` calls walk the inheritance chain one level at a time rather than
//!   always jumping to the root.
//!
//! # Error handling
//!
//! All helpers return `Result<Object, EvalError>`. The `?` operator propagates
//! errors up through [`eval_expression`](Evaluator::eval_expression) to the
//! statement evaluator, which ultimately surfaces them to the REPL or the
//! program's error output.

use std::collections::HashMap;
use std::rc::Rc;

use crate::ast::expression::{self, Expression, Indentifier};
use crate::ast::literal::Literal;
use crate::token::Operator;

use super::env::{Env, Environment};
use super::objects::{HashKey, Object};
use super::{EvalError, Evaluator};

/// Sentinel key used to look up the `this` binding inside method/constructor bodies.
const THIS: &str = "this";

/// Sentinel key used to track which class is currently executing, enabling
/// correct `super` chain traversal across multiple inheritance levels.
const CLASS_CONTEXT: &str = "class_context";

// Expression evaluation
impl Evaluator {
    /// Dispatch an [`Expression`] node to the appropriate evaluation helper.
    ///
    /// This is the central routing function for the expression evaluator. Every
    /// expression in the AST eventually reaches this function, which matches on
    /// the variant and delegates to a focused helper. Errors short-circuit via
    /// `?` in the helpers and bubble up here unchanged.
    ///
    /// # Errors
    ///
    /// Propagates any [`EvalError`] returned by the matched helper — see each
    /// helper's documentation for the specific variants they can produce.
    pub(super) fn eval_expression(
        &self,
        expr: &Expression,
        env: &Env,
    ) -> Result<Object, EvalError> {
        match expr {
            Expression::Literal(lit) => self.eval_literal(lit, env),
            Expression::Identifier(ident) => Self::eval_identifier(ident, env),
            Expression::Prefix(prefix) => self.eval_prefix(prefix, env),
            Expression::Infix(infix) => self.eval_infix(infix, env),
            Expression::If(if_expr) => self.eval_if(if_expr, env),
            Expression::Assignment(assign) => self.eval_assignment(assign, env),
            Expression::Call(call) => self.eval_call(call, env),
            Expression::Index(idx) => self.eval_index(idx, env),
            Expression::New(new) => self.eval_new(new, env),
            Expression::Super(super_expr) => self.eval_super(super_expr, env),
            Expression::This(_) => self.eval_this(env),
            Expression::Property(prop) => self.eval_property(prop, env),
        }
    }

    /// Evaluate a function-call expression.
    ///
    /// Evaluates the callee expression, evaluates each argument left-to-right,
    /// and then executes the function body inside a fresh child environment
    /// that binds each parameter to its corresponding argument value.
    ///
    /// A bare [`Object::Return`] sentinel produced by the body is unwrapped
    /// here so the caller receives the inner value rather than the sentinel.
    /// All other objects are returned as-is (including [`Object::Null`] for
    /// void functions).
    ///
    /// # Errors
    ///
    /// - [`EvalError::Runtime`] if the callee does not evaluate to an
    ///   [`Object::Function`].
    /// - [`EvalError::WrongArgCount`] if the number of supplied arguments
    ///   differs from the function's parameter list length.
    /// - Any error produced while evaluating the callee, the arguments, or the
    ///   function body.
    fn eval_call(&self, call: &expression::CallExpression, env: &Env) -> Result<Object, EvalError> {
        let function = self.eval_expression(call.function(), env)?;
        let args = self.parse_expressions(call.args(), env)?;

        match function {
            Object::Function(func) => {
                if args.len() != func.params.len() {
                    return Err(EvalError::wrong_arg_count(func.params.len(), args.len()));
                }

                let call_env = Environment::new_child(&func.env, false);
                for (param, arg) in func.params.iter().zip(args) {
                    call_env.borrow_mut().define(param.value(), arg);
                }

                let result = self.eval_block(&func.body, &call_env)?;

                match result {
                    Object::Return(val) => Ok(*val),
                    other => Ok(other),
                }
            }
            _ => Err(EvalError::runtime("expected a function")),
        }
    }

    /// Evaluate a `super(…)` or `super.method(…)` expression.
    ///
    /// Handles two distinct forms:
    ///
    /// 1. **Constructor delegation** — `super(arg1, arg2, …)` invokes the
    ///    parent class's constructor with the current `this` already bound, so
    ///    the parent can initialize its own fields on the shared instance.
    /// 2. **Method delegation** — `super.method(…)` looks up a named method on
    ///    the parent class and calls it with `this` bound to the current
    ///    instance.
    ///
    /// In both cases the `"__class_context__"` key is advanced to the parent
    /// class before the body executes. This means that if the parent
    /// constructor or method itself contains a `super()` call, the chain will
    /// walk up one further level rather than re-entering the same parent.
    ///
    /// # Errors
    ///
    /// - [`EvalError::Runtime`] if `"this"` is not in scope or is not an
    ///   [`Object::Instance`].
    /// - [`EvalError::Runtime`] if the current class has no parent.
    /// - [`EvalError::WrongArgCount`] if the argument count does not match the
    ///   parent constructor/method parameter count.
    /// - [`EvalError::Runtime`] if the method name is not found on the parent
    ///   class.
    /// - Any error produced while evaluating arguments or the parent body.
    fn eval_super(
        &self,
        super_expr: &expression::SuperExpression,
        env: &Env,
    ) -> Result<Object, EvalError> {
        let this = env
            .borrow()
            .get("this")
            .ok_or_else(|| EvalError::runtime("'this' is not available in this context"))?;

        let instance_class = match &this {
            Object::Instance(inst) => *inst.class.clone(),
            _ => return Err(EvalError::runtime("'this' is not an instance")),
        };

        let current_class = match env.borrow().get("__class_context__") {
            Some(Object::ClassContext(cls)) => cls,
            _ => instance_class,
        };

        let parent_class = current_class.parent.map(|b| *b).ok_or_else(|| {
            EvalError::runtime(format!(
                "class '{}' has no parent class",
                current_class.name
            ))
        })?;

        let args = self.parse_expressions(super_expr.args(), env)?;

        if super_expr.is_constructor_call() {
            let call_info = parent_class
                .constructor
                .as_deref()
                .map(|ctor| (ctor.env.clone(), ctor.params.clone(), ctor.body.clone()));

            match call_info {
                None if args.is_empty() => Ok(this),

                None => Err(EvalError::runtime(format!(
                    "class '{}' has no constructor but {} argument(s) were supplied",
                    parent_class.name,
                    args.len()
                ))),

                Some((ctor_env, ctor_params, ctor_body)) => {
                    if args.len() != ctor_params.len() {
                        return Err(EvalError::wrong_arg_count(ctor_params.len(), args.len()));
                    }

                    let call_env = Environment::new_child(&ctor_env, false);

                    // Bind this so the constructor can set fields via self.x = ...
                    call_env.borrow_mut().define("this", this.clone());

                    // Advance the class context to the parent so that any super()
                    // call *inside* the parent constructor walks up one more level.
                    call_env
                        .borrow_mut()
                        .define("__class_context__", Object::ClassContext(parent_class));

                    for (param, arg) in ctor_params.iter().zip(args) {
                        call_env.borrow_mut().define(param.value(), arg);
                    }

                    self.eval_block(&ctor_body, &call_env)?;

                    Ok(this)
                }
            }
        } else {
            let method_name = match super_expr.method() {
                Some(Expression::Identifier(ident)) => ident.value().to_owned(),
                _ => return Err(EvalError::runtime("invalid super method expression")),
            };

            let (method_env_ref, method_params, method_body) = parent_class
                .find_method(&method_name)
                .map(|m| (m.env.clone(), m.params.clone(), m.body.clone()))
                .ok_or_else(|| {
                    EvalError::runtime(format!(
                        "method '{}' not found in parent class '{}'",
                        method_name, parent_class.name
                    ))
                })?;

            if args.len() != method_params.len() {
                return Err(EvalError::wrong_arg_count(method_params.len(), args.len()));
            }

            let call_env = Environment::new_child(&method_env_ref, false);
            call_env.borrow_mut().define("this", this);
            call_env
                .borrow_mut()
                .define("__class_context__", Object::ClassContext(parent_class));

            for (param, arg) in method_params.iter().zip(args) {
                call_env.borrow_mut().define(param.value(), arg);
            }

            match self.eval_block(&method_body, &call_env)? {
                Object::Return(val) => Ok(*val),
                other => Ok(other),
            }
        }
    }

    /// Evaluate a [`Literal`] node into its runtime [`Object`].
    ///
    /// Primitive literals (`Integer`, `Bool`, `String`, `Null`) are converted
    /// through `From` / `Into` implementations and require no environment access.
    /// Composite literals (`Array`, `Hash`) eagerly evaluate every element or
    /// key-value pair, short-circuiting on the first error. Function literals
    /// capture the *current* environment by reference-counted clone, forming a
    /// closure.
    ///
    /// # Errors
    ///
    /// - Any error from evaluating array elements or hash pairs.
    /// - [`EvalError::TypeMismatch`] if a hash key expression evaluates to a
    ///   non-hashable type (see [`HashKey`]).
    pub(super) fn eval_literal(&self, lit: &Literal, env: &Env) -> Result<Object, EvalError> {
        match lit {
            Literal::Integer(i) => Ok(i.value().into()),
            Literal::Bool(b) => Ok(b.value().into()),
            Literal::String(s) => Ok(s.value().into()),
            Literal::Null(_) => Ok(Object::Null),
            Literal::Array(a) => self.eval_array_literal(a, env),
            Literal::Hash(h) => self.eval_hash_literal(h, env),
            Literal::Func(f) => Ok(Object::function(
                f.params().to_vec(),
                f.body().clone(),
                Rc::clone(env),
            )),
        }
    }

    /// Evaluate an array literal by eagerly evaluating each element expression.
    ///
    /// Elements are evaluated left-to-right and collected into a `Vec`. The
    /// first evaluation error short-circuits the entire collection via the
    /// `collect::<Result<Vec<_>, _>>()` pattern.
    ///
    /// # Errors
    ///
    /// Propagates any [`EvalError`] produced while evaluating an element.
    fn eval_array_literal(
        &self,
        lit: &crate::ast::literal::ArrayLiteral,
        env: &Env,
    ) -> Result<Object, EvalError> {
        let elements = lit
            .elements()
            .iter()
            .map(|e| self.eval_expression(e, env))
            .collect::<Result<Vec<_>, _>>()?;
        Ok(Object::Array(elements))
    }

    /// Evaluate a hash literal by evaluating each key-value pair expression.
    ///
    /// Pairs are evaluated in iteration order (source order of the literal).
    /// Each key expression is converted to a [`HashKey`] immediately after
    /// evaluation; non-hashable types are rejected with [`EvalError::TypeMismatch`]
    /// before any value is inserted. Duplicate keys are silently overwritten —
    /// last writer wins, matching typical language behavior for hash literals.
    ///
    /// # Errors
    ///
    /// - Any [`EvalError`] from evaluating a key or value expression.
    /// - [`EvalError::TypeMismatch`] if a key evaluates to a non-hashable
    ///   [`Object`] variant (anything other than integer, boolean, or string).
    fn eval_hash_literal(
        &self,
        lit: &crate::ast::literal::HashLiteral,
        env: &Env,
    ) -> Result<Object, EvalError> {
        let mut map = HashMap::new();
        for (key_expr, val_expr) in lit.pairs() {
            let key = self.eval_expression(key_expr, env)?;
            let val = self.eval_expression(val_expr, env)?;
            map.insert(HashKey::try_from(key)?, val);
        }
        Ok(Object::Hash(map))
    }

    /// Look up an identifier in the current environment chain.
    ///
    /// Walks the lexical scope chain from the innermost environment outward.
    /// Returns the first binding found for `ident.value()`. Because closures
    /// capture their environment at definition time, this naturally implements
    /// lexical (static) scoping.
    ///
    /// # Errors
    ///
    /// - [`EvalError::UndefinedVariable`] if the name is not found in any
    ///   enclosing scope.
    fn eval_identifier(ident: &Indentifier, env: &Env) -> Result<Object, EvalError> {
        let name = ident.value();
        if let Some(val) = env.borrow().get(name) {
            return Ok(val);
        }
        Err(EvalError::UndefinedVariable(ident.value().to_owned()))
    }

    /// Evaluate an `if` / `else if` / `else` chain.
    ///
    /// Iterates over the condition–consequence pairs in order, evaluating each
    /// condition until one is truthy (per [`Object::is_truthy`]). The
    /// corresponding consequence block is then evaluated and its result
    /// returned. If no condition is truthy the `else` block is evaluated; if
    /// there is no `else` block, [`Object::Null`] is returned.
    ///
    /// Because `if` is an expression in Mutant Lang, it produces a value that
    /// can appear on the right-hand side of an assignment.
    ///
    /// # Errors
    ///
    /// Propagates any [`EvalError`] from evaluating a condition or a branch body.
    fn eval_if(&self, if_expr: &expression::IfExpression, env: &Env) -> Result<Object, EvalError> {
        for (condition, consequence) in if_expr.conditions().iter().zip(if_expr.consequences()) {
            let condition_val = self.eval_expression(condition, env)?;
            if condition_val.is_truthy() {
                return self.eval_block(consequence, env);
            }
        }

        if_expr
            .alternative()
            .map_or_else(|| Ok(Object::Null), |alt| self.eval_block(alt, env))
    }

    /// Evaluate a subscript index expression (`left[index]`).
    ///
    /// Supports two collection types:
    ///
    /// - **Array** — the index must be an `Integer`. Negative indices and
    ///   out-of-bounds accesses are rejected with a runtime error. Within-bounds
    ///   accesses move the element out of the temporary `Vec` (the bounds check
    ///   above guarantees the `unwrap` cannot panic).
    /// - **Hash** — the index is converted to a [`HashKey`]; a missing key
    ///   returns [`Object::Null`] rather than an error, mirroring typical
    ///   scripting-language behavior.
    ///
    /// # Errors
    ///
    /// - [`EvalError::Runtime`] if the array index is out of bounds.
    /// - [`EvalError::TypeMismatch`] if an array is indexed with a non-integer,
    ///   or if the left-hand side is neither an array nor a hash.
    /// - [`EvalError::TypeMismatch`] if the hash key is a non-hashable type.
    /// - Any error from evaluating `left` or `index`.
    fn eval_index(
        &self,
        index_expr: &expression::IndexExpression,
        env: &Env,
    ) -> Result<Object, EvalError> {
        let left = self.eval_expression(index_expr.left(), env)?;
        let index = self.eval_expression(index_expr.index(), env)?;

        match (left, index) {
            (Object::Array(elements), Object::Integer(i)) => {
                let len = elements.len() as i64;
                if i < 0 || i >= len {
                    return Err(EvalError::Runtime(format!(
                        "index out of bounds: {i} is not in range [0, {len})"
                    )));
                }
                Ok(elements.into_iter().nth(i as usize).unwrap())
            }

            (Object::Array(_), other) => Err(EvalError::TypeMismatch {
                expected: "Integer".into(),
                got: format!("{other}"),
            }),

            (Object::Hash(map), index) => {
                let key = HashKey::try_from(index)?;
                Ok(map.get(&key).cloned().unwrap_or(Object::Null))
            }

            (other, _) => Err(EvalError::TypeMismatch {
                expected: "Array or Hash".into(),
                got: format!("{other}"),
            }),
        }
    }

    /// Evaluate the `this` keyword.
    ///
    /// Reads the `"this"` binding from the current environment and verifies
    /// that it holds an [`Object::Instance`]. Using `this` outside of a method
    /// or constructor body — where the binding is never defined — returns an
    /// error.
    ///
    /// # Errors
    ///
    /// - [`EvalError::Runtime`] if `"this"` is not defined in any enclosing scope.
    /// - [`EvalError::Runtime`] if `"this"` is defined but does not hold an
    ///   [`Object::Instance`] (guarded defensively; should not occur under
    ///   normal execution).
    fn eval_this(&self, env: &Env) -> Result<Object, EvalError> {
        let this = env
            .borrow()
            .get("this")
            .ok_or_else(|| EvalError::runtime("'this' is not available in this context"))?;

        match this {
            Object::Instance(_) => Ok(this),
            _ => Err(EvalError::runtime("'this' is not an instance")),
        }
    }

    /// Evaluate a `new ClassName(…)` expression.
    ///
    /// Allocates a fresh [`Object::Instance`] backed by a new child
    /// environment, then invokes the class's constructor (if present) with
    /// `"this"` pre-bound to the new instance. Field mutations performed by
    /// the constructor — via `this.field = value` assignments — are reflected
    /// on the instance object that is ultimately returned.
    ///
    /// If the class has no constructor and no arguments are supplied, the
    /// instance is returned with no additional initialization. Supplying
    /// arguments to a constructor-less class is an error.
    ///
    /// # Errors
    ///
    /// - [`EvalError::Runtime`] if the callee expression does not evaluate to
    ///   an [`Object::Class`].
    /// - [`EvalError::WrongArgCount`] if the argument count does not match the
    ///   constructor's parameter list.
    /// - [`EvalError::Runtime`] if arguments are passed to a class that has no
    ///   constructor.
    /// - Any error from evaluating the constructor body or the argument expressions.
    fn eval_new(
        &self,
        new_expr: &expression::NewExpression,
        env: &Env,
    ) -> Result<Object, EvalError> {
        let class = self.eval_expression(new_expr.class_name(), env)?;
        let Object::Class(class_obj) = class else {
            return Err(EvalError::runtime("expected a class"));
        };

        let args = self.parse_expressions(new_expr.args(), env)?;
        let instance = Object::instance(class_obj.clone(), Environment::new_child(env, false));

        if let Some(ctor) = &class_obj.constructor {
            if args.len() != ctor.params.len() {
                return Err(EvalError::WrongArgCount {
                    expected: ctor.params.len(),
                    got: args.len(),
                });
            }

            let ctor_env = Environment::new_child(&ctor.env, false);
            ctor_env.borrow_mut().define("this", instance.clone());

            for (param, arg) in ctor.params.iter().zip(args) {
                ctor_env.borrow_mut().define(param.value(), arg);
            }

            self.eval_block(&ctor.body, &ctor_env)?;
        } else if !args.is_empty() {
            return Err(EvalError::runtime(format!(
                "class '{}' has no constructor but {} argument(s) were supplied",
                class_obj.name,
                args.len()
            )));
        }

        Ok(instance)
    }

    /// Evaluate a slice of [`Expression`] nodes into a `Vec<Object>`.
    ///
    /// Convenience helper used wherever a list of argument expressions must be
    /// evaluated before a call. Evaluation is left-to-right; the first error
    /// short-circuits collection via iterator `collect`.
    ///
    /// # Errors
    ///
    /// Propagates the first [`EvalError`] encountered while evaluating any
    /// argument expression.
    fn parse_expressions(&self, args: &[Expression], env: &Env) -> Result<Vec<Object>, EvalError> {
        args.iter()
            .map(|arg| self.eval_expression(arg, env))
            .collect()
    }

    /// Evaluate a prefix expression (`!expr` or `-expr`).
    ///
    /// Dispatches to the `Not` / `Neg` operator implementations on [`Object`],
    /// which are defined via the standard `std::ops` traits. Any other
    /// [`Operator`] in prefix position is a runtime error — the parser should
    /// prevent this, but the guard is retained defensively.
    ///
    /// # Errors
    ///
    /// - [`EvalError::Runtime`] for unrecognized prefix operators.
    /// - Any [`EvalError`] returned by the `!` or `-` trait implementations on
    ///   [`Object`] (e.g. negating a non-numeric type).
    /// - Any error from evaluating the operand expression.
    fn eval_prefix(
        &self,
        prefix: &expression::PrefixExpression,
        env: &Env,
    ) -> Result<Object, EvalError> {
        let right = self.eval_expression(prefix.right(), env)?;
        match prefix.operator() {
            Operator::Bang => !right,
            Operator::Minus => -right,
            _ => Err(EvalError::runtime(format!(
                "unknown operator: {}",
                prefix.operator()
            ))),
        }
    }

    /// Evaluate an infix (binary) expression.
    ///
    /// Evaluation proceeds in three ordered stages to ensure correct semantics:
    ///
    /// 1. **Null guard** — `==` and `!=` with `null` operands are handled first
    ///    using structural equality rules (two nulls are equal; null is not equal
    ///    to any non-null value). Any other operator applied to a `null` operand
    ///    is a runtime error. This stage borrows `left` and `right` so it does
    ///    not consume them before the arithmetic stage.
    ///
    /// 2. **Arithmetic** — `+`, `-`, `*`, `/`, `%`, and `//` (integer division)
    ///    delegate to [`std::ops`] trait implementations on [`Object`], which
    ///    consume `left` and `right` by value.
    ///
    /// 3. **Comparison and boolean** — `==`, `!=`, `<`, `<=`, `>`, `>=`, `&&`,
    ///    `||` are handled by exhaustive pattern matching on `(&left, &right)`.
    ///    Mixed-type comparisons fall through to the catch-all arm and return
    ///    [`EvalError::TypeMismatch`].
    ///
    /// # Errors
    ///
    /// - [`EvalError::Runtime`] for any operator applied to a `null` operand
    ///   (other than `==` / `!=`).
    /// - Any [`EvalError`] from the arithmetic trait implementations (e.g.
    ///   division by zero, type mismatch in `+`).
    /// - [`EvalError::TypeMismatch`] for comparisons or boolean operators
    ///   applied to incompatible or mismatched types.
    /// - Any error from evaluating the left or right operand expressions.
    fn eval_infix(
        &self,
        infix: &expression::InfixExpression,
        env: &Env,
    ) -> Result<Object, EvalError> {
        let left = self.eval_expression(infix.left(), env)?;
        let right = self.eval_expression(infix.right(), env)?;
        let op = infix.operator();

        // Stage 1 — null guard (borrow only, no consume)
        // Must come first: null + anything should say "cannot be used with null",
        // not "type mismatch for +".
        match (op, &left, &right) {
            (Operator::Eq, Object::Null, Object::Null) => return Ok(true.into()),
            (Operator::NotEq, Object::Null, Object::Null) => return Ok(false.into()),
            (Operator::Eq, Object::Null, _) | (Operator::Eq, _, Object::Null) => {
                return Ok(false.into())
            }
            (Operator::NotEq, Object::Null, _) | (Operator::NotEq, _, Object::Null) => {
                return Ok(true.into())
            }
            (_, Object::Null, _) | (_, _, Object::Null) => {
                return Err(EvalError::runtime(format!(
                    "operator '{op}' cannot be used with null"
                )));
            }
            _ => {}
        }

        // Stage 2 — arithmetic (consumes left/right via std::ops traits)
        match op {
            Operator::Plus => return left + right,
            Operator::Minus => return left - right,
            Operator::Asterisk => return left * right,
            Operator::Slash => return left / right,
            Operator::Modulus => return left % right,
            Operator::IntDivision => return left.int_div(&right),
            _ => {}
        }

        // Stage 3 — comparisons and boolean (borrow is fine, left/right untouched)
        match (op, &left, &right) {
            // --- Integer comparison ---
            (Operator::Eq, Object::Integer(l), Object::Integer(r)) => Ok((l == r).into()),
            (Operator::NotEq, Object::Integer(l), Object::Integer(r)) => Ok((l != r).into()),
            (Operator::LessThan, Object::Integer(l), Object::Integer(r)) => Ok((l < r).into()),
            (Operator::LessThanOrEqual, Object::Integer(l), Object::Integer(r)) => {
                Ok((l <= r).into())
            }
            (Operator::GreaterThan, Object::Integer(l), Object::Integer(r)) => Ok((l > r).into()),
            (Operator::GreaterThanOrEqual, Object::Integer(l), Object::Integer(r)) => {
                Ok((l >= r).into())
            }

            (Operator::Eq, Object::Str(l), Object::Str(r)) => Ok((l == r).into()),
            (Operator::NotEq, Object::Str(l), Object::Str(r)) => Ok((l != r).into()),
            (Operator::LessThan, Object::Str(l), Object::Str(r)) => Ok((l < r).into()),
            (Operator::GreaterThan, Object::Str(l), Object::Str(r)) => Ok((l > r).into()),

            // --- Boolean ---
            (Operator::Eq, Object::Boolean(l), Object::Boolean(r)) => Ok((l == r).into()),
            (Operator::NotEq, Object::Boolean(l), Object::Boolean(r)) => Ok((l != r).into()),
            (Operator::And, Object::Boolean(l), Object::Boolean(r)) => Ok((*l && *r).into()),
            (Operator::Or, Object::Boolean(l), Object::Boolean(r)) => Ok((*l || *r).into()),

            _ => Err(EvalError::TypeMismatch {
                expected: "matching types for operator".into(),
                got: format!("{left} {op} {right}"),
            }),
        }
    }

    /// Evaluate a property-access expression (`object.property`).
    ///
    /// Resolves `object` to an [`Object::Instance`], then looks up the property
    /// name. Field lookup takes priority over method lookup — if both a field
    /// and a method share a name, the field wins.
    ///
    /// When the property resolves to a method, a *bound method* is returned: a
    /// new [`Object::Function`] whose captured environment has `"this"` bound to
    /// the instance. This allows the returned function to be called later as a
    /// first-class value without losing its receiver.
    ///
    /// # Errors
    ///
    /// - [`EvalError::Runtime`] if `object` does not evaluate to an
    ///   [`Object::Instance`].
    /// - [`EvalError::Runtime`] if the property name expression is not an
    ///   [`Expression::Identifier`].
    /// - [`EvalError::Runtime`] if the name is not found as either a field or a
    ///   method on the instance's class.
    /// - Any error from evaluating the `object` expression.
    fn eval_property(
        &self,
        property: &expression::PropertyExpression,
        env: &Env,
    ) -> Result<Object, EvalError> {
        let object = self.eval_expression(property.object(), env)?;

        let Object::Instance(instance) = object else {
            return Err(EvalError::runtime("expected an instance"));
        };

        let prop_name = match property.property() {
            Expression::Identifier(ident) => ident.value(),
            _ => return Err(EvalError::runtime("invalid property name")),
        };

        if let Some(val) = instance.get_field(prop_name) {
            return Ok(val.clone());
        }

        let method = instance
            .find_method(prop_name)
            .ok_or_else(|| EvalError::runtime(format!("method {prop_name} not found")))
            .cloned()?;

        let bound_env = Environment::new_child(&method.env, false);
        bound_env
            .borrow_mut()
            .define("this", Object::Instance(instance));

        Ok(Object::function(
            method.params.clone(),
            method.body,
            bound_env,
        ))
    }

    /// Evaluate an assignment expression.
    ///
    /// Supports three assignment targets:
    ///
    /// - **Identifier** (`name = value`) — walks the environment chain to find
    ///   the existing binding and overwrites it. Assigning to a name declared
    ///   as immutable (e.g. `const`) is rejected. Assigning to an undeclared
    ///   name is also an error — this is not implicit variable declaration.
    ///   Returns the assigned value.
    ///
    /// - **Index** (`container[i] = value`) — retrieves a mutable clone of the
    ///   container, applies the update to the element or key, then writes the
    ///   modified container back into the environment. Supports both `Array`
    ///   (integer index, bounds-checked) and `Hash` (any hashable key).
    ///   Returns [`Object::Null`].
    ///
    /// - **Property** (`object.field = value` or `this.field = value`) —
    ///   retrieves the instance from the environment, mutates the named field
    ///   on a clone, then writes the updated instance back. Only single-level
    ///   targets are supported; chained paths like `a.b.c = v` are rejected.
    ///   Returns [`Object::Null`].
    ///
    /// # Errors
    ///
    /// - [`EvalError::Runtime`] for immutability violations, undefined
    ///   variables, out-of-bounds indices, non-instance property targets, or
    ///   unsupported target expressions.
    /// - [`EvalError::TypeMismatch`] for wrong index types on arrays or hashes.
    /// - [`EvalError::TypeMismatch`] if a hash key is not a hashable type.
    /// - Any error from evaluating the right-hand-side value expression.
    fn eval_assignment(
        &self,
        assignment: &expression::AssignmentExpression,
        env: &Env,
    ) -> Result<Object, EvalError> {
        let value = self.eval_expression(assignment.value(), env)?;

        match assignment.name() {
            Expression::Identifier(ident) => {
                let name = ident.value();

                if env.borrow().is_immutable(name) {
                    return Err(EvalError::runtime(format!(
                        "cannot reassign constant {name}"
                    )));
                }

                if !env.borrow_mut().assign(name, value.clone()) {
                    return Err(EvalError::runtime(format!("variable {name} not defined")));
                }

                Ok(value)
            }

            Expression::Index(index) => {
                let container_name = match index.left() {
                    Expression::Identifier(ident) => ident.value(),
                    _ => return Err(EvalError::runtime("invalid assignment target")),
                };

                let mut container = env
                    .borrow()
                    .get(container_name)
                    .ok_or_else(|| EvalError::undefined(container_name))?;

                let index = self.eval_expression(index.index(), env)?;

                match (&mut container, index) {
                    (Object::Array(elements), Object::Integer(i)) => {
                        let len = elements.len() as i64;
                        if i < 0 || i >= len {
                            return Err(EvalError::runtime(format!(
                                "index out of bounds: {i} is not in range [0, {len})"
                            )));
                        }
                        elements[i as usize] = value;
                    }
                    (Object::Array(_), other) => {
                        return Err(EvalError::type_mismatch("Integer", other));
                    }
                    (Object::Hash(map), index) => {
                        map.insert(HashKey::try_from(index)?, value);
                    }
                    (other, _) => {
                        return Err(EvalError::type_mismatch("Array or Hash", other));
                    }
                }

                env.borrow_mut().assign(container_name, container);
                Ok(Object::Null)
            }

            Expression::Property(prop) => {
                let object_name = match prop.object() {
                    Expression::Identifier(ident) => ident.value(),
                    Expression::This(_) => "this",
                    _ => {
                        return Err(EvalError::runtime(
                            "complex property assignment targets are not supported",
                        ))
                    }
                };

                let prop_name = match prop.property() {
                    Expression::Identifier(ident) => ident.value(),
                    _ => return Err(EvalError::runtime("invalid property name")),
                };

                let mut obj = env
                    .borrow()
                    .get(object_name)
                    .ok_or_else(|| EvalError::undefined(object_name.to_string()))?;

                match &mut obj {
                    Object::Instance(instance) => {
                        instance.set_field(prop_name, value);
                    }

                    other => {
                        return Err(EvalError::runtime(format!(
                            "cannot assign property on non-instance: {other}"
                        )));
                    }
                }

                env.borrow_mut().assign(object_name, obj);
                Ok(Object::Null)
            }

            _ => Err(EvalError::runtime("invalid assignment target")),
        }
    }
}
