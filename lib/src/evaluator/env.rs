//! Scoped variable environment for the Mutant Lang tree-walking evaluator.
//!
//! This module provides the runtime binding store used during evaluation. Every
//! scope — global, function, or block — is represented by an [`Environment`]
//! node. Scopes are chained into a singly-linked tree via [`parent`](Environment)
//! pointers, forming the classic *environment model* of lexical scoping.
//!
//! # Sharing and mutation
//!
//! Because closures must capture their enclosing environment and because the
//! evaluator may hold multiple live references to the same scope at once, each
//! environment node is wrapped in `Rc<RefCell<…>>` and exposed through the
//! [`Env`] type alias. `Rc` provides shared ownership without a garbage
//! collector; `RefCell` provides interior mutability so that assignment can
//! mutate through a shared reference at runtime.
//!
//! The design deliberately avoids `Arc` (thread-safe reference counting)
//! because the evaluator is single-threaded.

use crate::evaluator::objects::Object;
use std::cell::RefCell;
use std::collections::{HashMap, HashSet};
use std::rc::Rc;

pub type Env = Rc<RefCell<Environment>>;

/// A single scope frame in the interpreter's lexical environment chain.
///
/// Each `Environment` holds the variable bindings introduced in one scope and
/// an optional pointer to its enclosing scope. Variable lookup walks the chain
/// from innermost to outermost, implementing standard lexical scoping rules.
///
/// Construct environments through the associated factory methods [`new`] and
/// [`new_child`] rather than directly — both return an [`Env`] handle so
/// callers never hold a bare, unshared `Environment`.
///
/// [`new`]: Environment::new
/// [`new_child`]: Environment::new_child
#[derive(Debug)]
pub struct Environment {
    bindings: HashMap<String, Object>,
    immutables: HashSet<String>,
    parent: Option<Env>,
    is_block: bool,
}

impl Environment {
    /// Create a new global (root) environment with no parent scope.
    ///
    /// Use this once at the start of a top-level evaluation to produce the
    /// outermost [`Env`]. All subsequent scopes should be created with
    /// [`new_child`](Self::new_child).
    ///
    /// # Examples
    ///
    /// ```rust
    /// # use mutant_lang::evaluator::env::Environment;
    /// let global = Environment::new();
    /// ```
    pub fn new() -> Env {
        Rc::new(RefCell::new(Self {
            bindings: HashMap::new(),
            immutables: HashSet::new(),
            parent: None,
            is_block: false,
        }))
    }

    /// Create a child scope whose enclosing scope is `parent`.
    ///
    /// The new scope starts empty; the `parent` pointer is a cloned `Rc`,
    /// so no binding data is copied. The `is_block` flag distinguishes a
    /// block scope (`true`) from a function-call scope (`false`).
    ///
    /// # Examples
    ///
    /// ```rust
    /// # use mutant_lang::evaluator::env::Environment;
    /// let global = Environment::new();
    /// let func_scope = Environment::new_child(&global, false);
    /// let block_scope = Environment::new_child(&func_scope, true);
    /// ```
    pub fn new_child(parent: &Env, is_block: bool) -> Env {
        Rc::new(RefCell::new(Self {
            bindings: HashMap::new(),
            immutables: HashSet::new(),
            parent: Some(Rc::clone(parent)),
            is_block,
        }))
    }

    /// Look up a variable by name, walking the scope chain if necessary.
    ///
    /// Searches this scope first, then each ancestor in turn. Returns `None`
    /// if the name is not bound anywhere in the chain.
    ///
    /// The returned value is a clone of the stored [`Object`]. Because
    /// `Object` uses `Rc` internally for heap-allocated variants, the clone
    /// is shallow — it shares the underlying data rather than copying it.
    ///
    /// # Examples
    ///
    /// ```rust
    /// # use mutant_lang::evaluator::env::Environment;
    /// # use mutant_lang::evaluator::objects::Object;
    /// let env = Environment::new();
    /// env.borrow_mut().define("x", Object::Integer(42));
    ///
    /// assert_eq!(env.borrow().get("x"), Some(Object::Integer(42)));
    /// assert_eq!(env.borrow().get("y"), None);
    /// ```
    pub fn get(&self, name: &str) -> Option<Object> {
        if let Some(val) = self.bindings.get(name) {
            return Some(val.clone());
        }
        self.parent.as_ref()?.borrow().get(name)
    }

    /// Bind `name` to `val` in this scope, shadowing any outer binding.
    ///
    /// Corresponds to a `let` declaration in the source language. If `name`
    /// is already bound in this scope the old value is silently replaced;
    /// bindings in enclosing scopes are unaffected.
    ///
    /// Accepts any `impl Into<String>` so callers can pass `&str` or `String`
    /// without an explicit conversion.
    ///
    /// # Examples
    ///
    /// ```rust
    /// # use mutant_lang::evaluator::env::Environment;
    /// # use mutant_lang::evaluator::objects::Object;
    /// let env = Environment::new();
    /// env.borrow_mut().define("x", Object::Integer(1));
    /// env.borrow_mut().define("x", Object::Integer(2)); // rebind
    /// assert_eq!(env.borrow().get("x"), Some(Object::Integer(2)));
    /// ```
    pub fn define(&mut self, name: impl Into<String>, val: Object) {
        self.bindings.insert(name.into(), val);
    }

    /// Bind `name` to `val` and mark it as immutable (`const`).
    ///
    /// After this call, [`is_immutable`](Self::is_immutable) returns `true`
    /// for `name` throughout the entire scope chain, preventing reassignment
    /// via [`assign`](Self::assign). The evaluator is responsible for checking
    /// immutability before calling `assign`.
    ///
    /// # Examples
    ///
    /// ```rust
    /// # use mutant_lang::evaluator::env::Environment;
    /// # use mutant_lang::evaluator::objects::Object;
    /// let env = Environment::new();
    /// env.borrow_mut().define_const("PI", Object::Float(3.14159));
    /// assert!(env.borrow().is_immutable("PI"));
    /// ```
    pub fn define_const(&mut self, name: impl Into<String>, val: Object) {
        let name = name.into();
        self.immutables.insert(name.clone());
        self.bindings.insert(name, val);
    }

    /// Return `true` if `name` was declared `const` anywhere in the scope chain.
    ///
    /// Walks from this scope toward the root, returning `true` as soon as the
    /// name is found in any [`immutables`](Environment) set. Returns `false`
    /// if the name is mutable or not bound at all.
    ///
    /// The evaluator should call this before [`assign`](Self::assign) to
    /// produce a meaningful "cannot assign to const" error rather than silently
    /// overwriting a constant.
    ///
    /// # Examples
    ///
    /// ```rust
    /// # use mutant_lang::evaluator::env::Environment;
    /// # use mutant_lang::evaluator::objects::Object;
    /// let env = Environment::new();
    /// env.borrow_mut().define("x", Object::Integer(1));
    /// env.borrow_mut().define_const("C", Object::Integer(99));
    ///
    /// assert!(!env.borrow().is_immutable("x"));
    /// assert!(env.borrow().is_immutable("C"));
    /// ```
    pub fn is_immutable(&self, name: &str) -> bool {
        if self.immutables.contains(name) {
            return true;
        }
        self.parent
            .as_ref()
            .is_some_and(|p| p.borrow().is_immutable(name))
    }

    /// Reassign an existing binding in the nearest scope where it is defined.
    ///
    /// Walks the scope chain from innermost to outermost looking for `name`.
    /// When found, overwrites the binding with `val` and returns `true`.
    /// Returns `false` if the name is not bound in any enclosing scope,
    /// indicating an undefined-variable error that the caller should surface.
    ///
    /// This method does **not** check [`is_immutable`](Self::is_immutable);
    /// the evaluator must guard against const reassignment before calling it.
    ///
    /// # Examples
    ///
    /// ```rust
    /// # use mutant_lang::evaluator::env::Environment;
    /// # use mutant_lang::evaluator::objects::Object;
    /// let env = Environment::new();
    /// env.borrow_mut().define("x", Object::Integer(0));
    ///
    /// let updated = env.borrow_mut().assign("x", Object::Integer(10));
    /// assert!(updated);
    /// assert_eq!(env.borrow().get("x"), Some(Object::Integer(10)));
    ///
    /// let not_found = env.borrow_mut().assign("y", Object::Integer(5));
    /// assert!(!not_found);
    /// ```
    pub fn assign(&mut self, name: &str, val: Object) -> bool {
        if self.bindings.contains_key(name) {
            self.bindings.insert(name.to_string(), val);
            return true;
        }
        if let Some(parent) = &self.parent {
            return parent.borrow_mut().assign(name, val);
        }
        false
    }

    /// Return `true` if `name` is bound in this scope, ignoring any parent.
    ///
    /// Useful for detecting shadowing or for checking whether a `let`
    /// redeclaration in the same block should be allowed or rejected.
    /// Unlike [`get`](Self::get), this never walks up the scope chain.
    ///
    /// # Examples
    ///
    /// ```rust
    /// # use mutant_lang::evaluator::env::Environment;
    /// # use mutant_lang::evaluator::objects::Object;
    /// let parent = Environment::new();
    /// parent.borrow_mut().define("x", Object::Integer(1));
    ///
    /// let child = Environment::new_child(&parent, false);
    /// assert!(!child.borrow().contains_locally("x")); // in parent, not here
    /// child.borrow_mut().define("x", Object::Integer(2));
    /// assert!(child.borrow().contains_locally("x"));  // now local
    /// ```
    pub fn contains_locally(&self, name: &str) -> bool {
        self.bindings.contains_key(name)
    }
}
