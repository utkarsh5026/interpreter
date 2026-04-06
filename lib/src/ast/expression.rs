use crate::token::{Operator, TokenPosition};

use std::fmt;

use super::literal::Literal;
use super::{TokenSpan, statements};

#[derive(Debug, Clone)]
pub enum Expression {
    Identifier(Indentifier),
    Infix(InfixExpression),
    Prefix(PrefixExpression),
    Assignment(AssignmentExpression),
    Call(CallExpression),
    New(NewExpression),
    Super(SuperExpression),
    Literal(Literal),
    Index(IndexExpression),
    This(ThisExpression),
    Property(PropertyExpression),
    If(IfExpression),
}

impl fmt::Display for Expression {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            Self::Identifier(ident) => write!(f, "{ident}"),
            Self::Infix(infix) => write!(f, "{infix}"),
            Self::Assignment(assignment) => write!(f, "{assignment}"),
            Self::Literal(literal) => write!(f, "{literal}"),
            Self::Call(call) => write!(f, "{call}"),
            Self::New(new) => write!(f, "{new}"),
            Self::Super(super_expr) => write!(f, "{super_expr}"),
            Self::Index(idx) => write!(f, "{idx}"),
            Self::This(this) => write!(f, "{this}"),
            Self::Prefix(prefix) => write!(f, "{prefix}"),
            Self::If(if_expr) => write!(f, "{if_expr}"),
            Self::Property(property) => write!(f, "{property}"),
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
            Self::Call(e) => &e.span.end,
            Self::New(e) => &e.span.end,
            Self::Super(e) => &e.span.end,
            Self::Index(e) => &e.span.end,
            Self::This(e) => &e.span.end,
            Self::Prefix(e) => &e.span.end,
            Self::If(e) => &e.span.end,
            Self::Property(e) => &e.span.end,
        }
    }
}
#[derive(Debug, Clone)]
pub struct Indentifier {
    span: TokenSpan,
}

impl Indentifier {
    pub(crate) fn new(span: impl Into<TokenSpan>) -> Self {
        Self { span: span.into() }
    }

    pub(crate) fn value(&self) -> &str {
        self.span.literal()
    }
}

impl fmt::Display for Indentifier {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "{}", self.value())
    }
}
#[derive(Debug, Clone)]
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
#[derive(Debug, Clone)]
pub struct PrefixExpression {
    span: TokenSpan,
    operator: Operator,
    right: Box<Expression>,
}

impl PrefixExpression {
    pub(crate) fn new(
        span: impl Into<TokenSpan>,
        operator: Operator,
        right: Box<Expression>,
    ) -> Self {
        Self {
            span: span.into(),
            operator,
            right,
        }
    }

    pub(crate) const fn operator(&self) -> &Operator {
        &self.operator
    }

    pub(crate) const fn right(&self) -> &Expression {
        &self.right
    }
}

impl fmt::Display for PrefixExpression {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "({}{})", self.operator, self.right)
    }
}
#[derive(Debug, Clone)]
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
#[derive(Debug, Clone)]
pub struct CallExpression {
    span: TokenSpan,
    function: Box<Expression>,
    args: Vec<Expression>,
}

impl CallExpression {
    pub(crate) fn new(
        span: impl Into<TokenSpan>,
        function: Box<Expression>,
        args: Vec<Expression>,
    ) -> Self {
        Self {
            span: span.into(),
            function,
            args,
        }
    }

    pub(crate) const fn function(&self) -> &Expression {
        &self.function
    }

    pub(crate) fn args(&self) -> &[Expression] {
        &self.args
    }
}

impl fmt::Display for CallExpression {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        let args = self
            .args
            .iter()
            .map(std::string::ToString::to_string)
            .collect::<Vec<_>>()
            .join(", ");
        write!(f, "{}({})", self.function, args)
    }
}
#[derive(Debug, Clone)]
pub struct NewExpression {
    span: TokenSpan,
    class_name: Box<Expression>,
    args: Vec<Expression>,
}

impl NewExpression {
    pub(crate) fn new(
        span: impl Into<TokenSpan>,
        class_name: Box<Expression>,
        args: Vec<Expression>,
    ) -> Self {
        Self {
            span: span.into(),
            class_name,
            args,
        }
    }

    pub(crate) const fn class_name(&self) -> &Expression {
        &self.class_name
    }

    pub(crate) fn args(&self) -> &[Expression] {
        &self.args
    }
}

impl fmt::Display for NewExpression {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        let args = self
            .args
            .iter()
            .map(std::string::ToString::to_string)
            .collect::<Vec<_>>()
            .join(", ");
        write!(f, "new {}({})", self.class_name, args)
    }
}
#[derive(Debug, Clone)]
pub struct SuperExpression {
    span: TokenSpan,
    args: Vec<Expression>,
    method: Option<Box<Expression>>,
}
impl SuperExpression {
    pub(crate) fn new(
        span: impl Into<TokenSpan>,
        method: Option<Expression>,
        args: Vec<Expression>,
    ) -> Self {
        Self {
            span: span.into(),
            method: method.map(Box::new),
            args,
        }
    }

    pub(crate) fn method(&self) -> Option<&Expression> {
        self.method.as_deref()
    }

    pub(crate) fn args(&self) -> &[Expression] {
        &self.args
    }

    pub(crate) const fn is_constructor_call(&self) -> bool {
        self.method.is_none()
    }
}

impl fmt::Display for SuperExpression {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        let args = self
            .args
            .iter()
            .map(std::string::ToString::to_string)
            .collect::<Vec<_>>()
            .join(", ");

        match &self.method {
            None => write!(f, "super({args})"),
            Some(method) => write!(f, "super.{method}({args})"),
        }
    }
}
#[derive(Debug, Clone)]
pub struct IndexExpression {
    span: TokenSpan,
    left: Box<Expression>,
    index: Box<Expression>,
}

impl IndexExpression {
    pub(crate) fn new(
        span: impl Into<TokenSpan>,
        left: Box<Expression>,
        index: Box<Expression>,
    ) -> Self {
        Self {
            span: span.into(),
            left,
            index,
        }
    }

    pub(crate) const fn left(&self) -> &Expression {
        &self.left
    }

    pub(crate) const fn index(&self) -> &Expression {
        &self.index
    }
}

impl fmt::Display for IndexExpression {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "({}[{}])", self.left, self.index)
    }
}
#[derive(Debug, Clone)]
pub struct ThisExpression {
    span: TokenSpan,
}

impl ThisExpression {
    pub(crate) fn new(span: impl Into<TokenSpan>) -> Self {
        Self { span: span.into() }
    }
}

impl fmt::Display for ThisExpression {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "this")
    }
}
#[derive(Debug, Clone)]
pub struct IfExpression {
    span: TokenSpan,
    conditions: Vec<Expression>,
    consequences: Vec<statements::BlockStatement>,
    alternative: Option<statements::BlockStatement>,
}

impl IfExpression {
    pub(crate) fn new(
        span: impl Into<TokenSpan>,
        conditions: Vec<Expression>,
        consequences: Vec<statements::BlockStatement>,
        alternative: Option<statements::BlockStatement>,
    ) -> Self {
        Self {
            span: span.into(),
            conditions,
            consequences,
            alternative,
        }
    }

    pub(crate) fn conditions(&self) -> &[Expression] {
        &self.conditions
    }

    pub(crate) fn consequences(&self) -> &[statements::BlockStatement] {
        &self.consequences
    }

    pub(crate) const fn alternative(&self) -> Option<&statements::BlockStatement> {
        self.alternative.as_ref()
    }
}

impl fmt::Display for IfExpression {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "if ({}) {}", self.conditions[0], self.consequences[0])?;
        for (cond, cons) in self.conditions[1..].iter().zip(&self.consequences[1..]) {
            write!(f, " elif ({cond}) {cons}")?;
        }
        if let Some(alt) = &self.alternative {
            write!(f, " else {alt}")?;
        }
        Ok(())
    }
}

#[derive(Debug, Clone)]
pub struct PropertyExpression {
    span: TokenSpan,
    object: Box<Expression>,
    property: Box<Expression>,
}

impl PropertyExpression {
    pub(crate) fn new(
        span: impl Into<TokenSpan>,
        object: Box<Expression>,
        property: Box<Expression>,
    ) -> Self {
        Self {
            span: span.into(),
            object,
            property,
        }
    }

    pub(crate) fn object(&self) -> &Expression {
        &self.object
    }

    pub(crate) fn property(&self) -> &Expression {
        &self.property
    }
}

impl fmt::Display for PropertyExpression {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "{}.{}", self.object, self.property)
    }
}
