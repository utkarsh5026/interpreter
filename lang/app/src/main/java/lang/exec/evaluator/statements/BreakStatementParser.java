package lang.exec.evaluator.statements;

import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.objects.BreakObject;
import lang.exec.objects.Environment;
import lang.exec.evaluator.base.EvaluationContext;
import lang.ast.statements.BreakStatement;
import lang.exec.base.BaseObject;

public class BreakStatementParser implements NodeEvaluator<BreakStatement> {

    @Override
    public BaseObject evaluate(BreakStatement node, Environment env, EvaluationContext context) {
        return BreakObject.INSTANCE;
    }
}
