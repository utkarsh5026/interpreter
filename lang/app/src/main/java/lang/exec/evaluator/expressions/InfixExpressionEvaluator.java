package lang.exec.evaluator.expressions;

import java.util.Optional;
import lang.ast.expressions.InfixExpression;

import lang.exec.base.BaseObject;
import lang.exec.objects.*;

import lang.exec.evaluator.base.EvaluationContext;
import lang.exec.evaluator.base.NodeEvaluator;

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
                right);
    }

    /**
     * 🔤 String-based operations (enhanced for consistency)
     * 
     * Handles operations when both operands are strings and the result
     * should remain a string.
     */
    private BaseObject evalStringInfixExpression(String operator, BaseObject left, BaseObject right) {
        if (!ObjectValidator.isString(left) || !ObjectValidator.isString(right)) {
            return createTypeMismatchError(operator, left, right);
        }

        String leftString = ObjectValidator.asString(left).getValue();
        String rightString = ObjectValidator.asString(right).getValue();

        switch (operator) {
            case "+":
                return new StringObject(String.join("", leftString, rightString));

            case "==":
                return new BooleanObject(leftString.equals(rightString));

            case "!=":
                return new BooleanObject(!leftString.equals(rightString));

            case ">":
                return new BooleanObject(leftString.compareTo(rightString) > 0);

            case "<":
                return new BooleanObject(leftString.compareTo(rightString) < 0);

            case "<=":
                return new BooleanObject(leftString.compareTo(rightString) <= 0);

            case ">=":
                return new BooleanObject(leftString.compareTo(rightString) >= 0);

            default:
                return createInvalidOperatorError(operator, left, right);
        }
    }

    /**
     * 🔢 Integer-based numeric operations (enhanced for consistency)
     * 
     * Handles operations when both operands are integers and the result
     * should remain an integer.
     */
    private BaseObject evalIntegerInfixExpression(String operator, BaseObject left, BaseObject right) {
        if (!ObjectValidator.isInteger(left) || !ObjectValidator.isInteger(right)) {
            return createTypeMismatchError(operator, left, right);
        }

        long leftInteger = ObjectValidator.asInteger(left).getValue();
        long rightInteger = ObjectValidator.asInteger(right).getValue();

        switch (operator) {
            case "+":
                return new IntegerObject(leftInteger + rightInteger);

            case "-":
                return new IntegerObject(leftInteger - rightInteger);

            case "*":
                return new IntegerObject(leftInteger * rightInteger);

            case "/":
                if (rightInteger == 0) {
                    return new ErrorObject("division by zero");
                }
                return new FloatObject((double) leftInteger / rightInteger);

            case "%":
                if (rightInteger == 0) {
                    return new ErrorObject("modulo by zero");
                }
                return new IntegerObject(leftInteger % rightInteger);

            case "==":
                return new BooleanObject(leftInteger == rightInteger);

            case "!=":
                return new BooleanObject(leftInteger != rightInteger);

            case "<":
                return new BooleanObject(leftInteger < rightInteger);

            case "<=":
                return new BooleanObject(leftInteger <= rightInteger);

            case ">":
                return new BooleanObject(leftInteger > rightInteger);

            case ">=":
                return new BooleanObject(leftInteger >= rightInteger);

            default:
                return createInvalidOperatorError(operator, left, right);
        }
    }

    /**
     * 🔄 Boolean-based operations (enhanced for consistency)
     * 
     * Handles operations when both operands are booleans and the result
     * should remain a boolean.
     */
    private BaseObject evalBooleanInfixExpression(String operator, BaseObject left, BaseObject right) {
        if (!ObjectValidator.isBoolean(left) || !ObjectValidator.isBoolean(right)) {
            return createTypeMismatchError(operator, left, right);
        }

        boolean leftBoolean = ObjectValidator.asBoolean(left).getValue();
        boolean rightBoolean = ObjectValidator.asBoolean(right).getValue();

        switch (operator) {
            case "==":
                return new BooleanObject(leftBoolean == rightBoolean);

            case "!=":
                return new BooleanObject(leftBoolean != rightBoolean);

            case "&&":
                return new BooleanObject(leftBoolean && rightBoolean);

            case "||":
                return new BooleanObject(leftBoolean || rightBoolean);

            default:
                return createInvalidOperatorError(operator, left, right);
        }
    }

    /**
     * 🌊 Float-based numeric operations
     * 
     * Handles all arithmetic and comparison operations on floating-point numbers.
     * Both operands are converted to double for calculation.
     */
    private BaseObject evalFloatInfixExpression(String operator, BaseObject left, BaseObject right) {
        Optional<Double> leftDouble = NumericOperations.toDouble(left);
        Optional<Double> rightDouble = NumericOperations.toDouble(right);

        if (!leftDouble.isPresent() || !rightDouble.isPresent()) {
            return createTypeMismatchError(operator, left, right);
        }

        double leftDoubleValue = leftDouble.get();
        double rightDoubleValue = rightDouble.get();

        switch (operator) {
            case "+":
                return new FloatObject(leftDoubleValue + rightDoubleValue);

            case "-":
                return new FloatObject(leftDoubleValue - rightDoubleValue);

            case "*":
                return new FloatObject(leftDoubleValue * rightDoubleValue);

            case "/":
                if (rightDoubleValue == 0.0) {
                    // IEEE 754 behavior: 1.0/0.0 = Infinity, -1.0/0.0 = -Infinity
                    if (leftDoubleValue > 0.0) {
                        return new FloatObject(Double.POSITIVE_INFINITY);
                    } else if (leftDoubleValue < 0.0) {
                        return new FloatObject(Double.NEGATIVE_INFINITY);
                    } else {
                        return new FloatObject(Double.NaN); // 0.0/0.0 = NaN
                    }
                }
                return new FloatObject(leftDoubleValue / rightDoubleValue);

            case "%":
                return new FloatObject(leftDoubleValue % rightDoubleValue);

            // Comparison operations always return boolean
            case "==":
                return new BooleanObject(Double.compare(leftDoubleValue, rightDoubleValue) == 0);

            case "!=":
                return new BooleanObject(Double.compare(leftDoubleValue, rightDoubleValue) != 0);

            case "<":
                return new BooleanObject(Double.compare(leftDoubleValue, rightDoubleValue) < 0);

            case "<=":
                return new BooleanObject(Double.compare(leftDoubleValue, rightDoubleValue) <= 0);

            case ">":
                return new BooleanObject(Double.compare(leftDoubleValue, rightDoubleValue) > 0);

            case ">=":
                return new BooleanObject(Double.compare(leftDoubleValue, rightDoubleValue) >= 0);

            default:
                return createInvalidOperatorError(operator, left, right);
        }
    }

    /**
     * 🔢 Enhanced numeric operation evaluation with float support
     * 
     * From first principles, numeric operations need to:
     * 1. Determine the promoted type (int or float)
     * 2. Convert operands to the promoted type
     * 3. Perform the operation
     * 4. Return result in appropriate type
     * 
     * Type promotion rules:
     * - If either operand is float → result is float
     * - If both operands are integer → result is integer (except for division)
     * - Division always returns float (5/2 = 2.5, not 2)
     */
    private BaseObject evalNumericInfixExpression(String operator, BaseObject left, BaseObject right) {
        Class<?> promotedType = getPromotedTypeForOperation(operator, left, right);

        if (promotedType == Double.class) {
            return evalFloatInfixExpression(operator, left, right);
        } else {
            return evalIntegerInfixExpression(operator, left, right);
        }
    }

    /**
     * 🎯 Determines the promoted type for numeric operations
     * 
     * Special case: Division always promotes to float for mathematical correctness
     * All other operations follow standard type promotion rules
     */
    private Class<?> getPromotedTypeForOperation(String operator, BaseObject left, BaseObject right) {
        if (operator.equals("/")) {
            return Double.class;
        }

        return NumericOperations.getPromotedType(left, right);
    }

    /**
     * 🎯 Main infix expression evaluation with enhanced numeric support
     * 
     * From first principles, the evaluation strategy is:
     * 1. Handle null operations (special case)
     * 2. Handle string operations (concatenation, comparison)
     * 3. Handle numeric operations with type promotion
     * 4. Handle boolean operations
     * 5. Return appropriate error for unsupported combinations
     */
    private BaseObject evalInfixExpression(String operator, BaseObject left, BaseObject right) {
        if (ObjectValidator.isNull(left) || ObjectValidator.isNull(right)) {
            return evalNullInfixExpression(operator, left, right);
        }

        if (ObjectValidator.isString(left) && ObjectValidator.isString(right))
            return evalStringInfixExpression(operator, left, right);

        if (ObjectValidator.isNumeric(left) && ObjectValidator.isNumeric(right)) {
            return evalNumericInfixExpression(operator, left, right);
        }

        if (ObjectValidator.isBoolean(left) && ObjectValidator.isBoolean(right))
            return evalBooleanInfixExpression(operator, left, right);

        return createInvalidOperatorError(operator, left, right);
    }

    private BaseObject evalNullInfixExpression(String operator, BaseObject left, BaseObject right) {
        switch (operator) {
            case "==":
                // null == null -> true, null == anything -> false
                return new BooleanObject(ObjectValidator.isNull(left) && ObjectValidator.isNull(right));

            case "!=":
                // null != null -> false, null != anything -> true
                return new BooleanObject(!(ObjectValidator.isNull(left) && ObjectValidator.isNull(right)));

            default:
                // All other operations with null are errors
                return new ErrorObject("Cannot perform '" + operator + "' operation with null values. " +
                        "Only equality (==) and inequality (!=) operations are supported with null.");
        }
    }

    private ErrorObject createInvalidOperatorError(String operator, BaseObject left, BaseObject right) {
        return new ErrorObject("Invalid operator '" + operator + "' for types " + left.type() + " and " + right.type()
                + ". This operation is not supported.");
    }

    private ErrorObject createTypeMismatchError(String operator, BaseObject left, BaseObject right) {
        return new ErrorObject("Type mismatch: " + left.type() + " " + operator + " " + right.type()
                + ". This operation is not supported.");
    }
}