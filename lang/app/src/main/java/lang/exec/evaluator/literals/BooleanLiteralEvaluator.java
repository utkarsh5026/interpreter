package lang.exec.evaluator.literals;

import lang.exec.evaluator.base.EvaluationContext;
import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.objects.base.BaseObject;
import lang.exec.objects.env.Environment;
import lang.exec.objects.literals.BooleanObject;
import lang.ast.expressions.BooleanExpression;

public class BooleanLiteralEvaluator implements NodeEvaluator<BooleanExpression> {

    @Override
    public BaseObject evaluate(BooleanExpression node, Environment env, EvaluationContext context) {
        return new BooleanObject(node.getValue());
    }
}
