package lang.exec.evaluator.expressions;

import java.util.List;
import java.util.Optional;

import lang.ast.expressions.NewExpression;
import lang.exec.evaluator.base.EvaluationContext;
import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.objects.base.BaseObject;
import lang.exec.objects.classes.*;
import lang.exec.objects.env.Environment;
import lang.exec.objects.functions.FunctionObject;
import lang.exec.objects.literals.NullObject;
import lang.exec.objects.error.ErrorObject;
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
        BaseObject clazz = getClassObject(node, env, context);
        if (ObjectValidator.isError(clazz)) {
            return clazz;
        }

        ClassObject classObject = ObjectValidator.asClass(clazz);
        List<BaseObject> arguments = context.evaluateExpressions(node.getArguments(), env);
        for (BaseObject arg : arguments) {
            if (ObjectValidator.isError(arg)) {
                return arg;
            }
        }

        InstanceObject instance = classObject.createInstance();
        Optional<ErrorObject> constructorResult = createConstructor(classObject, arguments, env, context, instance,
                node);

        if (constructorResult.isPresent()) {
            return constructorResult.get();
        }

        return instance;
    }

    /**
     * 🔍 Evaluates the class object from the new expression
     */
    private BaseObject getClassObject(NewExpression node, Environment env, EvaluationContext context) {
        BaseObject classObj = context.evaluate(node.getClassName(), env);
        if (ObjectValidator.isError(classObj)) {
            return classObj;
        }

        if (!ObjectValidator.isClass(classObj)) {
            return context.createError("Cannot instantiate non-class object: " + classObj.type(), node.position());
        }

        return classObj;
    }

    /**
     * 🔍 Creates a constructor for the class object
     */
    private Optional<ErrorObject> createConstructor(
            ClassObject classObject,
            List<BaseObject> arguments,
            Environment env,
            EvaluationContext context,
            InstanceObject instance,
            NewExpression node) {
        if (classObject.hasConstructor()) {
            FunctionObject constructor = classObject.getConstructor().get();

            int requiredArgs = constructor.getParameters().size();
            if (arguments.size() != requiredArgs) {
                String message = String.format("Constructor argument mismatchs: %s requires %d got %d",
                        classObject.getName(), requiredArgs, arguments.size());
                return Optional.of(context.createError(message, node.position()));
            }

            BaseObject constructorResult = callConstructor(constructor, instance, arguments, env, context);
            if (ObjectValidator.isError(constructorResult)) {
                return Optional.of(ObjectValidator.asError(constructorResult));
            }
        } else if (node.hasArguments()) {
            return Optional.of(context.createError("No constructor found for class: " + classObject.getName(),
                    node.position()));
        }

        return Optional.empty();
    }

    /**
     * 🏗️ Calls a constructor function with proper this binding
     */
    private BaseObject callConstructor(
            FunctionObject constructor,
            InstanceObject instance,
            List<BaseObject> arguments,
            Environment env,
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
