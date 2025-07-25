package lang.exec.evaluator.statements;

import lang.exec.objects.base.BaseObject;
import lang.exec.objects.env.Environment;
import lang.exec.objects.loop.ContinueObject;
import lang.ast.statements.ContinueStatement;
import lang.exec.evaluator.base.EvaluationContext;
import lang.exec.evaluator.base.NodeEvaluator;

public class ContinueStatementParser implements NodeEvaluator<ContinueStatement> {

    @Override
    public BaseObject evaluate(ContinueStatement node, Environment env, EvaluationContext context) {
        return ContinueObject.INSTANCE;
    }
}
