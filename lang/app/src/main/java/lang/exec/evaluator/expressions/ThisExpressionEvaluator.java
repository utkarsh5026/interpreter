package lang.exec.evaluator.expressions;

import java.util.Optional;

import lang.ast.expressions.ThisExpression;
import lang.exec.base.BaseObject;
import lang.exec.evaluator.base.EvaluationContext;
import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.objects.env.Environment;

/**
 * 👆 ThisExpressionEvaluator - Current Instance Reference Evaluator 👆
 * 
 * Evaluates 'this' expressions to return the current object instance.
 * 
 * From first principles, 'this' evaluation involves:
 * 1. Look up 'this' binding in current environment
 * 2. Return the bound instance object
 * 3. Error if 'this' is not bound (not in instance context)
 */
public class ThisExpressionEvaluator implements NodeEvaluator<ThisExpression> {

    @Override
    public BaseObject evaluate(ThisExpression node, Environment env, EvaluationContext context) {
        Optional<BaseObject> thisObj = env.resolveVariable("this");

        if (thisObj.isEmpty()) {
            return context.createError("'this' is not available in this context", node.position());
        }

        return thisObj.get();
    }
}