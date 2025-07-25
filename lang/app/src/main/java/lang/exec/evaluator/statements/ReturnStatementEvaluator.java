package lang.exec.evaluator.statements;

import lang.exec.evaluator.base.EvaluationContext;
import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.objects.base.BaseObject;
import lang.exec.objects.env.Environment;
import lang.exec.objects.functions.ReturnObject;
import lang.exec.validator.ObjectValidator;

import lang.ast.statements.ReturnStatement;

public class ReturnStatementEvaluator implements NodeEvaluator<ReturnStatement> {

    @Override
    public BaseObject evaluate(ReturnStatement node, Environment env, EvaluationContext context) {
        BaseObject value = context.evaluate(node.getReturnValue(), env);

        if (ObjectValidator.isError(value)) {
            return value;
        }

        return new ReturnObject(value);
    }
}
