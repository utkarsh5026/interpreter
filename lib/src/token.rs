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
//! Two free functions, [`lookup_identifier`] and [`is_keyword`], provide fast
//! keyword classification used by the lexer immediately after it reads an
//! identifier.
//!
//! # Design note
//!
//! [`TokenType`] and [`TokenPosition`] both derive [`Copy`], keeping tokens
//! cheap to pass around without thinking about ownership. [`Token`] itself owns
//! its `literal` string and therefore only derives [`Clone`]; callers that need
//! to inspect many tokens without consuming them should borrow `&Token`.

#![allow(dead_code)]

/// Every distinct syntactic category the Mutant Lang lexer can produce.
///
/// A `TokenType` describes the *role* of a lexeme — it does **not** carry the
/// raw source text. The actual characters are stored alongside the variant in
/// [`Token::literal`].
///
/// Variants are grouped by function:
///
/// | Group | Variants |
/// |---|---|
/// | Value / literal kinds | [`Illegal`](Self::Illegal), [`Eof`](Self::Eof), [`Identifier`](Self::Identifier), [`Int`](Self::Int), [`Float`](Self::Float), [`String`](Self::String), [`FString`](Self::FString) |
/// | Arithmetic operators | [`Plus`](Self::Plus) … [`Modulus`](Self::Modulus) |
/// | Comparison operators | [`LessThan`](Self::LessThan) … [`NotEq`](Self::NotEq) |
/// | Logical operators | [`And`](Self::And), [`Or`](Self::Or) |
/// | Compound assignment | [`PlusAssign`](Self::PlusAssign) … [`ModulusAssign`](Self::ModulusAssign) |
/// | Bitwise operators | [`BitwiseAnd`](Self::BitwiseAnd) … [`BitwiseRightShift`](Self::BitwiseRightShift) |
/// | Delimiters | [`Comma`](Self::Comma) … [`Dot`](Self::Dot) |
/// | Bracket pairs | [`LParen`](Self::LParen) … [`RBracket`](Self::RBracket) |
/// | Reserved keywords | [`Function`](Self::Function) … [`Null`](Self::Null) |
///
/// # Examples
///
/// ```rust
/// # use mutant_lang::token::TokenType;
/// let t = TokenType::Plus;
/// assert_eq!(t.to_string(), "+");
/// ```
#[derive(Debug, PartialEq, Eq, Clone, Copy)]
pub enum TokenType {
    /// A character or token the lexer does not recognise.
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

/// Formats a [`TokenType`] as its canonical source-text representation.
///
/// The output mirrors exactly what the lexer would have read from source —
/// keywords appear as their lowercase spelling (`fn`, `let`, …) and operators
/// as their punctuation (`+`, `<=`, `&&`, …). This makes `Display` useful for
/// error messages that quote back the offending token.
///
/// # Examples
///
/// ```rust
/// # use mutant_lang::token::TokenType;
/// assert_eq!(TokenType::Plus.to_string(),           "+");
/// assert_eq!(TokenType::LessThanOrEqual.to_string(),"<=");
/// assert_eq!(TokenType::Function.to_string(),       "fn");
/// assert_eq!(TokenType::Eof.to_string(),            "EOF");
/// ```
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

/// Formats the position as a human-readable location string.
///
/// Output form: `line <L>, column <C>`. This is used directly in error
/// messages produced by the lexer and parser.
///
/// # Examples
///
/// ```rust
/// # use mutant_lang::token::TokenPosition;
/// let pos = TokenPosition::new(3, 12);
/// assert_eq!(pos.to_string(), "line 3, column 12");
/// ```
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
    ///
    /// # Examples
    ///
    /// ```rust
    /// # use mutant_lang::token::{Token, TokenType, TokenPosition};
    /// let pos = TokenPosition::new(2, 4);
    /// let tok = Token::new(TokenType::Identifier, "counter", pos);
    /// assert_eq!(tok.kind,    TokenType::Identifier);
    /// assert_eq!(tok.literal, "counter");
    /// ```
    pub fn new(kind: TokenType, literal: impl Into<String>, position: TokenPosition) -> Self {
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

/// Returns `true` if `ident` is a reserved keyword in Mutant Lang.
///
/// This is a thin wrapper around [`lookup_identifier`] that answers the
/// yes/no question directly, without requiring the caller to pattern-match on
/// a [`TokenType`]. Useful in validation passes or code-generation stages that
/// need to avoid emitting reserved words as user-defined names.
///
/// # Examples
///
/// ```rust
/// # use mutant_lang::token::is_keyword;
/// assert!(is_keyword("fn"));
/// assert!(is_keyword("null"));
/// assert!(!is_keyword("myFunction"));
/// assert!(!is_keyword(""));
/// ```
#[must_use]
pub fn is_keyword(ident: &str) -> bool {
    lookup_identifier(ident) != TokenType::Identifier
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
pub(crate) enum Operator {
    /// Addition operator (`+`).
    Plus(TokenType),
    /// Subtraction or unary negation (`-`).
    Minus(TokenType),
    /// Multiplication (`*`).
    Asterisk(TokenType),
    /// Floating-point division (`/`).
    Slash(TokenType),
    /// Integer (floor) division (`//`).
    IntDivision(TokenType),
    /// Modulus / remainder (`%`).
    Modulus(TokenType),
    /// Logical or bitwise NOT prefix (`!`).
    Bang(TokenType),

    /// Equality comparison (`==`).
    Eq(TokenType),
    /// Inequality comparison (`!=`).
    NotEq(TokenType),
    /// Less-than comparison (`<`).
    LessThan(TokenType),
    /// Greater-than comparison (`>`).
    GreaterThan(TokenType),
    /// Less-than-or-equal comparison (`<=`).
    LessThanOrEqual(TokenType),
    /// Greater-than-or-equal comparison (`>=`).
    GreaterThanOrEqual(TokenType),

    /// Short-circuit logical AND (`&&`).
    And(TokenType),
    /// Short-circuit logical OR (`||`).
    Or(TokenType),

    /// Simple assignment (`=`).
    Assign(TokenType),
    /// Addition-assignment (`+=`).
    PlusAssign(TokenType),
    /// Subtraction-assignment (`-=`).
    MinusAssign(TokenType),
    /// Multiplication-assignment (`*=`).
    AsteriskAssign(TokenType),
    /// Division-assignment (`/=`).
    SlashAssign(TokenType),
    /// Modulus-assignment (`%=`).
    ModulusAssign(TokenType),

    /// Bitwise AND (`&`).
    BitwiseAnd(TokenType),
    /// Bitwise OR (`|`).
    BitwiseOr(TokenType),
    /// Bitwise XOR (`^`).
    BitwiseXor(TokenType),
    /// Bitwise NOT / one's complement (`~`).
    BitwiseNot(TokenType),
    /// Left bit-shift (`<<`).
    BitwiseLeftShift(TokenType),
    /// Right bit-shift (`>>`).
    BitwiseRightShift(TokenType),
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
    type Error = TokenType; // returns the offending type on failure

    fn try_from(t: TokenType) -> Result<Self, Self::Error> {
        match t {
            TokenType::Plus => Ok(Self::Plus(t)),
            TokenType::Minus => Ok(Self::Minus(t)),
            TokenType::Asterisk => Ok(Self::Asterisk(t)),
            TokenType::Slash => Ok(Self::Slash(t)),
            TokenType::IntDivision => Ok(Self::IntDivision(t)),
            TokenType::Modulus => Ok(Self::Modulus(t)),
            TokenType::Bang => Ok(Self::Bang(t)),
            TokenType::Eq => Ok(Self::Eq(t)),
            TokenType::NotEq => Ok(Self::NotEq(t)),
            TokenType::LessThan => Ok(Self::LessThan(t)),
            TokenType::GreaterThan => Ok(Self::GreaterThan(t)),
            TokenType::LessThanOrEqual => Ok(Self::LessThanOrEqual(t)),
            TokenType::GreaterThanOrEqual => Ok(Self::GreaterThanOrEqual(t)),
            TokenType::And => Ok(Self::And(t)),
            TokenType::Or => Ok(Self::Or(t)),
            TokenType::Assign => Ok(Self::Assign(t)),
            TokenType::PlusAssign => Ok(Self::PlusAssign(t)),
            TokenType::MinusAssign => Ok(Self::MinusAssign(t)),
            TokenType::AsteriskAssign => Ok(Self::AsteriskAssign(t)),
            TokenType::SlashAssign => Ok(Self::SlashAssign(t)),
            TokenType::ModulusAssign => Ok(Self::ModulusAssign(t)),
            TokenType::BitwiseAnd => Ok(Self::BitwiseAnd(t)),
            TokenType::BitwiseOr => Ok(Self::BitwiseOr(t)),
            TokenType::BitwiseXor => Ok(Self::BitwiseXor(t)),
            TokenType::BitwiseNot => Ok(Self::BitwiseNot(t)),
            TokenType::BitwiseLeftShift => Ok(Self::BitwiseLeftShift(t)),
            TokenType::BitwiseRightShift => Ok(Self::BitwiseRightShift(t)),
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
    const fn token_type(&self) -> TokenType {
        match self {
            Self::Plus(t)
            | Self::Minus(t)
            | Self::Asterisk(t)
            | Self::Slash(t)
            | Self::IntDivision(t)
            | Self::Modulus(t)
            | Self::Bang(t)
            | Self::Eq(t)
            | Self::NotEq(t)
            | Self::LessThan(t)
            | Self::GreaterThan(t)
            | Self::LessThanOrEqual(t)
            | Self::GreaterThanOrEqual(t)
            | Self::And(t)
            | Self::Or(t)
            | Self::Assign(t)
            | Self::PlusAssign(t)
            | Self::MinusAssign(t)
            | Self::AsteriskAssign(t)
            | Self::SlashAssign(t)
            | Self::ModulusAssign(t)
            | Self::BitwiseAnd(t)
            | Self::BitwiseOr(t)
            | Self::BitwiseXor(t)
            | Self::BitwiseNot(t)
            | Self::BitwiseLeftShift(t)
            | Self::BitwiseRightShift(t) => *t,
        }
    }
}

/// Formats the operator as its canonical source-text symbol.
///
/// Delegates to [`TokenType`]'s [`Display`](std::fmt::Display) impl via
/// [`Operator::token_type`], so the output is always consistent with what
/// the lexer read from source (e.g. `"+"`, `"<="`, `"&&"`).
///
/// # Examples
///
/// ```rust
/// # use mutant_lang::token::{TokenType, Operator};
/// let op = Operator::try_from(TokenType::BitwiseLeftShift).unwrap();
/// assert_eq!(op.to_string(), "<<");
/// ```
impl std::fmt::Display for Operator {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "{}", self.token_type())
    }
}
