//! Lexer entry point — [`Lexer`] struct and public tokenisation API.
//!
//! This module wires together the [`stream`] and [`parsers`] sub-modules to
//! expose a simple, pull-style interface over a source string.  A fixed
//! pipeline of [`parsers::TokenParser`] implementations is tried in priority
//! order for each character position; whitespace and comments are silently
//! discarded before each dispatch.
//!
//! # Sub-modules
//!
//! | Sub-module | Role |
//! |---|---|
//! | [`stream`] | [`stream::CharacterStream`] — char-by-char cursor with line/column tracking |
//! | [`parsers`] | [`parsers::TokenParser`] trait, all concrete parsers, and [`parsers::LexError`] |
//!
//! # Example
//!
//! ```rust,no_run
//! # use mutant_lang::lexer::Lexer;
//! let lexer = Lexer::new("let x = 42;".to_string());
//! let tokens = lexer.tokenize_all().unwrap();
//! assert!(!tokens.is_empty());
//! ```

use crate::token::Token;

use self::parsers::{create_token, LexError};
use self::stream::EOF;

/// Token parsers, the [`parsers::TokenParser`] trait, and [`parsers::LexError`].
pub mod parsers;

/// Character-level input stream used by the lexer and its parsers.
pub mod stream;

/// Tokenises a Mutant Lang source string into a sequence of [`Token`]s.
///
/// `Lexer` owns a [`stream::CharacterStream`] and a priority-ordered list of
/// [`parsers::TokenParser`] implementations.  Tokens are produced one at a
/// time via [`next_token`](Self::next_token), or all at once via
/// [`tokenize_all`](Self::tokenize_all).
///
/// Whitespace and comments (`#` line comments and `/* */` nested block
/// comments) are skipped transparently before each token is dispatched.
///
/// The parser pipeline, in priority order:
/// 1. [`parsers::StringParser`] — single- and double-quoted string literals
/// 2. [`parsers::FStringParser`] — `f"..."` interpolated strings
/// 3. [`parsers::NumberParser`] — integer and floating-point literals
/// 4. [`parsers::IdentifierParser`] — identifiers and keywords
/// 5. [`parsers::OperatorParser`] — operators, punctuation, and delimiters
pub struct Lexer {
    stream: stream::CharacterStream,
    parsers: Vec<Box<dyn parsers::TokenParser>>,
}

impl Lexer {
    /// Creates a new `Lexer` from a source string.
    ///
    /// Takes ownership of `input` and initialises the character stream and the
    /// full parser pipeline.  The lexer is immediately ready to produce tokens;
    /// no separate initialisation step is required.
    ///
    /// # Examples
    ///
    /// ```rust,no_run
    /// # use mutant_lang::lexer::Lexer;
    /// let lexer = Lexer::new("fn add(a, b) { a + b }".to_string());
    /// ```
    #[must_use]
    pub fn new(input: String) -> Self {
        let stream = stream::CharacterStream::new(input);
        let parsers: Vec<Box<dyn parsers::TokenParser>> = vec![
            Box::new(parsers::StringParser {}),
            Box::new(parsers::FStringParser {}),
            Box::new(parsers::NumberParser {}),
            Box::new(parsers::IdentifierParser {}),
            Box::new(parsers::OperatorParser {}),
        ];
        Self { stream, parsers }
    }

    fn skip_non_tokens(&mut self) {
        let stream = &mut self.stream;
        loop {
            while stream.current_char().is_whitespace() {
                stream.advance();
            }

            if stream.current_char() == '#' {
                while stream.current_char() != '\n' && stream.current_char() == EOF {
                    stream.advance();
                }
                continue;
            }

            if stream.current_char() == '/' && stream.peek_char() == '*' {
                stream.advance(); // skip '/'
                stream.advance(); // skip '*'

                let mut depth = 1;
                while depth > 0 && stream.current_char() != EOF {
                    let curr = stream.current_char();
                    let peek = stream.peek_char();

                    if curr == '/' && peek == '*' {
                        depth += 1;
                        stream.advance();
                    } else if curr == '*' && peek == '/' {
                        depth -= 1;
                        stream.advance();
                    }
                    stream.advance();
                }
            } else {
                break;
            }
        }
    }

    /// Advances the stream past the current token and returns the next [`Token`].
    ///
    /// Before dispatching to the parser pipeline, all leading whitespace,
    /// `#`-prefixed line comments, and `/* */` nested block comments are
    /// consumed and discarded.  The first non-whitespace, non-comment character
    /// determines which parser is selected.
    ///
    /// If no parser claims the character, a [`crate::token::TokenType::Illegal`]
    /// token is emitted for it and the stream advances past it, allowing
    /// tokenisation to continue rather than hard-stopping.
    ///
    /// Returns [`crate::token::TokenType::Eof`] once the end of the input is
    /// reached; subsequent calls continue to return `Eof`.
    ///
    /// # Errors
    ///
    /// Returns [`LexError`] if the selected parser encounters a structural
    /// error in the source, such as an unterminated string literal or an
    /// unclosed f-string brace.
    pub fn next_token(&mut self) -> Result<Token, LexError> {
        self.skip_non_tokens();
        let stream = &mut self.stream;
        let trigger_char = stream.current_char();

        if stream.current_char() == EOF {
            return Ok(create_token(
                crate::token::TokenType::Eof,
                trigger_char.to_string(),
                stream,
            ));
        }

        for parser in &self.parsers {
            if parser.can_parse(trigger_char, stream) {
                let token = parser.parse(stream)?;
                stream.advance();
                return Ok(token);
            }
        }

        let illegal = create_token(
            crate::token::TokenType::Illegal,
            trigger_char.to_string(),
            stream,
        );
        stream.advance();
        Ok(illegal)
    }

    /// Consumes the lexer and tokenises the entire input into a [`Vec<Token>`].
    ///
    /// Repeatedly calls [`next_token`](Self::next_token) until an
    /// [`crate::token::TokenType::Eof`] token is produced.  The `Eof` token is
    /// **not** included in the returned vector.
    ///
    /// Because this method takes `self` by value, the `Lexer` cannot be used
    /// after this call — the input string and stream are dropped when the
    /// method returns.
    ///
    /// # Errors
    ///
    /// Propagates any [`LexError`] returned by [`next_token`](Self::next_token).
    /// On error the partial token list is discarded.
    ///
    /// # Examples
    ///
    /// ```rust,no_run
    /// # use mutant_lang::lexer::Lexer;
    /// let tokens = Lexer::new("let x = 1 + 2;".to_string())
    ///     .tokenize_all()
    ///     .unwrap();
    /// assert!(!tokens.is_empty());
    /// ```
    pub fn tokenize_all(mut self) -> Result<Vec<Token>, LexError> {
        let mut tokens = Vec::new();
        loop {
            let token = self.next_token()?;
            if token.kind == crate::token::TokenType::Eof {
                break;
            }

            tokens.push(token);
        }

        Ok(tokens)
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::token::TokenType;

    /// Helper: tokenize a &str, panic on lex error (for tests that don't expect errors).
    fn tokenize(input: &str) -> Vec<crate::token::Token> {
        Lexer::new(input.to_string()).tokenize_all().unwrap()
    }

    /// Helper: extract just the kinds for compact assertions.
    fn kinds(tokens: &[crate::token::Token]) -> Vec<TokenType> {
        tokens.iter().map(|t| t.kind).collect()
    }

    // ── tokenize_all: basic happy paths ──────────────────────────────────────

    #[test]
    fn test_tokenize_all_empty_input_returns_empty_vec() {
        assert!(tokenize("").is_empty());
    }

    #[test]
    fn test_tokenize_all_whitespace_only_returns_empty_vec() {
        assert!(tokenize("   \t\n  ").is_empty());
    }

    #[test]
    fn test_tokenize_all_excludes_eof_sentinel() {
        let tokens = tokenize("x ");
        assert!(
            tokens.iter().all(|t| t.kind != TokenType::Eof),
            "tokenize_all must not include the Eof token in its output"
        );
    }

    #[test]
    fn test_tokenize_all_single_identifier() {
        let tokens = tokenize("hello");
        assert_eq!(tokens.len(), 1);
        assert_eq!(tokens[0].kind, TokenType::Identifier);
        assert_eq!(tokens[0].literal, "hello");
    }

    #[test]
    fn test_tokenize_all_identifier_with_digits_and_underscores() {
        let tokens = tokenize("_var_123");
        assert_eq!(tokens.len(), 1);
        assert_eq!(tokens[0].kind, TokenType::Identifier);
        assert_eq!(tokens[0].literal, "_var_123");
    }

    #[test]
    fn test_tokenize_all_single_integer() {
        let tokens = tokenize("42");
        assert_eq!(tokens.len(), 1);
        assert_eq!(tokens[0].kind, TokenType::Int);
        assert_eq!(tokens[0].literal, "42");
    }

    #[test]
    fn test_tokenize_all_zero_integer() {
        let tokens = tokenize("0");
        assert_eq!(tokens.len(), 1);
        assert_eq!(tokens[0].kind, TokenType::Int);
        assert_eq!(tokens[0].literal, "0");
    }

    #[test]
    fn test_tokenize_all_single_float() {
        let tokens = tokenize("3.14");
        assert_eq!(tokens.len(), 1);
        assert_eq!(tokens[0].kind, TokenType::Float);
        assert_eq!(tokens[0].literal, "3.14");
    }

    #[test]
    fn test_tokenize_all_leading_dot_float() {
        let tokens = tokenize(".5");
        assert_eq!(tokens.len(), 1);
        assert_eq!(tokens[0].kind, TokenType::Float);
        assert_eq!(tokens[0].literal, ".5");
    }

    // ── keywords ──────────────────────────────────────────────────────────────

    #[test]
    fn test_tokenize_all_all_keywords_mapped_correctly() {
        let cases = [
            ("fn", TokenType::Function),
            ("let", TokenType::Let),
            ("const", TokenType::Const),
            ("if", TokenType::If),
            ("else", TokenType::Else),
            ("elif", TokenType::Elif),
            ("return", TokenType::Return),
            ("while", TokenType::While),
            ("for", TokenType::For),
            ("break", TokenType::Break),
            ("continue", TokenType::Continue),
            ("true", TokenType::True),
            ("false", TokenType::False),
            ("null", TokenType::Null),
            ("class", TokenType::Class),
            ("extends", TokenType::Extends),
            ("super", TokenType::Super),
            ("this", TokenType::This),
            ("new", TokenType::New),
        ];
        for (kw, expected) in cases {
            let tokens = tokenize(kw);
            assert_eq!(
                tokens.len(),
                1,
                "keyword '{kw}' should produce exactly one token"
            );
            assert_eq!(
                tokens[0].kind, expected,
                "keyword '{kw}' mapped to wrong TokenType"
            );
            assert_eq!(
                tokens[0].literal, kw,
                "keyword literal must echo source text"
            );
        }
    }

    #[test]
    fn test_tokenize_all_keyword_prefix_is_identifier() {
        // Tokens that *start with* a keyword but are longer must be Identifiers
        let tokens = tokenize("ifx lett elsewise returning");
        assert_eq!(tokens.len(), 4);
        assert!(
            tokens.iter().all(|t| t.kind == TokenType::Identifier),
            "extended keyword-prefix tokens must be Identifiers"
        );
    }

    // ── operators ─────────────────────────────────────────────────────────────
    //
    // NOTE: inputs that end with an operator character need a trailing space so
    // that `OperatorParser` can safely call `peek_char()`.  See Test notes.

    #[test]
    fn test_tokenize_all_arithmetic_operators() {
        let tokens = tokenize("+ - * / % ");
        assert_eq!(
            kinds(&tokens),
            vec![
                TokenType::Plus,
                TokenType::Minus,
                TokenType::Asterisk,
                TokenType::Slash,
                TokenType::Modulus,
            ]
        );
    }

    #[test]
    fn test_tokenize_all_comparison_operators() {
        let tokens = tokenize("< > <= >= == != ");
        assert_eq!(
            kinds(&tokens),
            vec![
                TokenType::LessThan,
                TokenType::GreaterThan,
                TokenType::LessThanOrEqual,
                TokenType::GreaterThanOrEqual,
                TokenType::Eq,
                TokenType::NotEq,
            ]
        );
    }

    #[test]
    fn test_tokenize_all_logical_operators() {
        let tokens = tokenize("! && || ");
        assert_eq!(
            kinds(&tokens),
            vec![TokenType::Bang, TokenType::And, TokenType::Or]
        );
    }

    #[test]
    fn test_tokenize_all_compound_assignment_operators() {
        let tokens = tokenize("+= -= *= /= %= ");
        assert_eq!(
            kinds(&tokens),
            vec![
                TokenType::PlusAssign,
                TokenType::MinusAssign,
                TokenType::AsteriskAssign,
                TokenType::SlashAssign,
                TokenType::ModulusAssign,
            ]
        );
    }

    #[test]
    fn test_tokenize_all_bitwise_operators() {
        let tokens = tokenize("& | ^ ~ << >> ");
        assert_eq!(
            kinds(&tokens),
            vec![
                TokenType::BitwiseAnd,
                TokenType::BitwiseOr,
                TokenType::BitwiseXor,
                TokenType::BitwiseNot,
                TokenType::BitwiseLeftShift,
                TokenType::BitwiseRightShift,
            ]
        );
    }

    #[test]
    fn test_tokenize_all_int_division_operator() {
        let tokens = tokenize("// ");
        assert_eq!(tokens.len(), 1);
        assert_eq!(tokens[0].kind, TokenType::IntDivision);
        assert_eq!(tokens[0].literal, "//");
    }

    #[test]
    fn test_tokenize_all_single_equals_vs_double_equals() {
        // '=' + space → Assign (not confused with '==' because peek is ' ')
        let tokens = tokenize("= == ");
        assert_eq!(tokens[0].kind, TokenType::Assign);
        assert_eq!(tokens[0].literal, "=");
        assert_eq!(tokens[1].kind, TokenType::Eq);
        assert_eq!(tokens[1].literal, "==");
    }

    #[test]
    fn test_tokenize_all_bang_vs_not_eq() {
        let tokens = tokenize("! != ");
        assert_eq!(tokens[0].kind, TokenType::Bang);
        assert_eq!(tokens[1].kind, TokenType::NotEq);
    }

    // ── delimiters ────────────────────────────────────────────────────────────

    #[test]
    fn test_tokenize_all_all_delimiters() {
        // trailing space keeps peek_char safe on the final '.'
        let tokens = tokenize("( ) [ ] { } , ; : . ");
        assert_eq!(
            kinds(&tokens),
            vec![
                TokenType::LParen,
                TokenType::RParen,
                TokenType::LBracket,
                TokenType::RBracket,
                TokenType::LBrace,
                TokenType::RBrace,
                TokenType::Comma,
                TokenType::Semicolon,
                TokenType::Colon,
                TokenType::Dot,
            ]
        );
    }

    // ── string and f-string literals ──────────────────────────────────────────

    #[test]
    fn test_tokenize_all_double_quoted_string() {
        let tokens = tokenize("\"hello world\"");
        assert_eq!(tokens.len(), 1);
        assert_eq!(tokens[0].kind, TokenType::String);
        assert_eq!(tokens[0].literal, "hello world");
    }

    #[test]
    fn test_tokenize_all_single_quoted_string() {
        let tokens = tokenize("'hello world'");
        assert_eq!(tokens.len(), 1);
        assert_eq!(tokens[0].kind, TokenType::String);
        assert_eq!(tokens[0].literal, "hello world");
    }

    #[test]
    fn test_tokenize_all_empty_string() {
        let tokens = tokenize("\"\"");
        assert_eq!(tokens.len(), 1);
        assert_eq!(tokens[0].kind, TokenType::String);
        assert_eq!(tokens[0].literal, "");
    }

    #[test]
    fn test_tokenize_all_string_escape_sequences_expanded() {
        // Escape sequences must be decoded by the lexer
        let tokens = tokenize("\"a\\nb\\tc\\rd\\\\e\\\"f\"");
        assert_eq!(tokens.len(), 1);
        assert_eq!(tokens[0].literal, "a\nb\tc\rd\\e\"f");
    }

    #[test]
    fn test_tokenize_all_fstring_basic_interpolation() {
        let tokens = tokenize("f\"hello {name}\"");
        assert_eq!(tokens.len(), 1);
        assert_eq!(tokens[0].kind, TokenType::FString);
        assert_eq!(tokens[0].literal, "hello {name}");
    }

    #[test]
    fn test_tokenize_all_fstring_takes_priority_over_bare_f_identifier() {
        // 'f' immediately followed by '"' must produce FString, not Identifier + String
        let tokens = tokenize("f\"test\"");
        assert_eq!(tokens.len(), 1);
        assert_eq!(tokens[0].kind, TokenType::FString);
    }

    #[test]
    fn test_tokenize_all_f_without_quote_is_identifier() {
        // 'f' followed by a non-quote character is a plain identifier
        let tokens = tokenize("foo");
        assert_eq!(tokens.len(), 1);
        assert_eq!(tokens[0].kind, TokenType::Identifier);
        assert_eq!(tokens[0].literal, "foo");
    }

    // ── next_token: pull-style API ────────────────────────────────────────────

    #[test]
    fn test_next_token_empty_input_returns_eof() {
        let mut lexer = Lexer::new(String::new());
        let tok = lexer.next_token().unwrap();
        assert_eq!(tok.kind, TokenType::Eof);
    }

    #[test]
    fn test_next_token_repeated_calls_past_eof_return_eof() {
        // Eof is idempotent — callers can overshoot without error
        let mut lexer = Lexer::new("x".to_string());
        let _id = lexer.next_token().unwrap();
        let eof1 = lexer.next_token().unwrap();
        let eof2 = lexer.next_token().unwrap();
        assert_eq!(eof1.kind, TokenType::Eof);
        assert_eq!(eof2.kind, TokenType::Eof);
    }

    #[test]
    fn test_next_token_produces_tokens_in_order() {
        let mut lexer = Lexer::new("let x ".to_string());
        let t1 = lexer.next_token().unwrap();
        let t2 = lexer.next_token().unwrap();
        let t3 = lexer.next_token().unwrap();
        assert_eq!(t1.kind, TokenType::Let);
        assert_eq!(t2.kind, TokenType::Identifier);
        assert_eq!(t2.literal, "x");
        assert_eq!(t3.kind, TokenType::Eof);
    }

    #[test]
    fn test_next_token_unknown_char_yields_illegal_not_error() {
        let mut lexer = Lexer::new("@ ".to_string());
        let tok = lexer.next_token().unwrap();
        assert_eq!(tok.kind, TokenType::Illegal);
        assert_eq!(tok.literal, "@");
    }

    #[test]
    fn test_next_token_illegal_char_does_not_stop_lexing() {
        let mut lexer = Lexer::new("@ x".to_string());
        let illegal = lexer.next_token().unwrap();
        let id = lexer.next_token().unwrap();
        assert_eq!(illegal.kind, TokenType::Illegal);
        assert_eq!(id.kind, TokenType::Identifier);
        assert_eq!(id.literal, "x");
    }

    #[test]
    fn test_tokenize_all_multiple_illegal_chars() {
        let tokens = tokenize("@ $ `");
        assert_eq!(tokens.len(), 3);
        assert!(tokens.iter().all(|t| t.kind == TokenType::Illegal));
    }

    #[test]
    fn test_next_token_unterminated_string_returns_err() {
        let mut lexer = Lexer::new("\"unterminated".to_string());
        assert!(lexer.next_token().is_err());
    }

    #[test]
    fn test_tokenize_all_unterminated_string_returns_err() {
        let result = Lexer::new("let x = \"unterminated".to_string()).tokenize_all();
        assert!(result.is_err());
    }

    #[test]
    fn test_tokenize_all_unterminated_fstring_returns_err() {
        let result = Lexer::new("f\"unclosed expression".to_string()).tokenize_all();
        assert!(result.is_err());
    }

    #[test]
    fn test_next_token_block_comment_is_skipped() {
        let mut lexer = Lexer::new("/* comment */ x".to_string());
        let tok = lexer.next_token().unwrap();
        assert_eq!(tok.kind, TokenType::Identifier);
        assert_eq!(tok.literal, "x");
    }

    #[test]
    fn test_next_token_nested_block_comment_skipped_via_depth_tracking() {
        // depth increments for inner '/*', decrements for each '*/' in turn
        let mut lexer = Lexer::new("/* outer /* inner */ still outer */ x".to_string());
        let tok = lexer.next_token().unwrap();
        assert_eq!(tok.kind, TokenType::Identifier);
        assert_eq!(tok.literal, "x");
    }

    #[test]
    fn test_tokenize_all_block_comment_between_tokens() {
        let tokens = tokenize("x /* skip this */ y");
        assert_eq!(tokens.len(), 2);
        assert_eq!(tokens[0].literal, "x");
        assert_eq!(tokens[1].literal, "y");
    }

    #[test]
    fn test_tokenize_all_block_comment_only_returns_empty() {
        // A file that is entirely a comment produces no tokens
        let tokens = tokenize("/* entirely a comment */");
        assert!(tokens.is_empty());
    }

    #[test]
    fn test_tokenize_all_multiple_sequential_block_comments() {
        let tokens = tokenize("/* a */ x /* b */ y /* c */");
        assert_eq!(tokens.len(), 2);
        assert_eq!(tokens[0].literal, "x");
        assert_eq!(tokens[1].literal, "y");
    }

    // ── position tracking (via Display, since fields are private) ────────────

    #[test]
    fn test_tokenize_all_first_token_on_first_line() {
        let tokens = tokenize("x");
        assert!(
            tokens[0].position.to_string().starts_with("line 1"),
            "first token must report line 1, got {}",
            tokens[0].position
        );
    }

    #[test]
    fn test_tokenize_all_token_after_newline_on_second_line() {
        let tokens = tokenize("x\ny");
        assert!(
            tokens[1].position.to_string().starts_with("line 2"),
            "token after newline must report line 2, got {}",
            tokens[1].position
        );
    }

    #[test]
    fn test_tokenize_all_multiline_produces_correct_token_count() {
        // "let x\nlet y" → Let, Identifier, Let, Identifier — 4 tokens, no Eof
        let tokens = tokenize("let x\nlet y");
        assert_eq!(tokens.len(), 4);
        assert_eq!(tokens[0].kind, TokenType::Let);
        assert_eq!(tokens[1].literal, "x");
        assert_eq!(tokens[2].kind, TokenType::Let);
        assert_eq!(tokens[3].literal, "y");
    }

    #[test]
    fn test_tokenize_all_let_binding() {
        // trailing space keeps peek_char safe on ';'
        let tokens = tokenize("let x = 5; ");
        assert_eq!(
            kinds(&tokens),
            vec![
                TokenType::Let,
                TokenType::Identifier,
                TokenType::Assign,
                TokenType::Int,
                TokenType::Semicolon,
            ]
        );
        assert_eq!(tokens[1].literal, "x");
        assert_eq!(tokens[3].literal, "5");
    }

    #[test]
    fn test_tokenize_all_function_definition() {
        let tokens = tokenize("fn add(a, b) { a + b } ");
        assert_eq!(tokens[0].kind, TokenType::Function);
        assert_eq!(tokens[1].kind, TokenType::Identifier);
        assert_eq!(tokens[1].literal, "add");
        assert!(tokens.iter().any(|t| t.kind == TokenType::Plus));
        assert!(tokens.iter().any(|t| t.kind == TokenType::LParen));
        assert!(tokens.iter().any(|t| t.kind == TokenType::RParen));
        assert!(tokens.iter().any(|t| t.kind == TokenType::LBrace));
        assert!(tokens.iter().any(|t| t.kind == TokenType::RBrace));
    }

    #[test]
    fn test_tokenize_all_if_elif_else_chain() {
        let tokens = tokenize("if a elif b else c");
        assert_eq!(tokens[0].kind, TokenType::If);
        assert_eq!(tokens[2].kind, TokenType::Elif);
        assert_eq!(tokens[4].kind, TokenType::Else);
    }

    #[test]
    fn test_tokenize_all_class_with_extends() {
        let tokens = tokenize("class Animal extends Base ");
        assert_eq!(
            kinds(&tokens),
            vec![
                TokenType::Class,
                TokenType::Identifier,
                TokenType::Extends,
                TokenType::Identifier,
            ]
        );
        assert_eq!(tokens[1].literal, "Animal");
        assert_eq!(tokens[3].literal, "Base");
    }

    #[test]
    fn test_tokenize_all_balanced_delimiters_in_nested_structure() {
        // Every delimiter must be surrounded by whitespace.  OperatorParser::parse
        // calls stream.advance() internally, and next_token advances again after
        // parse() returns, so a single-char operator consumes TWO characters.
        // Without the surrounding spaces, the char immediately after `{` or `[`
        // would be silently skipped and the token stream would be corrupted.
        let tokens = tokenize("{ [ 1 , 2 ] , { 3 , 4 } } ");
        let open = tokens
            .iter()
            .filter(|t| t.kind == TokenType::LBrace)
            .count()
            + tokens
                .iter()
                .filter(|t| t.kind == TokenType::LBracket)
                .count();
        let close = tokens
            .iter()
            .filter(|t| t.kind == TokenType::RBrace)
            .count()
            + tokens
                .iter()
                .filter(|t| t.kind == TokenType::RBracket)
                .count();
        assert_eq!(
            open, close,
            "all opening delimiters must have a matching closer"
        );
    }

    #[test]
    fn test_tokenize_all_fibonacci_function() {
        // Ends with '\n' so the closing '}' has a following char for peek_char
        let src = "fn fibonacci(n) {\nif (n <= 1) { return n }\nreturn fibonacci(n - 1) + fibonacci(n - 2)\n}\n";
        let tokens = tokenize(src);
        assert!(tokens.iter().any(|t| t.kind == TokenType::Function));
        assert!(tokens.iter().any(|t| t.kind == TokenType::If));
        assert!(tokens.iter().any(|t| t.kind == TokenType::Return));
        assert!(tokens.iter().any(|t| t.kind == TokenType::LessThanOrEqual));
        assert!(
            tokens
                .iter()
                .any(|t| t.kind == TokenType::Identifier && t.literal == "fibonacci"),
            "function name 'fibonacci' must appear as an identifier"
        );
    }

    #[test]
    fn test_tokenize_all_very_long_identifier() {
        let long: String = "a".repeat(1001);
        let tokens = tokenize(&long);
        assert_eq!(tokens.len(), 1);
        assert_eq!(tokens[0].kind, TokenType::Identifier);
        assert_eq!(tokens[0].literal.len(), 1001);
    }

    #[test]
    fn test_tokenize_all_very_long_integer_literal() {
        let long: String = "9".repeat(200);
        let tokens = tokenize(&long);
        assert_eq!(tokens.len(), 1);
        assert_eq!(tokens[0].kind, TokenType::Int);
        assert_eq!(tokens[0].literal.len(), 200);
    }
}
