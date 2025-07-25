package lang.exec.evaluator.statements;

import lang.exec.objects.base.BaseObject;
import lang.exec.objects.env.Environment;
import lang.exec.objects.loop.BreakObject;
import lang.ast.statements.BreakStatement;
import lang.exec.evaluator.base.EvaluationContext;
import lang.exec.evaluator.base.NodeEvaluator;

public class BreakStatementParser implements NodeEvaluator<BreakStatement> {

    @Override
    public BaseObject evaluate(BreakStatement node, Environment env, EvaluationContext context) {
        return BreakObject.INSTANCE;
    }
}
