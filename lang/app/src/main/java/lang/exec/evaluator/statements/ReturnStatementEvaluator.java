package lang.exec.evaluator.statements;

import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.base.BaseObject;

import lang.exec.objects.Environment;
import lang.exec.validator.ObjectValidator;

import lang.ast.statements.ReturnStatement;
import lang.exec.evaluator.base.EvaluationContext;

import lang.exec.objects.ReturnObject;

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
