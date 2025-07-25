package lang.exec.objects.env;

import java.util.Map;

import lang.exec.objects.functions.BuiltinObject;
import lang.exec.builtins.BuiltinRegistry;

/**
 * 🏗️ EnvironmentFactory - Scope Creation Specialist 🏗️
 * 
 * Centralized factory for creating different types of environments.
 * This ensures consistent scope behavior across the entire system.
 */
public class EnvironmentFactory {

    /**
     * Creates a global environment with builtin functions
     */
    public static final Environment createGlobalEnvironment(BuiltinRegistry builtinRegistry) {
        Environment global = new Environment();

        Map<String, BuiltinObject> builtins = BuiltinRegistry.getAllBuiltins();
        builtins.forEach((name, builtin) -> {
            global.defineVariable(name, builtin);
        });

        return global;
    }

    /**
     * Creates a function scope for function calls
     */
    public static final Environment createFunctionScope(Environment parent) {
        return new Environment(parent, false);
    }

    /**
     * Creates a block scope for { } blocks
     */
    public static final Environment createBlockScope(Environment parent) {
        return new Environment(parent, true);
    }

    /**
     * Creates a loop scope (for break/continue handling)
     */
    public static final Environment createLoopScope(Environment parent) {
        return new Environment(parent, true);
    }

    /**
     * Creates an empty environment
     */
    public static final Environment empty() {
        return new Environment(null, false);
    }
}