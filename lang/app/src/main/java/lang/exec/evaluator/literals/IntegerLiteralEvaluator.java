package lang.exec.evaluator.literals;

import lang.exec.evaluator.base.EvaluationContext;
import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.objects.base.BaseObject;
import lang.exec.objects.env.Environment;
import lang.exec.objects.literals.IntegerObject;
import lang.ast.literals.IntegerLiteral;

public class IntegerLiteralEvaluator implements NodeEvaluator<IntegerLiteral> {

    @Override
    public BaseObject evaluate(IntegerLiteral node, Environment env, EvaluationContext context) {
        return new IntegerObject(node.getValue());
    }
}
