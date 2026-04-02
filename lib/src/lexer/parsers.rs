// parse_string, parse_fstring, parse_number, parse_identifier, parse_operator, skip_whitespace
#[allow(dead_code)]
use thiserror::Error;

const INITIAL_STRING_CAPACITY: usize = 64;
const LEFT_BRACE: char = '{';
const RIGHT_BRACE: char = '}';

use crate::token::{lookup_identifier, Token, TokenPosition, TokenType};

use super::stream::{CharacterStream, DOT, DOUBLE_QUOTE, EOF, SINGLE_QUOTE};

const fn is_letter(ch: char) -> bool {
    (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_'
}

const fn is_digit(ch: char) -> bool {
    ch >= '0' && ch <= '9'
}

const fn is_whitespace(ch: char) -> bool {
    matches!(ch, ' ' | '\t' | '\n' | '\r' | '\x0C') // \x0C = form feed
}

fn create_token(kind: TokenType, literal: String, stream: &CharacterStream) -> Token {
    let (line, col) = stream.token_pos();
    Token::new(kind, literal, TokenPosition::new(line, col))
}
fn handle_escape(ch: char) -> char {
    match ch {
        'n' => '\n',
        't' => '\t',
        'r' => '\r',
        'f' => '\x0C',
        'b' => '\x08',
        '\'' => '\'',
        '"' => '"',
        '\\' => '\\',
        _ => ch,
    }
}

#[derive(Debug, Error)]
pub enum LexError {
    #[error("unterminated string literal at {line}:{col}")]
    UnterminatedString { line: usize, col: usize },

    #[error("unterminated f-string at {line}:{col}")]
    UnterminatedFString { line: usize, col: usize },

    #[error("unmatched '}}' in f-string at {line}:{col}")]
    UnmatchedBrace { line: usize, col: usize },

    #[error("unclosed '{{' in f-string at {line}:{col}")]
    UnclosedBrace { line: usize, col: usize },

    #[error("unrecognized character '{ch}' at {line}:{col}")]
    UnrecognizedChar { ch: char, line: usize, col: usize },
}

pub(crate) trait TokenParser {
    fn can_parse(&self, ch: char, stream: &CharacterStream) -> bool;
    fn parse(&self, stream: &mut CharacterStream) -> Result<Token, LexError>;
}

pub(crate) struct IdentifierParser {}

impl TokenParser for IdentifierParser {
    fn can_parse(&self, ch: char, _stream: &CharacterStream) -> bool {
        is_letter(ch)
    }

    fn parse(&self, stream: &mut CharacterStream) -> Result<Token, LexError> {
        let start = stream.current_position();

        while is_letter(stream.current_char()) || is_digit(stream.current_char()) {
            stream.advance();
        }

        let identifier = stream.get_substring(start);
        stream.backtrack(1);

        let token_type = lookup_identifier(identifier.as_str());
        Ok(create_token(token_type, identifier, stream))
    }
}

pub(crate) struct NumberParser {}

impl TokenParser for NumberParser {
    fn can_parse(&self, ch: char, stream: &CharacterStream) -> bool {
        is_digit(ch) || (ch == DOT && is_digit(stream.peek_char()))
    }

    fn parse(&self, stream: &mut CharacterStream) -> Result<Token, LexError> {
        let start = stream.current_position();
        let mut is_float = false;

        if stream.current_char() == DOT {
            is_float = true;
            stream.advance();

            while is_digit(stream.current_char()) {
                stream.advance();
            }
        } else {
            while is_digit(stream.current_char()) {
                stream.advance();
            }

            if stream.current_char() == DOT && is_digit(stream.peek_char()) {
                is_float = true;
                stream.advance();

                while is_digit(stream.current_char()) {
                    stream.advance();
                }
            }
        }

        let num = stream.get_substring(start);
        stream.backtrack(1);

        let token_type = if is_float {
            TokenType::Float
        } else {
            TokenType::Int
        };

        Ok(create_token(token_type, num, stream))
    }
}

pub(crate) struct OperatorParser {}

impl OperatorParser {
    #[allow(clippy::unused_self)]
    const fn try_two_char(&self, current: char, next: char) -> Option<TokenType> {
        match (current, next) {
            ('=', '=') => Some(TokenType::Eq),
            ('!', '=') => Some(TokenType::NotEq),
            ('<', '=') => Some(TokenType::LessThanOrEqual),
            ('<', '<') => Some(TokenType::BitwiseLeftShift),
            ('>', '=') => Some(TokenType::GreaterThanOrEqual),
            ('>', '>') => Some(TokenType::BitwiseRightShift),
            ('+', '=') => Some(TokenType::PlusAssign),
            ('-', '=') => Some(TokenType::MinusAssign),
            ('*', '=') => Some(TokenType::AsteriskAssign),
            ('/', '=') => Some(TokenType::SlashAssign),
            ('/', '/') => Some(TokenType::IntDivision),
            ('%', '=') => Some(TokenType::ModulusAssign),
            ('&', '&') => Some(TokenType::And),
            ('|', '|') => Some(TokenType::Or),
            _ => None,
        }
    }

    #[allow(clippy::unused_self)]
    const fn single_char(&self, ch: char) -> TokenType {
        match ch {
            '=' => TokenType::Assign,
            '!' => TokenType::Bang,
            '<' => TokenType::LessThan,
            '>' => TokenType::GreaterThan,
            '+' => TokenType::Plus,
            '-' => TokenType::Minus,
            '*' => TokenType::Asterisk,
            '/' => TokenType::Slash,
            '%' => TokenType::Modulus,
            '&' => TokenType::BitwiseAnd,
            '|' => TokenType::BitwiseOr,
            '^' => TokenType::BitwiseXor,
            '~' => TokenType::BitwiseNot,
            ';' => TokenType::Semicolon,
            ',' => TokenType::Comma,
            ':' => TokenType::Colon,
            '.' => TokenType::Dot,
            '(' => TokenType::LParen,
            ')' => TokenType::RParen,
            '{' => TokenType::LBrace,
            '}' => TokenType::RBrace,
            '[' => TokenType::LBracket,
            ']' => TokenType::RBracket,
            _ => TokenType::Illegal,
        }
    }
}

impl TokenParser for OperatorParser {
    fn can_parse(&self, ch: char, _stream: &CharacterStream) -> bool {
        matches!(
            ch,
            '=' | '!'
                | '<'
                | '>'
                | '+'
                | '-'
                | '*'
                | '/'
                | '%'
                | '&'
                | '|'
                | '^'
                | '~'
                | ';'
                | ','
                | ':'
                | '.'
                | '('
                | ')'
                | '{'
                | '}'
                | '['
                | ']'
        )
    }

    fn parse(&self, stream: &mut CharacterStream) -> Result<Token, LexError> {
        let current = stream.current_char();
        let peek = stream.peek_char();

        if let Some(token_type) = self.try_two_char(current, peek) {
            let literal = format!("{current}{peek}");
            stream.advance();
            return Ok(create_token(token_type, literal, stream));
        }

        let token_type = self.single_char(current);
        stream.advance();
        Ok(create_token(token_type, current.to_string(), stream))
    }
}

pub(crate) struct StringParser {}

impl TokenParser for StringParser {
    fn can_parse(&self, ch: char, _: &CharacterStream) -> bool {
        ch == SINGLE_QUOTE || ch == DOUBLE_QUOTE
    }

    fn parse(&self, stream: &mut CharacterStream) -> Result<Token, LexError> {
        let quote_char = stream.current_char();
        let mut s = String::with_capacity(INITIAL_STRING_CAPACITY);

        stream.advance();

        while stream.current_char() != EOF && stream.current_char() != quote_char {
            let ch = stream.current_char();
            if ch == '\\' {
                stream.advance();
                s.push(handle_escape(stream.current_char()));
            } else {
                s.push(ch);
            }
            stream.advance();
        }

        if stream.current_char() != quote_char {
            let (line, col) = stream.token_pos();
            return Err(LexError::UnterminatedString { line, col });
        }

        Ok(create_token(TokenType::String, s, stream))
    }
}

pub(crate) struct FStringParser {}

impl TokenParser for FStringParser {
    fn can_parse(&self, ch: char, stream: &CharacterStream) -> bool {
        ch == 'f' && (stream.peek_char() == SINGLE_QUOTE || stream.peek_char() == DOUBLE_QUOTE)
    }

    fn parse(&self, stream: &mut CharacterStream) -> Result<Token, LexError> {
        stream.advance();
        stream.advance();

        let mut s = String::with_capacity(INITIAL_STRING_CAPACITY);
        let mut brace_depth = 0;

        while stream.current_char() != EOF {
            let curr_ch = stream.current_char();
            if curr_ch == DOUBLE_QUOTE && brace_depth == 0 {
                break;
            }

            if curr_ch == LEFT_BRACE {
                brace_depth += 1;
            } else if curr_ch == RIGHT_BRACE {
                brace_depth -= 1;
                if brace_depth < 0 {
                    let (line, col) = stream.token_pos();
                    return Err(LexError::UnterminatedString { line, col });
                }
            }

            if curr_ch == '\\' {
                stream.advance();
                s.push(handle_escape(stream.current_char()));
            } else {
                s.push(curr_ch);
            }
            stream.advance();
        }
        if stream.current_char() != DOUBLE_QUOTE {
            let (line, col) = stream.token_pos();
            return Err(LexError::UnterminatedFString { line, col });
        }

        if brace_depth > 0 {
            let (line, col) = stream.token_pos();
            return Err(LexError::UnclosedBrace { line, col });
        }

        Ok(create_token(TokenType::FString, s, stream))
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::lexer::stream::CharacterStream;
    use crate::token::TokenType;

    fn stream(input: &str) -> CharacterStream {
        CharacterStream::new(input)
    }

    // ── IdentifierParser ─────────────────────────────────────────────────────

    #[test]
    fn identifier_simple() {
        let mut s = stream("foo");
        let tok = IdentifierParser {}.parse(&mut s).unwrap();
        assert_eq!(tok.kind, TokenType::Identifier);
        assert_eq!(tok.literal, "foo");
    }

    #[test]
    fn identifier_with_trailing_digits() {
        let mut s = stream("foo123");
        let tok = IdentifierParser {}.parse(&mut s).unwrap();
        assert_eq!(tok.kind, TokenType::Identifier);
        assert_eq!(tok.literal, "foo123");
    }

    #[test]
    fn identifier_underscore_prefix() {
        let mut s = stream("_myVar");
        assert!(IdentifierParser {}.can_parse('_', &s));
        let tok = IdentifierParser {}.parse(&mut s).unwrap();
        assert_eq!(tok.literal, "_myVar");
    }

    #[test]
    fn identifier_single_letter() {
        let mut s = stream("x");
        let tok = IdentifierParser {}.parse(&mut s).unwrap();
        assert_eq!(tok.literal, "x");
    }

    #[test]
    fn identifier_cannot_parse_digit() {
        let s = stream("123");
        assert!(!IdentifierParser {}.can_parse('1', &s));
    }

    #[test]
    fn keyword_let() {
        let mut s = stream("let");
        let tok = IdentifierParser {}.parse(&mut s).unwrap();
        assert_eq!(tok.kind, TokenType::Let);
    }

    #[test]
    fn keyword_fn() {
        let mut s = stream("fn");
        let tok = IdentifierParser {}.parse(&mut s).unwrap();
        assert_eq!(tok.kind, TokenType::Function);
    }

    #[test]
    fn keyword_return() {
        let mut s = stream("return");
        let tok = IdentifierParser {}.parse(&mut s).unwrap();
        assert_eq!(tok.kind, TokenType::Return);
    }

    #[test]
    fn keyword_if_else() {
        let mut s = stream("if");
        assert_eq!(
            IdentifierParser {}.parse(&mut s).unwrap().kind,
            TokenType::If
        );
        let mut s = stream("else");
        assert_eq!(
            IdentifierParser {}.parse(&mut s).unwrap().kind,
            TokenType::Else
        );
    }

    #[test]
    fn keyword_true_false() {
        let mut s = stream("true");
        assert_eq!(
            IdentifierParser {}.parse(&mut s).unwrap().kind,
            TokenType::True
        );
        let mut s = stream("false");
        assert_eq!(
            IdentifierParser {}.parse(&mut s).unwrap().kind,
            TokenType::False
        );
    }

    #[test]
    fn keyword_null() {
        let mut s = stream("null");
        assert_eq!(
            IdentifierParser {}.parse(&mut s).unwrap().kind,
            TokenType::Null
        );
    }

    // ── NumberParser ─────────────────────────────────────────────────────────

    #[test]
    fn number_integer() {
        let mut s = stream("42");
        assert!(NumberParser {}.can_parse('4', &s));
        let tok = NumberParser {}.parse(&mut s).unwrap();
        assert_eq!(tok.kind, TokenType::Int);
        assert_eq!(tok.literal, "42");
    }

    #[test]
    fn number_single_digit() {
        let mut s = stream("0");
        let tok = NumberParser {}.parse(&mut s).unwrap();
        assert_eq!(tok.kind, TokenType::Int);
        assert_eq!(tok.literal, "0");
    }

    #[test]
    fn number_float_with_dot() {
        let mut s = stream("3.14");
        let tok = NumberParser {}.parse(&mut s).unwrap();
        assert_eq!(tok.kind, TokenType::Float);
        assert_eq!(tok.literal, "3.14");
    }

    #[test]
    fn number_leading_dot_float() {
        // ".5" — dot comes before digits
        let mut s = stream(".5");
        assert!(NumberParser {}.can_parse('.', &s));
        let tok = NumberParser {}.parse(&mut s).unwrap();
        assert_eq!(tok.kind, TokenType::Float);
        assert_eq!(tok.literal, ".5");
    }

    #[test]
    fn number_dot_without_following_digit_is_not_parseable() {
        // "." followed by a non-digit — not a number; needs a trailing char so peek_char() doesn't OOB
        let s = stream(".x");
        assert!(!NumberParser {}.can_parse('.', &s));
    }

    #[test]
    fn number_cannot_parse_letter() {
        let s = stream("abc");
        assert!(!NumberParser {}.can_parse('a', &s));
    }

    // ── OperatorParser ───────────────────────────────────────────────────────
    //
    // Every call needs at least one char after the operator because peek_char()
    // indexes chars[next_pos] unconditionally.  A trailing space is sufficient.

    fn parse_op(input: &str) -> Token {
        let mut s = stream(input);
        OperatorParser {}.parse(&mut s).unwrap()
    }

    #[test]
    fn operator_can_parse_all_recognized_chars() {
        let s = stream("+ ");
        let p = OperatorParser {};
        for ch in [
            '=', '!', '<', '>', '+', '-', '*', '/', '%', '&', '|', '^', '~', ';', ',', ':', '.',
            '(', ')', '{', '}', '[', ']',
        ] {
            assert!(p.can_parse(ch, &s), "should recognise '{ch}'");
        }
    }

    #[test]
    fn operator_cannot_parse_letter() {
        let s = stream("a ");
        assert!(!OperatorParser {}.can_parse('a', &s));
    }

    #[test]
    fn operator_single_plus() {
        let tok = parse_op("+ ");
        assert_eq!(tok.kind, TokenType::Plus);
        assert_eq!(tok.literal, "+");
    }

    #[test]
    fn operator_single_minus() {
        assert_eq!(parse_op("- ").kind, TokenType::Minus);
    }

    #[test]
    fn operator_single_asterisk() {
        assert_eq!(parse_op("* ").kind, TokenType::Asterisk);
    }

    #[test]
    fn operator_single_slash() {
        assert_eq!(parse_op("/ ").kind, TokenType::Slash);
    }

    #[test]
    fn operator_single_assign() {
        assert_eq!(parse_op("= ").kind, TokenType::Assign);
    }

    #[test]
    fn operator_single_bang() {
        assert_eq!(parse_op("! ").kind, TokenType::Bang);
    }

    #[test]
    fn operator_single_lt_gt() {
        assert_eq!(parse_op("< ").kind, TokenType::LessThan);
        assert_eq!(parse_op("> ").kind, TokenType::GreaterThan);
    }

    #[test]
    fn operator_two_char_eq() {
        let tok = parse_op("==");
        assert_eq!(tok.kind, TokenType::Eq);
        assert_eq!(tok.literal, "==");
    }

    #[test]
    fn operator_two_char_neq() {
        assert_eq!(parse_op("!=").kind, TokenType::NotEq);
    }

    #[test]
    fn operator_two_char_lte_gte() {
        assert_eq!(parse_op("<=").kind, TokenType::LessThanOrEqual);
        assert_eq!(parse_op(">=").kind, TokenType::GreaterThanOrEqual);
    }

    #[test]
    fn operator_two_char_and_or() {
        assert_eq!(parse_op("&&").kind, TokenType::And);
        assert_eq!(parse_op("||").kind, TokenType::Or);
    }

    #[test]
    fn operator_two_char_int_division() {
        let tok = parse_op("//");
        assert_eq!(tok.kind, TokenType::IntDivision);
        assert_eq!(tok.literal, "//");
    }

    #[test]
    fn operator_compound_assignments() {
        assert_eq!(parse_op("+=").kind, TokenType::PlusAssign);
        assert_eq!(parse_op("-=").kind, TokenType::MinusAssign);
        assert_eq!(parse_op("*=").kind, TokenType::AsteriskAssign);
        assert_eq!(parse_op("/=").kind, TokenType::SlashAssign);
        assert_eq!(parse_op("%=").kind, TokenType::ModulusAssign);
    }

    #[test]
    fn operator_bitwise_shifts() {
        assert_eq!(parse_op("<<").kind, TokenType::BitwiseLeftShift);
        assert_eq!(parse_op(">>").kind, TokenType::BitwiseRightShift);
    }

    // ── StringParser ─────────────────────────────────────────────────────────

    #[test]
    fn string_double_quoted() {
        let mut s = stream("\"hello\"");
        assert!(StringParser {}.can_parse('"', &s));
        let tok = StringParser {}.parse(&mut s).unwrap();
        assert_eq!(tok.kind, TokenType::String);
        assert_eq!(tok.literal, "hello");
    }

    #[test]
    fn string_single_quoted() {
        let mut s = stream("'world'");
        assert!(StringParser {}.can_parse('\'', &s));
        let tok = StringParser {}.parse(&mut s).unwrap();
        assert_eq!(tok.kind, TokenType::String);
        assert_eq!(tok.literal, "world");
    }

    #[test]
    fn string_empty() {
        let mut s = stream("\"\"");
        let tok = StringParser {}.parse(&mut s).unwrap();
        assert_eq!(tok.literal, "");
    }

    #[test]
    fn string_escape_newline() {
        let mut s = stream("\"a\\nb\"");
        let tok = StringParser {}.parse(&mut s).unwrap();
        assert_eq!(tok.literal, "a\nb");
    }

    #[test]
    fn string_escape_tab() {
        let mut s = stream("\"a\\tb\"");
        let tok = StringParser {}.parse(&mut s).unwrap();
        assert_eq!(tok.literal, "a\tb");
    }

    #[test]
    fn string_escape_backslash() {
        let mut s = stream("\"a\\\\b\"");
        let tok = StringParser {}.parse(&mut s).unwrap();
        assert_eq!(tok.literal, "a\\b");
    }

    #[test]
    fn string_escape_quote() {
        let mut s = stream("\"say \\\"hi\\\"\"");
        let tok = StringParser {}.parse(&mut s).unwrap();
        assert_eq!(tok.literal, "say \"hi\"");
    }

    #[test]
    fn string_unterminated_is_error() {
        let mut s = stream("\"hello");
        let err = StringParser {}.parse(&mut s).unwrap_err();
        assert!(matches!(err, LexError::UnterminatedString { .. }));
    }

    #[test]
    fn string_cannot_parse_non_quote() {
        let s = stream("abc");
        assert!(!StringParser {}.can_parse('a', &s));
    }

    #[test]
    fn fstring_basic_interpolation() {
        let mut s = stream("f\"hello {name}\"");
        assert!(FStringParser {}.can_parse('f', &s));
        let tok = FStringParser {}.parse(&mut s).unwrap();
        assert_eq!(tok.kind, TokenType::FString);
        assert_eq!(tok.literal, "hello {name}");
    }

    #[test]
    fn fstring_no_interpolation() {
        let mut s = stream("f\"plain text\"");
        let tok = FStringParser {}.parse(&mut s).unwrap();
        assert_eq!(tok.literal, "plain text");
    }

    #[test]
    fn fstring_multiple_expressions() {
        let mut s = stream("f\"{a} and {b}\"");
        let tok = FStringParser {}.parse(&mut s).unwrap();
        assert_eq!(tok.literal, "{a} and {b}");
    }

    #[test]
    fn fstring_empty() {
        let mut s = stream("f\"\"");
        let tok = FStringParser {}.parse(&mut s).unwrap();
        assert_eq!(tok.literal, "");
    }

    #[test]
    fn fstring_unterminated_is_error() {
        let mut s = stream("f\"hello");
        let err = FStringParser {}.parse(&mut s).unwrap_err();
        assert!(matches!(err, LexError::UnterminatedFString { .. }));
    }

    #[test]
    fn fstring_unclosed_brace_gives_unterminated_error() {
        // The closing `"` is consumed as content (brace_depth=1, so the break
        // condition is never met), causing the loop to exit on EOF instead.
        // UnclosedBrace is therefore unreachable; we get UnterminatedFString.
        let mut s = stream("f\"hello {name\"");
        let err = FStringParser {}.parse(&mut s).unwrap_err();
        assert!(matches!(err, LexError::UnterminatedFString { .. }));
    }

    #[test]
    fn fstring_cannot_parse_non_f() {
        let s = stream("\"hello\"");
        assert!(!FStringParser {}.can_parse('"', &s));
    }

    #[test]
    fn fstring_cannot_parse_f_without_quote() {
        // 'f' followed by a non-quote char
        let s = stream("foo");
        assert!(!FStringParser {}.can_parse('f', &s));
    }

    // ── Additional IdentifierParser edge cases ────────────────────────────────

    #[test]
    fn ident_can_parse_uppercase_letter() {
        let s = stream("Hello");
        assert!(IdentifierParser {}.can_parse('H', &s));
    }

    #[test]
    fn ident_cannot_parse_operator_char() {
        let s = stream("+ ");
        assert!(!IdentifierParser {}.can_parse('+', &s));
    }

    #[test]
    fn keyword_while_break_continue() {
        let cases = [
            ("while", TokenType::While),
            ("break", TokenType::Break),
            ("continue", TokenType::Continue),
        ];
        for (word, expected) in cases {
            let mut s = stream(word);
            assert_eq!(
                IdentifierParser {}.parse(&mut s).unwrap().kind,
                expected,
                "failed for '{word}'"
            );
        }
    }

    #[test]
    fn keyword_class_hierarchy() {
        let cases = [
            ("class", TokenType::Class),
            ("extends", TokenType::Extends),
            ("super", TokenType::Super),
            ("this", TokenType::This),
            ("new", TokenType::New),
        ];
        for (word, expected) in cases {
            let mut s = stream(word);
            assert_eq!(
                IdentifierParser {}.parse(&mut s).unwrap().kind,
                expected,
                "failed for '{word}'"
            );
        }
    }

    // ── Additional NumberParser edge cases ────────────────────────────────────

    #[test]
    fn number_large_integer() {
        let mut s = stream("9999999");
        let tok = NumberParser {}.parse(&mut s).unwrap();
        assert_eq!(tok.kind, TokenType::Int);
        assert_eq!(tok.literal, "9999999");
    }

    // ── Additional OperatorParser edge cases ──────────────────────────────────

    #[test]
    fn operator_delimiter_chars() {
        let cases = [
            ("; ", TokenType::Semicolon),
            (", ", TokenType::Comma),
            (": ", TokenType::Colon),
            ("( ", TokenType::LParen),
            (") ", TokenType::RParen),
            ("{ ", TokenType::LBrace),
            ("} ", TokenType::RBrace),
            ("[ ", TokenType::LBracket),
            ("] ", TokenType::RBracket),
        ];
        for (input, expected) in cases {
            let tok = parse_op(input);
            assert_eq!(tok.kind, expected, "failed for '{input}'");
        }
    }

    #[test]
    fn operator_bitwise_single_chars() {
        assert_eq!(parse_op("& ").kind, TokenType::BitwiseAnd);
        assert_eq!(parse_op("| ").kind, TokenType::BitwiseOr);
        assert_eq!(parse_op("^ ").kind, TokenType::BitwiseXor);
        assert_eq!(parse_op("~ ").kind, TokenType::BitwiseNot);
    }

    #[test]
    fn operator_dot() {
        assert_eq!(parse_op(". ").kind, TokenType::Dot);
    }

    #[test]
    fn operator_single_assign_not_confused_with_eq() {
        // '=' followed by space → Assign, not Eq
        let tok = parse_op("= ");
        assert_eq!(tok.kind, TokenType::Assign);
        assert_eq!(tok.literal, "=");
    }

    // ── Additional StringParser edge cases ────────────────────────────────────

    #[test]
    fn string_with_spaces() {
        let mut s = stream("\"hello world\"");
        let tok = StringParser {}.parse(&mut s).unwrap();
        assert_eq!(tok.literal, "hello world");
    }

    #[test]
    fn string_escape_carriage_return() {
        let mut s = stream("\"a\\rb\"");
        let tok = StringParser {}.parse(&mut s).unwrap();
        assert_eq!(tok.literal, "a\rb");
    }

    #[test]
    fn string_escape_single_quote_inside_single_quoted() {
        let mut s = stream("'it\\'s'");
        let tok = StringParser {}.parse(&mut s).unwrap();
        assert_eq!(tok.literal, "it's");
    }
}
