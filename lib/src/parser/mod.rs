use crate::ast::statements::Statement;
use crate::lexer::LexError;
use crate::lexer::Lexer;
use crate::token::{Token, TokenPosition, TokenType};

pub mod expressions;
pub mod precedence;
pub mod statements;

use thiserror::Error;

type ParseResult = Result<Statement, ParseError>;

#[derive(Debug, Error)]
pub enum ParseError {
    #[error("expected {expected}, got {got} at {position}")]
    UnexpectedToken {
        expected: TokenType,
        got: TokenType,
        position: TokenPosition,
    },

    #[error("no prefix parse function for {token_type} at {position}")]
    NoPrefixParser {
        token_type: TokenType,
        position: TokenPosition,
    },

    #[error("unexpected end of input")]
    UnexpectedEof,

    #[error("break or continue not in loop at {position}")]
    NotInLoop {
        token_type: TokenType,
        position: TokenPosition,
    },

    #[error("invalid integer literal '{literal}' at {position}")]
    InvalidIntLiteral {
        literal: String,
        position: TokenPosition,
    },

    #[error("expected a block '{{' at {position}")]
    ExpectedBlock { position: TokenPosition },

    #[error("invalid float literal '{literal}' at {position}")]
    InvalidFloatLiteral {
        literal: String,
        position: TokenPosition,
    },

    #[error("lex error: {0}")]
    Lex(#[from] LexError),
}

impl ParseError {
    #[must_use]
    pub(crate) const fn unexpected_token(expected: TokenType, got: &Token) -> Self {
        Self::UnexpectedToken {
            expected,
            got: got.kind,
            position: got.position,
        }
    }

    #[must_use]
    pub(crate) const fn no_prefix_parser(token: &Token) -> Self {
        Self::NoPrefixParser {
            token_type: token.kind,
            position: token.position,
        }
    }

    #[must_use]
    pub(crate) const fn not_in_loop(token: &Token) -> Self {
        Self::NotInLoop {
            token_type: token.kind,
            position: token.position,
        }
    }

    #[must_use]
    pub(crate) const fn expected_block(token: &Token) -> Self {
        Self::ExpectedBlock {
            position: token.position,
        }
    }
}

pub struct Parser {
    lexer: Lexer,
    loop_depth: usize,
    errors: Vec<ParseError>,
    current_token: Option<Token>,
    peek_token: Option<Token>,
}

impl Parser {
    /// Create a new `Parser` from a `Lexer` and prime the two-token look-ahead.
    ///
    /// Calls [`advance`](Self::advance) twice so that both `current_token` and
    /// `peek_token` are populated before the first call to
    /// [`parse_program`](Self::parse_program).
    ///
    /// # Errors
    ///
    /// Returns [`ParseError::Lex`] if the lexer emits a [`LexError`] while
    /// loading either of the two priming tokens.
    pub fn new(lexer: Lexer) -> Result<Self, ParseError> {
        let mut parser = Self {
            lexer,
            current_token: None,
            peek_token: None,
            loop_depth: 0,
            errors: Vec::new(),
        };
        parser.advance()?;
        parser.advance()?;
        Ok(parser)
    }

    /// Skip tokens until a statement boundary for error recovery.
    fn synchronize(&mut self) {
        loop {
            match self.current_token.as_ref().map(|t| t.kind) {
                None
                | Some(
                    TokenType::Eof
                    | TokenType::Let
                    | TokenType::Const
                    | TokenType::Return
                    | TokenType::While
                    | TokenType::For,
                ) => return,
                Some(TokenType::Semicolon) => {
                    let _ = self.advance();
                    return;
                }
                _ => {
                    let _ = self.advance();
                }
            }
        }
    }

    /// Parse all statements until EOF, returning them alongside any parse errors.
    ///
    /// This is the top-level entry point you call from a REPL or file runner.
    /// On a parse error it records the error, calls `synchronize` to skip to
    /// the next statement boundary, and continues — so a single bad line does
    /// not silence everything that follows.
    ///
    /// `std::mem::take` drains `self.errors` into the returned `Vec` and leaves
    /// an empty `Vec` in place, so the parser could theoretically be reused.
    pub fn parse_program(&mut self) -> (Vec<Statement>, Vec<ParseError>) {
        let mut statements = Vec::new();

        while self
            .current_token
            .as_ref()
            .is_some_and(|t| t.kind != TokenType::Eof)
        {
            match self.parse_statement() {
                Ok(stmt) => statements.push(stmt),
                Err(e) => {
                    self.errors.push(e);
                    self.synchronize();
                }
            }
        }

        (statements, std::mem::take(&mut self.errors))
    }

    fn parse_statement(&mut self) -> ParseResult {
        match self.current_token.as_ref().map(|t| t.kind) {
            Some(TokenType::Let) => self.parse_let(),
            Some(TokenType::Const) => self.parse_const(),
            Some(TokenType::Return) => self.parse_return(),
            Some(TokenType::Break) => self.parse_break(),
            Some(TokenType::Continue) => self.parse_continue(),
            Some(TokenType::While) => self.parse_while(),
            Some(TokenType::For) => self.parse_for(),
            _ => self.parse_expression_stmt(),
        }
    }

    fn advance(&mut self) -> Result<(), ParseError> {
        self.current_token = self.peek_token.take();
        self.peek_token = Some(self.lexer.next_token()?);
        Ok(())
    }

    fn is_curr_token(&self, kind: TokenType) -> bool {
        self.current_token
            .as_ref()
            .is_some_and(|token| token.kind == kind)
    }

    fn consume(&mut self, expected: TokenType) -> Result<Token, ParseError> {
        match &self.current_token {
            None => Err(ParseError::UnexpectedEof),
            Some(t) if t.kind != expected => Err(ParseError::unexpected_token(expected, t)),
            _ => {
                let token = self.current_token.take().unwrap();
                self.advance()?;
                Ok(token)
            }
        }
    }

    const fn enter_loop(&mut self) {
        self.loop_depth += 1;
    }

    const fn exit_loop(&mut self) {
        self.loop_depth -= 1;
    }
}
