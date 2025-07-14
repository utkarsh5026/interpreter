package lang.exec.evaluator.statements;

import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.objects.Environment;
import lang.exec.evaluator.base.EvaluationContext;
import lang.ast.statements.ContinueStatement;
import lang.exec.base.BaseObject;
import lang.exec.objects.ContinueObject;

public class ContinueStatementParser implements NodeEvaluator<ContinueStatement> {

    @Override
    public BaseObject evaluate(ContinueStatement node, Environment env, EvaluationContext context) {
        return ContinueObject.INSTANCE;
    }
}
