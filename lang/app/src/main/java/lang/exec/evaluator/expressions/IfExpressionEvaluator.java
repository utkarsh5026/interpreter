package lang.exec.evaluator.expressions;

import java.util.List;
import java.util.Optional;
import lang.ast.base.Expression;
import lang.ast.statements.BlockStatement;

import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.base.BaseObject;
import lang.exec.objects.env.Environment;
import lang.exec.objects.literals.NullObject;
import lang.exec.validator.ObjectValidator;

import lang.ast.expressions.IfExpression;
import lang.exec.evaluator.base.EvaluationContext;

public class IfExpressionEvaluator implements NodeEvaluator<IfExpression> {

    @Override
    public BaseObject evaluate(IfExpression node, Environment env, EvaluationContext context) {
        List<Expression> conditions = node.getConditions();
        List<BlockStatement> consequences = node.getConsequences();
        Optional<BlockStatement> alternative = node.getAlternative();

        for (int i = 0; i < conditions.size(); i++) {
            BaseObject condition = context.evaluate(conditions.get(i), env);

            if (ObjectValidator.isError(condition)) {
                return condition;
            }

            if (condition.isTruthy()) {
                return context.evaluate(consequences.get(i), env);
            }
        }

        if (alternative.isPresent()) {
            return context.evaluate(alternative.get(), env);
        }

        return NullObject.INSTANCE;
    }
}
