//! Statement-level parsing for Mutant Lang.
//!
//! This module extends [`Parser`] with methods that recognise and consume
//! statement-level constructs: variable bindings (`let`, `const`), control
//! flow (`return`, `break`, `continue`), loops (`for`, `while`), block
//! bodies, and expression statements.
//!
//! Each `parse_*` method is `pub(super)` so that only [`super::Parser`]'s
//! dispatch table in [`Parser::parse_statement`] can reach them.  Private
//! helpers (`parse_loop_control`, `parse_assignment`) are shared
//! internally to avoid duplication between symmetric statement kinds.
//!
//! # Parser state invariants
//!
//! All methods assume that `self.current_token` points to the first token
//! of the construct being parsed when the method is entered, and that
//! `self.peek_token` holds the look-ahead.  Every method leaves
//! `self.current_token` pointing to the first token of the *next*
//! unconsumed construct.
//!
//! Loop-depth tracking (`self.loop_depth`) is incremented by
//! [`Parser::enter_loop`] before parsing a loop body and decremented by
//! [`Parser::exit_loop`] immediately after, ensuring that `break` and
//! `continue` can only appear inside a loop at parse time.

use crate::ast::expression::{Expression, Indentifier};
use crate::ast::statements::Statement;
use crate::ast::TokenSpan;
use crate::token::{Operator, TokenType};

use super::precedence::Precedence;
use super::{ParseError, ParseResult, Parser};

impl Parser {
    /// Parse a loop-control statement (`break` or `continue`) using a
    /// caller-supplied constructor.
    ///
    /// This is a private generic helper that factors out the logic shared by
    /// [`parse_break`](Self::parse_break) and
    /// [`parse_continue`](Self::parse_continue).  It:
    ///
    /// 1. Rejects the keyword with [`ParseError::NotInLoop`] when it appears
    ///    outside any loop (i.e. when `self.loop_depth == 0`).
    /// 2. Consumes the `keyword` token.
    /// 3. Consumes the mandatory trailing semicolon.
    /// 4. Delegates node construction to `constructor`, passing the resulting
    ///    [`TokenSpan`].
    ///
    /// The type parameter `T` is the AST node type returned (typically
    /// `Statement`).  `U` is a `FnOnce` so that the closure can take
    /// ownership of the span without copying.
    ///
    /// # Errors
    ///
    /// - [`ParseError::NotInLoop`] — `keyword` appeared at loop depth 0.
    /// - [`ParseError::UnexpectedToken`] — the token following `keyword` was
    ///   not a semicolon.
    /// - [`ParseError::UnexpectedEof`] — input ended before the semicolon.
    fn parse_loop_control<T, U>(
        &mut self,
        keyword: TokenType,
        constructor: U,
    ) -> Result<T, ParseError>
    where
        U: FnOnce(TokenSpan) -> T,
    {
        if self.loop_depth == 0 {
            return Err(ParseError::not_in_loop(
                self.current_token.as_ref().unwrap(),
            ));
        }
        let start = self.consume(keyword)?;
        let end = self.consume(TokenType::Semicolon)?;
        Ok(constructor((start, end.position).into()))
    }

    /// Parse a `break;` statement.
    ///
    /// Delegates to [`parse_loop_control`](Self::parse_loop_control) with
    /// [`TokenType::Break`] and [`Statement::break_stmt`] as the constructor.
    /// The resulting node spans from the `break` keyword through the trailing
    /// semicolon.
    ///
    /// # Errors
    ///
    /// - [`ParseError::NotInLoop`] — `break` appeared outside a loop.
    /// - [`ParseError::UnexpectedToken`] — missing semicolon.
    /// - [`ParseError::UnexpectedEof`] — input ended prematurely.
    pub(super) fn parse_break(&mut self) -> ParseResult {
        self.parse_loop_control(TokenType::Break, Statement::break_stmt)
    }

    /// Parse a `continue;` statement.
    ///
    /// Delegates to [`parse_loop_control`](Self::parse_loop_control) with
    /// [`TokenType::Continue`] and [`Statement::continue_stmt`] as the
    /// constructor.  The resulting node spans from the `continue` keyword
    /// through the trailing semicolon.
    ///
    /// # Errors
    ///
    /// - [`ParseError::NotInLoop`] — `continue` appeared outside a loop.
    /// - [`ParseError::UnexpectedToken`] — missing semicolon.
    /// - [`ParseError::UnexpectedEof`] — input ended prematurely.
    pub(super) fn parse_continue(&mut self) -> ParseResult {
        self.parse_loop_control(TokenType::Continue, Statement::continue_stmt)
    }

    /// Parse a `return` statement, with or without a value.
    ///
    /// Two forms are recognised:
    ///
    /// - `return;` — produces a [`Statement::Return`] whose value is a
    ///   synthetic null [`Expression`], so the evaluator never needs to
    ///   special-case a missing return value.
    /// - `return <expr>;` — produces a [`Statement::Return`] whose value is
    ///   the fully-parsed expression.
    ///
    /// The span covers from the `return` token through the semicolon.
    ///
    /// # Errors
    ///
    /// - [`ParseError::UnexpectedToken`] — the token after the expression was
    ///   not a semicolon, or any token mismatch inside `parse_expression`.
    /// - [`ParseError::NoPrefixParser`] — the expression begins with a token
    ///   that has no registered prefix parse function.
    /// - [`ParseError::UnexpectedEof`] — input ended before the semicolon.
    pub(super) fn parse_return(&mut self) -> ParseResult {
        let ret = self.consume(TokenType::Return)?;

        if self.is_curr_token(TokenType::Semicolon) {
            let end = self.consume(TokenType::Semicolon)?;
            let null = Expression::null((ret.clone(), ret.position));
            return Ok(Statement::return_stmt((ret, end.position), null));
        }

        let expr = self.parse_expression(Precedence::Lowest)?;
        let end = self.consume(TokenType::Semicolon)?;
        Ok(Statement::return_stmt((ret, end.position), expr))
    }

    /// Parse a variable-binding statement (`let` or `const`) using a
    /// caller-supplied AST constructor.
    ///
    /// This private helper factors out the logic shared by
    /// [`parse_let`](Self::parse_let) and [`parse_const`](Self::parse_const).
    /// The grammar it accepts is:
    ///
    /// ```text
    /// <keyword> <identifier> = <expr> ;
    /// ```
    ///
    /// Steps:
    /// 1. Consumes `keyword` (either `let` or `const`).
    /// 2. Consumes an identifier token and wraps it in an [`Indentifier`] node.
    /// 3. Consumes the `=` assign token.
    /// 4. Parses the right-hand-side expression at the lowest precedence.
    /// 5. Consumes the trailing semicolon.
    /// 6. Calls `constructor` with the full span, identifier, and expression.
    ///
    /// The span passed to `constructor` covers from `keyword` through the
    /// semicolon via the [`Into<TokenSpan>`] conversion on a `(Token,
    /// TokenPosition)` tuple.
    ///
    /// # Errors
    ///
    /// - [`ParseError::UnexpectedToken`] — expected identifier, `=`, or `;`
    ///   but found something else.
    /// - [`ParseError::NoPrefixParser`] — the RHS expression starts with an
    ///   unparseable token.
    /// - [`ParseError::UnexpectedEof`] — input ended mid-statement.
    fn parse_assignment<U>(&mut self, keyword: TokenType, constructor: U) -> ParseResult
    where
        U: FnOnce(TokenSpan, Indentifier, Expression) -> Statement,
    {
        let keyword = self.consume(keyword)?;
        let name = self.consume(TokenType::Identifier)?;

        let ident = Indentifier::new((name.clone(), name.position));
        self.consume(TokenType::Assign)?;

        let value = self.parse_expression(Precedence::Lowest)?;
        let end = self.consume(TokenType::Semicolon)?;

        Ok(constructor((keyword, end.position).into(), ident, value))
    }

    /// Parse a `let` binding: `let <name> = <expr>;`.
    ///
    /// Wraps [`parse_assignment`](Self::parse_assignment) with
    /// [`TokenType::Let`] and [`Statement::let_stmt`].  The bound name is
    /// treated as mutable by the evaluator.
    ///
    /// # Errors
    ///
    /// See [`parse_assignment`](Self::parse_assignment) for the full error
    /// surface.
    pub(super) fn parse_let(&mut self) -> ParseResult {
        self.parse_assignment(TokenType::Let, Statement::let_stmt)
    }

    /// Parse a `const` binding: `const <name> = <expr>;`.
    ///
    /// Wraps [`parse_assignment`](Self::parse_assignment) with
    /// [`TokenType::Const`] and [`Statement::const_stmt`].  The evaluator is
    /// expected to reject reassignment to names bound with `const`.
    ///
    /// # Errors
    ///
    /// See [`parse_assignment`](Self::parse_assignment) for the full error
    /// surface.
    pub(super) fn parse_const(&mut self) -> ParseResult {
        self.parse_assignment(TokenType::Const, Statement::const_stmt)
    }

    /// Parse a braced block of statements: `{ <stmt>* }`.
    ///
    /// Consumes the opening `{`, repeatedly calls
    /// [`parse_statement`](Parser::parse_statement) until a closing `}` or
    /// EOF is reached, then consumes the `}`.  Reaching EOF before `}` does
    /// not itself produce an error from this method — the subsequent
    /// [`consume`](Parser::consume) of `}` will return
    /// [`ParseError::UnexpectedEof`].
    ///
    /// The returned [`Statement::Block`] node spans from `{` through `}` and
    /// owns the `Vec` of inner statements.
    ///
    /// This method is used by [`parse_for`](Self::parse_for),
    /// [`parse_while`](Self::parse_while), and (once implemented) function
    /// and if-expression bodies.
    ///
    /// # Errors
    ///
    /// - [`ParseError::UnexpectedToken`] — the first token was not `{`.
    /// - Any error propagated from parsing an inner statement.
    /// - [`ParseError::UnexpectedEof`] — the block was not closed before EOF.
    pub(super) fn parse_block(&mut self) -> ParseResult {
        let start = self.consume(TokenType::LBrace)?;
        let mut statements = Vec::new();

        loop {
            if self.is_curr_token(TokenType::RBrace) || self.is_curr_token(TokenType::Eof) {
                break;
            }

            let stmt = self.parse_statement()?;
            statements.push(stmt);
        }

        let end = self.consume(TokenType::RBrace)?;
        Ok(Statement::block((start, end.position), statements))
    }

    /// Parse a C-style `for` loop: `for (<let>; <cond>; <incr>) <block>`.
    ///
    /// The grammar accepted is:
    ///
    /// ```text
    /// for ( let <name> = <expr> ; <cond-expr> ; <incr-expr> ) <block>
    /// ```
    ///
    /// Notable design choices:
    ///
    /// - The initializer is **always** a `let` binding parsed via
    ///   [`parse_let`](Self::parse_let).  A bare expression initializer or a
    ///   `const` are not currently supported.
    /// - Loop depth is incremented via [`enter_loop`](Parser::enter_loop)
    ///   before the body is parsed and decremented via
    ///   [`exit_loop`](Parser::exit_loop) immediately after, so `break` and
    ///   `continue` are valid inside the body.
    /// - The body is always a [`Statement::Block`].  The irrefutable
    ///   `let … else { unreachable!() }` pattern makes this invariant
    ///   explicit: if [`parse_block`](Self::parse_block) ever returned a
    ///   non-`Block` variant, it would be a programmer error, not a user
    ///   error.
    ///
    /// # Errors
    ///
    /// - [`ParseError::UnexpectedToken`] — any consumed token did not match
    ///   the expected kind (`for`, `(`, `;`, `)`).
    /// - Any error from [`parse_let`](Self::parse_let),
    ///   [`parse_expression`](Parser::parse_expression), or
    ///   [`parse_block`](Self::parse_block).
    /// - [`ParseError::UnexpectedEof`] — input ended mid-statement.
    pub(super) fn parse_for(&mut self) -> ParseResult {
        let for_token = self.consume(TokenType::For)?;
        self.consume(TokenType::LParen)?;

        let init = self.parse_let()?;
        let condition = self.parse_expression(Precedence::Lowest)?;

        self.consume(TokenType::Semicolon)?;

        let increment = self.parse_expression(Precedence::Lowest)?;
        self.consume(TokenType::RParen)?;

        self.enter_loop();
        let body = self.parse_block()?;
        self.exit_loop();

        let end_pos = *body.end_position();
        let Statement::Block(block) = body else {
            unreachable!("parse_block always returns Statement::Block")
        };

        Ok(Statement::for_stmt(
            (for_token, end_pos),
            init,
            condition,
            increment,
            block,
        ))
    }

    /// Parse a `while` loop: `while (<cond>) <block>`.
    ///
    /// The grammar accepted is:
    ///
    /// ```text
    /// while ( <cond-expr> ) <block>
    /// ```
    ///
    /// Loop depth is managed identically to [`parse_for`](Self::parse_for):
    /// incremented before parsing the body, decremented after.  The same
    /// `let … else { unreachable!() }` guard enforces that the body is always
    /// a `Block` variant.
    ///
    /// # Errors
    ///
    /// - [`ParseError::UnexpectedToken`] — `while`, `(`, or `)` was missing.
    /// - Any error from [`parse_expression`](Parser::parse_expression) or
    ///   [`parse_block`](Self::parse_block).
    /// - [`ParseError::UnexpectedEof`] — input ended mid-statement.
    pub(super) fn parse_while(&mut self) -> ParseResult {
        let while_token = self.consume(TokenType::While)?;
        self.consume(TokenType::LParen)?;

        let condition = self.parse_expression(Precedence::Lowest)?;
        self.consume(TokenType::RParen)?;

        self.enter_loop();
        let body = self.parse_block()?;
        self.exit_loop();

        let end_pos = *body.end_position();
        let Statement::Block(block) = body else {
            unreachable!("parse_block always returns Statement::Block")
        };

        Ok(Statement::while_stmt(
            (while_token, end_pos),
            condition,
            block,
        ))
    }

    /// Parse an expression statement, currently handling compound-assignment
    /// operators only.
    ///
    /// **Current implementation status: partial / in progress.**
    ///
    /// The method recognises the subset of expression statements that begin
    /// with an identifier followed by a compound-assignment operator
    /// (`+=`, `-=`, `*=`, `/=`, `%=`).  It desugars them into an infix
    /// expression node — for example, `x += 1;` becomes an
    /// `Expression::Infix(x, +, 1)` wrapped in `Statement::Expression` —
    /// matching the semantics used by the Java reference implementation.
    ///
    /// The desugaring steps are:
    /// 1. Consume the identifier and build a left-hand [`Expression::Identifier`].
    /// 2. Inspect `peek_token` to determine the compound operator and derive
    ///    the underlying binary [`Operator`] (e.g. `PlusAssign` → `Plus`).
    /// 3. If the peek token is not a recognised compound-assign operator, a
    ///    [`ParseError::UnexpectedToken`] (expecting `=`) is returned.  This
    ///    is a placeholder behaviour — plain assignment and standalone
    ///    expression statements are not yet handled.
    /// 4. Parse the right-hand expression, consume the semicolon, and
    ///    construct the infix expression.
    ///
    /// # Errors
    ///
    /// - [`ParseError::UnexpectedToken`] (expected `=`) — the identifier was
    ///   followed by a token that is not a compound-assignment operator.
    /// - [`ParseError::UnexpectedEof`] — `peek_token` was `None` while
    ///   inspecting the operator.
    /// - Any error from [`parse_expression`](Parser::parse_expression).
    ///
    /// # Panics
    ///
    /// Panics via `todo!()` for any expression statement that does not match
    /// the compound-assignment pattern described above.
    pub(super) fn parse_expression_stmt(&mut self) -> ParseResult {
        let start = self.consume(TokenType::Identifier)?;
        if self.is_curr_token(TokenType::Identifier) && self.peek_token.is_some() {
            let left = Expression::identifier((start.clone(), start.position));

            let (peek_kind, peek_pos) = self
                .peek_token
                .as_ref()
                .map(|t| (t.kind, t.position))
                .ok_or(ParseError::UnexpectedEof)?;

            let base_op = match peek_kind {
                TokenType::PlusAssign => Operator::Plus,
                TokenType::MinusAssign => Operator::Minus,
                TokenType::AsteriskAssign => Operator::Asterisk,
                TokenType::SlashAssign => Operator::Slash,
                TokenType::ModulusAssign => Operator::Modulus,
                _ => {
                    return Err(ParseError::UnexpectedToken {
                        expected: TokenType::Assign,
                        got: peek_kind,
                        position: peek_pos,
                    })
                }
            };

            let right = self.parse_expression(Precedence::Lowest)?;
            let end = self.consume(TokenType::Semicolon)?;

            let expr = Expression::infix((start.clone(), end.position), left, right, base_op);
            return Ok(Statement::expression_stmt((start, end.position), expr));
        }

        let expr = self.parse_expression(Precedence::Lowest)?;
        if self.is_curr_token(TokenType::Semicolon) {
            let end = self.consume(TokenType::Semicolon)?;
            return Ok(Statement::expression_stmt((start, end.position), expr));
        }

        Ok(Statement::expression_stmt(
            (start, *expr.end_position()),
            expr,
        ))
    }
}
