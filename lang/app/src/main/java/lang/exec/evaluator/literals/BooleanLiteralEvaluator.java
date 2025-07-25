package lang.exec.evaluator.literals;

import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.base.BaseObject;

import lang.exec.objects.Environment;
import lang.exec.objects.literals.BooleanObject;
import lang.exec.evaluator.base.EvaluationContext;
import lang.ast.expressions.BooleanExpression;

public class BooleanLiteralEvaluator implements NodeEvaluator<BooleanExpression> {

    @Override
    public BaseObject evaluate(BooleanExpression node, Environment env, EvaluationContext context) {
        return new BooleanObject(node.getValue());
    }
}
