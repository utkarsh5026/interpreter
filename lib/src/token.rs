#![allow(dead_code)]
// TokenType enum, Token struct, TokenPosition, keywords

#[derive(Debug, PartialEq, Eq, Clone, Copy)]
pub enum TokenType {
    // Literals / values
    Illegal,
    Eof,
    Identifier,
    Int,
    Float,
    String,
    FString,

    // Operators
    Assign,
    Plus,
    Minus,
    Bang,
    Asterisk,
    Slash,
    IntDivision,
    Modulus,

    // Comparison
    LessThan,
    GreaterThan,
    LessThanOrEqual,
    GreaterThanOrEqual,
    Eq,
    NotEq,

    // Logical
    And,
    Or,

    // Compound assignment
    PlusAssign,
    MinusAssign,
    AsteriskAssign,
    SlashAssign,
    ModulusAssign,

    // Bitwise
    BitwiseAnd,
    BitwiseOr,
    BitwiseXor,
    BitwiseNot,
    BitwiseLeftShift,
    BitwiseRightShift,

    // Delimiters
    Comma,
    Semicolon,
    Colon,
    Dot,

    // Brackets
    LParen,
    RParen,
    LBrace,
    RBrace,
    LBracket,
    RBracket,

    // Keywords
    Function,
    Let,
    True,
    False,
    If,
    Else,
    Elif,
    Return,
    While,
    Break,
    Continue,
    For,
    Const,
    Class,
    Extends,
    Super,
    This,
    New,
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

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub struct TokenPosition {
    line: usize,
    column: usize,
}

impl TokenPosition {
    /// Creates a new `TokenPosition`.
    ///
    /// # Panics
    ///
    /// Panics if `line` is 0 (line numbers are 1-based).
    #[must_use]
    pub fn new(line: usize, column: usize) -> Self {
        assert!(line >= 1, "line number must be >= 1, got {line}");
        Self { line, column }
    }
}

impl std::fmt::Display for TokenPosition {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "line {}, column{}", self.line, self.column)
    }
}

#[derive(Debug, Clone)]
pub struct Token {
    pub(crate) kind: TokenType,
    pub(crate) literal: String,
    pub(crate) position: TokenPosition,
}

impl Token {
    pub fn new(kind: TokenType, literal: impl Into<String>, position: TokenPosition) -> Self {
        Self {
            kind,
            literal: literal.into(),
            position,
        }
    }
}

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

#[must_use]
pub fn is_keyword(ident: &str) -> bool {
    lookup_identifier(ident) != TokenType::Identifier
}
