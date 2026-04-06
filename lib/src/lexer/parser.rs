const INITIAL_STRING_CAPACITY: usize = 64;
const LEFT_BRACE: char = '{';
const RIGHT_BRACE: char = '}';

use crate::token::{self, Token, TokenPosition, TokenType, lookup_identifier};

use super::{LexError, Lexer};

impl Lexer {
    /// Return `true` if `ch` is a valid identifier-start or identifier-continue character.
    pub(super) const fn is_letter(ch: char) -> bool {
        (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_'
    }

    /// Return `true` if `ch` is an ASCII decimal digit (`0`–`9`).
    pub(super) const fn is_digit(ch: char) -> bool {
        ch >= '0' && ch <= '9'
    }

    /// Translate a backslash escape sequence character into its runtime value.
    ///
    /// The argument `ch` is the character *after* the backslash. Recognized
    /// sequences are the standard set: `\n`, `\t`, `\r`, `\f`, `\b`, `\'`, `\"`,
    /// and `\\`. Any unrecognized character is returned unchanged, so e.g. `\x`
    /// becomes `x` rather than producing an error.
    fn ensure_current_char(&self) -> Result<char, LexError> {
        self.current_char()
            .ok_or(LexError::UnexpectedEOF(self.line_col))
    }

    const fn handle_escape(ch: char) -> char {
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

    /// Construct a [`Token`] from a kind, literal string, and current stream position.
    ///
    /// The `(line, col)` coordinates are read from `stream` via
    /// [`token_pos`](CharacterStream::token_pos) at the moment of the call, so
    /// callers should invoke this function while the stream is still positioned on
    /// the *last* character of the token being built — not after advancing past it.
    #[must_use]
    pub(super) fn create_token(
        kind: TokenType,
        literal: String,
        (line, col): (usize, usize),
    ) -> Token {
        Token::new(kind, literal, TokenPosition::new(line, col))
    }

    /// Consume an identifier or keyword and return the corresponding [`Token`].
    ///
    /// Advances past all consecutive letter-or-digit characters, extracts the
    /// lexeme substring, then backtracks one position so the stream honours the
    /// last-character contract required by [`TokenParser`].
    ///
    /// # Errors
    ///
    /// Always succeeds — every valid identifier-start sequence yields a token.
    pub(super) fn parse_identifier(&mut self) -> token::Token {
        let start = self.current_position();

        loop {
            match self.current_char() {
                Some(ch) if Self::is_letter(ch) || Self::is_digit(ch) => self.advance(),
                _ => break,
            }
        }

        let identifier = self.get_substring(start);
        self.backtrack(1);

        let token_type = lookup_identifier(identifier.as_str());
        Self::create_token(token_type, identifier, self.line_col)
    }

    /// Consume a numeric literal and return an [`TokenType::Int`] or
    /// [`TokenType::Float`] token.
    ///
    /// The algorithm handles the leading-dot form first (`".5"`), then the
    /// standard integer-or-float form. The `is_float` flag is set as soon as a
    /// `.` followed by a digit is seen, and determines the final token type.
    /// Backtracks one position before returning to honour the last-character
    /// contract.
    ///
    /// # Errors
    ///
    /// Always succeeds given a valid numeric sequence; no error variants are
    /// currently produced.
    pub(super) fn parse_number(&mut self) -> token::Token {
        let start = self.current_position();
        let mut is_float = false;

        loop {
            match self.current_char() {
                Some(ch) if Self::is_digit(ch) => self.advance(),
                _ => break,
            }
        }

        if let Some(ch) = self.current_char() {
            if ch == '.' || self.peek_char().is_some_and(Self::is_digit) {
                is_float = true;
                self.advance();

                loop {
                    match self.current_char() {
                        Some(ch) if Self::is_digit(ch) => self.advance(),
                        _ => break,
                    }
                }
            }
        }

        let number_str = self.get_substring(start);
        self.backtrack(1);

        let token_type = if is_float {
            TokenType::Float
        } else {
            TokenType::Int
        };
        Self::create_token(token_type, number_str, self.line_col)
    }

    /// Consume an f-string literal and return a [`TokenType::FString`] token.
    ///
    /// Skips the `f` prefix and the opening `"` before collecting body
    /// characters. Tracks brace depth to allow `{` and `}` inside interpolation
    /// expressions. A `}` that would bring `brace_depth` below zero is treated
    /// as an unmatched brace and reported as [`LexError::UnterminatedString`].
    /// After the closing `"` is found, a non-zero `brace_depth` is reported as
    /// [`LexError::UnclosedBrace`].
    ///
    /// # Errors
    ///
    /// | Condition | Error |
    /// |-----------|-------|
    /// | `}` at brace depth 0 | [`LexError::UnterminatedString`] (see note below) |
    /// | EOF before closing `"` | [`LexError::UnterminatedFString`] |
    /// | Unclosed `{` at closing `"` | [`LexError::UnclosedBrace`] |
    ///
    /// > **Note:** The unmatched-`}` case currently emits `UnterminatedString`
    /// > rather than [`LexError::UnmatchedBrace`]. This appears to be a bug —
    /// > flag for review.
    pub(super) fn parse_string(&mut self) -> Result<Token, LexError> {
        let quote_char = self.ensure_current_char()?;
        let mut s = String::with_capacity(INITIAL_STRING_CAPACITY);

        self.advance();

        loop {
            match self.current_char() {
                Some(ch) if ch == quote_char => {
                    self.advance();
                    return Ok(Self::create_token(TokenType::String, s, self.line_col));
                }
                Some('\\') => {
                    self.advance();
                    match self.current_char() {
                        Some(escaped) => {
                            s.push(Self::handle_escape(escaped));
                            self.advance();
                        }
                        None => {
                            return Err(LexError::UnterminatedString {
                                line: self.line_col.0,
                                col: self.line_col.1,
                            });
                        }
                    }
                }
                Some(ch) => {
                    s.push(ch);
                    self.advance();
                }
                None => {
                    return Err(LexError::UnterminatedString {
                        line: self.line_col.0,
                        col: self.line_col.1,
                    });
                }
            }
        }
    }

    /// Consume one or two characters and return an operator or punctuation token.
    ///
    /// Peeks at the next character and tries [`try_two_char`](OperatorParser::try_two_char)
    /// first. If a two-character operator matches, advances past the second
    /// character and returns — leaving the stream on the second character per
    /// the [`TokenParser`] contract. Otherwise consumes the single character
    /// and returns immediately (the lexer's outer loop provides the final
    /// advance).
    ///
    /// # Errors
    ///
    /// Always succeeds for characters passing [`can_parse`](TokenParser::can_parse).
    ///
    /// # Panics
    ///
    /// Delegates to [`CharacterStream::peek_char`], which panics if the stream
    /// is positioned on the very last character of the input. In practice the
    /// lexer emits an [`EOF`] token before reaching that state.
    pub(super) fn parse_operator(&mut self) -> Result<Token, LexError> {
        let two_token_type = match (self.current_char(), self.peek_char()) {
            (Some('='), Some('=')) => Some(TokenType::Eq),
            (Some('!'), Some('=')) => Some(TokenType::NotEq),
            (Some('<'), Some('=')) => Some(TokenType::LessThanOrEqual),
            (Some('<'), Some('<')) => Some(TokenType::BitwiseLeftShift),
            (Some('>'), Some('=')) => Some(TokenType::GreaterThanOrEqual),
            (Some('>'), Some('>')) => Some(TokenType::BitwiseRightShift),
            (Some('+'), Some('=')) => Some(TokenType::PlusAssign),
            (Some('-'), Some('=')) => Some(TokenType::MinusAssign),
            (Some('*'), Some('=')) => Some(TokenType::AsteriskAssign),
            (Some('/'), Some('=')) => Some(TokenType::SlashAssign),
            (Some('/'), Some('/')) => Some(TokenType::IntDivision),
            (Some('%'), Some('=')) => Some(TokenType::ModulusAssign),
            (Some('&'), Some('&')) => Some(TokenType::And),
            (Some('|'), Some('|')) => Some(TokenType::Or),
            _ => None,
        };

        if let Some(two_tt) = two_token_type {
            let literal = format!(
                "{}{}",
                self.current_char().unwrap(),
                self.peek_char().unwrap()
            );
            self.advance();
            self.advance();
            return Ok(Self::create_token(two_tt, literal, self.line_col));
        }

        let one_token_type = match self.current_char() {
            Some('=') => Some(TokenType::Assign),
            Some('!') => Some(TokenType::Bang),
            Some('<') => Some(TokenType::LessThan),
            Some('>') => Some(TokenType::GreaterThan),
            Some('+') => Some(TokenType::Plus),
            Some('-') => Some(TokenType::Minus),
            Some('*') => Some(TokenType::Asterisk),
            Some('/') => Some(TokenType::Slash),
            Some('%') => Some(TokenType::Modulus),
            Some('&') => Some(TokenType::BitwiseAnd),
            Some('|') => Some(TokenType::BitwiseOr),
            Some('^') => Some(TokenType::BitwiseXor),
            Some('~') => Some(TokenType::BitwiseNot),
            Some(';') => Some(TokenType::Semicolon),
            Some(',') => Some(TokenType::Comma),
            Some(':') => Some(TokenType::Colon),
            Some('.') => Some(TokenType::Dot),
            Some('(') => Some(TokenType::LParen),
            Some(')') => Some(TokenType::RParen),
            Some('{') => Some(TokenType::LBrace),
            Some('}') => Some(TokenType::RBrace),
            Some('[') => Some(TokenType::LBracket),
            Some(']') => Some(TokenType::RBracket),
            _ => {
                let literal = self.current_char().unwrap().to_string();
                let pos = self.line_col;
                self.advance();
                return Ok(Self::create_token(TokenType::Illegal, literal, pos));
            }
        };

        if let Some(token_type) = one_token_type {
            let literal = self.current_char().unwrap().to_string();
            self.advance();
            return Ok(Self::create_token(token_type, literal, self.line_col));
        }
        unreachable!()
    }

    /// Consume an f-string literal and return a [`TokenType::FString`] token.
    ///
    /// Skips the `f` prefix and the opening `"` before collecting body
    /// characters. Tracks brace depth to allow `{` and `}` inside interpolation
    /// expressions. A `}` that would bring `brace_depth` below zero is treated
    /// as an unmatched brace and reported as [`LexError::UnterminatedString`].
    /// After the closing `"` is found, a non-zero `brace_depth` is reported as
    /// [`LexError::UnclosedBrace`].
    ///
    /// # Errors
    ///
    /// | Condition | Error |
    /// |-----------|-------|
    /// | `}` at brace depth 0 | [`LexError::UnterminatedString`] (see note below) |
    /// | EOF before closing `"` | [`LexError::UnterminatedFString`] |
    /// | Unclosed `{` at closing `"` | [`LexError::UnclosedBrace`] |
    ///
    /// > **Note:** The unmatched-`}` case currently emits `UnterminatedString`
    /// > rather than [`LexError::UnmatchedBrace`]. This appears to be a bug —
    /// > flag for review.
    pub(super) fn parse_f_string(&mut self) -> Result<Token, LexError> {
        // Skip the 'f' prefix and the opening '"'
        self.advance();
        self.advance();

        let mut s = String::with_capacity(INITIAL_STRING_CAPACITY);
        let mut brace_depth = 0;

        loop {
            match self.current_char() {
                Some(ch) if ch == '"' && brace_depth == 0 => {
                    self.advance();
                    return Ok(Self::create_token(TokenType::FString, s, self.line_col));
                }
                Some(ch) => {
                    match ch {
                        LEFT_BRACE => {
                            brace_depth += 1;
                            s.push(ch);
                        }
                        RIGHT_BRACE => {
                            if brace_depth == 0 {
                                return Err(LexError::UnmatchedBrace {
                                    line: self.line_col.0,
                                    col: self.line_col.1,
                                });
                            }
                            brace_depth -= 1;
                            s.push(ch);
                        }
                        '\\' => {
                            self.advance();
                            match self.current_char() {
                                Some(escaped) => {
                                    s.push(Self::handle_escape(escaped));
                                }
                                None => {
                                    return Err(LexError::UnterminatedFString {
                                        line: self.line_col.0,
                                        col: self.line_col.1,
                                    });
                                }
                            }
                        }
                        _ => s.push(ch),
                    }
                    self.advance();
                }
                None => {
                    return Err(LexError::UnterminatedFString {
                        line: self.line_col.0,
                        col: self.line_col.1,
                    });
                }
            }
        }
    }
}
