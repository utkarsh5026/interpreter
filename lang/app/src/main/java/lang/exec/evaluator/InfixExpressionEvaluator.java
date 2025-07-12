package lang.exec.evaluator;

import lang.ast.expressions.InfixExpression;
import lang.exec.base.BaseObject;
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
        // Step 1: Evaluate left operand
        BaseObject left = context.evaluate(node.getLeft(), env);
        if (ObjectValidator.isError(left))
            return left;

        // Step 2: Evaluate right operand
        BaseObject right = context.evaluate(node.getRight(), env);
        if (ObjectValidator.isError(right))
            return right;

        // Step 3: Delegate to expression evaluator for the actual operation
        return ExpressionEvaluator.evalInfixExpression(
                node.getOperator(),
                left,
                right);
    }
}