package lang.exec.evaluator.expressions;

import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.base.BaseObject;
import lang.exec.validator.ObjectValidator;

import lang.exec.objects.Environment;
import lang.exec.evaluator.base.EvaluationContext;
import lang.ast.expressions.AssignmentExpression;
import lang.exec.objects.ErrorObject;

public class AssignmentExpressionEvaluator implements NodeEvaluator<AssignmentExpression> {

    @Override
    public BaseObject evaluate(AssignmentExpression node, Environment env, EvaluationContext context) {
        String variableName = node.getName().getValue();
        Environment definingScope = env.getDefiningScope(variableName);

        if (definingScope == null) {
            return new ErrorObject("identifier not found: " + variableName);
        }

        if (definingScope.isConstant(variableName) && !definingScope.isBlockScope()) {
            return new ErrorObject(String.format("cannot assign to constant %s", variableName));
        }

        BaseObject value = context.evaluate(node.getValue(), env);

        if (ObjectValidator.isError(value)) {
            return value;
        }

        definingScope.set(variableName, value);
        return value;
    }

}
