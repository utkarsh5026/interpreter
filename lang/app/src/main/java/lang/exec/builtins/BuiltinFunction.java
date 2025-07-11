package lang.exec.builtins;

import lang.exec.base.BaseObject;
import lang.exec.objects.Environment;

/**
 * Functional interface for built-in functions.
 * 
 * This interface represents a function that can be called from the language.
 * It takes an array of arguments and an environment, and returns a result.
 */
@FunctionalInterface
public interface BuiltinFunction {
    BaseObject apply(BaseObject[] arguments, Environment environment);
}
