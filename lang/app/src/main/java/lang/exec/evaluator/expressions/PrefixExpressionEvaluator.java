package lang.exec.evaluator.expressions;

import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.base.BaseObject;
import lang.exec.evaluator.base.EvaluationContext;
import lang.exec.objects.BooleanObject;
import lang.exec.objects.ErrorObject;

import lang.ast.expressions.PrefixExpression;

import lang.exec.objects.Environment;
import lang.exec.validator.ObjectValidator;
import lang.exec.objects.IntegerObject;

public class PrefixExpressionEvaluator implements NodeEvaluator<PrefixExpression> {

    @Override
    public BaseObject evaluate(PrefixExpression node, Environment env, EvaluationContext context) {
        BaseObject right = context.evaluate(node.getRight(), env);
        if (ObjectValidator.isError(right)) {
            return right;
        }

        return evalPrefixExpression(node.getOperator(), right);
    }

    private BaseObject evalPrefixExpression(String operator, BaseObject right) {
        if (operator.equals("!")) {
            return evalLogicalNotOperator(right);
        }

        if (operator.equals("-")) {
            return evalNegationOperator(right);
        }

        return new ErrorObject(
                String.format("unknown operator: %s%s, You can only use ! or - operator with BOOLEAN or INTEGER",
                        operator, right.type()));
    }

    private final BooleanObject evalLogicalNotOperator(BaseObject value) {
        if (ObjectValidator.isBoolean(value)) {
            return new BooleanObject(!ObjectValidator.asBoolean(value).getValue());
        }

        if (ObjectValidator.isNull(value)) {
            return new BooleanObject(true);
        }

        return new BooleanObject(!value.isTruthy());
    }

    private final static BaseObject evalNegationOperator(BaseObject value) {
        if (ObjectValidator.isInteger(value)) {
            return new IntegerObject(-ObjectValidator.asInteger(value).getValue());
        }

        String errorMessage = String.format(
                "unknown operator: -%s, You can only use - operator with INTEGER like -5, -10, -100, -1000, etc.",
                value.type());

        return new ErrorObject(errorMessage);
    }

}
