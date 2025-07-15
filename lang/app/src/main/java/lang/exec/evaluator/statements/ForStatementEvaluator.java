package lang.exec.evaluator.statements;

import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.base.BaseObject;
import lang.exec.objects.NullObject;
import lang.exec.evaluator.base.LoopContext;
import lang.exec.validator.ObjectValidator;
import lang.exec.objects.ErrorObject;

import lang.exec.objects.Environment;
import lang.exec.evaluator.base.EvaluationContext;
import lang.ast.statements.ForStatement;

public class ForStatementEvaluator implements NodeEvaluator<ForStatement> {

    private final LoopContext loopContext;

    public ForStatementEvaluator(LoopContext loopContext) {
        this.loopContext = loopContext;
    }

    @Override
    public BaseObject evaluate(ForStatement node, Environment env, EvaluationContext context) {
        Environment loopEnv = context.newScope(env, true);
        loopContext.enterLoop();
        BaseObject result = NullObject.INSTANCE;

        try {
            BaseObject initResult = context.evaluate(node.getInitializer(), loopEnv);
            if (ObjectValidator.isError(initResult)) {
                return initResult;
            }

            while (true) {
                if (loopContext.isMaxIterationsReached()) {
                    String message = String.format("Maximum iterations (%d) reached for loop",
                            LoopContext.MAX_ITERATIONS);
                    return new ErrorObject(message);
                }

                BaseObject condition = context.evaluate(node.getCondition(), loopEnv);
                if (ObjectValidator.isError(condition)) {
                    return condition;
                }

                if (!condition.isTruthy()) {
                    break;
                }

                result = context.evaluate(node.getBody(), loopEnv);

                if (ObjectValidator.isReturnValue(result) || ObjectValidator.isError(result)) {
                    return result;
                }

                BaseObject updateResult = context.evaluate(node.getIncrement(), loopEnv);
                if (ObjectValidator.isError(updateResult)) {
                    return updateResult;
                }

                if (ObjectValidator.isBreak(result)) {
                    break;
                }

                if (ObjectValidator.isContinue(result)) {
                    continue;
                }
            }

        } finally {
            loopContext.exitLoop();
        }

        return processLoopResult(result);
    }

    protected BaseObject processLoopResult(BaseObject result) {
        if (ObjectValidator.isBreak(result)) {
            return NullObject.INSTANCE;
        }

        if (ObjectValidator.isContinue(result)) {
            return NullObject.INSTANCE;
        }

        return result;
    }
}
