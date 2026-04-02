use crate::token::Token;

use super::expression::{Expression, Indentifier};
use super::literal::FunctionLiteral;
use std::fmt;

pub(crate) enum Statement {
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

pub(crate) struct BlockStatement {
    token: Token,
    statements: Vec<Statement>,
}

impl BlockStatement {
    pub(crate) fn new(token: Token, statements: Vec<Statement>) -> Self {
        Self { token, statements }
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

pub(crate) struct BreakStatement {
    token: Token,
}

impl BreakStatement {
    pub(crate) const fn new(token: Token) -> Self {
        Self { token }
    }
}

impl fmt::Display for BreakStatement {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "{}", self.token.literal)
    }
}

pub(crate) struct ContinueStatement {
    token: Token,
}

impl ContinueStatement {
    pub(crate) const fn new(token: Token) -> Self {
        Self { token }
    }
}

impl fmt::Display for ContinueStatement {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "{}", self.token.literal)
    }
}

pub(crate) struct ConstStatement {
    token: Token,
    identifier: Indentifier,
    value: Expression,
}

impl ConstStatement {
    pub(crate) const fn new(token: Token, identifier: Indentifier, value: Expression) -> Self {
        Self {
            token,
            identifier,
            value,
        }
    }
}

impl fmt::Display for ConstStatement {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "{}", self.token.literal)
    }
}

pub(crate) struct ExpressionStatement {
    token: Token,
    expression: Expression,
}

impl ExpressionStatement {
    pub(crate) const fn new(token: Token, expression: Expression) -> Self {
        Self { token, expression }
    }
}

impl fmt::Display for ExpressionStatement {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "{}", self.token.literal)
    }
}

pub(crate) struct ForStatement {
    token: Token,
    initializer: Box<Statement>,
    condition: Expression,
    increment: Expression,
    body: BlockStatement,
}

impl ForStatement {
    pub(crate) const fn new(
        token: Token,
        initializer: Box<Statement>,
        condition: Expression,
        increment: Expression,
        body: BlockStatement,
    ) -> Self {
        Self {
            token,
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
    token: Token,
    name: Indentifier,
    value: Expression,
}

impl LetStatement {
    pub(crate) const fn new(token: Token, name: Indentifier, value: Expression) -> Self {
        Self { token, name, value }
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
        write!(f, "{} {} = {};", self.token.literal, self.name, self.value)
    }
}

pub(crate) struct ReturnStatement {
    token: Token,
    value: Expression,
}

impl ReturnStatement {
    pub(crate) const fn new(token: Token, value: Expression) -> Self {
        Self { token, value }
    }

    pub(crate) const fn value(&self) -> &Expression {
        &self.value
    }
}

impl fmt::Display for ReturnStatement {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "{} {};", self.token.literal, self.value)
    }
}

pub(crate) struct WhileStatement {
    token: Token,
    condition: Expression,
    body: BlockStatement,
}

impl WhileStatement {
    pub(crate) const fn new(token: Token, condition: Expression, body: BlockStatement) -> Self {
        Self {
            token,
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
    token: Token,
    name: Indentifier,
    parent_class: Option<Indentifier>,
    constructor: Option<FunctionLiteral>,
    methods: Vec<(Indentifier, FunctionLiteral)>,
}

impl ClassStatement {
    pub(crate) const fn new(
        token: Token,
        name: Indentifier,
        parent_class: Option<Indentifier>,
        constructor: Option<FunctionLiteral>,
        methods: Vec<(Indentifier, FunctionLiteral)>,
    ) -> Self {
        Self {
            token,
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

impl Statement {
    pub(crate) const fn token(&self) -> &Token {
        match self {
            Self::Let(s) => &s.token,
            Self::Const(s) => &s.token,
            Self::Return(s) => &s.token,
            Self::Expression(s) => &s.token,
            Self::Block(s) => &s.token,
            Self::For(s) => &s.token,
            Self::While(s) => &s.token,
            Self::Break(s) => &s.token,
            Self::Continue(s) => &s.token,
            Self::Class(s) => &s.token,
        }
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
