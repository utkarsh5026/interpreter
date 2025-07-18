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
import lang.exec.objects.errors.ErrorFactory;

/**
 * 🔄 PrefixExpressionEvaluator - Unary Operation Specialist (Enhanced with
 * Float Support) 🔄
 * 
 * Handles prefix (unary) operators with comprehensive support for both integers
 * and floats.
 * 
 * From first principles, unary operations need to:
 * 1. Preserve the original type when possible (-5 → int, -5.0 → float)
 * 2. Handle special float values (NaN, Infinity) appropriately
 * 3. Provide consistent behavior across numeric types
 * 
 * Enhanced operations:
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

        return evalPrefixExpression(node.getOperator(), right);
    }

    private BaseObject evalPrefixExpression(String operator, BaseObject right) {
        if (operator.equals("!")) {
            return evalLogicalNotOperator(right);
        }

        if (operator.equals("-")) {
            return evalNegationOperator(right);
        }

        return ErrorFactory.unknownOperator(operator, right.type().toString());
    }

    /**
     * ❗ Logical NOT operation (enhanced for better type handling)
     * 
     * From first principles, logical NOT should:
     * 1. Work on any type (using truthiness rules)
     * 2. Always return a boolean
     * 3. Use consistent truthiness across all types
     * 
     * Truthiness rules:
     * - Numbers: 0 and 0.0 are falsy, everything else is truthy
     * - Strings: empty string is falsy, non-empty is truthy
     * - Booleans: use their value directly
     * - Null: always falsy
     * - Arrays/Objects: empty is falsy, non-empty is truthy
     * - Special float values: NaN and Infinity are falsy
     */
    private BooleanObject evalLogicalNotOperator(BaseObject value) {
        return new BooleanObject(!value.isTruthy());
    }

    /**
     * ➖ Negation operation (enhanced with float support)
     * 
     * From first principles, negation should:
     * 1. Preserve the original type (int → int, float → float)
     * 2. Handle special float values correctly
     * 3. Return appropriate errors for non-numeric types
     * 
     * Special float cases:
     * - -NaN → NaN
     * - -Infinity → -Infinity
     * - -(-Infinity) → Infinity
     * - -0.0 → -0.0 (IEEE 754 signed zero)
     */
    private BaseObject evalNegationOperator(BaseObject value) {
        // Handle integer negation
        if (ObjectValidator.isInteger(value)) {
            long intValue = ObjectValidator.asInteger(value).getValue();
            if (intValue == Long.MIN_VALUE) {
                // -Long.MIN_VALUE would overflow, so promote to float
                return new FloatObject(-(double) intValue);
            }

            return new IntegerObject(-intValue);
        }

        // Handle float negation
        if (ObjectValidator.isFloat(value)) {
            double floatValue = ObjectValidator.asFloat(value).getValue();
            return new FloatObject(-floatValue);
        }

        // Error for non-numeric types
        return ErrorFactory.unknownOperator("-", value.type().toString());
    }
}
