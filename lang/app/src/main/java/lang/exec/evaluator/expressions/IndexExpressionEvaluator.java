package lang.exec.evaluator.expressions;

import lang.ast.expressions.IndexExpression;
import lang.exec.evaluator.base.EvaluationContext;
import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.validator.ObjectValidator;
import lang.exec.objects.base.BaseObject;
import lang.exec.objects.env.Environment;
import lang.exec.objects.literals.NullObject;
import lang.exec.objects.structures.*;
import lang.ast.base.Expression;

public class IndexExpressionEvaluator implements NodeEvaluator<IndexExpression> {

    @Override
    public BaseObject evaluate(IndexExpression node, Environment env, EvaluationContext context) {
        BaseObject left = context.evaluate(node.getLeft(), env);
        if (ObjectValidator.isError(left)) {
            return left;
        }

        if (ObjectValidator.isArray(left)) {
            return evalArrayIndexExpression(left, node.getIndex(), env, context);
        }

        if (ObjectValidator.isHash(left)) {
            return evalHashIndexExpression(left, node.getIndex(), env, context);
        }

        return context.createError("Index not supported for type: " + left.type(), node.position());
    }

    private BaseObject evalArrayIndexExpression(BaseObject array, Expression index, Environment env,
            EvaluationContext context) {
        ArrayObject arrayObject = ObjectValidator.asArray(array);
        BaseObject indexObject = context.evaluate(index, env);

        if (!ObjectValidator.isInteger(indexObject)) {
            return context.createError("Type mismatch: array index must be an integer", index.position());
        }

        int arrIndex = (int) ObjectValidator.asInteger(indexObject).getValue();
        int arrSize = arrayObject.getElements().size();
        if (arrIndex < 0 || arrIndex >= arrSize) {
            var errorMessage = String.format("Index out of bounds: %d is not in the range [0, %d]", arrIndex, arrSize);
            return context.createError(errorMessage, index.position());
        }

        return arrayObject.getElements().get(arrIndex);
    }

    private BaseObject evalHashIndexExpression(BaseObject hash, Expression index, Environment env,
            EvaluationContext context) {
        HashObject hashObject = ObjectValidator.asHash(hash);
        BaseObject indexObject = context.evaluate(index, env);

        if (!ObjectValidator.isString(indexObject) && !ObjectValidator.isInteger(indexObject)) {
            return context.createError("Type mismatch: hash index must be a string or integer", index.position());
        }

        String key = ObjectValidator.isString(indexObject) ? ObjectValidator.asString(indexObject).getValue()
                : String.valueOf(ObjectValidator.asInteger(indexObject).getValue());

        if (!hashObject.getPairs().containsKey(key)) {
            return NullObject.INSTANCE;
        }

        return hashObject.getPairs().get(key);
    }
}
