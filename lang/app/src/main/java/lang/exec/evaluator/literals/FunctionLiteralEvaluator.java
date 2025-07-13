package lang.exec.evaluator.literals;

import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.base.BaseObject;

import lang.exec.objects.Environment;
import lang.exec.evaluator.base.EvaluationContext;
import lang.ast.literals.FunctionLiteral;
import lang.exec.objects.FunctionObject;

public class FunctionLiteralEvaluator implements NodeEvaluator<FunctionLiteral> {

    @Override
    public BaseObject evaluate(FunctionLiteral node, Environment env, EvaluationContext context) {
        return new FunctionObject(env, node.getParameters(), node.getBody());
    }
}
