//! Lexical token types, positions, and values for the Mutant Lang interpreter.
//!
//! This module is the foundation of the lexer pipeline. It defines three
//! closely related concepts:
//!
//! - [`TokenType`] — the *kind* of a token (what syntactic role it plays).
//! - [`TokenPosition`] — where in the source text the token was found.
//! - [`Token`] — a complete lexical unit combining both of the above with the
//!   raw source text slice that produced it.
//!
//!
//! # Design note
//!
//! [`TokenType`] and [`TokenPosition`] both derive [`Copy`], keeping tokens
//! cheap to pass around without thinking about ownership. [`Token`] itself owns
//! its `literal` string and therefore only derives [`Clone`]; callers that need
//! to inspect many tokens without consuming them should borrow `&Token`.

/// Every distinct syntactic category the Mutant Lang lexer can produce.
///
/// A `TokenType` describes the *role* of a lexeme — it does **not** carry the
/// raw source text. The actual characters are stored alongside the variant in
/// [`Token::literal`].
#[derive(Debug, PartialEq, Eq, Clone, Copy)]
pub enum TokenType {
    /// A character or token the lexer does not recognize.
    ///
    /// The evaluator treats this as a hard error; the lexer emits it rather
    /// than panicking so that the error can be reported with position info.
    Illegal,

    /// Signals the end of the input stream.
    ///
    /// The parser uses this sentinel to detect when it has consumed all tokens
    /// without requiring an `Option` at every call site.
    Eof,

    /// A user-defined name: variable, function, or class identifier.
    ///
    /// The actual name is carried by [`Token::literal`]. Classification into
    /// keyword vs. plain identifier happens in [`lookup_identifier`].
    Identifier,

    /// An integer literal (`42`, `-7`).
    ///
    /// The raw digits are stored in [`Token::literal`]; parsing to a numeric
    /// type is deferred to the evaluator.
    Int,

    /// A floating-point literal (`3.14`, `0.5`).
    ///
    /// Like [`Int`](Self::Int), the numeric conversion is left to the
    /// evaluator.
    Float,

    /// A plain string literal enclosed in `"…"` or `'…'`.
    String,

    /// An interpolated (f-string) literal, e.g. `f"Hello, {name}!"`.
    ///
    /// The lexer emits the raw source text including the `f` prefix and braces;
    /// the evaluator is responsible for expanding the interpolated expressions.
    FString,

    /// Simple assignment (`=`).
    Assign,

    /// Addition (`+`).
    Plus,

    /// Subtraction or unary negation (`-`).
    Minus,

    /// Logical NOT or unary negation prefix (`!`).
    Bang,

    /// Multiplication (`*`).
    Asterisk,

    /// Floating-point division (`/`).
    Slash,

    /// Integer (floor) division (`//`), analogous to Python's `//` operator.
    IntDivision,

    /// Modulus / remainder (`%`).
    Modulus,

    /// Less-than comparison (`<`).
    LessThan,

    /// Greater-than comparison (`>`).
    GreaterThan,

    /// Less-than-or-equal comparison (`<=`).
    LessThanOrEqual,

    /// Greater-than-or-equal comparison (`>=`).
    GreaterThanOrEqual,

    /// Equality comparison (`==`).
    Eq,

    /// Inequality comparison (`!=`).
    NotEq,

    /// Short-circuit logical AND (`&&`).
    And,

    /// Short-circuit logical OR (`||`).
    Or,

    /// Addition-assignment (`+=`).
    PlusAssign,

    /// Subtraction-assignment (`-=`).
    MinusAssign,

    /// Multiplication-assignment (`*=`).
    AsteriskAssign,

    /// Division-assignment (`/=`).
    SlashAssign,

    /// Modulus-assignment (`%=`).
    ModulusAssign,

    /// Bitwise AND (`&`).
    ///
    /// Distinct from [`And`](Self::And) (`&&`); used in bit-manipulation
    /// expressions.
    BitwiseAnd,

    /// Bitwise OR (`|`).
    BitwiseOr,

    /// Bitwise XOR (`^`).
    BitwiseXor,

    /// Bitwise NOT / one's complement (`~`).
    BitwiseNot,

    /// Left bit-shift (`<<`).
    BitwiseLeftShift,

    /// Right bit-shift (`>>`).
    BitwiseRightShift,

    /// Argument / element separator (`,`).
    Comma,

    /// Statement terminator (`;`).
    Semicolon,

    /// Key-value separator in hash literals and type annotations (`:`).
    Colon,

    /// Member-access operator (`.`).
    Dot,

    /// Opening parenthesis (`(`).
    LParen,

    /// Closing parenthesis (`)`).
    RParen,

    /// Opening curly brace — begins blocks and hash literals (`{`).
    LBrace,

    /// Closing curly brace (`}`).
    RBrace,

    /// Opening square bracket — begins array literals and index expressions (`[`).
    LBracket,

    /// Closing square bracket (`]`).
    RBracket,

    /// The `fn` keyword — introduces a function literal or declaration.
    Function,

    /// The `let` keyword — introduces a mutable variable binding.
    Let,

    /// The boolean literal `true`.
    True,

    /// The boolean literal `false`.
    False,

    /// The `if` keyword — begins a conditional expression.
    If,

    /// The `else` keyword — begins the fallback branch of an `if` expression.
    Else,

    /// The `elif` keyword — begins an else-if branch (alternative to chained `if`/`else`).
    Elif,

    /// The `return` keyword — exits the current function with an optional value.
    Return,

    /// The `while` keyword — begins a pre-condition loop.
    While,

    /// The `break` keyword — exits the nearest enclosing loop immediately.
    Break,

    /// The `continue` keyword — skips the rest of the current loop iteration.
    Continue,

    /// The `for` keyword — begins an iterator-style loop.
    For,

    /// The `const` keyword — introduces an immutable binding.
    Const,

    /// The `class` keyword — begins a class declaration.
    Class,

    /// The `extends` keyword — declares a class's parent in an inheritance chain.
    Extends,

    /// The `super` keyword — refers to the parent class within a subclass body.
    Super,

    /// The `this` keyword — refers to the current object instance.
    This,

    /// The `new` keyword — allocates and initialises a class instance.
    New,

    /// The `null` keyword — represents the absence of a value.
    Null,
}

impl std::fmt::Display for TokenType {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        let s = match self {
            Self::Illegal => "ILLEGAL",
            Self::Eof => "EOF",
            Self::Identifier => "IDENTIFIER",
            Self::Int => "INT",
            Self::Float => "FLOAT",
            Self::String => "STRING",
            Self::FString => "F_STRING",
            Self::Assign => "=",
            Self::Plus => "+",
            Self::Minus => "-",
            Self::Bang => "!",
            Self::Asterisk => "*",
            Self::Slash => "/",
            Self::IntDivision => "//",
            Self::Modulus => "%",
            Self::LessThan => "<",
            Self::GreaterThan => ">",
            Self::LessThanOrEqual => "<=",
            Self::GreaterThanOrEqual => ">=",
            Self::Eq => "==",
            Self::NotEq => "!=",
            Self::And => "&&",
            Self::Or => "||",
            Self::PlusAssign => "+=",
            Self::MinusAssign => "-=",
            Self::AsteriskAssign => "*=",
            Self::SlashAssign => "/=",
            Self::ModulusAssign => "%=",
            Self::BitwiseAnd => "&",
            Self::BitwiseOr => "|",
            Self::BitwiseXor => "^",
            Self::BitwiseNot => "~",
            Self::BitwiseLeftShift => "<<",
            Self::BitwiseRightShift => ">>",
            Self::Comma => ",",
            Self::Semicolon => ";",
            Self::Colon => ":",
            Self::Dot => ".",
            Self::LParen => "(",
            Self::RParen => ")",
            Self::LBrace => "{",
            Self::RBrace => "}",
            Self::LBracket => "[",
            Self::RBracket => "]",
            Self::Function => "fn",
            Self::Let => "let",
            Self::True => "true",
            Self::False => "false",
            Self::If => "if",
            Self::Else => "else",
            Self::Elif => "elif",
            Self::Return => "return",
            Self::While => "while",
            Self::Break => "break",
            Self::Continue => "continue",
            Self::For => "for",
            Self::Const => "const",
            Self::Class => "class",
            Self::Extends => "extends",
            Self::Super => "super",
            Self::This => "this",
            Self::New => "new",
            Self::Null => "null",
        };
        write!(f, "{s}")
    }
}

/// Source-file location of a single token, in 1-based line/column coordinates.
///
/// Positions are captured by the lexer as it scans and are embedded in every
/// [`Token`]. The parser and evaluator surface them in error messages so the
/// user knows exactly where a problem occurred.
///
/// Both coordinates are stored as `usize` because negative line numbers are
/// not meaningful and the constraint is enforced at construction time by
/// [`TokenPosition::new`].
///
/// # Coordinate convention
///
/// - `line` is **1-based**: the first line of the file is line `1`.
/// - `column` is **0-based** by convention (assumed — verify with lexer
///   implementation).
#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub struct TokenPosition {
    line: usize,
    column: usize,
}

impl TokenPosition {
    /// Constructs a `TokenPosition` from a 1-based line number and a column offset.
    ///
    /// Use this whenever the lexer records where it read a token. The `line`
    /// invariant is checked eagerly so that bogus positions are caught at the
    /// point of construction rather than surfacing later in error output.
    ///
    /// # Panics
    ///
    /// Panics if `line` is `0`, because line numbers are 1-based throughout
    /// the interpreter.
    ///
    /// # Examples
    ///
    /// ```rust
    /// # use mutant_lang::token::TokenPosition;
    /// let pos = TokenPosition::new(1, 0);
    /// assert_eq!(pos.to_string(), "line 1, column 0");
    /// ```
    ///
    /// ```rust,should_panic
    /// # use mutant_lang::token::TokenPosition;
    /// let _ = TokenPosition::new(0, 5); // panics: line must be >= 1
    /// ```
    #[must_use]
    pub fn new(line: usize, column: usize) -> Self {
        assert!(line >= 1, "line number must be >= 1, got {line}");
        Self { line, column }
    }
}

impl std::fmt::Display for TokenPosition {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "line {}, column {}", self.line, self.column)
    }
}

/// A single lexical unit produced by the lexer.
///
/// A `Token` bundles three pieces of information together:
///
/// - `kind` — what syntactic role the token plays (see [`TokenType`]).
/// - `literal` — the exact slice of source text that was scanned, e.g.
///   `"42"`, `"myVar"`, `"fn"`. For tokens whose text is fixed (operators,
///   keywords), this echoes the canonical form; for [`Identifier`](TokenType::Identifier),
///   [`Int`](TokenType::Int), [`Float`](TokenType::Float), and
///   [`String`](TokenType::String) it carries the user-supplied value.
/// - `position` — where in the source file the token begins, for error
///   reporting.
///
/// `Token` owns its `literal` string. If you need to pass tokens around
/// cheaply without cloning, borrow `&Token` or copy just the `kind` field
/// (which is [`Copy`]).
///
/// # Examples
///
/// ```rust
/// # use mutant_lang::token::{Token, TokenType, TokenPosition};
/// let pos = TokenPosition::new(1, 0);
/// let tok = Token::new(TokenType::Int, "42", pos);
/// assert_eq!(tok.kind, TokenType::Int);
/// assert_eq!(tok.literal, "42");
/// ```
#[derive(Debug, Clone)]
pub struct Token {
    /// The syntactic category of this token.
    pub(crate) kind: TokenType,

    /// The exact source-text spelling of this token.
    ///
    /// For fixed-text tokens (operators, keywords) this matches [`TokenType`]'s
    /// `Display` output. For value tokens ([`Identifier`](TokenType::Identifier),
    /// [`Int`](TokenType::Int), etc.) this is the user-written text, e.g.
    /// `"myVar"` or `"3.14"`.
    pub(crate) literal: String,

    /// The source position at which this token begins.
    pub(crate) position: TokenPosition,
}

impl Token {
    /// Constructs a `Token` from its kind, literal text, and source position.
    ///
    /// Accepts any `literal` that implements `Into<String>` — pass a `&str`
    /// for zero-copy conversion from a string slice already held by the lexer,
    /// or a `String` if the literal was heap-allocated.
    pub(crate) fn new(
        kind: TokenType,
        literal: impl Into<String>,
        position: TokenPosition,
    ) -> Self {
        Self {
            kind,
            literal: literal.into(),
            position,
        }
    }
}

/// Maps an identifier string to its [`TokenType`], distinguishing keywords from user names.
///
/// Called by the lexer immediately after it finishes scanning an alphabetic
/// sequence. If `ident` matches a reserved keyword the corresponding
/// [`TokenType`] is returned; otherwise [`TokenType::Identifier`] is returned,
/// indicating a user-defined name.
///
/// The lookup is a single `match` expression — O(1) in practice because the
/// Rust compiler typically emits a jump table or a trie for string matches.
///
/// # Examples
///
/// ```rust
/// # use mutant_lang::token::{lookup_identifier, TokenType};
/// assert_eq!(lookup_identifier("fn"),     TokenType::Function);
/// assert_eq!(lookup_identifier("while"),  TokenType::While);
/// assert_eq!(lookup_identifier("null"),   TokenType::Null);
/// assert_eq!(lookup_identifier("myVar"),  TokenType::Identifier);
/// assert_eq!(lookup_identifier(""),       TokenType::Identifier);
/// ```
#[must_use]
pub fn lookup_identifier(ident: &str) -> TokenType {
    match ident {
        "fn" => TokenType::Function,
        "let" => TokenType::Let,
        "true" => TokenType::True,
        "false" => TokenType::False,
        "if" => TokenType::If,
        "elif" => TokenType::Elif,
        "else" => TokenType::Else,
        "return" => TokenType::Return,
        "while" => TokenType::While,
        "break" => TokenType::Break,
        "continue" => TokenType::Continue,
        "for" => TokenType::For,
        "const" => TokenType::Const,
        "class" => TokenType::Class,
        "extends" => TokenType::Extends,
        "super" => TokenType::Super,
        "this" => TokenType::This,
        "new" => TokenType::New,
        "null" => TokenType::Null,
        _ => TokenType::Identifier,
    }
}

/// A typed wrapper around [`TokenType`] that classifies a token as an operator.
///
/// Each variant carries the original [`TokenType`] it was constructed from,
/// so the underlying token kind is always recoverable via [`Operator::token_type`]
/// or [`Display`](std::fmt::Display) without an extra allocation.
///
/// The enum is crate-private (`pub(crate)`) because it is an internal
/// parsing/evaluation detail — callers outside the crate see operator
/// semantics only through AST nodes.
///
/// Construct an `Operator` from a token kind via the [`TryFrom<TokenType>`]
/// implementation. Any [`TokenType`] that does not correspond to an operator
/// returns `Err(token_type)`.
///
/// # Examples
///
/// ```rust
/// # use mutant_lang::token::{TokenType, Operator};
/// let op = Operator::try_from(TokenType::Plus).unwrap();
/// assert_eq!(op.to_string(), "+");
///
/// let err = Operator::try_from(TokenType::Identifier);
/// assert!(err.is_err());
/// ```
#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum Operator {
    Plus,
    Minus,
    Asterisk,
    Slash,
    IntDivision,
    Modulus,
    Bang,
    Eq,
    NotEq,
    LessThan,
    GreaterThan,
    LessThanOrEqual,
    GreaterThanOrEqual,
    And,
    Or,
    Assign,
    PlusAssign,
    MinusAssign,
    AsteriskAssign,
    SlashAssign,
    ModulusAssign,
    BitwiseAnd,
    BitwiseOr,
    BitwiseXor,
    BitwiseNot,
    BitwiseLeftShift,
    BitwiseRightShift,
}

/// Converts a [`TokenType`] into an [`Operator`], failing for non-operator kinds.
///
/// This is the canonical way to construct an `Operator`. The `TokenType` value
/// is moved *into* the matching variant so the original kind is always
/// recoverable without an extra field or allocation.
///
/// # Errors
///
/// Returns `Err(t)` — the original [`TokenType`] — if `t` does not correspond
/// to any operator (e.g. [`TokenType::Identifier`], [`TokenType::Eof`],
/// delimiters, or keywords). The caller can inspect or re-use the returned
/// value to produce a meaningful error message.
///
/// # Examples
///
/// ```rust
/// # use mutant_lang::token::{TokenType, Operator};
/// assert!(Operator::try_from(TokenType::Asterisk).is_ok());
/// assert_eq!(Operator::try_from(TokenType::Comma), Err(TokenType::Comma));
/// ```
impl TryFrom<TokenType> for Operator {
    type Error = TokenType;

    fn try_from(t: TokenType) -> Result<Self, Self::Error> {
        match t {
            TokenType::Plus => Ok(Self::Plus),
            TokenType::Minus => Ok(Self::Minus),
            TokenType::Asterisk => Ok(Self::Asterisk),
            TokenType::Slash => Ok(Self::Slash),
            TokenType::IntDivision => Ok(Self::IntDivision),
            TokenType::Modulus => Ok(Self::Modulus),
            TokenType::Bang => Ok(Self::Bang),
            TokenType::Eq => Ok(Self::Eq),
            TokenType::NotEq => Ok(Self::NotEq),
            TokenType::LessThan => Ok(Self::LessThan),
            TokenType::GreaterThan => Ok(Self::GreaterThan),
            TokenType::LessThanOrEqual => Ok(Self::LessThanOrEqual),
            TokenType::GreaterThanOrEqual => Ok(Self::GreaterThanOrEqual),
            TokenType::And => Ok(Self::And),
            TokenType::Or => Ok(Self::Or),
            TokenType::Assign => Ok(Self::Assign),
            TokenType::PlusAssign => Ok(Self::PlusAssign),
            TokenType::MinusAssign => Ok(Self::MinusAssign),
            TokenType::AsteriskAssign => Ok(Self::AsteriskAssign),
            TokenType::SlashAssign => Ok(Self::SlashAssign),
            TokenType::ModulusAssign => Ok(Self::ModulusAssign),
            TokenType::BitwiseAnd => Ok(Self::BitwiseAnd),
            TokenType::BitwiseOr => Ok(Self::BitwiseOr),
            TokenType::BitwiseXor => Ok(Self::BitwiseXor),
            TokenType::BitwiseNot => Ok(Self::BitwiseNot),
            TokenType::BitwiseLeftShift => Ok(Self::BitwiseLeftShift),
            TokenType::BitwiseRightShift => Ok(Self::BitwiseRightShift),
            other => Err(other),
        }
    }
}

impl Operator {
    /// Returns the [`TokenType`] that this operator was constructed from.
    ///
    /// Because every variant stores exactly one `TokenType` field, this
    /// operation is `const` and branch-free at runtime — the compiler
    /// generates a single move. Use this when you need to delegate to
    /// [`TokenType`]'s own [`Display`](std::fmt::Display) or comparisons
    /// without pattern-matching on every variant.
    #[must_use]
    #[allow(clippy::trivially_copy_pass_by_ref)]
    pub const fn token_type(&self) -> TokenType {
        match self {
            Self::Plus => TokenType::Plus,
            Self::Minus => TokenType::Minus,
            Self::Asterisk => TokenType::Asterisk,
            Self::Slash => TokenType::Slash,
            Self::IntDivision => TokenType::IntDivision,
            Self::Modulus => TokenType::Modulus,
            Self::Bang => TokenType::Bang,
            Self::Eq => TokenType::Eq,
            Self::NotEq => TokenType::NotEq,
            Self::LessThan => TokenType::LessThan,
            Self::GreaterThan => TokenType::GreaterThan,
            Self::LessThanOrEqual => TokenType::LessThanOrEqual,
            Self::GreaterThanOrEqual => TokenType::GreaterThanOrEqual,
            Self::And => TokenType::And,
            Self::Or => TokenType::Or,
            Self::Assign => TokenType::Assign,
            Self::PlusAssign => TokenType::PlusAssign,
            Self::MinusAssign => TokenType::MinusAssign,
            Self::AsteriskAssign => TokenType::AsteriskAssign,
            Self::SlashAssign => TokenType::SlashAssign,
            Self::ModulusAssign => TokenType::ModulusAssign,
            Self::BitwiseAnd => TokenType::BitwiseAnd,
            Self::BitwiseOr => TokenType::BitwiseOr,
            Self::BitwiseXor => TokenType::BitwiseXor,
            Self::BitwiseNot => TokenType::BitwiseNot,
            Self::BitwiseLeftShift => TokenType::BitwiseLeftShift,
            Self::BitwiseRightShift => TokenType::BitwiseRightShift,
        }
    }
}

impl std::fmt::Display for Operator {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "{}", self.token_type())
    }
}
