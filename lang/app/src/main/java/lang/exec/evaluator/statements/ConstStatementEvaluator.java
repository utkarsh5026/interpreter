package lang.exec.evaluator.statements;

import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.base.BaseObject;

import lang.exec.objects.ErrorObject;
import lang.exec.objects.Environment;
import lang.exec.validator.ObjectValidator;

import lang.ast.statements.ConstStatement;
import lang.exec.evaluator.base.EvaluationContext;

public class ConstStatementEvaluator implements NodeEvaluator<ConstStatement> {
    @Override
    public BaseObject evaluate(ConstStatement node, Environment env, EvaluationContext context) {

        String variableName = node.getName().getValue();

        if (env.has(variableName)) {
            return new ErrorObject("variable '" + variableName + "' already declared in this scope");
        }

        BaseObject value = context.evaluate(node.getValue(), env);

        if (ObjectValidator.isError(value)) {
            return value;
        }

        env.set(variableName, value);
        env.setConst(variableName, value);

        return value;
    }

}
