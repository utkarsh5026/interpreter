use crate::token::{Operator, Token};

use std::fmt;

use super::literal::Literal;

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

#[derive(Clone)]
pub struct Indentifier {
    token: Token,
    value: String,
}

impl Indentifier {
    pub(crate) const fn new(token: Token, value: String) -> Self {
        Self { token, value }
    }
}

impl fmt::Display for Indentifier {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "{}", self.value)
    }
}

pub struct InfixExpression {
    token: Token,
    left: Box<Expression>,
    right: Box<Expression>,
    operator: Operator,
}

impl InfixExpression {
    pub(crate) const fn new(
        token: Token,
        left: Box<Expression>,
        right: Box<Expression>,
        operator: Operator,
    ) -> Self {
        Self {
            token,
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
    token: Token,
    name: Box<Expression>,
    value: Box<Expression>,
}

impl AssignmentExpression {
    pub(crate) const fn new(token: Token, name: Box<Expression>, value: Box<Expression>) -> Self {
        Self { token, name, value }
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
        write!(f, "{}{}{}", self.name, self.token.literal, self.value)
    }
}
