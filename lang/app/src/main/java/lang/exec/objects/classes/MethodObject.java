package lang.exec.objects.classes;

import java.util.List;
import java.util.Optional;

import lang.exec.evaluator.base.EvaluationContext;
import lang.exec.objects.error.ErrorObject;
import lang.exec.objects.base.BaseObject;
import lang.exec.objects.base.ObjectType;
import lang.exec.objects.env.Environment;

/**
 * 🎯 Method - Universal Method Abstraction 🎯
 * 
 * From first principles, a method is something that:
 * 1. Can be called with arguments
 * 2. Has access to 'this' (the instance it's called on)
 * 3. Returns a result
 * 4. Can be either user-defined (from source code) or built-in (Java
 * implementation)
 * 
 * This abstraction handles both types without forcing one into the shape of the
 * other.
 */
public abstract class MethodObject implements BaseObject {
    protected final String name;
    protected final String description;
    protected final Environment environment;

    protected MethodObject(String name, String description, Environment environment) {
        this.name = name;
        this.description = description;
        this.environment = environment;
    }

    /**
     * 📞 Calls this method with the given instance and arguments
     */
    public abstract BaseObject call(InstanceObject instance, BaseObject[] arguments, EvaluationContext context);

    /**
     * ✅ Validates that the correct number of arguments were provided
     */
    protected Optional<ErrorObject> validateArgumentCount(BaseObject[] arguments, int expected) {
        if (arguments.length != expected) {
            var error = new ErrorObject(String.format(
                    "Method '%s' expects %d arguments, got %d",
                    name, expected, arguments.length));
            return Optional.of(error);
        }
        return Optional.empty();
    }

    /**
     * 📋 Gets the parameter names/count for this method
     */
    public abstract List<String> getParameterNames();

    /**
     * 🔢 Gets the number of parameters this method expects
     */
    public abstract int getParameterCount();

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Environment getEnvironment() {
        return environment;
    }

    @Override
    public String inspect() {
        return String.format("method %s(%s) { %s }",
                name,
                String.join(", ", getParameterNames()),
                description);
    }

    @Override
    public ObjectType type() {
        return ObjectType.FUNCTION;
    }
}