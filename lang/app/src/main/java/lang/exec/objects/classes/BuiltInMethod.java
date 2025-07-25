package lang.exec.objects.classes;

import java.util.List;

import lang.exec.evaluator.base.EvaluationContext;
import lang.exec.objects.base.BaseObject;
import lang.exec.objects.env.Environment;
import lang.exec.objects.error.ErrorObject;

/**
 * ⚡ BuiltInMethod - Methods Implemented in Java ⚡
 * 
 * These methods are implemented directly in Java code, not parsed from source.
 * No need for dummy AST nodes - they're pure Java functions.
 */
public class BuiltInMethod extends Method {

    /**
     * 🔧 Function interface for built-in method implementations
     */
    @FunctionalInterface
    public interface MethodImplementation {
        BaseObject execute(BaseObject instance, BaseObject[] arguments);
    }

    private final MethodImplementation implementation;
    private final List<String> parameterNames;

    public BuiltInMethod(String name, String description,
            List<String> parameterNames,
            MethodImplementation implementation,
            Environment environment) {
        super(name, description, environment);
        this.parameterNames = parameterNames;
        this.implementation = implementation;
    }

    @Override
    public BaseObject call(BaseObject instance, BaseObject[] arguments, EvaluationContext context) {
        var error = validateArgumentCount(arguments, parameterNames.size());
        if (error.isPresent())
            return context.createError(error.get().getMessage(), null);

        try {
            return implementation.execute(instance, arguments);
        } catch (Exception e) {
            return new ErrorObject("Error in built-in method " + name + ": " + e.getMessage());
        }
    }

    @Override
    public List<String> getParameterNames() {
        return parameterNames;
    }

    @Override
    public int getParameterCount() {
        return parameterNames.size();
    }
}
