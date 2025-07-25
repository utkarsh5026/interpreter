package lang.exec.evaluator.expressions;

import java.util.Optional;

import lang.exec.objects.base.BaseObject;
import lang.exec.objects.env.Environment;
import lang.exec.builtins.BuiltinRegistry;
import lang.exec.evaluator.base.EvaluationContext;
import lang.exec.evaluator.base.NodeEvaluator;
import lang.ast.base.Identifier;

/**
 * 🔍 Evaluates identifier expressions
 * 
 * This class is responsible for evaluating identifier expressions.
 * It checks if the identifier is a variable, a builtin function, or not found.
 */
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
