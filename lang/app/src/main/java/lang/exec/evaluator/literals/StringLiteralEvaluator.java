package lang.exec.evaluator.literals;

import lang.exec.evaluator.base.EvaluationContext;
import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.objects.base.BaseObject;
import lang.exec.objects.env.Environment;
import lang.exec.objects.literals.StringObject;
import lang.ast.literals.StringLiteral;

public class StringLiteralEvaluator implements NodeEvaluator<StringLiteral> {

    @Override
    public BaseObject evaluate(StringLiteral node, Environment env, EvaluationContext context) {
        return new StringObject(node.getValue());
    }
}
