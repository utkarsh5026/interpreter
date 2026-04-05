// Evaluator struct, eval() dispatch

pub mod env;
mod expressions;
pub mod objects;
pub mod statements;

use std::fmt;

use thiserror::Error;
#[derive(Debug, Error)]
pub enum EvalError {
    #[error("type mismatch: expected {expected}, got {got}")]
    TypeMismatch { expected: String, got: String },

    #[error("undefined variable '{0}'")]
    UndefinedVariable(String),

    #[error("wrong number of arguments: expected {expected}, got {got}")]
    WrongArgCount { expected: usize, got: usize },

    #[error("cannot reassign const '{0}'")]
    ImmutableAssignment(String),

    #[error("variable '{0}' already declared in this scope")]
    AlreadyDeclared(String),

    #[error("index out of bounds: {index} is not in range [0, {len})")]
    IndexOutOfBounds { index: i64, len: i64 },

    #[error("'this' is not available outside a class method")]
    InvalidThis,

    #[error("circular inheritance: '{0}' cannot extend itself")]
    CircularInheritance(String),

    #[error("{0}")]
    Runtime(String),
}

impl EvalError {
    /// Construct a [`EvalError::TypeMismatch`] without struct syntax.
    pub fn type_mismatch(expected: impl Into<String>, got: impl fmt::Display) -> Self {
        Self::TypeMismatch {
            expected: expected.into(),
            got: got.to_string(),
        }
    }

    /// Construct an [`EvalError::UndefinedVariable`] from any string-like value.
    pub fn undefined(name: impl Into<String>) -> Self {
        Self::UndefinedVariable(name.into())
    }

    /// Construct an [`EvalError::ImmutableAssignment`] from any string-like value.
    pub fn immutable(name: impl Into<String>) -> Self {
        Self::ImmutableAssignment(name.into())
    }

    /// Construct an [`EvalError::AlreadyDeclared`] from any string-like value.
    pub fn already_declared(name: impl Into<String>) -> Self {
        Self::AlreadyDeclared(name.into())
    }

    /// Construct an [`EvalError::WrongArgCount`].
    #[must_use]
    pub const fn wrong_arg_count(expected: usize, got: usize) -> Self {
        Self::WrongArgCount { expected, got }
    }

    /// Construct an [`EvalError::IndexOutOfBounds`].
    #[must_use]
    pub const fn index_out_of_bounds(index: i64, len: i64) -> Self {
        Self::IndexOutOfBounds { index, len }
    }

    /// Construct a [`EvalError::Runtime`] for any ad-hoc message.
    pub fn runtime(msg: impl Into<String>) -> Self {
        Self::Runtime(msg.into())
    }
}

#[derive(Debug, Default)]
pub struct Evaluator;

impl Evaluator {
    #[must_use]
    pub const fn new() -> Self {
        Self
    }
}
