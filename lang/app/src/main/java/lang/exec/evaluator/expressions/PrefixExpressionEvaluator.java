package lang.exec.evaluator.expressions;

import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.base.BaseObject;
import lang.exec.evaluator.base.EvaluationContext;
import lang.exec.objects.BooleanObject;
import lang.exec.objects.FloatObject;

import lang.ast.expressions.PrefixExpression;

import lang.exec.objects.Environment;
import lang.exec.validator.ObjectValidator;
import lang.exec.objects.IntegerObject;

/**
 * 🔄 PrefixExpressionEvaluator - Unary Operation Specialist (Enhanced with
 * Float Support) 🔄
 * 
 * Handles prefix (unary) operators with comprehensive support for both integers
 * and floats.
 * operations:
 * - Negation (-): Works on both int and float
 * - Logical NOT (!): Works on all types (uses truthiness)
 * 
 * Type preservation rules:
 * - -int → int
 * - -float → float
 * - !anything → boolean
 */
public class PrefixExpressionEvaluator implements NodeEvaluator<PrefixExpression> {

    @Override
    public BaseObject evaluate(PrefixExpression node, Environment env, EvaluationContext context) {
        BaseObject right = context.evaluate(node.getRight(), env);
        if (ObjectValidator.isError(right)) {
            return right;
        }

        return evalPrefixExpression(node.getOperator(), right, context, node);
    }

    private BaseObject evalPrefixExpression(String operator, BaseObject right, EvaluationContext context,
            PrefixExpression node) {
        if (operator.equals("!")) {
            return evalLogicalNotOperator(right);
        }

        if (operator.equals("-")) {
            return evalNegationOperator(right, context, node);
        }

        return context.createError("Unknown operator: " + operator, node.position());
    }

    /**
     * ❗ Logical NOT operation (enhanced for better type handling)
     */
    private BooleanObject evalLogicalNotOperator(BaseObject value) {
        return new BooleanObject(!value.isTruthy());
    }

    /**
     * ➖ Negation operation (enhanced with float support)
     */
    private BaseObject evalNegationOperator(BaseObject value, EvaluationContext context, PrefixExpression node) {
        if (ObjectValidator.isInteger(value)) {
            long intValue = ObjectValidator.asInteger(value).getValue();
            if (intValue == Long.MIN_VALUE) {
                return new FloatObject(-(double) intValue);
            }

            return new IntegerObject(-intValue);
        }

        if (ObjectValidator.isFloat(value)) {
            double floatValue = ObjectValidator.asFloat(value).getValue();
            return new FloatObject(-floatValue);
        }

        return context.createError("Unknown operator: -", node.position());
    }
}
