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
pub enum Statement {
    /// A variable declaration: `let x = expr;`.
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
    ///
    /// For most variants this is the keyword token (`let`, `return`, `for`,
    /// etc.).  For [`Statement::Expression`] it is the first token of the
    /// wrapped expression.  Used by the evaluator and error reporter to
    /// attribute diagnostics to a precise source location.
    pub(crate) fn token(&self) -> &Token {
        delegate_to_span!(self, start)
    }

    /// Returns a reference to the [`TokenPosition`] at which this statement
    /// ends in the source text.
    ///
    /// Used by the parser to attach accurate end-positions to enclosing nodes
    /// such as [`BlockStatement`].
    pub(crate) fn end_position(&self) -> &TokenPosition {
        delegate_to_span!(self, end)
    }
}

/// Delegates `Display` to each variant's concrete type.
///
/// The output is valid Mutant Lang source syntax.  Useful for debug printing,
/// REPL echo, and error messages that quote source fragments.
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
pub struct BlockStatement {
    /// Source span from the opening `{` through the closing `}`.
    span: TokenSpan,
    /// The ordered list of child statements inside the block.
    pub(crate) statements: Vec<Statement>,
}

impl BlockStatement {
    /// Constructs a `BlockStatement` from its source span and child statements.
    ///
    /// Takes ownership of both arguments.  Called by the parser immediately
    /// after consuming the closing `}`.
    pub(crate) const fn new(span: TokenSpan, statements: Vec<Statement>) -> Self {
        Self { span, statements }
    }
}

/// Formats each child statement in sequence with no extra separator.
///
/// Each child's own `Display` impl is responsible for trailing punctuation
/// (e.g. semicolons), so this impl simply concatenates them.
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
pub struct BreakStatement {
    /// Source span for the `break` keyword token.
    span: TokenSpan,
}

impl BreakStatement {
    /// Constructs a `BreakStatement` from the source span of its keyword.
    pub(crate) const fn new(span: TokenSpan) -> Self {
        Self { span }
    }
}

/// Formats as the literal keyword text (typically `"break"`).
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
pub(crate) struct ContinueStatement {
    /// Source span for the `continue` keyword token.
    span: TokenSpan,
}

impl ContinueStatement {
    /// Constructs a `ContinueStatement` from the source span of its keyword.
    pub(crate) const fn new(span: TokenSpan) -> Self {
        Self { span }
    }
}

/// Formats as the literal keyword text (typically `"continue"`).
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
pub(crate) struct ConstStatement {
    /// Source span from `const` through `;`.
    span: TokenSpan,
    /// The name being declared.
    identifier: Indentifier,
    /// The initialiser expression.
    value: Expression,
}

impl ConstStatement {
    /// Constructs a `ConstStatement` from its span, binding name, and value.
    ///
    /// Takes ownership of all three arguments.
    pub(crate) const fn new(span: TokenSpan, identifier: Indentifier, value: Expression) -> Self {
        Self {
            span,
            identifier,
            value,
        }
    }
}

/// Formats as `const <name> = <value>;`.
///
/// The keyword is taken from the span's start token literal so it round-trips
/// the exact source text.
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

/// An expression used in statement position: `expr;`.
///
/// Wraps an [`Expression`] that is evaluated purely for its side effects.
/// Whether the value is preserved (e.g. as the implicit return value of a
/// block) depends on the evaluator strategy rather than the AST node itself.
///
/// The `span` covers the first token of the expression through the trailing
/// `;` (if present).
pub(crate) struct ExpressionStatement {
    /// Source span for the expression token range.
    span: TokenSpan,
    /// The wrapped expression.
    expression: Expression,
}

impl ExpressionStatement {
    /// Constructs an `ExpressionStatement` from its span and inner expression.
    ///
    /// Takes ownership of both arguments.
    pub(crate) const fn new(span: TokenSpan, expression: Expression) -> Self {
        Self { span, expression }
    }
}

/// Formats as the inner expression's `Display` output, with no trailing semicolon.
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
pub(crate) struct ForStatement {
    /// Source span from the `for` keyword through the closing `}`.
    span: TokenSpan,
    /// The loop initializer, boxed to break the recursive `Statement` size cycle.
    initializer: Box<Statement>,
    /// The loop continuation condition, checked before each iteration.
    condition: Expression,
    /// The expression evaluated after each iteration body completes.
    increment: Expression,
    /// The loop body executed on each iteration.
    body: BlockStatement,
}

impl ForStatement {
    /// Constructs a `ForStatement` from its constituent parts.
    ///
    /// Takes ownership of all arguments.  `initializer` must already be boxed
    /// by the caller (see [`Statement::for_stmt`](super::Statement::for_stmt)).
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

    /// Returns a reference to the loop continuation condition expression.
    ///
    /// Evaluated by the evaluator before each iteration; a falsy result exits
    /// the loop.
    pub(crate) const fn condition(&self) -> &Expression {
        &self.condition
    }

    /// Returns a reference to the initializer statement.
    ///
    /// Derefs through the [`Box`] — no allocation occurs on access.
    pub(crate) fn initializer(&self) -> &Statement {
        self.initializer.as_ref()
    }

    /// Returns a reference to the increment expression.
    ///
    /// Evaluated after each successful execution of the loop body.
    pub(crate) const fn increment(&self) -> &Expression {
        &self.increment
    }

    /// Returns a reference to the loop body block.
    pub(crate) const fn body(&self) -> &BlockStatement {
        &self.body
    }
}

/// Formats as `for (<init>; <cond>; <incr>) {\n<body>\n}`.
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
pub(crate) struct LetStatement {
    /// Source span from `let` through `;`.
    span: TokenSpan,
    /// The name being declared.
    name: Indentifier,
    /// The initialiser expression whose value is bound to `name`.
    value: Expression,
}

impl LetStatement {
    /// Constructs a `LetStatement` from its span, binding name, and value.
    ///
    /// Takes ownership of all three arguments.
    pub(crate) const fn new(span: TokenSpan, name: Indentifier, value: Expression) -> Self {
        Self { span, name, value }
    }

    /// Returns a reference to the declared name.
    ///
    /// The evaluator uses this to register the binding in the current
    /// [`Environment`](crate::object::Environment) scope.
    pub(crate) const fn name(&self) -> &Indentifier {
        &self.name
    }

    /// Returns a reference to the initialiser expression.
    ///
    /// The evaluator evaluates this expression and stores the result under
    /// [`name`](Self::name) in the current scope.
    pub(crate) const fn value(&self) -> &Expression {
        &self.value
    }
}

/// Formats as `let <name> = <value>;`.
///
/// The keyword is taken from the span's start token literal so it round-trips
/// the exact source text.
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
pub(crate) struct ReturnStatement {
    /// Source span from `return` through `;`.
    span: TokenSpan,
    /// The expression whose evaluated result becomes the function's return value.
    value: Expression,
}

impl ReturnStatement {
    /// Constructs a `ReturnStatement` from its source span and return value expression.
    ///
    /// Takes ownership of both arguments.
    pub(crate) const fn new(span: TokenSpan, value: Expression) -> Self {
        Self { span, value }
    }

    /// Returns a reference to the return-value expression.
    ///
    /// The evaluator evaluates this and propagates the result up the call stack
    /// via an early-exit signal (typically a dedicated `ReturnValue` wrapper in
    /// the object system).
    pub(crate) const fn value(&self) -> &Expression {
        &self.value
    }
}

/// Formats as `return <value>;`.
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
pub(crate) struct WhileStatement {
    /// Source span from `while` through the closing `}`.
    span: TokenSpan,
    /// The loop continuation condition, re-evaluated before each iteration.
    condition: Expression,
    /// The loop body executed on each iteration where `condition` is truthy.
    body: BlockStatement,
}

impl WhileStatement {
    /// Constructs a `WhileStatement` from its span, condition, and body.
    ///
    /// Takes ownership of all three arguments.
    pub(crate) const fn new(span: TokenSpan, condition: Expression, body: BlockStatement) -> Self {
        Self {
            span,
            condition,
            body,
        }
    }

    /// Returns a reference to the loop continuation condition.
    ///
    /// The evaluator checks this before each iteration; a falsy result exits
    /// the loop.
    pub(crate) const fn condition(&self) -> &Expression {
        &self.condition
    }

    /// Returns a reference to the loop body block.
    ///
    /// Executed once per iteration for which [`condition`](Self::condition) is
    /// truthy.
    pub(crate) const fn body(&self) -> &BlockStatement {
        &self.body
    }
}

/// Formats as `while (<cond>) {\n<body>\n}`.
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
pub(crate) struct ClassStatement {
    /// Source span from `class` through the closing `}`.
    span: TokenSpan,
    /// The declared class name.
    name: Indentifier,
    /// The optional parent class name for single inheritance.
    parent_class: Option<Indentifier>,
    /// The optional constructor function; `None` means the class has no
    /// explicit constructor.
    constructor: Option<FunctionLiteral>,
    /// Named instance methods as `(name, function)` pairs, in declaration order.
    methods: Vec<(Indentifier, FunctionLiteral)>,
}

impl ClassStatement {
    /// Constructs a `ClassStatement` from all of its constituent parts.
    ///
    /// Takes ownership of all arguments.  `parent_class` and `constructor` are
    /// `None` for classes that do not use inheritance or explicit constructors.
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

    /// Returns a reference to the declared class name.
    ///
    /// Used by the evaluator to register the class object in the current
    /// environment scope under this name.
    pub(crate) const fn name(&self) -> &Indentifier {
        &self.name
    }

    /// Returns the optional parent class name, if this is a derived class.
    ///
    /// `None` indicates no `extends` clause.  The evaluator looks up the
    /// parent by name in the current scope when `Some`.
    pub(crate) const fn parent_class(&self) -> Option<&Indentifier> {
        self.parent_class.as_ref()
    }

    /// Returns the optional constructor function literal.
    ///
    /// `None` means the class has no explicit constructor; the evaluator may
    /// synthesise a default no-op constructor in that case.
    pub(crate) const fn constructor(&self) -> Option<&FunctionLiteral> {
        self.constructor.as_ref()
    }

    /// Returns the method list as a borrowed slice of `(name, body)` pairs.
    ///
    /// Methods are in declaration order.  The evaluator registers each method
    /// on the class prototype under the given name.
    pub(crate) const fn methods(&self) -> &[(Indentifier, FunctionLiteral)] {
        self.methods.as_slice()
    }
}

/// Formats the class header as `class <name>`, omitting the body.
///
/// The abbreviated form is intentional for use in error messages and debug
/// output where a full body dump would be too verbose.
impl fmt::Display for ClassStatement {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "class {}", self.name)
    }
}
