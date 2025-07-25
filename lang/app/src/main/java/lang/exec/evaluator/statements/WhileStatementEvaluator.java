package lang.exec.evaluator.statements;

import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.base.BaseObject;
import lang.exec.validator.ObjectValidator;

import lang.exec.objects.Environment;
import lang.exec.objects.literals.NullObject;
import lang.exec.evaluator.base.EvaluationContext;
import lang.ast.statements.WhileStatement;

import lang.exec.evaluator.base.LoopContext;

public class WhileStatementEvaluator implements NodeEvaluator<WhileStatement> {

    private final LoopContext loopContext;

    public WhileStatementEvaluator(LoopContext loopContext) {
        this.loopContext = loopContext;
    }

    @Override
    public BaseObject evaluate(WhileStatement node, Environment env, EvaluationContext context) {
        loopContext.enterLoop();
        BaseObject result = NullObject.INSTANCE;

        try {
            while (true) {
                if (loopContext.isMaxIterationsReached()) {
                    String message = String.format("Maximum iterations (%d) reached for loop",
                            LoopContext.MAX_ITERATIONS);
                    return context.createError(message, node.position());
                }

                BaseObject condition = context.evaluate(node.getCondition(), env);
                if (ObjectValidator.isError(condition)) {
                    return condition;
                }

                if (!condition.isTruthy()) {
                    break;
                }

                result = context.evaluate(node.getBody(), env);
                if (ObjectValidator.isError(result)) {
                    return result;
                }

                if (ObjectValidator.isReturnValue(result)) {
                    return result;
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
