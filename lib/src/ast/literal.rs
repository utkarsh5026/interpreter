//! Literal value nodes in the Mutant Lang AST.
//!
//! A [`Literal`] is any syntactic construct whose value is known at parse time
//! without further evaluation — numbers, strings, booleans, `null`, arrays,
//! hash maps, and function expressions (which are first-class values in Mutant
//! Lang).
//!
//! Each concrete literal type stores a [`TokenSpan`] that records the token
//! that introduced the literal and the source position at which it ended.
//! This span is used for error reporting and for implementing
//! [`Literal::end_position`].
//!
//! # Variant overview
//!
//! | Variant | Source syntax | Concrete type |
//! |---|---|---|
//! | [`Literal::Integer`] | `42`, `-7` | [`IntegerLiteral`] |
//! | [`Literal::String`] | `"hello"` | [`StringLiteral`] |
//! | [`Literal::Bool`] | `true`, `false` | [`BooleanLiteral`] |
//! | [`Literal::Null`] | `null` | [`NullLitreal`] |
//! | [`Literal::Array`] | `[1, 2, 3]` | [`ArrayLiteral`] |
//! | [`Literal::Hash`] | `{"a": 1}` | [`HashLiteral`] |
//! | [`Literal::Func`] | `fn(x) { x + 1 }` | [`FunctionLiteral`] |

use super::TokenSpan;
use super::expression::{Expression, Indentifier};
use super::statements::BlockStatement;
use crate::token::TokenPosition;

/// A closed set of all literal forms recognized by the Mutant Lang parser.
///
/// This enum is stored inside [`Expression::Literal`] whenever the parser
/// encounters a syntactic value rather than an operator or identifier.  The
/// evaluator matches on this enum to produce a runtime [`Object`].
///
/// Every variant wraps a dedicated struct that carries the source span and any
/// extra semantic data (e.g. the parsed `i64` for integers, the element list
/// for arrays).  This two-level design keeps the enum small while letting each
/// concrete type grow its own API independently.
///
/// [`Object`]: crate::object::Object
/// [`Expression::Literal`]: crate::ast::expression::Expression::Literal
#[derive(Debug, Clone)]
pub enum Literal {
    Func(FunctionLiteral),
    Array(ArrayLiteral),
    String(StringLiteral),
    Integer(IntegerLiteral),
    Hash(HashLiteral),
    Bool(BooleanLiteral),
    Float(FloatLiteral),
    Null(NullLitreal),
    FString(FStringLiteral),
}

impl Literal {
    /// Returns a reference to the [`TokenPosition`] at which this literal ends
    /// in the source text.
    pub(crate) const fn end_position(&self) -> &TokenPosition {
        match self {
            Self::Func(l) => &l.span.end,
            Self::Array(l) => &l.span.end,
            Self::String(l) => &l.span.end,
            Self::Integer(l) => &l.span.end,
            Self::Hash(l) => &l.span.end,
            Self::Bool(l) => &l.span.end,
            Self::Null(l) => &l.span.end,
            Self::Float(l) => &l.span.end,
            Self::FString(l) => &l.span.end,
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
            Self::Float(float) => write!(f, "{float}"),
            Self::FString(fstring) => write!(f, "{fstring}"),
        }
    }
}

/// An anonymous function expression in the AST: `fn(params…) { body }`.
///
/// Function literals are first-class values in Mutant Lang — they can be
/// assigned to variables, passed as arguments, and returned from other
/// functions.  The parser emits a `FunctionLiteral` whenever it encounters the
/// `fn` keyword in expression position.
///
/// The `span` covers from the `fn` keyword token through the closing `}` of
/// the body.  The literal text of the span's start token is used as the
/// keyword display string in [`Display`].
///
/// [`Display`]: std::fmt::Display
#[derive(Debug, Clone)]
pub struct FunctionLiteral {
    span: TokenSpan,
    body: BlockStatement,
    parameters: Vec<Indentifier>,
}

impl FunctionLiteral {
    pub(crate) const fn new(
        span: TokenSpan,
        parameters: Vec<Indentifier>,
        body: BlockStatement,
    ) -> Self {
        Self {
            span,
            body,
            parameters,
        }
    }

    pub(crate) const fn params(&self) -> &[Indentifier] {
        self.parameters.as_slice()
    }

    pub(crate) const fn body(&self) -> &BlockStatement {
        &self.body
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

/// An array literal in the AST: `[elem₀, elem₁, …]`.
///
/// Elements are stored as a `Vec<Expression>` and are evaluated left-to-right
/// by the evaluator to produce a runtime array object.  An empty `[]` is a
/// valid array literal with an empty element list.
///
/// The `span` covers from the opening `[` through the closing `]`.
#[derive(Debug, Clone)]
pub struct ArrayLiteral {
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

/// A string literal in the AST: `"hello"`.
///
/// The string value is not stored separately — it is read directly from the
/// start token's literal text via [`StringLiteral::value`].  This avoids an
/// extra heap allocation during parsing; the token already owns the string
/// data through [`TokenSpan`].
#[derive(Debug, Clone)]
pub struct StringLiteral {
    span: TokenSpan,
}

impl StringLiteral {
    pub(crate) const fn new(span: TokenSpan) -> Self {
        Self { span }
    }

    pub(crate) fn value(&self) -> &str {
        self.span.literal()
    }
}

impl std::fmt::Display for StringLiteral {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "{}", self.value())
    }
}

/// A 64-bit signed integer literal in the AST: `42`, `-7`.
///
/// The parsed numeric value is stored as an `i64` alongside the source span so
/// that the evaluator can retrieve it in O(1) without re-parsing the token
/// text.
#[derive(Debug, Clone)]
pub struct IntegerLiteral {
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

/// A hash-map literal in the AST: `{"key": value, …}`.
///
/// Key–value pairs are stored as a `Vec` of `(key, value)` expression pairs.
/// Insertion order is preserved, and duplicate keys are allowed at the AST
/// level (the evaluator will use the last value for a given key).
#[derive(Debug, Clone)]
pub struct HashLiteral {
    span: TokenSpan,
    pairs: Vec<(Expression, Expression)>,
}

impl HashLiteral {
    pub(crate) const fn new(span: TokenSpan, pairs: Vec<(Expression, Expression)>) -> Self {
        Self { span, pairs }
    }

    pub(crate) fn pairs(&self) -> &[(Expression, Expression)] {
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

/// A boolean literal in the AST: `true` or `false`.
///
/// The boolean value is not stored as a separate `bool` field; instead it is
/// derived on demand from the start token's literal text by comparing it to
/// the string `"true"`.  This mirrors the approach used by [`StringLiteral`]
/// and avoids redundancy with data already held in the [`TokenSpan`].
#[derive(Debug, Clone)]
pub struct BooleanLiteral {
    span: TokenSpan,
}

impl BooleanLiteral {
    pub(crate) const fn new(span: TokenSpan) -> Self {
        Self { span }
    }

    pub(crate) fn value(&self) -> bool {
        self.span.literal() == "true"
    }
}

impl std::fmt::Display for BooleanLiteral {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "{}", self.value())
    }
}

/// The `null` literal in the AST.
///
/// `null` carries no data beyond its source span.  The evaluator maps any
/// `NullLitreal` node to the singleton null runtime object.
#[derive(Debug, Clone)]
pub struct NullLitreal {
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

/// A 64-bit floating-point literal in the AST: `3.14`, `1e6`, `.5`.
///
/// The parsed `f64` value is stored alongside the source span so the evaluator
/// can retrieve it in O(1) without re-parsing.
#[derive(Debug, Clone)]
pub struct FloatLiteral {
    span: TokenSpan,
    value: f64,
}

impl FloatLiteral {
    pub(crate) const fn new(span: TokenSpan, value: f64) -> Self {
        Self { span, value }
    }

    pub(crate) const fn value(&self) -> f64 {
        self.value
    }
}

impl std::fmt::Display for FloatLiteral {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "{}", self.value)
    }
}

/// An f-string literal in the AST: `f"Hello {name}!"`.
///
/// F-strings interleave static text segments with embedded expressions.
/// `static_parts` and `expressions` are related by position:
/// the final string is `static_parts[0] + eval(expressions[0]) + static_parts[1] + …`
/// (there is always one more static part than expressions).
#[derive(Debug, Clone)]
pub struct FStringLiteral {
    pub(crate) span: TokenSpan,
    pub(crate) static_parts: Vec<String>,
    pub(crate) expressions: Vec<super::expression::Expression>,
}

impl FStringLiteral {
    pub(crate) const fn new(
        span: TokenSpan,
        static_parts: Vec<String>,
        expressions: Vec<super::expression::Expression>,
    ) -> Self {
        Self {
            span,
            static_parts,
            expressions,
        }
    }

    pub(crate) fn static_parts(&self) -> &[String] {
        &self.static_parts
    }

    pub(crate) fn expressions(&self) -> &[super::expression::Expression] {
        &self.expressions
    }
}

impl std::fmt::Display for FStringLiteral {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "f\"")?;
        for (i, part) in self.static_parts.iter().enumerate() {
            write!(f, "{part}")?;
            if let Some(expr) = self.expressions.get(i) {
                write!(f, "{{{expr}}}")?;
            }
        }
        write!(f, "\"")
    }
}
