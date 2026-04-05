//! Statement evaluation for the Mutant Lang tree-walking interpreter.
//!
//! This module extends [`Evaluator`] with an `impl` block that handles every
//! [`Statement`] variant produced by the parser. It is the runtime counterpart
//! to the AST definitions in [`crate::ast::statements`].
//!
//! # Execution model
//!
//! Statements are evaluated for their **side-effects** (binding variables,
//! running loops, returning values) rather than for a meaningful result. Most
//! methods therefore return [`Object::Null`] on success; the exceptions are
//! control-flow sentinels ([`Object::Return`], [`Object::Break`],
//! [`Object::Continue`]) which bubble up through nested call frames and loop
//! bodies until they are caught at the appropriate boundary.
//!
//! All variable bindings are stored in an [`Env`] — a reference-counted,
//! interior-mutable [`Environment`] chain — so that child scopes can shadow
//! parent bindings without mutating them.

use crate::ast::expression::Expression;
use crate::ast::statements::{self, Statement};

use super::env::{Env, Environment};
use super::objects::{FunctionObject, Object};
use super::{EvalError, Evaluator};

// Statement evaluation
impl Evaluator {
    /// Dispatch a single statement to its concrete evaluator.
    ///
    /// This is the central entry point for statement evaluation. It pattern-
    /// matches on the [`Statement`] variant and forwards to the appropriate
    /// private method. Two variants — `Break` and `Continue` — are handled
    /// inline because they carry no operands and always produce the same
    /// control-flow sentinel value.
    ///
    /// The returned [`Object`] is **not** a meaningful value in most cases; it
    /// is either [`Object::Null`] (statement produced no value) or one of the
    /// control-flow sentinels ([`Object::Return`], [`Object::Break`],
    /// [`Object::Continue`]) that the caller must propagate.
    ///
    /// # Errors
    ///
    /// Propagates any [`EvalError`] returned by the concrete evaluator for the
    /// matched variant (type mismatches, undefined variables, redeclarations,
    /// immutable-assignment violations, circular inheritance, etc.).
    pub fn eval_statement(&self, stmt: &Statement, env: &Env) -> Result<Object, EvalError> {
        match stmt {
            Statement::Let(s) => self.eval_let(s, env),
            Statement::Const(s) => self.eval_const(s, env),
            Statement::Return(s) => self.eval_return(s, env),
            Statement::Block(s) => self.eval_block(s, env),
            Statement::While(s) => self.eval_while(s, env),
            Statement::For(s) => self.eval_for(s, env),
            Statement::Break(_) => Ok(Object::Break),
            Statement::Continue(_) => Ok(Object::Continue),
            Statement::Expression(s) => self.eval_expression(s.expression(), env),
            Statement::Class(s) => Self::eval_class(s, env),
        }
    }

    /// Evaluate a `let` variable declaration and bind it in the current scope.
    ///
    /// Performs two pre-flight checks before evaluating the initialiser
    /// expression:
    ///
    /// 1. **Immutability guard** — if a `const` with the same name is already
    ///    visible in any enclosing scope, the assignment is rejected. This
    ///    prevents shadowing a constant with a mutable binding.
    /// 2. **Redeclaration guard** — if `var_name` is already declared *in the
    ///    current scope* (not a parent), a duplicate-declaration error is
    ///    returned. Shadowing across scope boundaries is allowed.
    ///
    /// On success the binding is stored via [`Environment::define`] and
    /// [`Object::Null`] is returned (statement has no expression value).
    ///
    /// # Errors
    ///
    /// - [`EvalError::ImmutableAssignment`] — a `const` with the same name
    ///   exists in an enclosing scope.
    /// - [`EvalError::AlreadyDeclared`] — the name is already declared in the
    ///   *current* (local) scope.
    /// - Any [`EvalError`] propagated from evaluating the initialiser
    ///   expression.
    fn eval_let(&self, stmt: &statements::LetStatement, env: &Env) -> Result<Object, EvalError> {
        let var_name = stmt.name().value();

        if env.borrow().is_immutable(var_name) {
            return Err(EvalError::ImmutableAssignment(var_name.to_string()));
        }

        if env.borrow().contains_locally(var_name) {
            return Err(EvalError::AlreadyDeclared(var_name.to_string()));
        }

        let val = self.eval_expression(stmt.value(), env)?;
        env.borrow_mut().define(var_name, val);
        Ok(Object::Null)
    }

    /// Evaluate a `const` declaration and bind an immutable value in the current scope.
    ///
    /// Unlike [`eval_let`](Self::eval_let), this method does **not** check for
    /// an existing immutable binding in outer scopes — a `const` may shadow
    /// another `const` from an enclosing scope. It only rejects a redeclaration
    /// *within the same scope*.
    ///
    /// The value is stored via [`Environment::define_const`], which marks the
    /// binding as immutable so that any future `let` or assignment targeting
    /// the same name triggers [`EvalError::ImmutableAssignment`].
    ///
    /// # Errors
    ///
    /// - [`EvalError::AlreadyDeclared`] — the name is already declared in the
    ///   current scope (whether mutable or immutable).
    /// - Any [`EvalError`] propagated from evaluating the initialiser
    ///   expression.
    fn eval_const(
        &self,
        stmt: &statements::ConstStatement,
        env: &Env,
    ) -> Result<Object, EvalError> {
        let var_name = stmt.name().value();

        if env.borrow().contains_locally(var_name) {
            return Err(EvalError::AlreadyDeclared(var_name.to_string()));
        }

        let val = self.eval_expression(stmt.value(), env)?;
        env.borrow_mut().define_const(var_name, val);
        Ok(Object::Null)
    }

    /// Evaluate a block statement in a new child scope.
    ///
    /// Creates a fresh [`Environment`] that is a child of `env` (with
    /// `is_block = true`) so that bindings declared inside the block do not
    /// leak into the enclosing scope.  Statements are executed sequentially;
    /// execution short-circuits as soon as a control-flow sentinel is produced:
    ///
    /// | Sentinel produced     | Action                                      |
    /// |-----------------------|---------------------------------------------|
    /// | [`Object::Return`]    | Stop immediately; propagate to the caller.  |
    /// | [`Object::Break`]     | Stop immediately; propagate to the loop.    |
    /// | [`Object::Continue`]  | Stop immediately; propagate to the loop.    |
    ///
    /// The last evaluated value is returned, which may be one of the
    /// sentinels above or simply the result of the last expression statement.
    ///
    /// # Errors
    ///
    /// Propagates any [`EvalError`] from evaluating individual statements.
    pub(super) fn eval_block(
        &self,
        stmt: &statements::BlockStatement,
        env: &Env,
    ) -> Result<Object, EvalError> {
        let block_env = Environment::new_child(env, true);
        let mut result = Object::Null;
        for stmt in &stmt.statements {
            result = self.eval_statement(stmt, &block_env)?;
            match &result {
                Object::Return(_) | Object::Break | Object::Continue => break,
                _ => {}
            }
        }

        Ok(result)
    }

    /// Evaluate a loop condition and, if truthy, execute one iteration of the loop body.
    ///
    /// This is a shared helper factored out of [`eval_while`](Self::eval_while)
    /// and [`eval_for`](Self::eval_for) to avoid duplicating the
    /// condition-check / body-execution / sentinel-handling pattern.
    ///
    /// Returns:
    ///
    /// - `Ok(None)` — the condition was falsy **or** the body produced
    ///   [`Object::Break`]; in either case the calling loop should terminate.
    /// - `Ok(Some(Object::Return(_)))` — a `return` statement was encountered
    ///   inside the body; the caller must propagate it out of the enclosing
    ///   function.
    /// - `Ok(Some(other))` — normal iteration result (including
    ///   [`Object::Continue`], which is handled by the loop driver in
    ///   [`eval_while`](Self::eval_while) / [`eval_for`](Self::eval_for) by
    ///   simply running the next iteration).
    ///
    /// # Errors
    ///
    /// Propagates any [`EvalError`] from evaluating the condition expression or
    /// the body block.
    fn eval_loop_body(
        &self,
        condition: &Expression,
        body: &statements::BlockStatement,
        env: &Env,
    ) -> Result<Option<Object>, EvalError> {
        let cond = self.eval_expression(condition, env)?;
        if !cond.is_truthy() {
            return Ok(None);
        }

        let result = self.eval_block(body, env)?;
        match result {
            Object::Break => Ok(None),
            _ => Ok(Some(result)),
        }
    }

    /// Evaluate a `while` loop statement.
    ///
    /// Repeatedly evaluates the condition and body via
    /// [`eval_loop_body`](Self::eval_loop_body) until one of the following
    /// termination conditions is met:
    ///
    /// - The condition evaluates to a falsy value → `Ok(Object::Null)`.
    /// - A `break` statement is executed inside the body → `Ok(Object::Null)`.
    /// - A `return` statement is executed inside the body → the
    ///   [`Object::Return`] sentinel is propagated to the enclosing function
    ///   frame.
    ///
    /// `while` loops are always expressions of type [`Object::Null`] from the
    /// perspective of the surrounding scope (unless they short-circuit via
    /// `return`).
    ///
    /// # Errors
    ///
    /// Propagates any [`EvalError`] from condition or body evaluation.
    fn eval_while(
        &self,
        stmt: &statements::WhileStatement,
        env: &Env,
    ) -> Result<Object, EvalError> {
        loop {
            match self.eval_loop_body(stmt.condition(), stmt.body(), env)? {
                None => break,
                Some(r @ Object::Return(_)) => return Ok(r),
                _ => {}
            }
        }
        Ok(Object::Null)
    }

    /// Evaluate a `for` loop statement.
    ///
    /// Behaves like [`eval_while`](Self::eval_while) but additionally evaluates
    /// the increment expression **after every successful body iteration**.  The
    /// increment is *not* evaluated when `break`, `return`, or a falsy
    /// condition causes the loop to exit.
    ///
    /// Termination semantics are identical to [`eval_while`](Self::eval_while):
    ///
    /// - Falsy condition or `break` → `Ok(Object::Null)`.
    /// - `return` inside the body → propagate [`Object::Return`] upward.
    ///
    /// The result of the increment expression is discarded; it is evaluated
    /// purely for its side-effects (typically `i = i + 1`).
    ///
    /// # Errors
    ///
    /// Propagates any [`EvalError`] from condition, body, or increment
    /// evaluation.
    fn eval_for(&self, stmt: &statements::ForStatement, env: &Env) -> Result<Object, EvalError> {
        loop {
            match self.eval_loop_body(stmt.condition(), stmt.body(), env)? {
                None => break,
                Some(r @ Object::Return(_)) => return Ok(r),
                _ => {}
            }
            self.eval_expression(stmt.increment(), env)?;
        }
        Ok(Object::Null)
    }

    /// Evaluate a `return` statement and wrap its value in [`Object::Return`].
    ///
    /// The value expression is evaluated eagerly and then boxed inside
    /// [`Object::Return`].  This sentinel travels up the call stack through
    /// [`eval_block`](Self::eval_block) and loop evaluators until it is
    /// unwrapped by the function-call site in the expression evaluator.
    ///
    /// Wrapping in a `Box` keeps [`Object`] a fixed size on the stack while
    /// still allowing the return value to be any [`Object`] variant, including
    /// arbitrarily large ones.
    ///
    /// # Errors
    ///
    /// Propagates any [`EvalError`] from evaluating the return value expression.
    fn eval_return(
        &self,
        stmt: &statements::ReturnStatement,
        env: &Env,
    ) -> Result<Object, EvalError> {
        let val = self.eval_expression(stmt.value(), env)?;
        Ok(Object::Return(Box::new(val)))
    }

    /// Evaluate a `class` declaration and register it in the current environment.
    ///
    /// Class evaluation proceeds in four phases:
    ///
    /// 1. **Redeclaration check** — rejects the declaration if `class_name` is
    ///    already bound in the current scope.
    /// 2. **Parent resolution** — if an `extends` clause is present, looks up
    ///    the parent name in `env`, verifies it resolves to an
    ///    [`Object::Class`], and walks the inheritance chain to detect circular
    ///    inheritance (e.g. `class A extends A`).
    /// 3. **Constructor and method capture** — the optional constructor and all
    ///    named methods are converted to [`Object::Function`] / [`Object::function`]
    ///    values, capturing the *current* `env` as a closure environment.
    ///    This means methods close over the scope in which the class is defined,
    ///    not the scope of a particular instance.
    /// 4. **Binding** — the assembled [`Object::Class`] is stored in `env` via
    ///    [`Environment::define`] and [`Object::Null`] is returned.
    ///
    /// # Errors
    ///
    /// - [`EvalError::AlreadyDeclared`] — `class_name` is already declared in
    ///   the current scope.
    /// - [`EvalError::UndefinedVariable`] — the parent class name does not
    ///   resolve in the current environment.
    /// - [`EvalError::TypeMismatch`] — the name in the `extends` clause
    ///   resolves to a non-class value.
    /// - [`EvalError::Runtime`] — circular inheritance was detected in the
    ///   parent chain.
    fn eval_class(stmt: &statements::ClassStatement, env: &Env) -> Result<Object, EvalError> {
        let class_name = stmt.name().value();

        if env.borrow().contains_locally(class_name) {
            return Err(EvalError::AlreadyDeclared(class_name.to_string()));
        }

        let parent = if let Some(parent_ident) = stmt.parent_class() {
            let resolved = env
                .borrow()
                .get(parent_ident.value())
                .ok_or_else(|| EvalError::UndefinedVariable(parent_ident.value().to_string()))?;

            {
                let Object::Class(cls) = &resolved else {
                    return Err(EvalError::type_mismatch("class", resolved));
                };

                let mut current = Some(cls);
                while let Some(c) = current {
                    if c.name == class_name {
                        return Err(EvalError::runtime(format!(
                            "circular inheritance: '{class_name}' cannot extend itself"
                        )));
                    }
                    current = c.parent.as_deref();
                }
            }
            let Object::Class(class_obj) = resolved else {
                unreachable!()
            };
            Some(class_obj)
        } else {
            None
        };

        let constructor = stmt.constructor().map(|ctor| {
            FunctionObject::new(ctor.params().to_vec(), ctor.body().clone(), Env::clone(env))
        });

        let methods = stmt
            .methods()
            .iter()
            .map(|(name, func)| {
                let method = super::objects::FunctionObject::new(
                    func.params().to_vec(),
                    func.body().clone(),
                    Env::clone(env),
                );

                (name.value().to_string(), method)
            })
            .collect();

        let class = Object::class(class_name, parent, constructor, methods, Env::clone(env));
        env.borrow_mut().define(class_name, class);
        Ok(Object::Null)
    }
}
