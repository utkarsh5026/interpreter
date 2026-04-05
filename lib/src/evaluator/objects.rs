use crate::ast::expression::Indentifier;
use crate::ast::statements::BlockStatement;
use crate::evaluator::env::{Env, Environment};
use std::collections::HashMap;
use std::fmt;

use super::EvalError;

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub enum HashKey {
    Integer(i64),
    Boolean(bool),
    Str(String),
}

#[derive(Debug, Clone)]
pub struct FunctionObject {
    pub params: Vec<Indentifier>,
    pub body: BlockStatement,
    pub env: Env,
}

impl FunctionObject {
    pub const fn new(params: Vec<Indentifier>, body: BlockStatement, env: Env) -> Self {
        Self { params, body, env }
    }

    /// Create a call-site environment with arguments bound to parameters.
    ///
    /// Uses the function's captured `env` as the parent so that lexical
    /// scoping works correctly — the body sees variables from the closure's
    /// definition site, not the call site.
    #[must_use]
    pub fn make_call_env(&self, args: Vec<Object>) -> Env {
        let call_env = Environment::new_child(&self.env, false);
        for (param, arg) in self.params.iter().zip(args) {
            call_env.borrow_mut().define(param.value(), arg);
        }
        call_env
    }
}

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

#[derive(Debug, Clone)]
pub struct ClassObject {
    pub name: String,
    pub parent: Option<Box<Self>>,
    pub constructor: Option<Box<FunctionObject>>,
    pub methods: HashMap<String, FunctionObject>,
    pub env: Env,
}

impl ClassObject {
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
    #[must_use]
    pub fn find_method(&self, name: &str) -> Option<&FunctionObject> {
        if let Some(method) = self.methods.get(name) {
            return Some(method);
        }
        self.parent.as_deref()?.find_method(name)
    }
}

impl fmt::Display for ClassObject {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "<class {}>", self.name)
    }
}

#[derive(Debug, Clone)]
pub struct InstanceObject {
    pub class: Box<ClassObject>,
    pub fields: HashMap<String, Object>,
    pub env: Env,
}

impl InstanceObject {
    pub fn new(class: ClassObject, env: Env) -> Self {
        Self {
            class: Box::new(class),
            fields: HashMap::new(),
            env,
        }
    }

    pub fn get_field(&self, name: &str) -> Option<&Object> {
        self.fields.get(name)
    }

    pub fn set_field(&mut self, name: impl Into<String>, val: Object) {
        self.fields.insert(name.into(), val);
    }

    pub fn find_method(&self, name: &str) -> Option<&FunctionObject> {
        self.class.find_method(name)
    }
}

impl fmt::Display for InstanceObject {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "<instance of {}>", self.class.name)
    }
}

#[derive(Debug, Clone)]
pub enum Object {
    Integer(i64),
    Float(f64),
    Boolean(bool),
    Str(String),
    Null,
    Array(Vec<Self>),
    Hash(HashMap<HashKey, Self>),
    Function(FunctionObject),
    Builtin(fn(Vec<Self>) -> Self),
    Return(Box<Self>),
    Break,
    Continue,
    Class(ClassObject),
    Instance(InstanceObject),
    ClassContext(ClassObject),
}

impl Object {
    pub fn integer(value: i64) -> Self {
        Object::Integer(value)
    }

    pub fn float(value: f64) -> Self {
        Object::Float(value)
    }

    pub fn boolean(value: bool) -> Self {
        Object::Boolean(value)
    }

    pub fn string(value: impl Into<String>) -> Self {
        Object::Str(value.into())
    }

    pub fn null() -> Self {
        Object::Null
    }

    pub fn array(items: Vec<Object>) -> Self {
        Object::Array(items)
    }

    pub fn hash(map: HashMap<HashKey, Object>) -> Self {
        Object::Hash(map)
    }

    pub fn function(params: Vec<Indentifier>, body: BlockStatement, env: Env) -> Self {
        Object::Function(FunctionObject::new(params, body, env))
    }

    pub fn builtin(f: fn(Vec<Self>) -> Object) -> Self {
        Self::Builtin(f)
    }

    #[must_use]
    pub fn return_val(obj: Self) -> Self {
        Self::Return(Box::new(obj))
    }

    pub fn class(
        name: impl Into<String>,
        parent: Option<ClassObject>,
        constructor: Option<FunctionObject>,
        methods: HashMap<String, FunctionObject>,
        env: Env,
    ) -> Self {
        Self::Class(ClassObject::new(name, parent, constructor, methods, env))
    }

    pub fn instance(class: ClassObject, env: Env) -> Self {
        Self::Instance(InstanceObject::new(class, env))
    }

    #[must_use]
    pub fn is_truthy(&self) -> bool {
        match self {
            Object::Boolean(b) => *b,
            Object::Null => false,
            Object::Integer(n) => *n != 0,
            Object::Float(f) => *f != 0.0 && f.is_finite(),
            Object::Str(s) => !s.is_empty(),
            Object::Array(a) => !a.is_empty(),
            Object::ClassContext(_) => false,
            _ => true,
        }
    }

    pub fn int_div(self, rhs: &Self) -> Result<Object, EvalError> {
        match (&self, &rhs) {
            (Object::Integer(l), Object::Integer(r)) => {
                if *r == 0 {
                    return Err(EvalError::runtime("division by zero"));
                }
                Ok((*l / *r).into())
            }
            _ => match coerce_to_floats(&self, &rhs) {
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

impl From<i64> for Object {
    fn from(value: i64) -> Self {
        Self::Integer(value)
    }
}

impl From<f64> for Object {
    fn from(value: f64) -> Self {
        Self::Float(value)
    }
}

impl From<bool> for Object {
    fn from(value: bool) -> Self {
        Self::Boolean(value)
    }
}

impl From<String> for Object {
    fn from(value: String) -> Self {
        Object::Str(value)
    }
}

impl From<&str> for Object {
    fn from(value: &str) -> Self {
        Object::Str(value.to_owned())
    }
}

impl From<Vec<Object>> for Object {
    fn from(items: Vec<Object>) -> Self {
        Object::Array(items)
    }
}

impl From<HashMap<HashKey, Object>> for Object {
    fn from(map: HashMap<HashKey, Object>) -> Self {
        Object::Hash(map)
    }
}

impl TryFrom<Object> for HashKey {
    type Error = EvalError;

    fn try_from(obj: Object) -> Result<Self, Self::Error> {
        match obj {
            Object::Integer(n) => Ok(HashKey::Integer(n)),
            Object::Boolean(b) => Ok(HashKey::Boolean(b)),
            Object::Str(s) => Ok(HashKey::Str(s)),
            other => Err(EvalError::TypeMismatch {
                expected: "Integer, Boolean, or String".into(),
                got: format!("{other}"),
            }),
        }
    }
}

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

impl std::ops::Add for Object {
    type Output = Result<Object, EvalError>;

    fn add(self, rhs: Object) -> Self::Output {
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

impl std::ops::Sub for Object {
    type Output = Result<Object, EvalError>;

    fn sub(self, rhs: Object) -> Self::Output {
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

impl std::ops::Mul for Object {
    type Output = Result<Object, EvalError>;

    fn mul(self, rhs: Object) -> Self::Output {
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

impl std::ops::Div for Object {
    type Output = Result<Object, EvalError>;

    fn div(self, rhs: Object) -> Self::Output {
        match (&self, &rhs) {
            (Self::Integer(l), Self::Integer(r)) => {
                if *r == 0 {
                    return Err(EvalError::runtime("division by zero"));
                }
                Ok((*l / *r).into())
            }
            _ => match (to_f64(&self), to_f64(&rhs)) {
                (Some(l), Some(r)) => Ok((l / r).into()), // f64 → ±inf, not a panic
                _ => Err(EvalError::type_mismatch(
                    "numeric",
                    format!("{self} / {rhs}"),
                )),
            },
        }
    }
}

impl std::ops::Rem for Object {
    type Output = Result<Object, EvalError>;

    fn rem(self, rhs: Object) -> Self::Output {
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

const fn to_f64(obj: &Object) -> Option<f64> {
    match obj {
        Object::Integer(i) => Some(*i as f64),
        Object::Float(f) => Some(*f),
        _ => None,
    }
}

const fn coerce_to_floats(l: &Object, r: &Object) -> Option<(f64, f64)> {
    match (l, r) {
        (Object::Float(a), Object::Float(b)) => Some((*a, *b)),
        (Object::Float(a), Object::Integer(b)) => Some((*a, *b as f64)),
        (Object::Integer(a), Object::Float(b)) => Some((*a as f64, *b)),
        _ => None,
    }
}
