package lang.exec.evaluator.statements;

import java.util.List;

import lang.ast.base.Statement;
import lang.exec.evaluator.base.EvaluationContext;
import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.objects.base.BaseObject;
import lang.exec.objects.env.Environment;
import lang.exec.objects.literals.NullObject;
import lang.ast.statements.BlockStatement;
import lang.exec.validator.ObjectValidator;

public class BlockStatementEvaluator implements NodeEvaluator<BlockStatement> {

    @Override
    public BaseObject evaluate(BlockStatement node, Environment env, EvaluationContext context) {
        Environment newEnv = context.newScope(env, true);
        List<Statement> statements = node.getStatements();

        BaseObject result = NullObject.INSTANCE;

        for (Statement statement : statements) {
            result = context.evaluate(statement, newEnv);

            if (ObjectValidator.isError(result)
                    || ObjectValidator.isReturnValue(result)
                    || ObjectValidator.isBreakOrContinue(result)) {
                break;
            }
        }

        return result;
    }
}
