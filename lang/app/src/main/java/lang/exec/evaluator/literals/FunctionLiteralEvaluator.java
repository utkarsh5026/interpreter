package lang.exec.evaluator.literals;

import lang.exec.evaluator.base.EvaluationContext;
import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.objects.base.BaseObject;
import lang.exec.objects.env.Environment;
import lang.exec.objects.functions.FunctionObject;
import lang.ast.literals.FunctionLiteral;

public class FunctionLiteralEvaluator implements NodeEvaluator<FunctionLiteral> {

    @Override
    public BaseObject evaluate(FunctionLiteral node, Environment env, EvaluationContext context) {
        return new FunctionObject(env, node.getParameters(), node.getBody());
    }
}
