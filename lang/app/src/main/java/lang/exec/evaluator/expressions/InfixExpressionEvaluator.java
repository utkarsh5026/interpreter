package lang.exec.evaluator.expressions;

import java.util.Optional;
import lang.ast.expressions.InfixExpression;
import lang.exec.evaluator.base.*;
import lang.exec.objects.base.BaseObject;
import lang.exec.objects.env.Environment;
import lang.exec.objects.error.ErrorObject;
import lang.exec.objects.builtins.*;
import lang.exec.objects.classes.*;
import lang.token.TokenPosition;
import lang.exec.validator.*;

/**
 * 🧮 InfixExpressionEvaluator - Binary Operation Specialist 🧮
 * 
 * Handles all operations between two values: 5 + 3, "hello" + "world", etc.
 * This evaluator focuses purely on infix operations and delegates everything
 * else.
 */
public class InfixExpressionEvaluator implements NodeEvaluator<InfixExpression> {

    @Override
    public BaseObject evaluate(InfixExpression node, Environment env, EvaluationContext context) {
        BaseObject left = context.evaluate(node.getLeft(), env);
        if (ObjectValidator.isError(left))
            return left;

        BaseObject right = context.evaluate(node.getRight(), env);
        if (ObjectValidator.isError(right))
            return right;

        return evalInfixExpression(
                node.getOperator(),
                left,
                right,
                context,
                node);
    }

    /**
     * 🎯 Attempts to call dunder method on left operand
     * 
     * From first principles, dunder method resolution:
     * 1. Check if left operand is an instance object
     * 2. Look up the appropriate dunder method name for the operator
     * 3. Search for the method in the instance's class hierarchy
     * 4. If found, call the method with right operand as argument
     * 5. Return the result, or empty if no dunder method available
     */
    private Optional<BaseObject> tryDunderMethod(String operator, BaseObject left, BaseObject right,
            EvaluationContext context) {
        System.out.println("Trying dunder method for operator: " + operator + " on " + left + " and " + right);
        if (!ObjectValidator.isInstance(left)) {
            return Optional.empty();
        }

        Optional<String> dunderMethodName = BaseObjectClass.getDunderMethodForOperator(operator);
        if (dunderMethodName.isEmpty()) {
            return Optional.empty();
        }

        InstanceObject instance = ObjectValidator.asInstance(left);
        Optional<MethodObject> dunderMethod = instance.findMethod(dunderMethodName.get());
        if (dunderMethod.isEmpty()) {
            return Optional.empty();
        }

        BaseObject result = dunderMethod.get().call(
                instance,
                new BaseObject[] { right },
                context,
                env -> env);

        if (ObjectValidator.isReturnValue(result)) {
            return Optional.of(ObjectValidator.asReturnValue(result).getValue());
        }

        return Optional.of(result);
    }

    /**
     * 🎯 Main infix expression evaluation
     */
    private BaseObject evalInfixExpression(String operator, BaseObject left, BaseObject right,
            EvaluationContext context, InfixExpression node) {
        TokenPosition position = node.position();

        Optional<BaseObject> result = tryDunderMethod(operator, left, right, context);
        if (result.isPresent()) {
            return result.get();
        }

        if (hasNullOperand(left, right)) {
            return evalNullInfixExpression(operator, left, right, position, context);
        }

        return createInvalidOperatorError(operator, left, right);
    }

    /**
     * 🔍 Checks if either operand is null
     */
    private boolean hasNullOperand(BaseObject left, BaseObject right) {
        return ObjectValidator.isNull(left) || ObjectValidator.isNull(right);
    }

    /**
     * 🔍 Evaluates null infix expressions
     */
    private BaseObject evalNullInfixExpression(String operator, BaseObject left, BaseObject right,
            TokenPosition position, EvaluationContext context) {
        switch (operator) {
            case "==":
                return BooleanClass
                        .createBooleanInstance(ObjectValidator.isNull(left) && ObjectValidator.isNull(right));

            case "!=":
                return BooleanClass
                        .createBooleanInstance(!(ObjectValidator.isNull(left) && ObjectValidator.isNull(right)));

            default:
                return context.createError("Cannot perform '" + operator + "' operation with null values. " +
                        "Only equality (==) and inequality (!=) operations are supported with null.", position);
        }
    }

    /**
     * 🔍 Creates an invalid operator error
     */
    private ErrorObject createInvalidOperatorError(String operator, BaseObject left, BaseObject right) {
        return new ErrorObject("Invalid operator '" + operator + "' for types " + left.type() + " and " + right.type()
                + ". This operation is not supported.");
    }
}