package lang.exec.evaluator.expressions;

import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.base.BaseObject;

import lang.exec.objects.Environment;
import lang.exec.objects.ErrorObject;
import lang.exec.evaluator.base.EvaluationContext;
import lang.exec.builtins.BuiltinRegistry;

import lang.ast.base.Identifier;

public class IndentifierEvaluator implements NodeEvaluator<Identifier> {

    @Override
    public BaseObject evaluate(Identifier node, Environment env, EvaluationContext context) {
        if (BuiltinRegistry.isBuiltin(node.getValue())) {
            return BuiltinRegistry.getBuiltin(node.getValue());
        }

        BaseObject value = env.get(node.getValue());
        if (value != null) {
            return value;
        }

        return new ErrorObject("identifier not found: " + node.getValue());
    }
}
