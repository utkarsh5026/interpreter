package lang.exec.evaluator.literals;

import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.base.BaseObject;
import lang.exec.objects.Environment;
import lang.exec.objects.literals.FloatObject;
import lang.exec.evaluator.base.EvaluationContext;
import lang.ast.literals.FloatLiteral;

public class FloatLiteralEvaluator implements NodeEvaluator<FloatLiteral> {

    @Override
    public BaseObject evaluate(FloatLiteral node, Environment env, EvaluationContext context) {
        double value = node.getValue();
        return new FloatObject(value);
    }
}
