package lang.exec.evaluator.literals;

import lang.exec.evaluator.base.EvaluationContext;
import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.objects.base.BaseObject;
import lang.exec.objects.builtins.FloatClass;
import lang.exec.objects.env.Environment;
import lang.ast.literals.FloatLiteral;

public class FloatLiteralEvaluator implements NodeEvaluator<FloatLiteral> {

    @Override
    public BaseObject evaluate(FloatLiteral node, Environment env, EvaluationContext context) {
        double value = node.getValue();
        return FloatClass.createFloatInstance(value);
    }
}
