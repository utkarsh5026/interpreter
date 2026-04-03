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

use super::expression::{Expression, Indentifier};
use super::statements::BlockStatement;
use super::TokenSpan;
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
pub enum Literal {
    /// A first-class function expression: `fn(a, b) { a + b }`.
    Func(FunctionLiteral),

    /// An array literal: `[expr, expr, …]`.
    Array(ArrayLiteral),

    /// A string literal: `"hello, world"`.
    String(StringLiteral),

    /// A 64-bit signed integer literal: `42`.
    Integer(IntegerLiteral),

    /// A hash-map literal: `{"key": value, …}`.
    Hash(HashLiteral),

    /// A boolean literal: `true` or `false`.
    Bool(BooleanLiteral),

    /// The `null` literal.
    Null(NullLitreal),
}

impl Literal {
    /// Returns a reference to the [`TokenPosition`] at which this literal ends
    /// in the source text.
    ///
    /// The end position is stored inside the inner span of each variant and is
    /// used by the parser to attach accurate source ranges to enclosing AST
    /// nodes.  Because every branch simply field-accesses `span.end`, this
    /// method is `const`.
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

/// Delegates to the `Display` impl of each inner literal type.
///
/// The output is valid Mutant Lang source syntax, suitable for debug printing
/// and REPL echo.
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
pub struct FunctionLiteral {
    /// Source span from `fn` through the closing `}`.
    span: TokenSpan,
    /// The parsed body of the function.
    body: BlockStatement,
    /// Formal parameter names, in declaration order.
    parameters: Vec<Indentifier>,
}

impl FunctionLiteral {
    /// Constructs a new `FunctionLiteral` from its constituent parts.
    ///
    /// Takes ownership of `span`, `parameters`, and `body`.  Called by the
    /// parser immediately after it has consumed the closing `}` of the function
    /// body.
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

    /// Returns the formal parameter list as a borrowed slice of [`Indentifier`]s.
    ///
    /// The slice is in declaration order and is empty for zero-parameter
    /// functions.  The evaluator uses this to bind argument values to names
    /// when the function is called.
    pub(crate) const fn params(&self) -> &[Indentifier] {
        self.parameters.as_slice()
    }
}

/// Formats the function literal as `fn(a, b) {\n<body>\n}`.
///
/// The keyword (`fn`) is taken from the start token's literal text so that any
/// future aliases or syntax variations round-trip correctly through `Display`.
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
pub(crate) struct ArrayLiteral {
    /// Source span from `[` through `]`.
    span: TokenSpan,
    /// The element expressions, in source order.
    elements: Vec<Expression>,
}

impl ArrayLiteral {
    /// Constructs an `ArrayLiteral` from its source span and element list.
    ///
    /// Takes ownership of both arguments.  Called by the parser after it has
    /// consumed the closing `]`.
    pub(crate) const fn new(span: TokenSpan, elements: Vec<Expression>) -> Self {
        Self { span, elements }
    }

    /// Returns the element expressions as a borrowed slice, in source order.
    ///
    /// An empty slice indicates an empty array literal (`[]`).
    pub(crate) const fn elements(&self) -> &[Expression] {
        self.elements.as_slice()
    }
}

/// Formats the array as `[elem₀, elem₁, …]` using each element's `Display`.
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
pub(crate) struct StringLiteral {
    /// Source span whose start token carries the string text (without quotes).
    span: TokenSpan,
}

impl StringLiteral {
    /// Constructs a `StringLiteral` from its source span.
    ///
    /// The span's start token literal must already have had the surrounding
    /// quote characters stripped by the lexer.
    pub(crate) const fn new(span: TokenSpan) -> Self {
        Self { span }
    }

    /// Returns the string content as a borrowed `str`.
    ///
    /// The lifetime of the returned slice is tied to `self` (and transitively
    /// to the [`TokenSpan`] it wraps).  The lexer is expected to have stripped
    /// the surrounding `"` delimiters before storing the literal in the token.
    pub(crate) fn value(&self) -> &str {
        self.span.literal()
    }
}

/// Formats the string using its raw content (no surrounding quotes).
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
pub(crate) struct IntegerLiteral {
    /// Source span for the integer token.
    span: TokenSpan,
    /// The numeric value parsed from the token text.
    value: i64,
}

impl IntegerLiteral {
    /// Constructs an `IntegerLiteral` from its source span and parsed value.
    ///
    /// `value` must have already been parsed from the token's literal text by
    /// the parser before this constructor is called.
    pub(crate) const fn new(span: TokenSpan, value: i64) -> Self {
        Self { span, value }
    }

    /// Returns the integer value.
    ///
    /// Returns a copy (not a reference) because `i64` is `Copy`.
    pub(crate) const fn value(&self) -> i64 {
        self.value
    }
}

/// Formats the integer as its decimal representation.
impl std::fmt::Display for IntegerLiteral {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "{}", self.value)
    }
}

/// A hash-map literal in the AST: `{"key": value, …}`.
///
/// Key–value pairs are stored in a [`HashMap`] keyed on [`Expression`].  Using
/// `Expression` as a map key requires `Expression` to implement `Hash` and
/// `Eq`; the evaluator evaluates each key expression to determine the runtime
/// hash bucket.
///
/// Iteration order over the pairs is unspecified (standard `HashMap`
/// behaviour), so `Display` output may vary between runs.
///
/// [`HashMap`]: std::collections::HashMap
pub(crate) struct HashLiteral {
    /// Source span from `{` through `}`.
    span: TokenSpan,
    /// The key–value pairs as parsed expressions.
    pairs: std::collections::HashMap<Expression, Expression>,
}

impl HashLiteral {
    /// Constructs a `HashLiteral` from its source span and pair map.
    ///
    /// Takes ownership of both arguments.  Called by the parser after consuming
    /// the closing `}` of the hash literal.
    pub(crate) const fn new(
        span: TokenSpan,
        pairs: std::collections::HashMap<Expression, Expression>,
    ) -> Self {
        Self { span, pairs }
    }

    /// Returns a reference to the key–value pair map.
    ///
    /// The evaluator iterates over this map, evaluates each key and value
    /// expression, and inserts the results into a runtime hash object.
    pub(crate) const fn pairs(&self) -> &std::collections::HashMap<Expression, Expression> {
        &self.pairs
    }
}

/// Formats the hash literal as `{k₀: v₀, k₁: v₁, …}`.
///
/// Iteration order is unspecified; do not rely on a stable ordering.
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
pub(crate) struct BooleanLiteral {
    /// Source span whose start token literal is either `"true"` or `"false"`.
    span: TokenSpan,
}

impl BooleanLiteral {
    /// Constructs a `BooleanLiteral` from its source span.
    ///
    /// The span's start token literal must be exactly `"true"` or `"false"` —
    /// the lexer is responsible for ensuring this invariant.
    pub(crate) const fn new(span: TokenSpan) -> Self {
        Self { span }
    }

    /// Returns the boolean value by inspecting the source token text.
    ///
    /// Returns `true` when the token literal is `"true"`, `false` otherwise.
    /// The invariant that only `"true"` and `"false"` tokens produce a
    /// `BooleanLiteral` is maintained by the parser.
    pub(crate) fn value(&self) -> bool {
        self.span.literal() == "true"
    }
}

/// Formats the boolean as `true` or `false`.
impl std::fmt::Display for BooleanLiteral {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "{}", self.value())
    }
}

/// The `null` literal in the AST.
///
/// `null` carries no data beyond its source span.  The evaluator maps any
/// `NullLitreal` node to the singleton null runtime object.
///
/// # Note on the type name
///
/// The name `NullLitreal` is a typo of "NullLiteral" inherited from the
/// original source.  It is preserved here to avoid a breaking rename; consider
/// fixing it in a dedicated refactor commit.
pub(crate) struct NullLitreal {
    /// Source span for the `null` token.
    span: TokenSpan,
}

impl NullLitreal {
    /// Constructs a `NullLitreal` from its source span.
    pub(crate) const fn new(span: TokenSpan) -> Self {
        Self { span }
    }
}

/// Always formats as the string `"null"`.
impl std::fmt::Display for NullLitreal {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "null")
    }
}
