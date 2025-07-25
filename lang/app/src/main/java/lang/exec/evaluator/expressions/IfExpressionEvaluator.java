package lang.exec.evaluator.expressions;

import java.util.List;
import java.util.Optional;
import lang.ast.base.Expression;
import lang.ast.statements.BlockStatement;
import lang.exec.evaluator.base.EvaluationContext;
import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.objects.base.BaseObject;
import lang.exec.objects.env.Environment;
import lang.exec.objects.literals.NullObject;
import lang.exec.validator.ObjectValidator;

import lang.ast.expressions.IfExpression;

public class IfExpressionEvaluator implements NodeEvaluator<IfExpression> {

    @Override
    public BaseObject evaluate(IfExpression node, Environment env, EvaluationContext context) {
        List<Expression> conditions = node.getConditions();
        List<BlockStatement> consequences = node.getConsequences();
        Optional<BlockStatement> alternative = node.getAlternative();

        Optional<BaseObject> conditionResult = evaluateConditions(conditions, consequences, env, context);

        if (conditionResult.isPresent()) {
            return conditionResult.get();
        }

        if (alternative.isPresent()) {
            return context.evaluate(alternative.get(), env);
        }

        return NullObject.INSTANCE;
    }

    private Optional<BaseObject> evaluateConditions(List<Expression> conditions, List<BlockStatement> consequences,
            Environment env, EvaluationContext context) {
        for (int i = 0; i < conditions.size(); i++) {
            BaseObject condition = context.evaluate(conditions.get(i), env);

            if (ObjectValidator.isError(condition)) {
                return Optional.of(condition);
            }

            if (condition.isTruthy()) {
                var result = context.evaluate(consequences.get(i), env);
                return Optional.of(result);
            }
        }

        return Optional.empty();
    }
}
