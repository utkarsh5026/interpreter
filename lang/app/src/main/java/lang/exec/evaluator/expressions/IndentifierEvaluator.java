package lang.exec.evaluator.expressions;

import java.util.Optional;

import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.base.BaseObject;

import lang.exec.objects.Environment;
import lang.exec.evaluator.base.EvaluationContext;
import lang.exec.builtins.BuiltinRegistry;
import lang.exec.objects.errors.ErrorFactory;

import lang.ast.base.Identifier;

public class IndentifierEvaluator implements NodeEvaluator<Identifier> {

    @Override
    public BaseObject evaluate(Identifier node, Environment env, EvaluationContext context) {
        Optional<BaseObject> value = env.resolveVariable(node.getValue());
        if (value.isPresent()) {
            return value.get();
        }

        if (BuiltinRegistry.isBuiltin(node.getValue())) {
            return BuiltinRegistry.getBuiltin(node.getValue());
        }

        return ErrorFactory.identifierNotFound(node.getValue());
    }
}
