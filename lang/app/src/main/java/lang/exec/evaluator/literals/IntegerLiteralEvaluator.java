package lang.exec.evaluator.literals;

import lang.exec.evaluator.base.EvaluationContext;
import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.objects.base.BaseObject;
import lang.exec.objects.builtins.IntegerClass;
import lang.exec.objects.env.Environment;
import lang.ast.literals.IntegerLiteral;

public class IntegerLiteralEvaluator implements NodeEvaluator<IntegerLiteral> {

    @Override
    public BaseObject evaluate(IntegerLiteral node, Environment env, EvaluationContext context) {
        return IntegerClass.createIntegerInstance(node.getValue());
    }
}
