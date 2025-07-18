package lang.exec.evaluator.expressions;

import java.util.Optional;
import lang.ast.expressions.PropertyExpression;
import lang.ast.expressions.IndexExpression;
import lang.exec.evaluator.base.NodeEvaluator;

import lang.exec.validator.ObjectValidator;
import lang.ast.utils.*;
import lang.exec.evaluator.base.EvaluationContext;
import lang.ast.expressions.AssignmentExpression;

import lang.exec.base.BaseObject;
import lang.exec.objects.*;
import lang.ast.base.*;
import lang.exec.objects.errors.ErrorFactory;

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
        BaseObject assignedValue = context.evaluate(node.getValue(), env);
        if (ObjectValidator.isError(assignedValue)) {
            return assignedValue;
        }

        if (node.isIdentifierAssignment()) {
            return assignToVariable(node, assignedValue, env);
        }

        if (node.isIndexAssignment()) {
            return assignToIndex(node, assignedValue, env, context);
        }

        if (node.isPropertyAssignment()) {
            return assignToProperty(node, assignedValue, env, context);
        }

        return ErrorFactory.invalidAssignmentTarget(node.getTarget().getClass().getSimpleName());

    }

    /**
     * 🏷️ Handles simple identifier assignment: x = value
     * 
     * This is the traditional variable assignment where we store a value
     * in a variable name within the current environment scope.
     */
    private BaseObject assignToVariable(AssignmentExpression node, BaseObject value, Environment env) {
        String variableName = AstCaster.asIdentifier(node.getTarget()).getValue();
        Optional<Environment> definingScope = env.findVariableDeclarationScope(variableName);

        if (definingScope.isEmpty()) {
            return ErrorFactory.identifierNotFound(variableName);
        }

        if (definingScope.get().isVariableImmutable(variableName)) {
            return ErrorFactory.constantAssignment(variableName);
        }

        definingScope.get().defineVariable(variableName, value);
        return value;
    }

    /**
     * 🗂️ Handles index assignment: array[0] = value or hash["key"] = value
     * 
     */
    private BaseObject assignToIndex(
            AssignmentExpression node,
            BaseObject value,
            Environment env,
            EvaluationContext context) {
        IndexExpression indexExpr = AstCaster.asIndexExpression(node.getTarget());

        BaseObject container = context.evaluate(indexExpr.getLeft(), env);
        if (ObjectValidator.isError(container)) {
            return container;
        }

        BaseObject indexObject = context.evaluate(indexExpr.getIndex(), env);
        if (ObjectValidator.isError(indexObject)) {
            return indexObject;
        }

        if (ObjectValidator.isArray(container)) {
            return assignToArrayElement(container, indexObject, value);
        }

        if (ObjectValidator.isHash(container)) {
            return assignToHashEntry(container, indexObject, value);
        }

        return ErrorFactory.indexNotSupported(container.type());
    }

    /**
     * 📋 Handles array index assignment: array[index] = value
     */
    private BaseObject assignToArrayElement(BaseObject arrayObject, BaseObject indexObject, BaseObject value) {
        ArrayObject array = ObjectValidator.asArray(arrayObject);

        if (!ObjectValidator.isInteger(indexObject)) {
            return ErrorFactory.typeMismatch(array.type(), "array index", indexObject.type());
        }

        int index = (int) ObjectValidator.asInteger(indexObject).getValue();
        if (!array.isValidIndex(index)) {
            return ErrorFactory.indexOutOfBounds(index, array.size());
        }

        try {
            return array.set(index, value);
        } catch (IndexOutOfBoundsException e) {
            return ErrorFactory.indexError(e.getMessage());
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
    private BaseObject assignToHashEntry(BaseObject hashObject, BaseObject keyObject, BaseObject value) {
        HashObject hash = ObjectValidator.asHash(hashObject);

        if (!ObjectValidator.isString(keyObject) && !ObjectValidator.isInteger(keyObject)) {
            return ErrorFactory.typeMismatch(hash.type(), "hash key", keyObject.type());
        }

        String key = null;
        if (ObjectValidator.isString(keyObject)) {
            key = ObjectValidator.asString(keyObject).getValue();
        } else if (ObjectValidator.isInteger(keyObject)) {
            key = String.valueOf(ObjectValidator.asInteger(keyObject).getValue());
        } else {
            return ErrorFactory.typeMismatch(hash.type(), "hash key", keyObject.type());
        }

        return hash.set(key, value);
    }

    private BaseObject assignToProperty(AssignmentExpression node, BaseObject value, Environment env,
            EvaluationContext context) {
        PropertyExpression propertyExpr = AstCaster.asPropertyExpression(node.getTarget());

        BaseObject targetObject = context.evaluate(propertyExpr.getObject(), env);
        if (ObjectValidator.isError(targetObject)) {
            return targetObject;
        }

        Optional<String> propertyName = extractPropertyName(propertyExpr.getProperty(), env, context);
        if (propertyName.isEmpty()) {
            return ErrorFactory.invalidPropertyName();
        }

        if (ObjectValidator.isInstance(targetObject)) {
            InstanceObject instance = ObjectValidator.asInstance(targetObject);
            return instance.setProperty(propertyName.get(), value);
        }

        return ErrorFactory.propertyError("Cannot assign property '" + propertyName.get() + "' on non-instance object: "
                + targetObject.type());

    }

    /**
     * 🏷️ Extracts property name from property expression
     */
    private Optional<String> extractPropertyName(Expression propertyExpr, Environment env, EvaluationContext context) {
        if (AstValidator.isIdentifier(propertyExpr)) {
            return Optional.of(AstCaster.asIdentifier(propertyExpr).getValue());
        }

        BaseObject result = context.evaluate(propertyExpr, env);
        if (ObjectValidator.isString(result)) {
            return Optional.of(ObjectValidator.asString(result).getValue());
        }

        return Optional.empty();
    }
}