package lang.exec.evaluator.expressions;

import java.util.List;

import lang.ast.expressions.NewExpression;
import lang.exec.base.BaseObject;
import lang.exec.evaluator.base.EvaluationContext;
import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.objects.*;
import lang.exec.validator.ObjectValidator;

/**
 * 🆕 NewExpressionEvaluator - Object Instantiation Evaluator 🆕
 * 
 * Evaluates new expressions to create class instances.
 * 
 * From first principles, object instantiation involves:
 * 1. Evaluate class expression to get ClassObject
 * 2. Create new InstanceObject from class
 * 3. Call constructor with provided arguments
 * 4. Return the initialized instance
 */
public class NewExpressionEvaluator implements NodeEvaluator<NewExpression> {

    @Override
    public BaseObject evaluate(NewExpression node, Environment env, EvaluationContext context) {
        BaseObject classObj = context.evaluate(node.getClassName(), env);
        if (ObjectValidator.isError(classObj)) {
            return classObj;
        }

        if (!ObjectValidator.isClass(classObj)) {
            return context.createError("Cannot instantiate non-class object: " + classObj.type(), node.position());
        }

        ClassObject clazz = (ClassObject) classObj;
        List<BaseObject> arguments = context.evaluateExpressions(node.getArguments(), env);
        for (BaseObject arg : arguments) {
            if (ObjectValidator.isError(arg)) {
                return arg;
            }
        }

        InstanceObject instance = clazz.createInstance();
        if (clazz.hasConstructor()) {
            FunctionObject constructor = clazz.getConstructor().get();

            int requiredArgs = constructor.getParameters().size();
            if (arguments.size() != requiredArgs) {
                String message = String.format("Constructor argument mismatchs: %s requires %d got %d",
                        clazz.getName(), requiredArgs, arguments.size());
                return context.createError(message, node.position());
            }

            BaseObject constructorResult = callConstructor(constructor, instance, arguments, env, context);
            if (ObjectValidator.isError(constructorResult)) {
                return constructorResult;
            }
        } else if (node.hasArguments()) {
            return context.createError("No constructor found for class: " + clazz.getName(), node.position());
        }

        return instance;
    }

    /**
     * 🏗️ Calls a constructor function with proper this binding
     */
    private BaseObject callConstructor(FunctionObject constructor, InstanceObject instance,
            List<BaseObject> arguments, Environment env,
            EvaluationContext context) {
        Environment constructorEnv = new Environment(constructor.getEnvironment(), false);
        constructorEnv.defineVariable("this", instance);

        for (int i = 0; i < constructor.getParameters().size(); i++) {
            String paramName = constructor.getParameters().get(i).getValue();
            constructorEnv.defineVariable(paramName, arguments.get(i));
        }

        BaseObject result = context.evaluate(constructor.getBody(), constructorEnv);

        if (ObjectValidator.isReturnValue(result)) {
            return NullObject.INSTANCE;
        }

        return result;
    }
}
