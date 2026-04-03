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

#[derive(Debug)]
pub struct TokenSpan {
    start: Token,
    end: TokenPosition,
}

impl TokenSpan {
    #[must_use]
    pub const fn new(start: Token, end: TokenPosition) -> Self {
        Self { start, end }
    }

    #[must_use]
    pub fn literal(&self) -> &str {
        &self.start.literal
    }

    #[must_use]
    pub const fn start_pos(&self) -> &TokenPosition {
        &self.start.position
    }

    #[must_use]
    pub const fn end_pos(&self) -> &TokenPosition {
        &self.end
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

    pub fn function(
        span: impl Into<TokenSpan>,
        params: Vec<Indentifier>,
        body: statements::BlockStatement,
    ) -> Self {
        Self::Literal(Literal::Func(FunctionLiteral::new(
            span.into(),
            params,
            body,
        )))
    }

    pub fn index(span: impl Into<TokenSpan>, left: Self, index: Self) -> Self {
        Self::Index(expression::IndexExpression::new(
            span.into(),
            Box::new(left),
            Box::new(index),
        ))
    }

    pub fn this(span: impl Into<TokenSpan>) -> Self {
        Self::This(expression::ThisExpression::new(span.into()))
    }

    pub fn integer(span: impl Into<TokenSpan>, value: i64) -> Self {
        Self::Literal(Literal::Integer(IntegerLiteral::new(span.into(), value)))
    }

    pub fn boolean(span: impl Into<TokenSpan>) -> Self {
        Self::Literal(Literal::Bool(BooleanLiteral::new(span.into())))
    }

    pub fn string(span: impl Into<TokenSpan>) -> Self {
        Self::Literal(Literal::String(StringLiteral::new(span.into())))
    }

    pub fn array(span: impl Into<TokenSpan>, elements: Vec<Self>) -> Self {
        Self::Literal(Literal::Array(ArrayLiteral::new(span.into(), elements)))
    }

    pub fn identifier(span: impl Into<TokenSpan>) -> Self {
        Self::Identifier(Indentifier::new(span.into()))
    }

    pub fn infix(span: impl Into<TokenSpan>, left: Self, right: Self, operator: Operator) -> Self {
        Self::Infix(expression::InfixExpression::new(
            span.into(),
            Box::new(left),
            Box::new(right),
            operator,
        ))
    }

    pub fn assignment(span: impl Into<TokenSpan>, name: Self, value: Self) -> Self {
        Self::Assignment(expression::AssignmentExpression::new(
            span.into(),
            Box::new(name),
            Box::new(value),
        ))
    }

    pub fn call(span: impl Into<TokenSpan>, function: Self, args: Vec<Self>) -> Self {
        Self::Call(expression::CallExpression::new(
            span.into(),
            Box::new(function),
            args,
        ))
    }

    pub fn new_expr(span: impl Into<TokenSpan>, class_name: Self, args: Vec<Self>) -> Self {
        Self::New(expression::NewExpression::new(
            span.into(),
            Box::new(class_name),
            args,
        ))
    }

    pub fn super_expr(span: impl Into<TokenSpan>, method: Option<Self>, args: Vec<Self>) -> Self {
        Self::Super(expression::SuperExpression::new(span.into(), method, args))
    }

    pub fn if_expr(
        span: impl Into<TokenSpan>,
        conditions: Vec<Self>,
        consequences: Vec<statements::BlockStatement>,
        alternative: Option<statements::BlockStatement>,
    ) -> Self {
        Self::If(expression::IfExpression::new(
            span.into(),
            conditions,
            consequences,
            alternative,
        ))
    }

    pub fn prefix(span: impl Into<TokenSpan>, operator: Operator, right: Self) -> Self {
        Self::Prefix(expression::PrefixExpression::new(
            span.into(),
            operator,
            Box::new(right),
        ))
    }
}

impl Statement {
    pub fn let_stmt(
        span: impl Into<TokenSpan>,
        name: Indentifier,
        value: expression::Expression,
    ) -> Self {
        Self::Let(LetStatement::new(span.into(), name, value))
    }

    pub fn const_stmt(
        span: impl Into<TokenSpan>,
        name: Indentifier,
        value: expression::Expression,
    ) -> Self {
        Self::Const(ConstStatement::new(span.into(), name, value))
    }

    pub fn return_stmt(span: impl Into<TokenSpan>, value: expression::Expression) -> Self {
        Self::Return(ReturnStatement::new(span.into(), value))
    }

    pub fn expression_stmt(span: impl Into<TokenSpan>, expr: expression::Expression) -> Self {
        Self::Expression(ExpressionStatement::new(span.into(), expr))
    }

    pub fn block(span: impl Into<TokenSpan>, stmts: Vec<Self>) -> Self {
        Self::Block(BlockStatement::new(span.into(), stmts))
    }

    pub fn while_stmt(
        span: impl Into<TokenSpan>,
        condition: expression::Expression,
        body: BlockStatement,
    ) -> Self {
        Self::While(WhileStatement::new(span.into(), condition, body))
    }

    pub fn for_stmt(
        span: impl Into<TokenSpan>,
        initializer: Self,
        condition: expression::Expression,
        increment: expression::Expression,
        body: BlockStatement,
    ) -> Self {
        Self::For(ForStatement::new(
            span.into(),
            Box::new(initializer),
            condition,
            increment,
            body,
        ))
    }

    pub fn break_stmt(span: impl Into<TokenSpan>) -> Self {
        Self::Break(BreakStatement::new(span.into()))
    }

    pub fn continue_stmt(span: impl Into<TokenSpan>) -> Self {
        Self::Continue(ContinueStatement::new(span.into()))
    }

    pub fn class_stmt(
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
