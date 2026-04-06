//! Expression parsing for the Mutant Lang parser.
//!
//! This module implements the expression sub-grammar of the Pratt (top-down
//! operator precedence) parser.  Every method here hangs off [`Parser`] and is
//! restricted to `pub(super)` or private visibility — callers outside the
//! `parser` module drive parsing through [`Parser::parse_statement`] in
//! `mod.rs`.
//!
//! ## Pratt parsing model
//!
//! The central entry point is [`Parser::parse_expression`] (defined in
//! `mod.rs`).  It delegates to two families of functions defined here:
//!
//! * **Prefix parsers** — [`parse_prefix_expression`](Parser::parse_prefix_expression)
//!   is the "nud" (null denotation) dispatch table.  It is called when a token
//!   appears at the *start* of an expression position (no left-hand side yet).
//! * **Infix parsers** — [`parse_infix_expression`](Parser::parse_infix_expression)
//!   and [`parse_call_expression`](Parser::parse_call_expression) are the "led"
//!   (left denotation) parsers.  They are called when a token appears *after* a
//!   complete left sub-expression.
//!
//! The loop in `parse_expression` calls the infix parser repeatedly as long as
//! the next token's [`Precedence`] exceeds the current precedence level,
//! building up a left-recursive expression tree bottom-up.
//!
//! ## Source span tracking
//!
//! Every `Expression` variant carries a [`TokenSpan`] that records the opening
//! token (which anchors the `start` position) and the closing token's
//! [`TokenPosition`] (the `end` position).  The helpers here are careful to
//! capture the end position from the *last consumed token* so the span covers
//! the full syntactic extent of each expression.

use core::str;

use crate::ast::TokenSpan;
use crate::ast::expression::{Expression, Indentifier};
use crate::ast::statements::Statement;
use crate::evaluator::EvalError;
use crate::lexer::Lexer;
use crate::token::{Operator, Token, TokenType};

use super::precedence::Precedence;
use super::{ParseError, Parser};

// Prefix and infix expression parsing
impl Parser {
    pub(crate) fn parse_expression(
        &mut self,
        precedence: Precedence,
    ) -> Result<Expression, ParseError> {
        let mut left = self.parse_prefix_expression()?;

        loop {
            let curr_precedence = self.current_token.as_ref().map(|t| Precedence::of(t.kind));

            if curr_precedence.is_none() || precedence >= curr_precedence.unwrap() {
                break;
            }

            left = match self.current_token.as_ref().map(|t| t.kind) {
                Some(TokenType::LParen) => self.parse_call_expression(left)?,
                Some(TokenType::LBracket) => self.parse_index_expression(left)?,
                _ => self.parse_infix_expression(left)?,
            };
        }

        Ok(left)
    }

    /// Dispatch to the appropriate prefix parse function for the current token.
    ///
    /// Acts as the "nud" (null denotation) table of the Pratt parser.  The
    /// current token is inspected and forwarded to the specialized sub-parser
    /// that knows how to build the corresponding [`Expression`] variant.
    ///
    /// Tokens that have no prefix position (e.g. a stray binary operator) map
    /// to [`ParseError::NoPrefixParser`].  A missing token produces
    /// [`ParseError::UnexpectedEof`].
    ///
    /// # Errors
    ///
    /// * [`ParseError::NoPrefixParser`] — the current token cannot start an
    ///   expression (e.g. a lone `+`).
    /// * [`ParseError::UnexpectedEof`] — the token stream ended before an
    ///   expression was expected.
    /// * Any error propagated from the specialised sub-parsers below.
    pub(super) fn parse_prefix_expression(&mut self) -> Result<Expression, ParseError> {
        match self.current_token.as_ref().map(|t| t.kind) {
            Some(TokenType::Int) => self.parse_int(),
            Some(TokenType::True | TokenType::False) => {
                self.parse_single_literal(Expression::boolean)
            }
            Some(TokenType::Bang | TokenType::Minus) => self.parse_prefix_operator(),
            Some(TokenType::String) => self.parse_single_literal(Expression::string),
            Some(TokenType::This) => self.parse_single_literal(Expression::this),
            Some(TokenType::Null) => self.parse_single_literal(Expression::null),
            Some(TokenType::Identifier) => self.parse_single_literal(Expression::identifier),
            Some(TokenType::LBracket) => self.parse_array_literal(),
            Some(TokenType::New) => self.parse_new_expr(),
            Some(TokenType::Super) => self.parse_super_expr(),
            Some(TokenType::Function) => self.parse_function_literal(),
            Some(TokenType::If) => self.parse_if_expression(),
            Some(TokenType::FString) => self.parse_f_string_literal(),

            Some(_) => Err(ParseError::no_prefix_parser(
                self.current_token.as_ref().unwrap(),
            )),
            None => Err(ParseError::UnexpectedEof),
        }
    }

    /// Parse a binary infix expression given its already-parsed left operand.
    ///
    /// Acts as the "led" (left denotation) handler for binary operators.  On
    /// entry, `self.current_token` holds the operator token.  The method:
    ///
    /// 1. Takes ownership of the operator token (removing it from
    ///    `current_token` via `take()`).
    /// 2. Records the operator's [`Precedence`] so the recursive call to
    ///    [`parse_expression`](Parser::parse_expression) applies the correct
    ///    binding power to the right operand.
    /// 3. Converts the `TokenType` to an [`Operator`] value stored inside the
    ///    resulting [`Expression::Infix`] node.
    ///
    /// The span of the produced node runs from the operator token's start
    /// position to the end position of the right-hand sub-expression.
    ///
    /// # Errors
    ///
    /// * [`ParseError::NoPrefixParser`] — the operator token cannot be
    ///   converted to an [`Operator`] (should not happen in practice if the
    ///   precedence table is consistent with the operator set).
    /// * Any error returned by the recursive [`parse_expression`](Parser::parse_expression) call.
    pub(super) fn parse_infix_expression(
        &mut self,
        left: Expression,
    ) -> Result<Expression, ParseError> {
        let op_token = self.current_token.take().unwrap();
        let precedence = Precedence::of(op_token.kind);

        let op = Operator::try_from(op_token.kind)
            .map_err(|_| ParseError::no_prefix_parser(&op_token))?;

        self.advance()?;

        let right = self.parse_expression(precedence)?;
        Ok(Expression::infix(
            (op_token, *right.end_position()),
            left,
            right,
            op,
        ))
    }

    /// Parse the current single-token literal using `constructor` to build the [`Expression`].
    ///
    /// Factors out the repetitive pattern shared by all single-token literal
    /// types (booleans, strings, `null`, `this`, identifiers):
    ///
    /// 1. Takes the current token out of `self.current_token`.
    /// 2. Records its position as both the start and end of the span (the
    ///    token occupies exactly one position).
    /// 3. Advances the parser to the next token.
    /// 4. Calls `constructor` with the resulting [`TokenSpan`].
    ///
    /// The `constructor` parameter is a *function pointer* (or any `FnOnce`
    /// closure) — in practice the callers pass associated functions like
    /// [`Expression::boolean`] or [`Expression::identifier`] directly, which
    /// avoids an extra allocation while keeping the dispatch table in
    /// [`parse_prefix_expression`](Parser::parse_prefix_expression) terse.
    ///
    /// # Errors
    ///
    /// * Any error returned by [`advance`](Parser::advance), which calls the
    ///   lexer and may surface a [`ParseError::Lex`] variant.
    fn parse_single_literal<F>(&mut self, constructor: F) -> Result<Expression, ParseError>
    where
        F: FnOnce(TokenSpan) -> Expression,
    {
        let tok = self.current_token.take().unwrap(); // safe — we're in a Some(_) arm
        let end = tok.position;
        self.advance()?;
        Ok(constructor(TokenSpan::new(tok, end)))
    }

    /// Parse a delimiter-enclosed, comma-separated list of items.
    ///
    /// This is the shared engine behind every comma-separated construct in the
    /// grammar: parameter lists, argument lists, array literals, etc.  The
    /// generic parameters keep it fully type-safe while eliminating boilerplate.
    ///
    /// ## Parameters
    ///
    /// * `start_token` — the opening delimiter (e.g. `(` or `[`).
    /// * `end_token` — the closing delimiter (e.g. `)` or `]`).
    /// * `parse_item` — a `FnMut` closure that parses a single list item and
    ///   advances the parser past it.  Using `FnMut` (rather than `Fn`) allows
    ///   the closure to carry mutable state if needed, and lets it borrow
    ///   `self` mutably via the `&mut Self` argument each time it is invoked.
    ///
    /// ## Algorithm
    ///
    /// 1. Consume `start_token`.
    /// 2. Loop until the current token is `end_token`:
    ///    a. Call `parse_item` and push the result.
    ///    b. If the stream ended unexpectedly, return [`ParseError::UnexpectedEof`].
    ///    c. Require either a `,` or `end_token`; anything else is
    ///    [`ParseError::UnexpectedToken`].
    ///    d. If a `,` is present, consume it (trailing commas are **not**
    ///    accepted — the comma must be followed by another item or the loop
    ///    exits when `end_token` is seen after the last item).
    /// 3. Consume `end_token`.
    ///
    /// The opening and closing delimiter tokens are returned alongside the item
    /// vector so callers can use their positions to build accurate [`TokenSpan`]s.
    ///
    /// # Errors
    ///
    /// * [`ParseError::UnexpectedToken`] — `start_token` is not current, or a
    ///   separator other than `,` or `end_token` is found between items.
    /// * [`ParseError::UnexpectedEof`] — the token stream ends before
    ///   `end_token` is reached.
    /// * Any error propagated from `parse_item`.
    fn parse_delimited_list<T, F>(
        &mut self,
        start_token: TokenType,
        end_token: TokenType,
        mut parse_item: F,
    ) -> Result<(Token, Vec<T>, Token), ParseError>
    where
        F: FnMut(&mut Self) -> Result<T, ParseError>,
    {
        let start = self.consume(start_token)?;
        let mut items = Vec::new();

        while !self.is_curr_token(end_token) {
            items.push(parse_item(self)?);

            if self.current_token.is_none() {
                return Err(ParseError::UnexpectedEof);
            }

            if !self.is_curr_token(TokenType::Comma) && !self.is_curr_token(end_token) {
                return Err(ParseError::unexpected_token(
                    TokenType::Comma,
                    self.current_token.as_ref().unwrap(),
                ));
            }

            if self.is_curr_token(TokenType::Comma) {
                self.consume(TokenType::Comma)?;
            }
        }

        let end = self.consume(end_token)?;
        Ok((start, items, end))
    }

    /// Parse a delimiter-enclosed, comma-separated list of expressions.
    ///
    /// A thin specialization of [`parse_delimited_list`](Self::parse_delimited_list)
    /// where every item is an [`Expression`] parsed at [`Precedence::Lowest`].
    /// This covers argument lists `(a, b + c, fn() { … })` and array element
    /// lists `[1, 2, x]`.
    ///
    /// Returns the opening delimiter token, the parsed expressions, and the
    /// closing delimiter token so callers can construct accurate spans.
    ///
    /// # Errors
    ///
    /// Propagates all errors from [`parse_delimited_list`](Self::parse_delimited_list)
    /// and from each [`parse_expression`](Parser::parse_expression) call.
    fn parse_expressions_list(
        &mut self,
        start_token: TokenType,
        end_token: TokenType,
    ) -> Result<(Token, Vec<Expression>, Token), ParseError> {
        self.parse_delimited_list(start_token, end_token, |p| {
            p.parse_expression(Precedence::Lowest)
        })
    }

    /// Parse a function or method call expression `callee(arg, …)`.
    ///
    /// Called by the infix dispatch in `mod.rs` when `(` is encountered after
    /// an expression — the already-parsed callee expression is passed in as
    /// `left`.  Delegates argument parsing to
    /// [`parse_expressions_list`](Self::parse_expressions_list) with `(` / `)`
    /// delimiters.
    ///
    /// The produced [`Expression::Call`] node spans from the `(` to the
    /// matching `)`.
    ///
    /// # Errors
    ///
    /// Propagates errors from [`parse_expressions_list`](Self::parse_expressions_list).
    fn parse_call_expression(&mut self, left: Expression) -> Result<Expression, ParseError> {
        let (l_paren, args, r_paren) =
            self.parse_expressions_list(TokenType::LParen, TokenType::RParen)?;
        Ok(Expression::call((l_paren, r_paren.position), left, args))
    }

    /// Parse an array literal `[elem, …]`.
    ///
    /// Delegates entirely to [`parse_expressions_list`](Self::parse_expressions_list)
    /// with `[` / `]` delimiters.  An empty array `[]` is valid and produces an
    /// [`Expression::Literal`] (array variant) with an empty element list.
    ///
    /// The node span runs from the opening `[` to the closing `]`.
    ///
    /// # Errors
    ///
    /// Propagates errors from [`parse_expressions_list`](Self::parse_expressions_list).
    pub(super) fn parse_array_literal(&mut self) -> Result<Expression, ParseError> {
        let (l_brace, elements, r_brace) =
            self.parse_expressions_list(TokenType::LBracket, TokenType::RBracket)?;
        Ok(Expression::array((l_brace, r_brace.position), elements))
    }

    /// Parse a `new` expression `new ClassName(arg, …)`.
    ///
    /// Syntax:
    /// ```text
    /// new  class_expr  (  arg , …  )
    /// ```
    ///
    /// The class name is itself parsed as an expression at
    /// [`Precedence::Call`], which allows member-access chains like
    /// `new Pkg.ClassName(…)` to be handled without a separate grammar rule.
    ///
    /// The node span runs from the `new` keyword to the closing `)` of the
    /// argument list.
    ///
    /// # Errors
    ///
    /// * [`ParseError::UnexpectedToken`] — `new` is not the current token, or
    ///   no `(` follows the class expression.
    /// * Any error from [`parse_expression`](Parser::parse_expression) while
    ///   parsing the class name, or from
    ///   [`parse_expressions_list`](Self::parse_expressions_list) for the args.
    pub(super) fn parse_new_expr(&mut self) -> Result<Expression, ParseError> {
        let new_tok = self.consume(TokenType::New)?;
        let class_name = self.parse_expression(Precedence::Call)?;

        let (_, args, r_paren) =
            self.parse_expressions_list(TokenType::LParen, TokenType::RParen)?;

        Ok(Expression::new_expr(
            (new_tok, r_paren.position),
            class_name,
            args,
        ))
    }

    /// Parse a `super` expression in one of two forms.
    ///
    /// The two accepted syntactic forms are:
    ///
    /// ```text
    /// super(arg, …)            — constructor delegation (method = None)
    /// super.method_expr(arg, …) — inherited method call (method = Some(…))
    /// ```
    ///
    /// After consuming `super`, the parser peeks at the next token to choose
    /// the form:
    ///
    /// * `(` → constructor call form.
    /// * `.` → the dot is consumed, followed by a method expression parsed at
    ///   [`Precedence::Call`], and then an argument list.
    /// * anything else → [`ParseError::UnexpectedToken`] (expected `(`).
    ///
    /// The node span runs from the `super` keyword to the closing `)` of
    /// whichever argument list is parsed.
    ///
    /// # Errors
    ///
    /// * [`ParseError::UnexpectedToken`] — neither `(` nor `.` follows `super`.
    /// * Any error from argument list or sub-expression parsing.
    ///
    /// # Panics
    ///
    /// Panics if `self.current_token` is `None` when the error branch is
    /// reached (i.e. when no `(` or `.` follows `super`), because it calls
    /// `unwrap()` on `current_token`.  This cannot happen in well-formed input
    /// since the lexer always provides an EOF sentinel.
    pub(super) fn parse_super_expr(&mut self) -> Result<Expression, ParseError> {
        let super_tok = self.consume(TokenType::Super)?;

        if self.is_curr_token(TokenType::LParen) {
            let (_, args, r_paren) =
                self.parse_expressions_list(TokenType::LParen, TokenType::RParen)?;
            return Ok(Expression::super_expr(
                (super_tok, r_paren.position),
                None,
                args,
            ));
        }

        if self.is_curr_token(TokenType::Dot) {
            let _ = self.consume(TokenType::Dot)?;
            let method = self.parse_expression(Precedence::Call)?;
            let (_, args, r_paren) =
                self.parse_expressions_list(TokenType::LParen, TokenType::RParen)?;
            return Ok(Expression::super_expr(
                (super_tok, r_paren.position),
                Some(method),
                args,
            ));
        }

        Err(ParseError::unexpected_token(
            TokenType::LParen,
            self.current_token.as_ref().unwrap(),
        ))
    }

    /// Parse a decimal integer literal into an [`Expression::Literal`] (integer variant).
    ///
    /// Consumes the `Int` token and attempts to parse its source text as an
    /// `i64` using the standard library's `str::parse`.  The value is stored
    /// directly inside the AST node so the evaluator does not need to re-parse
    /// the string at runtime.
    ///
    /// The node span is a single-token span (start == end).
    ///
    /// # Errors
    ///
    /// * [`ParseError::InvalidIntLiteral`] — the token's literal text is not a
    ///   valid base-10 `i64` (e.g. overflow, stray characters).  The offending
    ///   text and its source position are included in the error.
    /// * [`ParseError::UnexpectedToken`] — current token is not `Int`.
    fn parse_int(&mut self) -> Result<Expression, ParseError> {
        let tok = self.consume(TokenType::Int)?;
        let end = tok.position;
        let val = tok
            .literal
            .parse::<i64>()
            .map_err(|_| ParseError::InvalidIntLiteral {
                literal: tok.literal.clone(),
                position: tok.position,
            })?;
        Ok(Expression::integer((tok, end), val))
    }

    /// Parse an index expression `collection[index]`.
    ///
    /// Called by the infix dispatch when `[` is encountered after a complete
    /// expression (i.e. `left` is the collection being indexed).  Parses the
    /// inner index expression at [`Precedence::Lowest`] so any expression —
    /// including another infix — is valid as an index.
    ///
    /// The node span runs from the opening `[` to the closing `]`.
    ///
    /// # Errors
    ///
    /// * [`ParseError::UnexpectedToken`] — `[` or `]` is missing.
    /// * Any error from the inner [`parse_expression`](Parser::parse_expression) call.
    fn parse_index_expression(&mut self, left: Expression) -> Result<Expression, ParseError> {
        let l_brace = self.consume(TokenType::LBracket)?;
        let index = self.parse_expression(Precedence::Lowest)?;
        let r_brace = self.consume(TokenType::RBracket)?;
        Ok(Expression::index((l_brace, r_brace.position), left, index))
    }

    /// Parse a unary prefix operator expression (`!expr` or `-expr`).
    ///
    /// Only `!` (logical not) and `-` (arithmetic negation) are accepted as
    /// prefix operators.  Any other token at the current position produces a
    /// [`ParseError::NoPrefixParser`] error.
    ///
    /// The operand is parsed at [`Precedence::Prefix`], which is higher than
    /// most binary operators, ensuring that `-a * b` is parsed as `(-a) * b`
    /// rather than `-(a * b)`.
    ///
    /// The node span runs from the operator token to the end of the operand
    /// sub-expression.
    ///
    /// # Errors
    ///
    /// * [`ParseError::NoPrefixParser`] — the current token is neither `!` nor
    ///   `-`, or the `TokenType` cannot be converted to an [`Operator`].
    /// * Any error from the recursive [`parse_expression`](Parser::parse_expression) call.
    ///
    /// # Panics
    ///
    /// Assumes `self.current_token` is `Some` on entry (called only from
    /// within a match arm that has already confirmed this).
    fn parse_prefix_operator(&mut self) -> Result<Expression, ParseError> {
        let op = self.current_token.as_ref().unwrap();
        if op.kind == TokenType::Bang || op.kind == TokenType::Minus {
            let op_token = self.consume(op.kind)?;
            let right = self.parse_expression(Precedence::Prefix)?;

            let op = Operator::try_from(op_token.kind)
                .map_err(|_| ParseError::no_prefix_parser(&op_token))?;

            return Ok(Expression::prefix(
                (op_token, *right.end_position()),
                op,
                right,
            ));
        }
        Err(ParseError::no_prefix_parser(op))
    }

    /// Parse a `fn` function literal expression.
    ///
    /// Syntax:
    ///
    /// ```text
    /// fn ( param , … ) block
    /// ```
    ///
    /// Function literals are anonymous, first-class values in Mutant Lang —
    /// they can appear in any expression position (assigned to variables,
    /// passed as arguments, returned from other functions, etc.).
    ///
    /// The parsing steps are:
    ///
    /// 1. Consume the `fn` keyword.
    /// 2. Parse the parameter list with [`parse_delimited_list`](Self::parse_delimited_list)
    ///    using `(` / `)` as delimiters.  Each item must be a bare
    ///    [`Identifier`](crate::ast::expression::Indentifier) token — default
    ///    values and rest parameters are not supported.
    /// 3. Parse the body block with [`expect_block`](Self::expect_block), which
    ///    enforces that the body is a `{ … }` block statement.
    ///
    /// The span of the produced [`Expression::Function`] node runs from the
    /// `fn` keyword to the closing `)` of the parameter list.  (The body's
    /// position is separately tracked inside the [`BlockStatement`].)
    ///
    /// # Errors
    ///
    /// * [`ParseError::UnexpectedToken`] — `fn` is not the current token, or
    ///   `(` / `)` are missing around the parameter list, or a parameter is not
    ///   an [`Identifier`](crate::ast::expression::Indentifier).
    /// * [`ParseError::ExpectedBlock`] — the body is not a block statement
    ///   (surfaced by [`expect_block`](Self::expect_block)).
    /// * Any error from [`parse_delimited_list`](Self::parse_delimited_list) or
    ///   [`parse_statement`](Parser::parse_statement).
    fn parse_function_literal(&mut self) -> Result<Expression, ParseError> {
        let fn_token = self.consume(TokenType::Function)?;
        let (_, params, r_paren) =
            self.parse_delimited_list(TokenType::LParen, TokenType::RParen, |p| {
                p.consume(TokenType::Identifier).map(|tok| {
                    let end = tok.position;
                    Indentifier::new((tok, end))
                })
            })?;

        let body = self.expect_block(&fn_token)?;
        Ok(Expression::function(
            (fn_token, r_paren.position),
            params,
            body,
        ))
    }

    /// Parse an `if` / `elif` / `else` expression.
    ///
    /// Syntax (EBNF-like):
    ///
    /// ```text
    /// if ( condition ) block
    ///   ( elif ( condition ) block )*
    ///   ( else block )?
    /// ```
    ///
    /// The result is an [`Expression::If`] whose `conditions` and
    /// `consequences` vectors are parallel: `conditions[i]` governs
    /// `consequences[i]`.  The optional `else` branch is stored separately as
    /// `alternative`.
    ///
    /// This is an *expression* (not a statement) in Mutant Lang, meaning `if`
    /// can appear wherever any value-producing expression is valid — matching
    /// the language's design that every construct returns a value.
    ///
    /// [`expect_block`](Self::expect_block) is used to enforce that each branch
    /// body is a block statement `{ … }` rather than a bare expression.
    ///
    /// # Errors
    ///
    /// * [`ParseError::UnexpectedToken`] — any of `if`, `(`, `)` are missing,
    ///   or a branch body is not a block.
    /// * [`ParseError::ExpectedBlock`] — a branch body parses as a non-block
    ///   statement (surfaced by [`expect_block`](Self::expect_block)).
    /// * Any error from [`parse_expression`](Parser::parse_expression) for a
    ///   condition, or from [`parse_statement`](Parser::parse_statement) for a
    ///   block.
    fn parse_if_expression(&mut self) -> Result<Expression, ParseError> {
        let if_token = self.consume(TokenType::If)?;
        let mut conditions = Vec::new();
        let mut consequences = Vec::new();

        self.consume(TokenType::LParen)?;
        let condition = self.parse_expression(Precedence::Lowest)?;

        self.consume(TokenType::RParen)?;
        let consequence = self.expect_block(&if_token)?;

        conditions.push(condition);
        consequences.push(consequence);

        loop {
            if self.is_curr_token(TokenType::Elif) {
                let elif = self.consume(TokenType::Elif)?;
                self.consume(TokenType::LParen)?;
                let condition = self.parse_expression(Precedence::Lowest)?;
                self.consume(TokenType::RParen)?;
                let consequence = self.expect_block(&elif)?;

                conditions.push(condition);
                consequences.push(consequence);
            } else {
                break;
            }
        }

        let alternative = if self.is_curr_token(TokenType::Else) {
            let else_token = self.consume(TokenType::Else)?;
            let consequence = self.expect_block(&else_token)?;
            Some(consequence)
        } else {
            None
        };

        let end_pos = alternative.as_ref().map_or_else(
            || *consequences.last().unwrap().span.end_pos(),
            |b| *b.span.end_pos(),
        );

        Ok(Expression::if_expr(
            (if_token, end_pos),
            conditions,
            consequences,
            alternative,
        ))
    }

    /// Parse a floating-point literal into an [`Expression::Literal`] (float variant).
    ///
    /// Consumes the `Float` token and normalizes its source text before parsing
    /// so that the Rust standard library's `str::parse::<f64>()` accepts it:
    ///
    /// * A leading `.` (e.g. `.5`) is prefixed with `0` → `0.5`.
    /// * A trailing `.` (e.g. `1.`) is suffixed with `0` → `1.0`.
    ///
    /// After normalization the method also rejects literals that contain more
    /// than one `.` (e.g. `1.2.3`) before delegating to the standard parser,
    /// so the [`ParseError::InvalidFloatLiteral`] variant is returned for both
    /// malformed structure and values that overflow `f64`.
    ///
    /// The parsed `f64` value is embedded directly in the AST node so the
    /// evaluator does not re-parse the source text at runtime.  The node span
    /// is a single-token span (`start == end`).
    ///
    /// # Errors
    ///
    /// * [`ParseError::UnexpectedToken`] — the current token is not `Float`.
    /// * [`ParseError::InvalidFloatLiteral`] — the literal contains more than
    ///   one `.`, or `str::parse::<f64>()` rejects it (e.g. `f64` overflow).
    fn parse_float_literal(&mut self) -> Result<Expression, ParseError> {
        let tok = self.consume(TokenType::Float)?;
        let end = tok.position;

        let mut normalized = tok.literal.clone();

        if normalized.starts_with('.') {
            normalized.insert(0, '0');
        }
        if normalized.ends_with('.') {
            normalized.push('0');
        }

        if normalized.matches('.').count() > 1 {
            return Err(ParseError::InvalidFloatLiteral {
                literal: tok.literal,
                position: tok.position,
            });
        }

        let value = normalized
            .parse::<f64>()
            .map_err(|_| ParseError::InvalidFloatLiteral {
                literal: tok.literal.clone(),
                position: tok.position,
            })?;

        Ok(Expression::float((tok, end), value))
    }

    /// Parse a hash (object/map) literal `{ key: value, … }`.
    ///
    /// Syntax:
    ///
    /// ```text
    /// {  expr : expr  ,  …  }
    /// ```
    ///
    /// Keys and values are both arbitrary expressions parsed at
    /// [`Precedence::Lowest`], so any value-producing expression is valid on
    /// either side of the `:`.  Pairs are separated by `,`; trailing commas are
    /// not accepted (see [`parse_delimited_list`](Self::parse_delimited_list)).
    ///
    /// The produced [`Expression::Hash`] node contains a `Vec<(Expression,
    /// Expression)>` of key-value pairs and spans from the opening `{` to the
    /// closing `}`.
    ///
    /// # Errors
    ///
    /// * [`ParseError::UnexpectedToken`] — `{` is not the current token, a `:`
    ///   is missing between key and value, or an unexpected token appears where
    ///   `,` or `}` is required.
    /// * [`ParseError::UnexpectedEof`] — the token stream ends before `}`.
    /// * Any error from [`parse_expression`](Parser::parse_expression) while
    ///   parsing a key or value.
    fn parse_hash_literal(&mut self) -> Result<Expression, ParseError> {
        let (l_brace, pairs, r_brace) =
            self.parse_delimited_list(TokenType::LBrace, TokenType::RBrace, |p| {
                let key = p.parse_expression(Precedence::Lowest)?;
                p.consume(TokenType::Colon)?;
                let value = p.parse_expression(Precedence::Lowest)?;
                Ok((key, value))
            })?;

        Ok(Expression::hash((l_brace, r_brace.position), pairs))
    }

    /// Parse an f-string literal `f"…{expr}…"` into an [`Expression::Literal`]
    /// (f-string variant).
    ///
    /// On entry `current_token` must be [`TokenType::FString`].  The token's
    /// `literal` field holds the raw content between the `f"` and `"` delimiters
    /// — e.g. `Hello {name}!` — already validated by the lexer.
    ///
    /// ## Algorithm
    ///
    /// The method walks the content character-by-character, building two
    /// parallel vectors that together describe the interleaved structure of the
    /// string:
    ///
    /// * **`static_parts`** — plain text segments collected between (or around)
    ///   interpolations.
    /// * **`expressions`** — one parsed [`Expression`] per `{…}` interpolation.
    ///
    /// The invariant on exit is `static_parts.len() == expressions.len() + 1`:
    /// static parts *wrap* expressions, so an f-string with *n* interpolations
    /// produces *n + 1* static parts (the first and last of which may be empty).
    ///
    /// When `{` is encountered the accumulated static text is pushed onto
    /// `static_parts`, the buffer is cleared, and
    /// [`parse_expression_in_braces`](Self::parse_expression_in_braces) is
    /// called to find the matching `}`, extract the expression text, and parse
    /// it with a fresh sub-parser.  The outer loop then resumes at the position
    /// immediately after the closing `}`.
    ///
    /// A bare `}` outside an interpolation is rejected with
    /// [`ParseError::UnexpectedToken`]; the lexer normally prevents this, but
    /// the check is kept as a safety net.
    ///
    /// ## Errors
    ///
    /// * [`ParseError::UnexpectedToken`] — `FString` is not the current token,
    ///   or an unmatched `}` is found in the content.
    /// * Any error propagated from
    ///   [`parse_expression_in_braces`](Self::parse_expression_in_braces),
    ///   including sub-parser errors for malformed embedded expressions.
    fn parse_f_string_literal(&mut self) -> Result<Expression, ParseError> {
        let tok = self.consume(TokenType::FString)?;
        let tok_end = tok.position;

        let mut static_parts: Vec<String> = Vec::new();
        let mut expressions: Vec<Expression> = Vec::new();
        let chars: Vec<char> = tok.literal.chars().collect();
        let mut pos = 0;
        let mut current_static = String::new();

        while pos < chars.len() {
            match chars[pos] {
                '{' => {
                    static_parts.push(current_static.clone());
                    current_static.clear();
                    let (expr, end_pos) = Self::parse_expression_in_braces(&chars, pos + 1, &tok)?;
                    expressions.push(expr);
                    pos = end_pos;
                }
                '}' => {
                    // Unmatched closing brace — should not appear here because
                    // the lexer validates brace balance, but guard anyway.
                    return Err(ParseError::UnexpectedToken {
                        expected: TokenType::FString,
                        got: TokenType::RBrace,
                        position: tok.position,
                    });
                }
                ch => {
                    current_static.push(ch);
                    pos += 1;
                }
            }
        }

        static_parts.push(current_static);

        Ok(Expression::f_string(
            (tok, tok_end),
            static_parts,
            expressions,
        ))
    }

    /// Find the matching `}` for an interpolated expression that starts at
    /// `start_pos` (the character *after* the opening `{`), extract the text,
    /// and parse it with a fresh sub-parser.
    ///
    /// Returns the parsed `Expression` and the index in `chars` that is one
    /// past the closing `}` (i.e. where the caller should resume scanning).
    fn parse_expression_in_braces(
        chars: &[char],
        start_pos: usize,
        fstring_tok: &crate::token::Token,
    ) -> Result<(Expression, usize), ParseError> {
        let mut brace_depth = 1usize;
        let mut pos = start_pos;
        let mut in_string = false;
        let mut escaped = false;

        while pos < chars.len() && brace_depth > 0 {
            let ch = chars[pos];

            if escaped {
                escaped = false;
                pos += 1;
                continue;
            }

            if ch == '\\' {
                escaped = true;
                pos += 1;
                continue;
            }

            if in_string {
                if ch == '"' {
                    in_string = false;
                }
            } else {
                match ch {
                    '"' => in_string = true,
                    '{' => brace_depth += 1,
                    '}' => brace_depth -= 1,
                    _ => {}
                }
            }
            pos += 1;
        }

        if brace_depth > 0 {
            return Err(ParseError::UnexpectedEof);
        }

        let expr_text: String = chars[start_pos..pos - 1].iter().collect();
        let expr_text = expr_text.trim();

        if expr_text.is_empty() {
            return Err(ParseError::NoPrefixParser {
                token_type: TokenType::FString,
                position: fstring_tok.position,
            });
        }

        let lexer = Lexer::new(expr_text);
        let mut sub_parser = Self::new(lexer)?;
        let expr = sub_parser.parse_expression(Precedence::Lowest)?;

        Ok((expr, pos))
    }

    /// Parse the next statement and assert it is a block `{ … }`.
    ///
    /// Several constructs in the grammar require a braced block body — `if`,
    /// `elif`, `else`, `fn`, `while`, `for`, etc.  Rather than duplicating the
    /// "parse statement, then downcast to block" pattern in every caller, this
    /// helper centralizes both steps.
    ///
    /// ## Parameters
    ///
    /// * `token` — the keyword token that *introduced* the construct requiring
    ///   the block (e.g. the `if` or `fn` token).  It is used only to produce a
    ///   meaningful [`ParseError::ExpectedBlock`] that points back to the
    ///   construct's opening position, not to the offending non-block token.
    ///
    /// ## Errors
    ///
    /// * [`ParseError::ExpectedBlock`] — [`parse_statement`](Parser::parse_statement)
    ///   succeeded but returned a non-[`Block`](Statement::Block) statement.
    /// * Any error propagated from [`parse_statement`](Parser::parse_statement)
    ///   itself (missing `{`, lex errors, etc.).
    fn expect_block(
        &mut self,
        token: &Token,
    ) -> Result<crate::ast::statements::BlockStatement, ParseError> {
        match self.parse_statement()? {
            Statement::Block(block) => Ok(block),
            _ => Err(ParseError::expected_block(token)),
        }
    }
}
