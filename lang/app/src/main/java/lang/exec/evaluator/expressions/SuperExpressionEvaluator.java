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
import lang.exec.base.ObjectType;

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

    // 🔑 Reserved environment variable to track current class context
    private static final String CLASS_CONTEXT_VAR = "__class_context__";

    @Override
    public BaseObject evaluate(SuperExpression node, Environment env, EvaluationContext context) {
        Optional<BaseObject> thisObj = env.resolveVariable("this");
        if (thisObj.isEmpty()) {
            return context.createError("'this' is not available in this context", node.position());
        }

        if (!ObjectValidator.isInstance(thisObj.get())) {
            return context.createError("'this' is not available in this context", node.position());
        }

        InstanceObject instance = ObjectValidator.asInstance(thisObj.get());
        ClassObject currentClass = getCurrentClassContext(env, instance);

        if (!currentClass.hasParentClass()) {
            return context.createError("No parent class found for class: "
                    + currentClass.getName(),
                    node.position());
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
    private BaseObject evaluateSuperConstructorCall(
            SuperExpression node,
            ClassObject parentClass,
            InstanceObject instance,
            Environment env,
            EvaluationContext context) {
        // check if the constructir even exists
        if (!parentClass.hasConstructor()) {
            int argCount = node.getArguments().size();
            if (argCount == 0) {
                return instance;
            }

            var errorMessage = String.format("No constructor found for class: %s and %d arguments are provided",
                    parentClass.getName(), argCount);
            return context.createError(errorMessage, node.position());
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
            String message = String.format("Constructor argument mismatchs: %s requires %d got %d",
                    parentClass.getName(), requiredArgs, arguments.size());
            return context.createError(message, node.position());
        }

        return callParentConstructor(parentConstructor, instance, arguments, parentClass, env, context);
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
            String message = String.format("Constructor argument mismatchs: %s requires %d got %d",
                    parentClass.getName(), requiredArgs, arguments.size());
            return context.createError(message, node.position());
        }

        return callParentMethod(parentMethod, instance, arguments, env, context);
    }

    /**
     * 🏗️ Calls parent constructor with proper this binding AND class context
     * 
     * From first principles:
     * 1. Create a new environment for the constructor
     * 2. Bind 'this' to the current instance
     * 3. Bind parameters
     * 4. Execute the constructor
     * 5. Return the result
     */
    private BaseObject callParentConstructor(
            FunctionObject constructor,
            InstanceObject instance,
            List<BaseObject> arguments,
            ClassObject parentClass,
            Environment env,
            EvaluationContext context) {
        Environment constructorEnv = new Environment(constructor.getEnvironment(), false);
        constructorEnv.defineVariable(CLASS_CONTEXT_VAR, new ClassContextObject(parentClass));
        constructorEnv.defineVariable("this", instance);

        for (int i = 0; i < constructor.getParameters().size(); i++) {
            String paramName = constructor.getParameters().get(i).getValue();
            constructorEnv.defineVariable(paramName, arguments.get(i));
        }

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

    /**
     * 🎯 Gets the current class execution context
     * 
     * From first principles:
     * 1. Check if environment has explicit class context (set during
     * method/constructor calls)
     * 2. If not, fall back to instance's actual class (for top-level calls)
     */
    private ClassObject getCurrentClassContext(Environment env, InstanceObject instance) {
        Optional<BaseObject> classContext = env.resolveVariable(CLASS_CONTEXT_VAR);
        if (classContext.isPresent() && classContext.get().type() == ObjectType.CLASS_CONTEXT) {
            return ((ClassContextObject) classContext.get()).getClassObject();
        }
        return instance.getClassObject();
    }

    /**
     * 🎯 Class Context Object - Wraps a ClassObject for environment storage
     * 
     * This is a helper object that allows us to store class context information
     * in the environment variables.
     */
    private static class ClassContextObject implements BaseObject {
        private final ClassObject classObject;

        public ClassContextObject(ClassObject classObject) {
            this.classObject = classObject;
        }

        public ClassObject getClassObject() {
            return classObject;
        }

        @Override
        public ObjectType type() {
            return ObjectType.CLASS_CONTEXT;
        }

        @Override
        public String toString() {
            return "ClassContext(" + classObject.getName() + ")";
        }

        @Override
        public String inspect() {
            return "ClassContext(" + classObject.getName() + ")";
        }
    }
}