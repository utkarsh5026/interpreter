//! Operator precedence levels for Pratt (top-down operator precedence) parsing.
//!
//! Pratt parsing drives expression parsing by assigning each operator a numeric
//! *binding power* — the higher the power, the tighter the operator binds its
//! operands. This module encodes those powers as an ordered [`Precedence`] enum
//! whose discriminants serve as the binding-power table.
//!
//! The parser queries [`Precedence::of`] for each infix token it encounters and
//! compares the result against the *current* precedence to decide whether to
//! consume the token as part of the expression being built or to stop and let
//! the caller handle it.
//!
//! # Precedence order (lowest → highest)
//!
//! | Level          | Operators                    |
//! |----------------|------------------------------|
//! | `Lowest`       | (sentinel / fallback)        |
//! | `LogicalOr`    | `\|\|`                       |
//! | `LogicalAnd`   | `&&`                         |
//! | `Equals`       | `==` `!=` `=`                |
//! | `LessGreater`  | `<` `>` `<=` `>=`            |
//! | `Sum`          | `+` `-`                      |
//! | `Product`      | `*` `/` `%` `//`             |
//! | `Prefix`       | `-x` `!x`                    |
//! | `Call`         | `f(x)`                       |
//! | `Index`        | `a[i]` `a.b`                 |

use crate::token::TokenType;

/// Operator precedence levels for Pratt parsing.
///
/// Variants are listed from lowest to highest binding power. The derived
/// [`Ord`] impl uses declaration order as the discriminant, so comparisons
/// like `current_precedence < next_precedence` work directly without any
/// manual numeric mapping.
///
/// The parser uses these values in two ways:
/// - **Loop guard** — continue consuming infix operators only while the
///   incoming operator's precedence is strictly greater than the current level.
/// - **Right-hand side parsing** — pass a precedence to the recursive
///   `parse_expression` call to control how far right parsing continues.
///
/// # Examples
///
/// ```rust
/// # use mutant_lang::parser::precedence::Precedence;
/// // Product binds tighter than Sum, so `2 + 3 * 4` parses as `2 + (3 * 4)`.
/// assert!(Precedence::Product > Precedence::Sum);
///
/// // Index/call bind tightest among binary operators.
/// assert!(Precedence::Index > Precedence::Call);
/// assert!(Precedence::Call > Precedence::Prefix);
/// ```
#[derive(Debug, PartialEq, Eq, PartialOrd, Ord, Clone, Copy)]
pub enum Precedence {
    /// Sentinel value used as the initial precedence when parsing a fresh
    /// expression. Any real operator will be strictly greater than this,
    /// so the loop always enters on the first infix token.
    Lowest,

    /// Logical OR (`||`). Binds less tightly than `&&` so that
    /// `a && b || c` parses as `(a && b) || c`.
    LogicalOr,

    /// Logical AND (`&&`). Binds more tightly than `||` but less tightly
    /// than equality comparisons.
    LogicalAnd,

    /// Equality and assignment operators: `==`, `!=`, `=`.
    ///
    /// Note that `=` (assignment) is grouped here alongside `==`/`!=`.
    /// Whether assignment is actually parsed as an infix expression depends
    /// on the parser's statement-vs-expression context.
    Equals,

    /// Relational comparisons: `<`, `>`, `<=`, `>=`.
    LessGreater,

    /// Additive operators: `+` and `-`.
    Sum,

    /// Multiplicative operators: `*`, `/`, `%`, and integer division `//`.
    Product,

    /// Unary prefix operators: negation (`-x`) and logical NOT (`!x`).
    ///
    /// This variant has no associated [`TokenType`] in [`Precedence::of`]
    /// because prefix operators are not dispatched through the infix table —
    /// the parser handles them explicitly before entering the Pratt loop.
    /// The level exists so that the parser can pass `Prefix` as the
    /// right-hand-side precedence when recursing into a prefix expression,
    /// ensuring the operand is parsed at the correct binding power.
    Prefix,

    /// Function call operator: `f(x, y)`.
    ///
    /// [`TokenType::LParen`] maps to this level so that a `(` following any
    /// expression is treated as a call rather than a grouped sub-expression.
    Call,

    /// Subscript and member-access operators: `a[i]` and `a.b`.
    ///
    /// Both [`TokenType::LBracket`] and [`TokenType::Dot`] map here, giving
    /// index access and field/method access the same (highest) precedence.
    Index,
}

impl Precedence {
    /// Return the precedence for a given infix token type.
    ///
    /// Tokens that are not infix operators — or that the parser handles via a
    /// different mechanism (e.g. prefix operators) — return [`Precedence::Lowest`],
    /// which causes the Pratt loop to stop consuming tokens.
    ///
    /// This function is `const` so it can be evaluated at compile time inside
    /// const contexts (e.g. `static` tables or `const fn` callers).
    ///
    /// # Examples
    ///
    /// ```rust
    /// # use mutant_lang::parser::precedence::Precedence;
    /// # use mutant_lang::token::TokenType;
    /// assert_eq!(Precedence::of(TokenType::Plus),     Precedence::Sum);
    /// assert_eq!(Precedence::of(TokenType::Asterisk), Precedence::Product);
    /// assert_eq!(Precedence::of(TokenType::LParen),   Precedence::Call);
    /// assert_eq!(Precedence::of(TokenType::Dot),      Precedence::Index);
    ///
    /// // Unknown / non-infix tokens fall back to the sentinel.
    /// assert_eq!(Precedence::of(TokenType::Semicolon), Precedence::Lowest);
    /// ```
    #[must_use]
    pub const fn of(token_type: TokenType) -> Self {
        match token_type {
            TokenType::Eq | TokenType::Assign | TokenType::NotEq => Self::Equals,

            TokenType::LessThan
            | TokenType::GreaterThan
            | TokenType::LessThanOrEqual
            | TokenType::GreaterThanOrEqual => Self::LessGreater,

            TokenType::And => Self::LogicalAnd,
            TokenType::Or => Self::LogicalOr,

            TokenType::Plus | TokenType::Minus => Self::Sum,

            TokenType::Asterisk
            | TokenType::Slash
            | TokenType::Modulus
            | TokenType::IntDivision => Self::Product,

            TokenType::LParen => Self::Call,
            TokenType::LBracket | TokenType::Dot => Self::Index,
            _ => Self::Lowest,
        }
    }
}
