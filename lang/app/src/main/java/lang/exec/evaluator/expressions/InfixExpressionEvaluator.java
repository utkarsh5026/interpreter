package lang.exec.evaluator.expressions;

import lang.ast.expressions.InfixExpression;

import lang.exec.base.BaseObject;
import lang.exec.objects.ErrorObject;
import lang.exec.objects.StringObject;
import lang.exec.objects.BooleanObject;
import lang.exec.objects.IntegerObject;

import lang.exec.evaluator.base.EvaluationContext;
import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.objects.Environment;
import lang.exec.validator.ObjectValidator;

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

    private BaseObject evalStringInfixExpression(String operator, BaseObject left, BaseObject right) {
        if (!ObjectValidator.isString(left) || !ObjectValidator.isString(right)) {
            return new ErrorObject("type mismatch: STRING " + operator + " " + left.type() + " " + right.type());
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
                return new ErrorObject("unknown operator: " + operator + " " + left.type() + " " + right.type());
        }
    }

    private BaseObject evalIntegerInfixExpression(String operator, BaseObject left, BaseObject right) {
        if (!ObjectValidator.isInteger(left) || !ObjectValidator.isInteger(right)) {
            return new ErrorObject("type mismatch: INTEGER " + operator + " " + left.type() + " " + right.type());
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
                return new IntegerObject(leftInteger / rightInteger);

            case "%":
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
                return new ErrorObject("unknown operator: " + operator + " " + left.type() + " " + right.type());
        }
    }

    private BaseObject evalBooleanInfixExpression(String operator, BaseObject left, BaseObject right) {
        if (!ObjectValidator.isBoolean(left) || !ObjectValidator.isBoolean(right)) {
            return new ErrorObject("type mismatch: BOOLEAN " + operator + " " + left.type() + " " + right.type());
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
                return new ErrorObject("unknown operator: " + operator + " " + left.type() + " " + right.type());
        }
    }

    private BaseObject evalInfixExpression(String operator, BaseObject left, BaseObject right) {
        if (ObjectValidator.isString(left) && ObjectValidator.isString(right))
            return evalStringInfixExpression(operator, left, right);

        if (ObjectValidator.isInteger(left) && ObjectValidator.isInteger(right))
            return evalIntegerInfixExpression(operator, left, right);

        if (ObjectValidator.isBoolean(left) && ObjectValidator.isBoolean(right))
            return evalBooleanInfixExpression(operator, left, right);

        return new ErrorObject("unknown operator: " + operator + " " + left.type() + " " + right.type());
    }

}