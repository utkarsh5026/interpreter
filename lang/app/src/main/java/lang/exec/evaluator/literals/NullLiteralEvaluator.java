package lang.exec.evaluator.literals;

import lang.exec.objects.base.BaseObject;
import lang.exec.objects.env.Environment;
import lang.exec.objects.literals.NullObject;
import lang.ast.expressions.NullExpression;
import lang.exec.evaluator.base.EvaluationContext;
import lang.exec.evaluator.base.NodeEvaluator;

public class NullLiteralEvaluator implements NodeEvaluator<NullExpression> {

    @Override
    public BaseObject evaluate(NullExpression node, Environment env, EvaluationContext context) {
        return NullObject.INSTANCE;
    }

}
