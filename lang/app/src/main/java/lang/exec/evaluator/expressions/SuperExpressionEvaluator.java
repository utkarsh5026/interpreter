package lang.exec.evaluator.expressions;

import java.util.List;
import java.util.Optional;

import lang.ast.expressions.SuperExpression;
import lang.ast.utils.AstCaster;
import lang.ast.utils.AstValidator;
import lang.exec.objects.base.*;
import lang.exec.objects.classes.*;
import lang.exec.objects.env.Environment;
import lang.exec.objects.functions.FunctionObject;
import lang.exec.validator.ObjectValidator;
import lang.ast.base.Expression;
import lang.exec.evaluator.base.EvaluationContext;
import lang.exec.evaluator.base.NodeEvaluator;

/**
 * ⬆️ SuperExpressionEvaluator - Parent Class Access Evaluator ⬆️
 * 
 * Evaluates super expressions for calling parent class methods.
 * 
 * From first principles, super evaluation involves:
 * 1. Find current instance from 'this' binding
 * 2. Get EXECUTION CONTEXT class (not instance class!)
 * 3. Find method in parent class of execution context
 * 4. Call method with current instance as 'this'
 */
public class SuperExpressionEvaluator implements NodeEvaluator<SuperExpression> {

    // 🔑 Reserved environment variable to track current class context
    private static final String CLASS_CONTEXT_VAR = "__class_context__";

    private static final String THIS_VARIABLE = "this";

    private static final String NO_THIS_CONTEXT_ERROR = "'this' is not available in this context";
    private static final String NO_PARENT_CLASS_ERROR = "No parent class found for class: %s";
    private static final String NO_CONSTRUCTOR_ERROR = "No constructor found for class: %s with %d arguments";
    private static final String METHOD_NOT_FOUND_ERROR = "Method '%s' not found in parent class '%s'";
    private static final String ARGUMENT_MISMATCH_ERROR = "%s argument mismatch: requires %d, got %d";

    @Override
    public BaseObject evaluate(SuperExpression node, Environment env, EvaluationContext context) {
        Optional<BaseObject> thisObj = env.resolveVariable(THIS_VARIABLE);
        if (thisObj.isEmpty()) {
            return context.createError(NO_THIS_CONTEXT_ERROR, node.position());
        }

        if (!ObjectValidator.isInstance(thisObj.get())) {
            return context.createError(NO_THIS_CONTEXT_ERROR, node.position());
        }

        InstanceObject instance = ObjectValidator.asInstance(thisObj.get());
        ClassObject currentClass = getCurrentClassContext(env, instance);

        if (!currentClass.hasParentClass()) {
            var errorMessage = String.format(NO_PARENT_CLASS_ERROR, currentClass.getName());
            return context.createError(errorMessage, node.position());
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
        if (!parentClass.hasConstructor()) {
            int argCount = node.getArguments().size();
            if (argCount == 0) {
                return instance;
            }

            var errorMessage = String.format(NO_CONSTRUCTOR_ERROR, parentClass.getName(), argCount);
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
            var errorMessage = String.format(ARGUMENT_MISMATCH_ERROR, parentClass.getName(), requiredArgs,
                    arguments.size());
            return context.createError(errorMessage, node.position());
        }

        return callParentConstructor(parentConstructor, instance, arguments, parentClass, env, context);
    }

    /**
     * 🔧 Evaluates super method call: super.method(args)
     */
    private BaseObject evaluateSuperMethodCall(
            SuperExpression node,
            ClassObject parentClass,
            InstanceObject instance,
            Environment env,
            EvaluationContext context) {
        var methodName = extractMethodName(node.getMethod());
        if (methodName.isEmpty()) {
            return context.createError(METHOD_NOT_FOUND_ERROR, node.position());
        }

        Optional<MethodObject> method = parentClass.findMethod(methodName.get());
        if (!method.isPresent()) {
            var errorMessage = String.format(METHOD_NOT_FOUND_ERROR, methodName.get(), parentClass.getName());
            return context.createError(errorMessage, node.position());
        }

        List<BaseObject> arguments = context.evaluateExpressions(node.getArguments(), env);
        for (BaseObject arg : arguments) {
            if (ObjectValidator.isError(arg)) {
                return arg;
            }
        }

        MethodObject parentMethod = method.get();
        BaseObject result = parentMethod.call(instance, arguments.toArray(new BaseObject[0]), context,
                methodEnv -> {
                    methodEnv.defineVariable(CLASS_CONTEXT_VAR, new ClassContextObject(parentClass));
                    return methodEnv;
                });

        if (ObjectValidator.isReturnValue(result)) {
            return ObjectValidator.asReturnValue(result).getValue();
        }

        return result;
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
        constructorEnv.defineVariable(THIS_VARIABLE, instance);

        for (int i = 0; i < constructor.getParameters().size(); i++) {
            String paramName = constructor.getParameters().get(i).getValue();
            constructorEnv.defineVariable(paramName, arguments.get(i));
        }

        return context.evaluate(constructor.getBody(), constructorEnv);
    }

    /**
     * 🏷️ Extracts method name from method expression
     */
    private Optional<String> extractMethodName(Expression methodExpr) {
        if (AstValidator.isIdentifier(methodExpr)) {
            return Optional.of(AstCaster.asIdentifier(methodExpr).getValue());
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