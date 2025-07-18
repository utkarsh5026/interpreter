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
import lang.exec.validator.ObjectValidator;
import lang.exec.objects.errors.ErrorFactory;

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
            return ErrorFactory.invalidPropertyName();
        }

        if (ObjectValidator.isInstance(instance)) {
            return evaluateInstancePropertyAccess((InstanceObject) instance, propertyName.get());
        }

        return ErrorFactory.propertyError("Cannot access property '" + propertyName.get() + "' on non-instance object: "
                + instance.type().toString());

    }

    /**
     * 🎭 Evaluates property access on an instance object
     */
    private BaseObject evaluateInstancePropertyAccess(InstanceObject instance, String propertyName) {
        Optional<BaseObject> property = instance.getProperty(propertyName);
        if (property.isPresent()) {
            return property.get();
        }

        Optional<FunctionObject> method = instance.findMethod(propertyName);
        if (method.isPresent()) {
            return createBoundMethod(method.get(), instance);
        }

        return ErrorFactory.propertyError("Property '" + propertyName + "' not found on instance of "
                + instance.getClassObject().getName());
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
