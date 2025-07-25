package lang.exec.evaluator.expressions;

import java.util.Optional;

import lang.ast.base.Expression;
import lang.ast.expressions.PropertyExpression;
import lang.ast.utils.AstCaster;
import lang.ast.utils.AstValidator;
import lang.exec.base.BaseObject;
import lang.exec.evaluator.base.EvaluationContext;
import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.objects.*;
import lang.exec.objects.classes.InstanceObject;
import lang.exec.objects.functions.FunctionObject;
import lang.exec.validator.ObjectValidator;

/**
 * 🔗 PropertyExpressionEvaluator - Property Access Evaluator 🔗
 * 
 * Evaluates property access expressions (instance.property).
 * 
 * From first principles, property access involves:
 * 1. Evaluate object expression to get instance
 * 2. Get property name from property expression
 * 3. Look up property in instance or method in class
 * 4. Return property value or bound method
 */
public class PropertyExpressionEvaluator implements NodeEvaluator<PropertyExpression> {

    @Override
    public BaseObject evaluate(PropertyExpression node, Environment env, EvaluationContext context) {
        BaseObject instance = context.evaluate(node.getObject(), env);
        if (ObjectValidator.isError(instance)) {
            return instance;
        }

        Optional<String> propertyName = extractPropertyName(node.getProperty(), env, context);
        if (propertyName.isEmpty()) {
            return context.createError("Invalid property name", node.getProperty().position());
        }

        if (ObjectValidator.isInstance(instance)) {
            return evaluateInstancePropertyAccess(
                    ObjectValidator.asInstance(instance),
                    propertyName.get(),
                    context,
                    node);
        }

        String message = String.format("Cannot access property '%s' on non-instance object: %s",
                propertyName.get(), instance.type().toString());
        return context.createError(message, node.position());

    }

    /**
     * 🎭 Evaluates property access on an instance object
     */
    private BaseObject evaluateInstancePropertyAccess(InstanceObject instance, String propertyName,
            EvaluationContext context, PropertyExpression node) {
        Optional<BaseObject> property = instance.getProperty(propertyName);
        if (property.isPresent()) {
            return property.get();
        }

        Optional<FunctionObject> method = instance.findMethod(propertyName);
        if (method.isPresent()) {
            return createBoundMethod(method.get(), instance);
        }

        String message = String.format("Property '%s' not found on instance of %s",
                propertyName, instance.getClassObject().getName());
        return context.createError(message, node.position());
    }

    /**
     * 🔗 Creates a bound method (method with 'this' pre-bound to instance)
     */
    private BaseObject createBoundMethod(FunctionObject method, InstanceObject instance) {
        Environment boundEnv = new Environment(method.getEnvironment(), false);
        boundEnv.defineVariable("this", instance);

        return new FunctionObject(boundEnv, method.getParameters(), method.getBody());
    }

    /**
     * 🏷️ Extracts property name from property expression
     * 
     */
    private Optional<String> extractPropertyName(Expression propertyExpr, Environment env, EvaluationContext context) {
        if (AstValidator.isIdentifier(propertyExpr)) {
            var identifier = AstCaster.asIdentifier(propertyExpr);
            return Optional.of(identifier.getValue());
        }

        BaseObject result = context.evaluate(propertyExpr, env);
        if (ObjectValidator.isString(result)) {
            return Optional.of(ObjectValidator.asString(result).getValue());
        }

        return Optional.empty();
    }
}
