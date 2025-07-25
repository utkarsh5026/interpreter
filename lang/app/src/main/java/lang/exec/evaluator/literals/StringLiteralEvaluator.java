package lang.exec.evaluator.literals;

import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.base.BaseObject;

import lang.exec.objects.Environment;
import lang.exec.objects.literals.StringObject;
import lang.exec.evaluator.base.EvaluationContext;
import lang.ast.literals.StringLiteral;

public class StringLiteralEvaluator implements NodeEvaluator<StringLiteral> {

    @Override
    public BaseObject evaluate(StringLiteral node, Environment env, EvaluationContext context) {
        return new StringObject(node.getValue());
    }
}
