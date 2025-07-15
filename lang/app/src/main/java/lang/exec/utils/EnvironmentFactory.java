package lang.exec.utils;

import java.util.Map;

import lang.exec.objects.BuiltinObject;
import lang.exec.objects.Environment;
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
    public Environment createGlobalEnvironment(BuiltinRegistry builtinRegistry) {
        Environment global = new Environment();

        Map<String, BuiltinObject> builtins = BuiltinRegistry.getAllBuiltins();
        builtins.forEach((name, builtin) -> {
            global.set(name, builtin);
        });

        return global;
    }

    /**
     * Creates a function scope for function calls
     */
    public Environment createFunctionScope(Environment parent) {
        return new Environment(parent, false);
    }

    /**
     * Creates a block scope for { } blocks
     */
    public Environment createBlockScope(Environment parent) {
        return new Environment(parent, true);
    }

    /**
     * Creates a loop scope (for break/continue handling)
     */
    public Environment createLoopScope(Environment parent) {
        return new Environment(parent, true);
    }
}