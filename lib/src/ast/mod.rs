// Statement enum, Expression enum, Program struct

use crate::token::{Operator, Token, TokenPosition};

use self::expression::Expression;
use self::literal::{
    ArrayLiteral, BooleanLiteral, IntegerLiteral, Literal, NullLitreal, StringLiteral,
};

use expression::Indentifier;
use literal::FunctionLiteral;
use statements::{
    BlockStatement, BreakStatement, ClassStatement, ConstStatement, ContinueStatement,
    ExpressionStatement, ForStatement, LetStatement, ReturnStatement, Statement, WhileStatement,
};

pub mod expression;
pub mod literal;
pub mod statements;

pub struct TokenSpan {
    start: Token,
    end: TokenPosition,
}

impl TokenSpan {
    pub const fn new(start: Token, end: TokenPosition) -> Self {
        Self { start, end }
    }

    pub fn literal(&self) -> &str {
        &self.start.literal
    }

    pub fn start_pos(&self) -> &TokenPosition {
        &self.start.position
    }
}

impl From<(Token, TokenPosition)> for TokenSpan {
    fn from((start, end): (Token, TokenPosition)) -> Self {
        Self::new(start, end)
    }
}

impl Expression {
    pub fn null(span: impl Into<TokenSpan>) -> Self {
        Self::Literal(Literal::Null(NullLitreal::new(span.into())))
    }

    pub fn integer(span: impl Into<TokenSpan>, value: i64) -> Self {
        Self::Literal(Literal::Integer(IntegerLiteral::new(span.into(), value)))
    }

    pub fn boolean(span: impl Into<TokenSpan>, value: bool) -> Self {
        Self::Literal(Literal::Bool(BooleanLiteral::new(span.into(), value)))
    }

    pub fn string(span: impl Into<TokenSpan>, value: String) -> Self {
        Self::Literal(Literal::String(StringLiteral::new(span.into(), value)))
    }

    pub fn array(span: impl Into<TokenSpan>, elements: Vec<Expression>) -> Self {
        Self::Literal(Literal::Array(ArrayLiteral::new(span.into(), elements)))
    }

    pub fn identifier(span: impl Into<TokenSpan>, value: String) -> Self {
        Self::Identifier(Indentifier::new(span.into(), value))
    }

    pub fn infix(
        span: impl Into<TokenSpan>,
        left: Expression,
        right: Expression,
        operator: Operator,
    ) -> Self {
        Self::Infix(expression::InfixExpression::new(
            span.into(),
            Box::new(left),
            Box::new(right),
            operator,
        ))
    }

    pub fn assignment(span: impl Into<TokenSpan>, name: Expression, value: Expression) -> Self {
        Self::Assignment(expression::AssignmentExpression::new(
            span.into(),
            Box::new(name),
            Box::new(value),
        ))
    }
}

impl Statement {
    pub(crate) fn let_stmt(
        span: impl Into<TokenSpan>,
        name: Indentifier,
        value: expression::Expression,
    ) -> Self {
        Self::Let(LetStatement::new(span.into(), name, value))
    }

    pub(crate) fn const_stmt(
        span: impl Into<TokenSpan>,
        name: Indentifier,
        value: expression::Expression,
    ) -> Self {
        Self::Const(ConstStatement::new(span.into(), name, value))
    }

    pub(crate) fn return_stmt(span: impl Into<TokenSpan>, value: expression::Expression) -> Self {
        Self::Return(ReturnStatement::new(span.into(), value))
    }

    pub(crate) fn expression_stmt(
        span: impl Into<TokenSpan>,
        expr: expression::Expression,
    ) -> Self {
        Self::Expression(ExpressionStatement::new(span.into(), expr))
    }

    pub(crate) fn block(span: impl Into<TokenSpan>, stmts: Vec<Statement>) -> Self {
        Self::Block(BlockStatement::new(span.into(), stmts))
    }

    pub(crate) fn while_stmt(
        span: impl Into<TokenSpan>,
        condition: expression::Expression,
        body: BlockStatement,
    ) -> Self {
        Self::While(WhileStatement::new(span.into(), condition, body))
    }

    pub(crate) fn for_stmt(
        span: impl Into<TokenSpan>,
        initializer: Box<Statement>,
        condition: expression::Expression,
        increment: expression::Expression,
        body: BlockStatement,
    ) -> Self {
        Self::For(ForStatement::new(
            span.into(),
            initializer,
            condition,
            increment,
            body,
        ))
    }

    pub(crate) fn break_stmt(span: impl Into<TokenSpan>) -> Self {
        Self::Break(BreakStatement::new(span.into()))
    }

    pub(crate) fn continue_stmt(span: impl Into<TokenSpan>) -> Self {
        Self::Continue(ContinueStatement::new(span.into()))
    }

    pub(crate) fn class_stmt(
        span: impl Into<TokenSpan>,
        name: Indentifier,
        parent: Option<Indentifier>,
        constructor: Option<FunctionLiteral>,
        methods: Vec<(Indentifier, FunctionLiteral)>,
    ) -> Self {
        Self::Class(ClassStatement::new(
            span.into(),
            name,
            parent,
            constructor,
            methods,
        ))
    }
}
