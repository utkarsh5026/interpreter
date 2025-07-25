package lang.exec.evaluator.statements;

import lang.ast.statements.LetStatement;
import lang.exec.evaluator.base.EvaluationContext;
import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.objects.env.Environment;
import lang.exec.base.BaseObject;
import lang.exec.validator.ObjectValidator;

public class LetStatementEvaluator implements NodeEvaluator<LetStatement> {

    @Override
    public BaseObject evaluate(LetStatement node, Environment env, EvaluationContext context) {
        String varName = node.getName().getValue();

        if (env.containsVariableLocally(varName)) {
            return context.createError("Variable '" + varName + "' already declared in this scope",
                    node.getName().position());
        }

        BaseObject value = context.evaluate(node.getValue(), env);
        if (ObjectValidator.isError(value))
            return value;

        env.defineVariable(varName, value);

        return value;
    }
}
