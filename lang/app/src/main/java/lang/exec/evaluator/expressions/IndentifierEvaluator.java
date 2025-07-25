package lang.exec.evaluator.expressions;

import java.util.Optional;

import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.objects.env.Environment;
import lang.exec.base.BaseObject;
import lang.exec.evaluator.base.EvaluationContext;
import lang.exec.builtins.BuiltinRegistry;

import lang.ast.base.Identifier;

public class IndentifierEvaluator implements NodeEvaluator<Identifier> {

    @Override
    public BaseObject evaluate(Identifier node, Environment env, EvaluationContext context) {
        String identifier = node.getValue();
        Optional<BaseObject> value = env.resolveVariable(identifier);
        if (value.isPresent()) {
            return value.get();
        }

        if (BuiltinRegistry.isBuiltin(identifier)) {
            return BuiltinRegistry.getBuiltin(identifier);
        }

        return context.createError("Identifier not found: " + identifier, node.position());
    }
}
