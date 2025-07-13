package lang.exec.evaluator.statements;

import lang.ast.statements.LetStatement;
import lang.exec.evaluator.base.EvaluationContext;
import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.base.BaseObject;

import lang.exec.objects.ErrorObject;
import lang.exec.objects.Environment;
import lang.exec.validator.ObjectValidator;

public class LetStatementEvaluator implements NodeEvaluator<LetStatement> {

    @Override
    public BaseObject evaluate(LetStatement node, Environment env, EvaluationContext context) {
        String varName = node.getName().getValue();

        if (env.has(varName)) {
            return new ErrorObject("variable '" + varName + "' already declared in this scope");
        }

        BaseObject value = context.evaluate(node.getValue(), env);
        if (ObjectValidator.isError(value))
            return value;

        env.set(varName, value);

        return value;
    }
}
