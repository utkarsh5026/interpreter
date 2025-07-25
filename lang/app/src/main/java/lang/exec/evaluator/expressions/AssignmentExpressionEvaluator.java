package lang.exec.evaluator.expressions;

import java.util.Optional;
import lang.ast.expressions.PropertyExpression;
import lang.ast.expressions.IndexExpression;
import lang.exec.evaluator.base.NodeEvaluator;

import lang.exec.validator.ObjectValidator;
import lang.token.TokenPosition;
import lang.ast.utils.*;
import lang.exec.evaluator.base.EvaluationContext;
import lang.ast.expressions.AssignmentExpression;

import lang.exec.base.BaseObject;
import lang.exec.objects.*;
import lang.exec.objects.classes.InstanceObject;
import lang.ast.base.*;

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
            return assignToVariable(node, assignedValue, env, context);
        }

        if (node.isIndexAssignment()) {
            return assignToIndex(node, assignedValue, env, context);
        }

        if (node.isPropertyAssignment()) {
            return assignToProperty(node, assignedValue, env, context);
        }

        return context.createError("Invalid assignment target: " + node.getTarget().getClass().getSimpleName(),
                node.position());

    }

    /**
     * 🏷️ Handles simple identifier assignment: x = value
     * 
     * This is the traditional variable assignment where we store a value
     * in a variable name within the current environment scope.
     */
    private BaseObject assignToVariable(AssignmentExpression node, BaseObject value, Environment env,
            EvaluationContext context) {
        String variableName = AstCaster.asIdentifier(node.getTarget()).getValue();
        Optional<Environment> definingScope = env.findVariableDeclarationScope(variableName);

        if (definingScope.isEmpty()) {
            return context.createError("Identifier not found: " + variableName, node.getTarget().position());
        }

        if (definingScope.get().isVariableImmutable(variableName)) {
            return context.createError("Constant already assigned: " + variableName, node.getTarget().position());
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
        TokenPosition position = indexExpr.getLeft().position();

        BaseObject container = context.evaluate(indexExpr.getLeft(), env);
        if (ObjectValidator.isError(container)) {
            return container;
        }

        BaseObject indexObject = context.evaluate(indexExpr.getIndex(), env);
        if (ObjectValidator.isError(indexObject)) {
            return indexObject;
        }

        if (ObjectValidator.isArray(container)) {
            return assignToArrayElement(container, indexObject, value, context, position);
        }

        if (ObjectValidator.isHash(container)) {
            return assignToHashEntry(container, indexObject, value, context, position);
        }

        return context.createError("Index not supported for type: " + container.type(), node.position());
    }

    /**
     * 📋 Handles array index assignment: array[index] = value
     */
    private BaseObject assignToArrayElement(BaseObject arrayObject, BaseObject indexObject, BaseObject value,
            EvaluationContext context, TokenPosition position) {
        ArrayObject array = ObjectValidator.asArray(arrayObject);

        if (!ObjectValidator.isInteger(indexObject)) {
            return context.createError("Type mismatch: array index must be an integer", position);
        }

        int index = (int) ObjectValidator.asInteger(indexObject).getValue();
        if (!array.isValidIndex(index)) {
            return context.createError("Index out of bounds: " + index + " is not in the range [0, "
                    + array.size() + ")", position);
        }

        try {
            return array.set(index, value);
        } catch (IndexOutOfBoundsException e) {
            return context.createError(e.getMessage(), position);
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
    private BaseObject assignToHashEntry(BaseObject hashObject, BaseObject keyObject, BaseObject value,
            EvaluationContext context, TokenPosition position) {
        HashObject hash = ObjectValidator.asHash(hashObject);

        if (!ObjectValidator.isString(keyObject) && !ObjectValidator.isInteger(keyObject)) {
            return context.createError("Type mismatch: hash key must be a string or integer", position);
        }

        String key = null;
        if (ObjectValidator.isString(keyObject)) {
            key = ObjectValidator.asString(keyObject).getValue();
        } else if (ObjectValidator.isInteger(keyObject)) {
            key = String.valueOf(ObjectValidator.asInteger(keyObject).getValue());
        } else {
            return context.createError("Type mismatch: hash key must be a string or integer", position);
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
            return context.createError("Invalid property name", propertyExpr.getProperty().position());
        }

        if (ObjectValidator.isInstance(targetObject)) {
            InstanceObject instance = ObjectValidator.asInstance(targetObject);
            return instance.setProperty(propertyName.get(), value);
        }

        String message = String.format("Cannot assign property '%s' on non-instance object: %s",
                propertyName.get(), targetObject.type());
        return context.createError(message, propertyExpr.getProperty().position());

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