//! Statement nodes in the Mutant Lang AST.
//!
//! A [`Statement`] is any top-level construct that produces an effect but not
//! necessarily a value — variable declarations, control-flow keywords,
//! class definitions, and raw expressions used for side effects.
//!
//! # Design overview
//!
//! Every concrete statement type stores a [`TokenSpan`] that records the
//! introducing token and the source position at which the statement ends.
//! [`Statement`] itself is a thin dispatch enum; the `delegate_to_span!`
//! macro centralises the repetitive field-access pattern so that
//! [`Statement::token`] and [`Statement::end_position`] stay in sync with all
//! variants automatically.
//!
//! ## Concrete statement types
//!
//! | Variant | Source syntax | Concrete type |
//! |---|---|---|
//! | [`Statement::Let`] | `let x = expr;` | [`LetStatement`] |
//! | [`Statement::Const`] | `const x = expr;` | [`ConstStatement`] |
//! | [`Statement::Return`] | `return expr;` | [`ReturnStatement`] |
//! | [`Statement::Expression`] | `expr;` | [`ExpressionStatement`] |
//! | [`Statement::Block`] | `{ stmts… }` | [`BlockStatement`] |
//! | [`Statement::For`] | `for (init; cond; incr) { … }` | [`ForStatement`] |
//! | [`Statement::While`] | `while (cond) { … }` | [`WhileStatement`] |
//! | [`Statement::Break`] | `break` | [`BreakStatement`] |
//! | [`Statement::Continue`] | `continue` | [`ContinueStatement`] |
//! | [`Statement::Class`] | `class Foo { … }` | [`ClassStatement`] |

use super::TokenSpan;
use crate::token::{Token, TokenPosition};

use super::expression::{Expression, Indentifier};
use super::literal::FunctionLiteral;
use std::fmt;

/// Internal macro that routes a [`TokenSpan`] field access across all
/// [`Statement`] variants without repeating the match arm list.
///
/// Called as `delegate_to_span!(self, start)` or
/// `delegate_to_span!(self, end)`.  Each arm destructures the variant and
/// borrows the requested field directly from `span`, returning a `&Token` or
/// `&TokenPosition` respectively.
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

/// A closed set of all statement forms recognized by the Mutant Lang parser.
///
/// Every node in the program's top-level statement list, every block body, and
/// every loop body is represented as a `Statement`.  The evaluator matches on
/// this enum to decide how to execute each form.
///
/// # Ownership
///
/// Constructing a `Statement` takes ownership of the inner concrete type.
/// The [`Statement::Block`] variant owns its [`BlockStatement`] inline;
/// [`Statement::For`] boxes its initializer via [`ForStatement`] to break the
/// otherwise-recursive `Statement → ForStatement → Box<Statement>` size cycle.
#[derive(Debug, Clone)]
pub enum Statement {
    Let(LetStatement),

    /// An immutable variable declaration: `const x = expr;`.
    Const(ConstStatement),

    /// A return statement: `return expr;`.
    Return(ReturnStatement),

    /// An expression used as a statement: `expr;`.
    ///
    /// Produced when the parser encounters an expression in statement position.
    /// The expression is evaluated for its side effects; its value is
    /// discarded unless it is the final statement in a block.
    Expression(ExpressionStatement),

    /// A braced sequence of statements: `{ stmt₀ stmt₁ … }`.
    ///
    /// Used as the body of functions, loops, and conditionals.
    Block(BlockStatement),

    /// A C-style for loop: `for (init; cond; incr) { body }`.
    For(ForStatement),

    /// A while loop: `while (cond) { body }`.
    While(WhileStatement),

    /// A `break` statement that exits the innermost loop.
    Break(BreakStatement),

    /// A `continue` statement that skips to the next loop iteration.
    Continue(ContinueStatement),

    /// A class definition: `class Name [extends Parent] { … }`.
    Class(ClassStatement),
}

impl Statement {
    /// Returns a reference to the [`Token`] that introduced this statement.
    pub(crate) const fn token(&self) -> &Token {
        delegate_to_span!(self, start)
    }

    /// Returns a reference to the [`TokenPosition`] at which this statement
    pub(crate) const fn end_position(&self) -> &TokenPosition {
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

/// A braced sequence of statements forming a single scoped unit.
///
/// `BlockStatement` is the body type shared by functions, loops, and
/// conditionals.  It owns its child statements as a `Vec<Statement>` and is
/// always bounded by a `{` / `}` token pair, recorded in `span`.
///
/// An empty block (`{}`) is valid and results in an empty `statements` vec.
#[derive(Debug, Clone)]
pub struct BlockStatement {
    pub(crate) span: TokenSpan,
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

/// A `break` statement that exits the immediately enclosing loop.
///
/// `BreakStatement` carries only its source span; it has no operand.  The
/// evaluator signals loop termination when it encounters this node.  Using
/// `break` outside a loop is a runtime (or validation) error, not a parse
/// error — the parser emits this node unconditionally.
#[derive(Debug, Clone)]
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

/// A `continue` statement that skips to the next iteration of the enclosing loop.
///
/// Like [`BreakStatement`], `ContinueStatement` carries only its source span.
/// The evaluator uses this node to signal that the current loop body should
/// abort and the loop should advance to its next cycle.
#[derive(Debug, Clone)]
pub struct ContinueStatement {
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

/// An immutable variable declaration: `const x = expr;`.
///
/// `ConstStatement` binds `identifier` to the result of evaluating `value` and
/// marks the binding as immutable.  Attempting to reassign a `const`-bound
/// name is a runtime error enforced by the evaluator, not the parser.
///
/// The `span` covers from the `const` keyword through the closing `;`.
#[derive(Debug, Clone)]
pub struct ConstStatement {
    span: TokenSpan,
    name: Indentifier,
    value: Expression,
}

impl ConstStatement {
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

impl fmt::Display for ConstStatement {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "{} {} = {};", self.span.literal(), self.name, self.value)
    }
}

/// An expression used in statement position: `expr;`.
///
/// Wraps an [`Expression`] that is evaluated purely for its side effects.
/// Whether the value is preserved (e.g. as the implicit return value of a
/// block) depends on the evaluator strategy rather than the AST node itself.
///
/// The `span` covers the first token of the expression through the trailing
/// `;` (if present).
#[derive(Debug, Clone)]
pub struct ExpressionStatement {
    span: TokenSpan,
    expression: Expression,
}

impl ExpressionStatement {
    pub const fn new(span: TokenSpan, expression: Expression) -> Self {
        Self { span, expression }
    }

    pub const fn expression(&self) -> &Expression {
        &self.expression
    }
}

impl fmt::Display for ExpressionStatement {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "{}", self.expression)
    }
}

/// A C-style `for` loop: `for (init; cond; incr) { body }`.
///
/// The `initializer` is a full [`Statement`] (typically a [`LetStatement`] or
/// [`ExpressionStatement`]) and is heap-allocated via [`Box`] to break the
/// otherwise-recursive size cycle: `Statement` contains `ForStatement` which
/// would contain `Statement` inline.
///
/// The `condition` is re-evaluated before each iteration; when it is falsy the
/// loop exits.  The `increment` expression is evaluated after each successful
/// body execution.
#[derive(Debug, Clone)]
pub struct ForStatement {
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

/// A mutable variable declaration: `let x = expr;`.
///
/// `LetStatement` binds `name` to the result of evaluating `value` and
/// introduces the name into the current environment scope.  Unlike
/// [`ConstStatement`], the binding may be reassigned after declaration.
///
/// The `span` covers from the `let` keyword through the closing `;`.
#[derive(Debug, Clone)]
pub struct LetStatement {
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

/// A return statement: `return expr;`.
///
/// Causes the evaluator to unwind the current function call and yield `value`
/// as the function's result.  Using `return` outside a function body is a
/// runtime error enforced by the evaluator.
///
/// The `span` covers from the `return` keyword through the closing `;`.
#[derive(Debug, Clone)]
pub struct ReturnStatement {
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

/// A `while` loop: `while (cond) { body }`.
///
/// The `condition` is evaluated before each iteration; when it is falsy the
/// loop exits without executing `body`.  [`BreakStatement`] and
/// [`ContinueStatement`] inside `body` interact with the evaluator's loop
/// control flow.
///
/// The `span` covers from the `while` keyword through the closing `}`.
#[derive(Debug, Clone)]
pub struct WhileStatement {
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

/// A class definition statement: `class Name [extends Parent] { … }`.
///
/// Introduces a named class into the current scope.  A class may optionally
/// extend a single `parent_class` (single inheritance).  It may declare an
/// optional `constructor` [`FunctionLiteral`] and any number of named
/// `methods`.
///
/// The `Display` impl intentionally omits the body; see the documentation note
/// below if you need the full source representation.
///
/// The `span` covers from the `class` keyword through the closing `}`.
#[derive(Debug, Clone)]
pub struct ClassStatement {
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
