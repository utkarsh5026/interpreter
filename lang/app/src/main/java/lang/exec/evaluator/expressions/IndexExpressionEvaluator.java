package lang.exec.evaluator.expressions;

import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.objects.Environment;
import lang.exec.evaluator.base.EvaluationContext;
import lang.ast.expressions.IndexExpression;
import lang.exec.base.BaseObject;
import lang.exec.objects.ErrorObject;
import lang.exec.objects.ArrayObject;
import lang.exec.validator.ObjectValidator;
import lang.exec.objects.HashObject;
import lang.exec.objects.NullObject;

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

        return new ErrorObject("Index operator not supported for type: " + left.type());
    }

    private BaseObject evalArrayIndexExpression(BaseObject array, Expression index, Environment env,
            EvaluationContext context) {
        ArrayObject arrayObject = ObjectValidator.asArray(array);
        BaseObject indexObject = context.evaluate(index, env);

        if (!ObjectValidator.isInteger(indexObject)) {
            return new ErrorObject(String.format("Index must be an integer literal, got: %s", indexObject.inspect()));
        }

        int arrIndex = (int) ObjectValidator.asInteger(indexObject).getValue();

        if (arrIndex < 0 || arrIndex >= arrayObject.getElements().size()) {
            return new ErrorObject(
                    String.format("Index out of bounds: %d for array of size %d", arrIndex,
                            arrayObject.getElements().size()));
        }

        return arrayObject.getElements().get(arrIndex);
    }

    private BaseObject evalHashIndexExpression(BaseObject hash, Expression index, Environment env,
            EvaluationContext context) {
        HashObject hashObject = ObjectValidator.asHash(hash);
        BaseObject indexObject = context.evaluate(index, env);

        if (!ObjectValidator.isString(indexObject)) {
            return new ErrorObject(String.format("Index must be a string literal, got: %s", indexObject.inspect()));
        }

        String key = ObjectValidator.asString(indexObject).getValue();

        if (!hashObject.getPairs().containsKey(key)) {
            return NullObject.INSTANCE;
        }

        return hashObject.getPairs().get(key);
    }
}
