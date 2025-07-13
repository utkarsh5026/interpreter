package lang.exec.evaluator.statements;

import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.base.BaseObject;

import lang.exec.objects.Environment;
import lang.ast.statements.ExpressionStatement;
import lang.exec.evaluator.base.EvaluationContext;

public class ExpressionStatementEvaluator implements NodeEvaluator<ExpressionStatement> {

    @Override
    public BaseObject evaluate(ExpressionStatement node, Environment env, EvaluationContext context) {
        return context.evaluate(node.getExpression(), env);
    }
}
