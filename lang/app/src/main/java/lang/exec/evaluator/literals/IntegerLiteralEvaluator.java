package lang.exec.evaluator.literals;

import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.base.BaseObject;
import lang.exec.objects.IntegerObject;

import lang.exec.objects.Environment;
import lang.exec.evaluator.base.EvaluationContext;
import lang.ast.literals.IntegerLiteral;

public class IntegerLiteralEvaluator implements NodeEvaluator<IntegerLiteral> {

    @Override
    public BaseObject evaluate(IntegerLiteral node, Environment env, EvaluationContext context) {
        return new IntegerObject(node.getValue());
    }
}
