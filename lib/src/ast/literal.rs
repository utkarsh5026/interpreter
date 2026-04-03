use super::expression::{Expression, Indentifier};
use super::statements::BlockStatement;
use super::TokenSpan;
use crate::token::TokenPosition;

pub enum Literal {
    Func(FunctionLiteral),
    Array(ArrayLiteral),
    String(StringLiteral),
    Integer(IntegerLiteral),
    Hash(HashLiteral),
    Bool(BooleanLiteral),
    Null(NullLitreal),
}

impl Literal {
    pub(crate) const fn end_position(&self) -> &TokenPosition {
        match self {
            Self::Func(l) => &l.span.end,
            Self::Array(l) => &l.span.end,
            Self::String(l) => &l.span.end,
            Self::Integer(l) => &l.span.end,
            Self::Hash(l) => &l.span.end,
            Self::Bool(l) => &l.span.end,
            Self::Null(l) => &l.span.end,
        }
    }
}

impl std::fmt::Display for Literal {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self {
            Self::Func(func) => write!(f, "{func}"),
            Self::Array(array) => write!(f, "{array}"),
            Self::String(string) => write!(f, "{string}"),
            Self::Integer(integer) => write!(f, "{integer}"),
            Self::Hash(hash) => write!(f, "{hash}"),
            Self::Bool(boolean) => write!(f, "{boolean}"),
            Self::Null(null) => write!(f, "{null}"),
        }
    }
}

pub(crate) struct FunctionLiteral {
    span: TokenSpan,
    body: BlockStatement,
    parameters: Vec<Indentifier>,
}

impl FunctionLiteral {
    const fn new(span: TokenSpan, parameters: Vec<Indentifier>, body: BlockStatement) -> Self {
        Self {
            span,
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
            self.span.literal(),
            params,
            self.body
        )
    }
}

pub(crate) struct ArrayLiteral {
    span: TokenSpan,
    elements: Vec<Expression>,
}

impl ArrayLiteral {
    pub(crate) const fn new(span: TokenSpan, elements: Vec<Expression>) -> Self {
        Self { span, elements }
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
    span: TokenSpan,
    value: String,
}

impl StringLiteral {
    pub(crate) const fn new(span: TokenSpan, value: String) -> Self {
        Self { span, value }
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
    span: TokenSpan,
    value: i64,
}

impl IntegerLiteral {
    pub(crate) const fn new(span: TokenSpan, value: i64) -> Self {
        Self { span, value }
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
    span: TokenSpan,
    pairs: std::collections::HashMap<Expression, Expression>,
}

impl HashLiteral {
    pub(crate) const fn new(
        span: TokenSpan,
        pairs: std::collections::HashMap<Expression, Expression>,
    ) -> Self {
        Self { span, pairs }
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
    span: TokenSpan,
    value: bool,
}

impl BooleanLiteral {
    pub(crate) const fn new(span: TokenSpan, value: bool) -> Self {
        Self { span, value }
    }

    pub(crate) const fn value(&self) -> bool {
        self.value
    }
}

impl std::fmt::Display for BooleanLiteral {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "{}", self.value)
    }
}

pub(crate) struct NullLitreal {
    span: TokenSpan,
}

impl NullLitreal {
    pub(crate) const fn new(span: TokenSpan) -> Self {
        Self { span }
    }
}

impl std::fmt::Display for NullLitreal {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "null")
    }
}
