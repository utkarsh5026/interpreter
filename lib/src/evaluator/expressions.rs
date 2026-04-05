use std::collections::HashMap;
use std::rc::Rc;

use crate::ast::expression::{self, Expression, Indentifier};
use crate::ast::literal::Literal;
use crate::token::Operator;

use super::env::{Env, Environment};
use super::objects::{HashKey, Object};
use super::{EvalError, Evaluator};

const THIS: &str = "this";
const CLASS_CONTEXT: &str = "class_context";

// Expression evaluation
impl Evaluator {
    pub(super) fn eval_expression(
        &self,
        expr: &Expression,
        env: &Env,
    ) -> Result<Object, EvalError> {
        match expr {
            Expression::Literal(lit) => self.eval_literal(lit, env),
            Expression::Identifier(ident) => self.eval_identifier(ident, env),
            Expression::Prefix(prefix) => self.eval_prefix(prefix, env),
            Expression::Infix(infix) => self.eval_infix(infix, env),
            Expression::If(if_expr) => self.eval_if(if_expr, env),
            Expression::Assignment(assign) => self.eval_assignment(assign, env),
            Expression::Call(call) => self.eval_call(call, env),
            Expression::Index(idx) => self.eval_index(idx, env),
            Expression::New(new) => self.eval_new(new, env),
            Expression::Super(super_expr) => self.eval_super(super_expr, env),
            Expression::This(_) => self.eval_this(env),
            Expression::Property(prop) => self.eval_property(prop, env),
        }
    }

    fn eval_call(&self, call: &expression::CallExpression, env: &Env) -> Result<Object, EvalError> {
        let function = self.eval_expression(call.function(), env)?;
        let args = self.parse_expressions(call.args(), env)?;

        match function {
            Object::Function(func) => {
                if args.len() != func.params.len() {
                    return Err(EvalError::wrong_arg_count(func.params.len(), args.len()));
                }

                let call_env = Environment::new_child(&func.env, false);
                for (param, arg) in func.params.iter().zip(args) {
                    call_env.borrow_mut().define(param.value(), arg);
                }

                let result = self.eval_block(&func.body, &call_env)?;

                match result {
                    Object::Return(val) => Ok(*val),
                    other => Ok(other),
                }
            }
            _ => Err(EvalError::runtime("expected a function")),
        }
    }

    fn eval_super(
        &self,
        super_expr: &expression::SuperExpression,
        env: &Env,
    ) -> Result<Object, EvalError> {
        let this = env
            .borrow()
            .get("this")
            .ok_or_else(|| EvalError::runtime("'this' is not available in this context"))?;

        let instance_class = match &this {
            Object::Instance(inst) => *inst.class.clone(),
            _ => return Err(EvalError::runtime("'this' is not an instance")),
        };

        let current_class = match env.borrow().get("__class_context__") {
            Some(Object::ClassContext(cls)) => cls,
            _ => instance_class,
        };

        let parent_class = current_class.parent.map(|b| *b).ok_or_else(|| {
            EvalError::runtime(format!(
                "class '{}' has no parent class",
                current_class.name
            ))
        })?;

        let args = self.parse_expressions(super_expr.args(), env)?;

        if super_expr.is_constructor_call() {
            let call_info = parent_class
                .constructor
                .as_deref()
                .map(|ctor| (ctor.env.clone(), ctor.params.clone(), ctor.body.clone()));

            match call_info {
                None if args.is_empty() => Ok(this),

                None => Err(EvalError::runtime(format!(
                    "class '{}' has no constructor but {} argument(s) were supplied",
                    parent_class.name,
                    args.len()
                ))),

                Some((ctor_env, ctor_params, ctor_body)) => {
                    if args.len() != ctor_params.len() {
                        return Err(EvalError::wrong_arg_count(ctor_params.len(), args.len()));
                    }

                    let call_env = Environment::new_child(&ctor_env, false);

                    // Bind this so the constructor can set fields via self.x = ...
                    call_env.borrow_mut().define("this", this.clone());

                    // Advance the class context to the parent so that any super()
                    // call *inside* the parent constructor walks up one more level.
                    call_env
                        .borrow_mut()
                        .define("__class_context__", Object::ClassContext(parent_class));

                    for (param, arg) in ctor_params.iter().zip(args) {
                        call_env.borrow_mut().define(param.value(), arg);
                    }

                    self.eval_block(&ctor_body, &call_env)?;

                    Ok(this)
                }
            }
        } else {
            let method_name = match super_expr.method() {
                Some(Expression::Identifier(ident)) => ident.value().to_owned(),
                _ => return Err(EvalError::runtime("invalid super method expression")),
            };

            let (method_env_ref, method_params, method_body) = parent_class
                .find_method(&method_name)
                .map(|m| (m.env.clone(), m.params.clone(), m.body.clone()))
                .ok_or_else(|| {
                    EvalError::runtime(format!(
                        "method '{}' not found in parent class '{}'",
                        method_name, parent_class.name
                    ))
                })?;

            if args.len() != method_params.len() {
                return Err(EvalError::wrong_arg_count(method_params.len(), args.len()));
            }

            let call_env = Environment::new_child(&method_env_ref, false);
            call_env.borrow_mut().define("this", this);
            call_env
                .borrow_mut()
                .define("__class_context__", Object::ClassContext(parent_class));

            for (param, arg) in method_params.iter().zip(args) {
                call_env.borrow_mut().define(param.value(), arg);
            }

            match self.eval_block(&method_body, &call_env)? {
                Object::Return(val) => Ok(*val),
                other => Ok(other),
            }
        }
    }

    pub(super) fn eval_literal(&self, lit: &Literal, env: &Env) -> Result<Object, EvalError> {
        match lit {
            Literal::Integer(i) => Ok(i.value().into()),
            Literal::Bool(b) => Ok(b.value().into()),
            Literal::String(s) => Ok(s.value().into()),
            Literal::Null(_) => Ok(Object::Null),
            Literal::Array(a) => self.eval_array_literal(a, env),
            Literal::Hash(h) => self.eval_hash_literal(h, env),
            Literal::Func(f) => Ok(Object::function(
                f.params().to_vec(),
                f.body().clone(),
                Rc::clone(env),
            )),
        }
    }

    fn eval_array_literal(
        &self,
        lit: &crate::ast::literal::ArrayLiteral,
        env: &Env,
    ) -> Result<Object, EvalError> {
        let elements = lit
            .elements()
            .iter()
            .map(|e| self.eval_expression(e, env))
            .collect::<Result<Vec<_>, _>>()?;
        Ok(Object::Array(elements))
    }

    fn eval_hash_literal(
        &self,
        lit: &crate::ast::literal::HashLiteral,
        env: &Env,
    ) -> Result<Object, EvalError> {
        let mut map = HashMap::new();
        for (key_expr, val_expr) in lit.pairs() {
            let key = self.eval_expression(key_expr, env)?;
            let val = self.eval_expression(val_expr, env)?;
            map.insert(HashKey::try_from(key)?, val);
        }
        Ok(Object::Hash(map))
    }

    fn eval_identifier(&self, ident: &Indentifier, env: &Env) -> Result<Object, EvalError> {
        let name = ident.value();
        if let Some(val) = env.borrow().get(name) {
            return Ok(val);
        }
        Err(EvalError::UndefinedVariable(ident.value().to_owned()))
    }

    fn eval_if(&self, if_expr: &expression::IfExpression, env: &Env) -> Result<Object, EvalError> {
        for (condition, consequence) in if_expr.conditions().iter().zip(if_expr.consequences()) {
            let condition_val = self.eval_expression(condition, env)?;
            if condition_val.is_truthy() {
                return self.eval_block(consequence, env);
            }
        }

        if_expr
            .alternative()
            .map_or_else(|| Ok(Object::Null), |alt| self.eval_block(alt, env))
    }

    fn eval_index(
        &self,
        index_expr: &expression::IndexExpression,
        env: &Env,
    ) -> Result<Object, EvalError> {
        let left = self.eval_expression(index_expr.left(), env)?;
        let index = self.eval_expression(index_expr.index(), env)?;

        match (left, index) {
            (Object::Array(elements), Object::Integer(i)) => {
                let len = elements.len() as i64;
                if i < 0 || i >= len {
                    return Err(EvalError::Runtime(format!(
                        "index out of bounds: {i} is not in range [0, {len})"
                    )));
                }
                Ok(elements.into_iter().nth(i as usize).unwrap())
            }

            (Object::Array(_), other) => Err(EvalError::TypeMismatch {
                expected: "Integer".into(),
                got: format!("{other}"),
            }),

            (Object::Hash(map), index) => {
                let key = HashKey::try_from(index)?;
                Ok(map.get(&key).cloned().unwrap_or(Object::Null))
            }

            (other, _) => Err(EvalError::TypeMismatch {
                expected: "Array or Hash".into(),
                got: format!("{other}"),
            }),
        }
    }

    fn eval_this(&self, env: &Env) -> Result<Object, EvalError> {
        let this = env
            .borrow()
            .get("this")
            .ok_or_else(|| EvalError::runtime("'this' is not available in this context"))?;

        match this {
            Object::Instance(_) => Ok(this),
            _ => Err(EvalError::runtime("'this' is not an instance")),
        }
    }

    fn eval_new(
        &self,
        new_expr: &expression::NewExpression,
        env: &Env,
    ) -> Result<Object, EvalError> {
        let class = self.eval_expression(new_expr.class_name(), env)?;
        let Object::Class(class_obj) = class else {
            return Err(EvalError::runtime("expected a class"));
        };

        let args = self.parse_expressions(new_expr.args(), env)?;
        let instance = Object::instance(class_obj.clone(), Environment::new_child(env, false));

        if let Some(ctor) = &class_obj.constructor {
            if args.len() != ctor.params.len() {
                return Err(EvalError::WrongArgCount {
                    expected: ctor.params.len(),
                    got: args.len(),
                });
            }

            let ctor_env = Environment::new_child(&ctor.env, false);
            ctor_env.borrow_mut().define("this", instance.clone());

            for (param, arg) in ctor.params.iter().zip(args) {
                ctor_env.borrow_mut().define(param.value(), arg);
            }

            self.eval_block(&ctor.body, &ctor_env)?;
        } else if !args.is_empty() {
            return Err(EvalError::runtime(format!(
                "class '{}' has no constructor but {} argument(s) were supplied",
                class_obj.name,
                args.len()
            )));
        }

        Ok(instance)
    }

    fn parse_expressions(&self, args: &[Expression], env: &Env) -> Result<Vec<Object>, EvalError> {
        args.iter()
            .map(|arg| self.eval_expression(arg, env))
            .collect()
    }

    fn eval_prefix(
        &self,
        prefix: &expression::PrefixExpression,
        env: &Env,
    ) -> Result<Object, EvalError> {
        let right = self.eval_expression(prefix.right(), env)?;
        match prefix.operator() {
            Operator::Bang => !right,
            Operator::Minus => -right,
            _ => Err(EvalError::runtime(format!(
                "unknown operator: {}",
                prefix.operator()
            ))),
        }
    }

    fn eval_infix(
        &self,
        infix: &expression::InfixExpression,
        env: &Env,
    ) -> Result<Object, EvalError> {
        let left = self.eval_expression(infix.left(), env)?;
        let right = self.eval_expression(infix.right(), env)?;
        let op = infix.operator();

        // Stage 1 — null guard (borrow only, no consume)
        // Must come first: null + anything should say "cannot be used with null",
        // not "type mismatch for +".
        match (op, &left, &right) {
            (Operator::Eq, Object::Null, Object::Null) => return Ok(true.into()),
            (Operator::NotEq, Object::Null, Object::Null) => return Ok(false.into()),
            (Operator::Eq, Object::Null, _) | (Operator::Eq, _, Object::Null) => {
                return Ok(false.into())
            }
            (Operator::NotEq, Object::Null, _) | (Operator::NotEq, _, Object::Null) => {
                return Ok(true.into())
            }
            (_, Object::Null, _) | (_, _, Object::Null) => {
                return Err(EvalError::runtime(format!(
                    "operator '{op}' cannot be used with null"
                )));
            }
            _ => {}
        }

        // Stage 2 — arithmetic (consumes left/right via std::ops traits)
        match op {
            Operator::Plus => return left + right,
            Operator::Minus => return left - right,
            Operator::Asterisk => return left * right,
            Operator::Slash => return left / right,
            Operator::Modulus => return left % right,
            Operator::IntDivision => return left.int_div(&right),
            _ => {}
        }

        // Stage 3 — comparisons and boolean (borrow is fine, left/right untouched)
        match (op, &left, &right) {
            // --- Integer comparison ---
            (Operator::Eq, Object::Integer(l), Object::Integer(r)) => Ok((l == r).into()),
            (Operator::NotEq, Object::Integer(l), Object::Integer(r)) => Ok((l != r).into()),
            (Operator::LessThan, Object::Integer(l), Object::Integer(r)) => Ok((l < r).into()),
            (Operator::LessThanOrEqual, Object::Integer(l), Object::Integer(r)) => {
                Ok((l <= r).into())
            }
            (Operator::GreaterThan, Object::Integer(l), Object::Integer(r)) => Ok((l > r).into()),
            (Operator::GreaterThanOrEqual, Object::Integer(l), Object::Integer(r)) => {
                Ok((l >= r).into())
            }

            (Operator::Eq, Object::Str(l), Object::Str(r)) => Ok((l == r).into()),
            (Operator::NotEq, Object::Str(l), Object::Str(r)) => Ok((l != r).into()),
            (Operator::LessThan, Object::Str(l), Object::Str(r)) => Ok((l < r).into()),
            (Operator::GreaterThan, Object::Str(l), Object::Str(r)) => Ok((l > r).into()),

            // --- Boolean ---
            (Operator::Eq, Object::Boolean(l), Object::Boolean(r)) => Ok((l == r).into()),
            (Operator::NotEq, Object::Boolean(l), Object::Boolean(r)) => Ok((l != r).into()),
            (Operator::And, Object::Boolean(l), Object::Boolean(r)) => Ok((*l && *r).into()),
            (Operator::Or, Object::Boolean(l), Object::Boolean(r)) => Ok((*l || *r).into()),

            _ => Err(EvalError::TypeMismatch {
                expected: "matching types for operator".into(),
                got: format!("{left} {op} {right}"),
            }),
        }
    }

    fn eval_property(
        &self,
        property: &expression::PropertyExpression,
        env: &Env,
    ) -> Result<Object, EvalError> {
        let object = self.eval_expression(property.object(), env)?;

        let Object::Instance(instance) = object else {
            return Err(EvalError::runtime("expected an instance"));
        };

        let prop_name = match property.property() {
            Expression::Identifier(ident) => ident.value(),
            _ => return Err(EvalError::runtime("invalid property name")),
        };

        if let Some(val) = instance.get_field(prop_name) {
            return Ok(val.clone());
        }

        let method = instance
            .find_method(prop_name)
            .ok_or_else(|| EvalError::runtime(format!("method {prop_name} not found")))
            .cloned()?;

        let bound_env = Environment::new_child(&method.env, false);
        bound_env
            .borrow_mut()
            .define("this", Object::Instance(instance));

        Ok(Object::function(
            method.params.clone(),
            method.body,
            bound_env,
        ))
    }

    fn eval_assignment(
        &self,
        assignment: &expression::AssignmentExpression,
        env: &Env,
    ) -> Result<Object, EvalError> {
        let value = self.eval_expression(assignment.value(), env)?;

        match assignment.name() {
            Expression::Identifier(ident) => {
                let name = ident.value();

                if env.borrow().is_immutable(name) {
                    return Err(EvalError::runtime(format!(
                        "cannot reassign constant {name}"
                    )));
                }

                if !env.borrow_mut().assign(name, value.clone()) {
                    return Err(EvalError::runtime(format!("variable {name} not defined")));
                }

                Ok(value)
            }

            Expression::Index(index) => {
                let container_name = match index.left() {
                    Expression::Identifier(ident) => ident.value(),
                    _ => return Err(EvalError::runtime("invalid assignment target")),
                };

                let mut container = env
                    .borrow()
                    .get(container_name)
                    .ok_or_else(|| EvalError::undefined(container_name))?;

                let index = self.eval_expression(index.index(), env)?;

                match (&mut container, index) {
                    (Object::Array(elements), Object::Integer(i)) => {
                        let len = elements.len() as i64;
                        if i < 0 || i >= len {
                            return Err(EvalError::runtime(format!(
                                "index out of bounds: {i} is not in range [0, {len})"
                            )));
                        }
                        elements[i as usize] = value;
                    }
                    (Object::Array(_), other) => {
                        return Err(EvalError::type_mismatch("Integer", other));
                    }
                    (Object::Hash(map), index) => {
                        map.insert(HashKey::try_from(index)?, value);
                    }
                    (other, _) => {
                        return Err(EvalError::type_mismatch("Array or Hash", other));
                    }
                }

                env.borrow_mut().assign(container_name, container);
                Ok(Object::Null)
            }

            Expression::Property(prop) => {
                let object_name = match prop.object() {
                    Expression::Identifier(ident) => ident.value(),
                    Expression::This(_) => "this",
                    _ => {
                        return Err(EvalError::runtime(
                            "complex property assignment targets are not supported",
                        ))
                    }
                };

                let prop_name = match prop.property() {
                    Expression::Identifier(ident) => ident.value(),
                    _ => return Err(EvalError::runtime("invalid property name")),
                };

                let mut obj = env
                    .borrow()
                    .get(object_name)
                    .ok_or_else(|| EvalError::undefined(object_name.to_string()))?;

                match &mut obj {
                    Object::Instance(instance) => {
                        instance.set_field(prop_name, value);
                    }

                    other => {
                        return Err(EvalError::runtime(format!(
                            "cannot assign property on non-instance: {other}"
                        )));
                    }
                }

                env.borrow_mut().assign(object_name, obj);
                Ok(Object::Null)
            }

            _ => Err(EvalError::runtime("invalid assignment target")),
        }
    }
}
