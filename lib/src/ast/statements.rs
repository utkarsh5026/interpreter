use super::TokenSpan;
use crate::token::{Token, TokenPosition};

use super::expression::{Expression, Indentifier};
use super::literal::FunctionLiteral;
use std::fmt;

macro_rules! delegate_to_span {
    ($self:ident, $field:ident) => {
        match $self {
            Self::Let(s) => &s.span.$field,
            Self::Const(s) => &s.span.$field,
            Self::Return(s) => &s.span.$field,
            Self::Expression(s) => &s.span.$field,
            Self::Block(s) => &s.span.$field,
            Self::For(s) => &s.span.$field,
            Self::While(s) => &s.span.$field,
            Self::Break(s) => &s.span.$field,
            Self::Continue(s) => &s.span.$field,
            Self::Class(s) => &s.span.$field,
        }
    };
}

pub enum Statement {
    Let(LetStatement),
    Const(ConstStatement),
    Return(ReturnStatement),
    Expression(ExpressionStatement),
    Block(BlockStatement),
    For(ForStatement),
    While(WhileStatement),
    Break(BreakStatement),
    Continue(ContinueStatement),
    Class(ClassStatement),
}

impl Statement {
    pub(crate) fn token(&self) -> &Token {
        delegate_to_span!(self, start)
    }

    pub(crate) fn end_position(&self) -> &TokenPosition {
        delegate_to_span!(self, end)
    }
}

impl fmt::Display for Statement {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            Self::Let(s) => write!(f, "{s}"),
            Self::Const(s) => write!(f, "{s}"),
            Self::Return(s) => write!(f, "{s}"),
            Self::Expression(s) => write!(f, "{s}"),
            Self::Block(s) => write!(f, "{s}"),
            Self::For(s) => write!(f, "{s}"),
            Self::While(s) => write!(f, "{s}"),
            Self::Break(s) => write!(f, "{s}"),
            Self::Continue(s) => write!(f, "{s}"),
            Self::Class(s) => write!(f, "{s}"),
        }
    }
}

pub struct BlockStatement {
    span: TokenSpan,
    pub(crate) statements: Vec<Statement>,
}

impl BlockStatement {
    pub(crate) const fn new(span: TokenSpan, statements: Vec<Statement>) -> Self {
        Self { span, statements }
    }
}

impl fmt::Display for BlockStatement {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        for stmt in &self.statements {
            write!(f, "{stmt}")?;
        }
        Ok(())
    }
}

pub struct BreakStatement {
    span: TokenSpan,
}

impl BreakStatement {
    pub(crate) const fn new(span: TokenSpan) -> Self {
        Self { span }
    }
}

impl fmt::Display for BreakStatement {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "{}", self.span.literal())
    }
}

pub(crate) struct ContinueStatement {
    span: TokenSpan,
}

impl ContinueStatement {
    pub(crate) const fn new(span: TokenSpan) -> Self {
        Self { span }
    }
}

impl fmt::Display for ContinueStatement {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "{}", self.span.literal())
    }
}

pub(crate) struct ConstStatement {
    span: TokenSpan,
    identifier: Indentifier,
    value: Expression,
}

impl ConstStatement {
    pub(crate) const fn new(span: TokenSpan, identifier: Indentifier, value: Expression) -> Self {
        Self {
            span,
            identifier,
            value,
        }
    }
}

impl fmt::Display for ConstStatement {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(
            f,
            "{} {} = {};",
            self.span.literal(),
            self.identifier,
            self.value
        )
    }
}

pub(crate) struct ExpressionStatement {
    span: TokenSpan,
    expression: Expression,
}

impl ExpressionStatement {
    pub(crate) const fn new(span: TokenSpan, expression: Expression) -> Self {
        Self { span, expression }
    }
}

impl fmt::Display for ExpressionStatement {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "{}", self.expression)
    }
}

pub(crate) struct ForStatement {
    span: TokenSpan,
    initializer: Box<Statement>,
    condition: Expression,
    increment: Expression,
    body: BlockStatement,
}

impl ForStatement {
    pub(crate) const fn new(
        span: TokenSpan,
        initializer: Box<Statement>,
        condition: Expression,
        increment: Expression,
        body: BlockStatement,
    ) -> Self {
        Self {
            span,
            initializer,
            condition,
            increment,
            body,
        }
    }

    pub(crate) const fn condition(&self) -> &Expression {
        &self.condition
    }

    pub(crate) fn initializer(&self) -> &Statement {
        self.initializer.as_ref()
    }

    pub(crate) const fn increment(&self) -> &Expression {
        &self.increment
    }

    pub(crate) const fn body(&self) -> &BlockStatement {
        &self.body
    }
}

impl fmt::Display for ForStatement {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(
            f,
            "for ({}; {}; {}) {{\n{}\n}}",
            self.initializer, self.condition, self.increment, self.body
        )
    }
}

pub(crate) struct LetStatement {
    span: TokenSpan,
    name: Indentifier,
    value: Expression,
}

impl LetStatement {
    pub(crate) const fn new(span: TokenSpan, name: Indentifier, value: Expression) -> Self {
        Self { span, name, value }
    }

    pub(crate) const fn name(&self) -> &Indentifier {
        &self.name
    }

    pub(crate) const fn value(&self) -> &Expression {
        &self.value
    }
}

impl fmt::Display for LetStatement {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "{} {} = {};", self.span.literal(), self.name, self.value)
    }
}

pub(crate) struct ReturnStatement {
    span: TokenSpan,
    value: Expression,
}

impl ReturnStatement {
    pub(crate) const fn new(span: TokenSpan, value: Expression) -> Self {
        Self { span, value }
    }

    pub(crate) const fn value(&self) -> &Expression {
        &self.value
    }
}

impl fmt::Display for ReturnStatement {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "{} {};", self.span.literal(), self.value)
    }
}

pub(crate) struct WhileStatement {
    span: TokenSpan,
    condition: Expression,
    body: BlockStatement,
}

impl WhileStatement {
    pub(crate) const fn new(span: TokenSpan, condition: Expression, body: BlockStatement) -> Self {
        Self {
            span,
            condition,
            body,
        }
    }

    pub(crate) const fn condition(&self) -> &Expression {
        &self.condition
    }

    pub(crate) const fn body(&self) -> &BlockStatement {
        &self.body
    }
}

impl fmt::Display for WhileStatement {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "while ({}) {{\n{}\n}}", self.condition, self.body)
    }
}

pub(crate) struct ClassStatement {
    span: TokenSpan,
    name: Indentifier,
    parent_class: Option<Indentifier>,
    constructor: Option<FunctionLiteral>,
    methods: Vec<(Indentifier, FunctionLiteral)>,
}

impl ClassStatement {
    pub(crate) const fn new(
        span: TokenSpan,
        name: Indentifier,
        parent_class: Option<Indentifier>,
        constructor: Option<FunctionLiteral>,
        methods: Vec<(Indentifier, FunctionLiteral)>,
    ) -> Self {
        Self {
            span,
            name,
            parent_class,
            constructor,
            methods,
        }
    }

    pub(crate) const fn name(&self) -> &Indentifier {
        &self.name
    }

    pub(crate) const fn parent_class(&self) -> Option<&Indentifier> {
        self.parent_class.as_ref()
    }

    pub(crate) const fn constructor(&self) -> Option<&FunctionLiteral> {
        self.constructor.as_ref()
    }

    pub(crate) const fn methods(&self) -> &[(Indentifier, FunctionLiteral)] {
        self.methods.as_slice()
    }
}

impl fmt::Display for ClassStatement {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "class {}", self.name)
    }
}
