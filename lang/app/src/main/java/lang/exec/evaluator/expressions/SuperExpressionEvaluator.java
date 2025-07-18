package lang.exec.evaluator.expressions;

import java.util.List;
import java.util.Optional;

import lang.ast.expressions.SuperExpression;
import lang.exec.base.BaseObject;
import lang.exec.evaluator.base.EvaluationContext;
import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.objects.*;
import lang.exec.validator.ObjectValidator;
import lang.ast.base.Expression;
import lang.exec.objects.errors.ErrorFactory;

/**
 * ⬆️ SuperExpressionEvaluator - Parent Class Access Evaluator ⬆️
 * 
 * Evaluates super expressions for calling parent class methods.
 * 
 * From first principles, super evaluation involves:
 * 1. Find current instance from 'this' binding
 * 2. Get parent class from current instance's class
 * 3. Find method in parent class
 * 4. Call method with current instance as 'this'
 */
public class SuperExpressionEvaluator implements NodeEvaluator<SuperExpression> {

    @Override
    public BaseObject evaluate(SuperExpression node, Environment env, EvaluationContext context) {
        // Get current instance ('this' must be bound in environment)
        Optional<BaseObject> thisObj = env.resolveVariable("this");
        if (thisObj.isEmpty()) {
            return ErrorFactory.thisNotAvailable();
        }

        if (!ObjectValidator.isInstance(thisObj.get())) {
            return ErrorFactory.thisNotAvailable();
        }

        InstanceObject instance = ObjectValidator.asInstance(thisObj.get());
        ClassObject currentClass = instance.getClassObject();

        if (!currentClass.hasParentClass()) {
            return ErrorFactory.superNoParent(currentClass.getName());
        }

        ClassObject parentClass = currentClass.getParentClass().get();

        if (node.isConstructorCall()) {
            return evaluateSuperConstructorCall(node, parentClass, instance, env, context);
        } else {
            return evaluateSuperMethodCall(node, parentClass, instance, env, context);
        }
    }

    /**
     * 🏗️ Evaluates super constructor call: super(args)
     */
    private BaseObject evaluateSuperConstructorCall(SuperExpression node, ClassObject parentClass,
            InstanceObject instance, Environment env,
            EvaluationContext context) {
        if (!parentClass.hasConstructor()) {
            if (node.getArguments().size() > 0) {
                return ErrorFactory.noConstructor(parentClass.getName());
            }
            return instance;
        }

        FunctionObject parentConstructor = parentClass.getConstructor().get();

        List<BaseObject> arguments = context.evaluateExpressions(node.getArguments(), env);
        for (BaseObject arg : arguments) {
            if (ObjectValidator.isError(arg)) {
                return arg;
            }
        }

        int requiredArgs = parentConstructor.getParameters().size();
        if (arguments.size() != requiredArgs) {
            return ErrorFactory.constructorArgumentMismatch(
                    parentClass.getName(),
                    requiredArgs,
                    arguments.size());
        }

        return callParentConstructor(parentConstructor, instance, arguments, env, context);
    }

    /**
     * 🔧 Evaluates super method call: super.method(args)
     */
    private BaseObject evaluateSuperMethodCall(SuperExpression node, ClassObject parentClass,
            InstanceObject instance, Environment env,
            EvaluationContext context) {
        var methodName = extractMethodName(node.getMethod(), env, context);
        if (!methodName.isPresent()) {
            return new ErrorObject("Invalid method name in super call");
        }

        Optional<FunctionObject> method = parentClass.findMethod(methodName.get());
        if (!method.isPresent()) {
            return new ErrorObject("Method '" + methodName.get() + "' not found in parent class '" +
                    parentClass.getName() + "'");
        }

        List<BaseObject> arguments = context.evaluateExpressions(node.getArguments(), env);
        for (BaseObject arg : arguments) {
            if (ObjectValidator.isError(arg)) {
                return arg;
            }
        }

        FunctionObject parentMethod = method.get();
        int requiredArgs = parentMethod.getParameters().size();
        if (arguments.size() != requiredArgs) {
            return ErrorFactory.constructorArgumentMismatch(
                    parentClass.getName(),
                    requiredArgs,
                    arguments.size());
        }

        return callParentMethod(parentMethod, instance, arguments, env, context);
    }

    /**
     * 🏗️ Calls parent constructor with proper this binding
     */
    private BaseObject callParentConstructor(FunctionObject constructor, InstanceObject instance,
            List<BaseObject> arguments, Environment env,
            EvaluationContext context) {
        // Create constructor environment
        Environment constructorEnv = new Environment(constructor.getEnvironment(), false);

        // Bind 'this' to current instance
        constructorEnv.defineVariable("this", instance);

        // Bind parameters
        for (int i = 0; i < constructor.getParameters().size(); i++) {
            String paramName = constructor.getParameters().get(i).getValue();
            constructorEnv.defineVariable(paramName, arguments.get(i));
        }

        // Execute constructor
        return context.evaluate(constructor.getBody(), constructorEnv);
    }

    /**
     * 🔧 Calls parent method with proper this binding
     */
    private BaseObject callParentMethod(FunctionObject method, InstanceObject instance,
            List<BaseObject> arguments, Environment env,
            EvaluationContext context) {
        // Create method environment
        Environment methodEnv = new Environment(method.getEnvironment(), false);

        // Bind 'this' to current instance
        methodEnv.defineVariable("this", instance);

        // Bind parameters
        for (int i = 0; i < method.getParameters().size(); i++) {
            String paramName = method.getParameters().get(i).getValue();
            methodEnv.defineVariable(paramName, arguments.get(i));
        }

        // Execute method
        BaseObject result = context.evaluate(method.getBody(), methodEnv);

        // Handle return values
        if (ObjectValidator.isReturnValue(result)) {
            return ObjectValidator.asReturnValue(result).getValue();
        }

        return result;
    }

    /**
     * 🏷️ Extracts method name from method expression
     */
    private Optional<String> extractMethodName(Expression methodExpr, Environment env,
            EvaluationContext context) {
        if (methodExpr instanceof lang.ast.base.Identifier) {
            return Optional.of(((lang.ast.base.Identifier) methodExpr).getValue());
        }
        return Optional.empty();
    }
}