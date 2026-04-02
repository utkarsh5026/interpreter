use crate::token::Token;

use super::expression::{Expression, Indentifier};
use super::statements::BlockStatement;

pub(crate) enum Literal {
    Func(FunctionLiteral),
    Array(ArrayLiteral),
    String(StringLiteral),
    Integer(IntegerLiteral),
    Hash(HashLiteral),
    Bool(BooleanLiteral),
    Null(NullLitreal),
}

pub(crate) struct FunctionLiteral {
    token: Token,
    body: BlockStatement,
    parameters: Vec<Indentifier>,
}

impl FunctionLiteral {
    const fn new(token: Token, parameters: Vec<Indentifier>, body: BlockStatement) -> Self {
        Self {
            token,
            body,
            parameters,
        }
    }

    pub(crate) const fn params(&self) -> &[Indentifier] {
        self.parameters.as_slice()
    }
}

impl std::fmt::Display for FunctionLiteral {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        let params = self
            .parameters
            .iter()
            .map(ToString::to_string)
            .collect::<Vec<String>>()
            .join(", ");
        write!(
            f,
            "{}({}) {{\n{}\n}}",
            self.token.literal, params, self.body
        )
    }
}

pub(crate) struct ArrayLiteral {
    token: Token,
    elements: Vec<Expression>,
}

impl ArrayLiteral {
    pub(crate) const fn new(token: Token, elements: Vec<Expression>) -> Self {
        Self { token, elements }
    }

    pub(crate) const fn elements(&self) -> &[Expression] {
        self.elements.as_slice()
    }
}

impl std::fmt::Display for ArrayLiteral {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        let elements = self
            .elements
            .iter()
            .map(ToString::to_string)
            .collect::<Vec<String>>()
            .join(", ");
        write!(f, "[{elements}]")
    }
}

pub(crate) struct StringLiteral {
    token: Token,
    value: String,
}

impl StringLiteral {
    pub(crate) const fn new(token: Token, value: String) -> Self {
        Self { token, value }
    }

    pub(crate) const fn value(&self) -> &str {
        self.value.as_str()
    }
}

impl std::fmt::Display for StringLiteral {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "{}", self.value)
    }
}

pub(crate) struct IntegerLiteral {
    token: Token,
    value: i64,
}

impl IntegerLiteral {
    pub(crate) const fn new(token: Token, value: i64) -> Self {
        Self { token, value }
    }

    pub(crate) const fn value(&self) -> i64 {
        self.value
    }
}

impl std::fmt::Display for IntegerLiteral {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "{}", self.value)
    }
}

pub(crate) struct HashLiteral {
    token: Token,
    pairs: std::collections::HashMap<Expression, Expression>,
}

impl HashLiteral {
    pub(crate) const fn new(
        token: Token,
        pairs: std::collections::HashMap<Expression, Expression>,
    ) -> Self {
        Self { token, pairs }
    }

    pub(crate) const fn pairs(&self) -> &std::collections::HashMap<Expression, Expression> {
        &self.pairs
    }
}

impl std::fmt::Display for HashLiteral {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        let pairs = self
            .pairs
            .iter()
            .map(|(k, v)| format!("{k}: {v}"))
            .collect::<Vec<String>>()
            .join(", ");
        write!(f, "{{{pairs}}}")
    }
}

pub(crate) struct BooleanLiteral {
    token: Token,
    value: bool,
}

impl BooleanLiteral {
    pub(crate) const fn new(token: Token, value: bool) -> Self {
        Self { token, value }
    }

    pub(crate) const fn value(&self) -> bool {
        self.value
    }
}

pub(crate) struct NullLitreal {
    token: Token,
}

impl NullLitreal {
    pub(crate) const fn new(token: Token) -> Self {
        Self { token }
    }
}

impl std::fmt::Display for NullLitreal {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "null")
    }
}
