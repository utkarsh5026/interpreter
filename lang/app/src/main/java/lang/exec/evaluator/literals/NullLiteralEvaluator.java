package lang.exec.evaluator.literals;

import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.objects.Environment;
import lang.exec.objects.NullObject;
import lang.exec.evaluator.base.EvaluationContext;
import lang.ast.expressions.NullExpression;
import lang.exec.base.BaseObject;

public class NullLiteralEvaluator implements NodeEvaluator<NullExpression> {

    @Override
    public BaseObject evaluate(NullExpression node, Environment env, EvaluationContext context) {
        return NullObject.INSTANCE;
    }

}
