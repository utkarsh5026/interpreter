use crate::token::{Operator, TokenPosition};

use std::fmt;

use super::literal::Literal;
use super::TokenSpan;

pub enum Expression {
    Identifier(Indentifier),
    Infix(InfixExpression),
    Assignment(AssignmentExpression),
    Literal(Literal),
}

impl fmt::Display for Expression {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            Self::Identifier(ident) => write!(f, "{ident}"),
            Self::Infix(infix) => write!(f, "{infix}"),
            Self::Assignment(assignment) => write!(f, "{assignment}"),
            Self::Literal(literal) => write!(f, "{literal}"),
        }
    }
}

impl Expression {
    pub(crate) const fn end_position(&self) -> &TokenPosition {
        match self {
            Self::Identifier(e) => &e.span.end,
            Self::Infix(e) => &e.span.end,
            Self::Assignment(e) => &e.span.end,
            Self::Literal(e) => e.end_position(),
        }
    }
}

pub struct Indentifier {
    span: TokenSpan,
    value: String,
}

impl Indentifier {
    pub(crate) fn new(span: impl Into<TokenSpan>, value: String) -> Self {
        Self {
            span: span.into(),
            value,
        }
    }
}

impl fmt::Display for Indentifier {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "{}", self.value)
    }
}

pub struct InfixExpression {
    span: TokenSpan,
    left: Box<Expression>,
    right: Box<Expression>,
    operator: Operator,
}

impl InfixExpression {
    pub(crate) fn new(
        span: impl Into<TokenSpan>,
        left: Box<Expression>,
        right: Box<Expression>,
        operator: Operator,
    ) -> Self {
        Self {
            span: span.into(),
            left,
            right,
            operator,
        }
    }

    pub(crate) const fn left(&self) -> &Expression {
        &self.left
    }

    pub(crate) const fn right(&self) -> &Expression {
        &self.right
    }

    pub(crate) const fn operator(&self) -> &Operator {
        &self.operator
    }
}

impl fmt::Display for InfixExpression {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "({}{}{})", self.left, self.operator, self.right)
    }
}

pub struct AssignmentExpression {
    span: TokenSpan,
    name: Box<Expression>,
    value: Box<Expression>,
}

impl AssignmentExpression {
    pub(crate) fn new(
        span: impl Into<TokenSpan>,
        name: Box<Expression>,
        value: Box<Expression>,
    ) -> Self {
        Self {
            span: span.into(),
            name,
            value,
        }
    }

    pub(crate) const fn name(&self) -> &Expression {
        &self.name
    }

    pub(crate) const fn value(&self) -> &Expression {
        &self.value
    }
}

impl fmt::Display for AssignmentExpression {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "{} = {}", self.name, self.value)
    }
}
