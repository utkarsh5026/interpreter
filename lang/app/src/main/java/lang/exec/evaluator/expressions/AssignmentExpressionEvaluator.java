package lang.exec.evaluator.expressions;

import lang.ast.expressions.IndexExpression;
import lang.exec.evaluator.base.NodeEvaluator;

import lang.exec.validator.ObjectValidator;
import lang.ast.utils.AstCaster;

import lang.exec.evaluator.base.EvaluationContext;
import lang.ast.expressions.AssignmentExpression;

import lang.exec.base.BaseObject;
import lang.exec.objects.ErrorObject;
import lang.exec.objects.ArrayObject;
import lang.exec.objects.HashObject;
import lang.exec.objects.Environment;

/**
 * 📝 AssignmentExpressionEvaluator - Universal Assignment Specialist 📝
 * 
 * Handles all types of assignment expressions:
 * 1. Simple identifier assignment: x = value
 * 2. Array index assignment: array[0] = value
 * 3. Hash key assignment: hash["key"] = value
 */
public class AssignmentExpressionEvaluator implements NodeEvaluator<AssignmentExpression> {

    @Override
    public BaseObject evaluate(AssignmentExpression node, Environment env, EvaluationContext context) {
        BaseObject value = context.evaluate(node.getValue(), env);
        if (ObjectValidator.isError(value)) {
            return value;
        }

        // Handle different types of assignment targets
        if (node.isIdentifierAssignment()) {
            return evaluateIdentifierAssignment(node, value, env);
        } else if (node.isIndexAssignment()) {
            return evaluateIndexAssignment(node, value, env, context);
        } else {
            return new ErrorObject("Invalid assignment target: " + node.getTarget().getClass().getSimpleName());
        }
    }

    /**
     * 🏷️ Handles simple identifier assignment: x = value
     * 
     * This is the traditional variable assignment where we store a value
     * in a variable name within the current environment scope.
     */
    private BaseObject evaluateIdentifierAssignment(AssignmentExpression node, BaseObject value, Environment env) {
        String variableName = AstCaster.asIdentifier(node.getTarget()).getValue();
        Environment definingScope = env.getDefiningScope(variableName);

        if (definingScope == null) {
            return new ErrorObject("identifier not found: " + variableName);
        }

        if (definingScope.isConstant(variableName)) {
            return new ErrorObject(String.format("cannot assign to constant %s", variableName));
        }

        definingScope.set(variableName, value);
        return value;
    }

    /**
     * 🗂️ Handles index assignment: array[0] = value or hash["key"] = value
     * 
     */
    private BaseObject evaluateIndexAssignment(AssignmentExpression node, BaseObject value,
            Environment env, EvaluationContext context) {
        IndexExpression indexExpr = AstCaster.asIndexExpression(node.getTarget());

        BaseObject targetObject = context.evaluate(indexExpr.getLeft(), env);
        if (ObjectValidator.isError(targetObject)) {
            return targetObject;
        }

        BaseObject indexObject = context.evaluate(indexExpr.getIndex(), env);
        if (ObjectValidator.isError(indexObject)) {
            return indexObject;
        }

        if (ObjectValidator.isArray(targetObject)) {
            return evaluateArrayIndexAssignment(targetObject, indexObject, value);
        } else if (ObjectValidator.isHash(targetObject)) {
            return evaluateHashIndexAssignment(targetObject, indexObject, value);
        } else {
            return new ErrorObject("Index assignment not supported for type: " + targetObject.type());
        }
    }

    /**
     * 📋 Handles array index assignment: array[index] = value
     */
    private BaseObject evaluateArrayIndexAssignment(BaseObject arrayObject, BaseObject indexObject, BaseObject value) {
        ArrayObject array = ObjectValidator.asArray(arrayObject);

        if (!ObjectValidator.isInteger(indexObject)) {
            return new ErrorObject(String.format(
                    "Array index must be an integer, got: %s", indexObject.type()));
        }

        int index = (int) ObjectValidator.asInteger(indexObject).getValue();
        if (!array.isValidIndex(index)) {
            return new ErrorObject(String.format(
                    "Array index out of bounds: %d for array of size %d", index, array.size()));
        }

        try {
            return array.set(index, value);
        } catch (IndexOutOfBoundsException e) {
            return new ErrorObject(e.getMessage());
        }
    }

    /**
     * 🗃️ Handles hash index assignment: hash["key"] = value
     * 
     * From first principles, hash assignment means:
     * 1. Verify the key is a valid string
     * 2. Update or create the key-value pair in the hash
     * 3. Return the assigned value
     */
    private BaseObject evaluateHashIndexAssignment(BaseObject hashObject, BaseObject keyObject, BaseObject value) {
        HashObject hash = ObjectValidator.asHash(hashObject);

        if (!ObjectValidator.isString(keyObject) && !ObjectValidator.isInteger(keyObject)) {
            return new ErrorObject(String.format(
                    "Hash key must be a string or integer, got: %s", keyObject.type()));
        }

        String key = ObjectValidator.isString(keyObject) ? ObjectValidator.asString(keyObject).getValue()
                : String.valueOf(ObjectValidator.asInteger(keyObject).getValue());
        return hash.set(key, value);
    }
}