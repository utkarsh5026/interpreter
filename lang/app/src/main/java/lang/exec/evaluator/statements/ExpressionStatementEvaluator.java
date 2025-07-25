package lang.exec.evaluator.statements;

import lang.exec.objects.base.BaseObject;
import lang.exec.objects.env.Environment;
import lang.exec.evaluator.base.EvaluationContext;
import lang.exec.evaluator.base.NodeEvaluator;
import lang.ast.statements.ExpressionStatement;

public class ExpressionStatementEvaluator implements NodeEvaluator<ExpressionStatement> {

    @Override
    public BaseObject evaluate(ExpressionStatement node, Environment env, EvaluationContext context) {
        return context.evaluate(node.getExpression(), env);
    }
}
