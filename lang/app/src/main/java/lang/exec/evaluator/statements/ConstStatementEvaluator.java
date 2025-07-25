package lang.exec.evaluator.statements;

import lang.exec.objects.base.BaseObject;
import lang.exec.objects.env.Environment;
import lang.exec.evaluator.base.EvaluationContext;
import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.validator.ObjectValidator;

import lang.ast.statements.ConstStatement;

public class ConstStatementEvaluator implements NodeEvaluator<ConstStatement> {
    @Override
    public BaseObject evaluate(ConstStatement node, Environment env, EvaluationContext context) {

        String variableName = node.getName().getValue();

        if (env.containsVariableLocally(variableName)) {
            return context.createError("Constant already assigned: " + variableName, node.getName().position());
        }

        BaseObject value = context.evaluate(node.getValue(), env);

        if (ObjectValidator.isError(value)) {
            return value;
        }

        env.defineVariable(variableName, value);
        env.defineConstant(variableName, value);

        return value;
    }

}
