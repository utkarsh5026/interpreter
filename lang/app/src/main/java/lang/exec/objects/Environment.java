package lang.exec.objects;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lang.exec.base.BaseObject;

/**
 * Environment represents a lexical scope for variable storage and retrieval.
 * 
 * Key Concepts from First Principles:
 * 
 * 1. LEXICAL SCOPING: Variables are resolved based on where they are defined in
 * the code,
 * not where they are called. This is implemented using a chain of environments.
 * 
 * 2. SCOPE CHAIN: Each environment can have a parent (outer) environment. When
 * looking up
 * a variable, we search the current environment first, then walk up the chain.
 * 
 * 3. VARIABLE SHADOWING: Inner scopes can declare variables with the same name
 * as outer
 * scopes, "shadowing" the outer variable.
 * 
 * 4. CONSTANTS: Some variables cannot be reassigned after declaration.
 */
public class Environment {

    private final Map<String, BaseObject> store;

    // Reference to the enclosing scope (null for global scope)
    private final Environment outer;

    // Set of variable names that are constants (cannot be reassigned)
    private final Set<String> constants;

    // Flag indicating if this is a block scope (for let/const scoping rules)
    private final boolean isBlockScope;

    /**
     * Creates a new global environment (no parent).
     */
    public Environment() {
        this(null, false);
    }

    /**
     * Creates a new environment with an optional parent.
     * 
     * @param outer        the parent environment, or null for global scope
     * @param isBlockScope true if this represents a block scope (like { })
     */
    public Environment(Environment outer, boolean isBlockScope) {
        this.store = new HashMap<>();
        this.outer = outer;
        this.constants = new HashSet<>();
        this.isBlockScope = isBlockScope;
    }

    /**
     * Retrieves a variable from this environment or any parent environment.
     * 
     * This implements the SCOPE RESOLUTION algorithm:
     * 1. Check current environment
     * 2. If not found and there's a parent, recursively check parent
     * 3. Return null if not found anywhere
     * 
     * @param name the variable name to look up
     * @return the value, or null if not found
     */
    public BaseObject get(String name) {
        BaseObject value = store.get(name);

        // If not found in current scope, try parent scope
        if (value == null && outer != null) {
            return outer.get(name);
        }

        return value;
    }

    /**
     * Sets a variable in the current environment.
     * 
     * @param name  the variable name
     * @param value the value to store
     * @return the value that was stored
     */
    public BaseObject set(String name, BaseObject value) {
        store.put(name, value);
        return value;
    }

    /**
     * Sets a constant variable (cannot be reassigned).
     * 
     * @param name  the constant name
     * @param value the value to store
     * @return the value that was stored
     */
    public BaseObject setConst(String name, BaseObject value) {
        constants.add(name);
        return set(name, value);
    }

    /**
     * Checks if a variable exists in the current environment only (not parents).
     * 
     * @param name the variable name to check
     * @return true if the variable exists in this environment
     */
    public boolean has(String name) {
        return store.containsKey(name);
    }

    /**
     * Checks if a variable is marked as constant in this environment or any parent.
     * 
     * @param name the variable name to check
     * @return true if the variable is a constant
     */
    public boolean isConstant(String name) {
        if (constants.contains(name)) {
            return true;
        }

        // Check parent environments
        return outer != null && outer.isConstant(name);
    }

    /**
     * Finds the environment where a variable is defined.
     * This is used for assignment operations - we need to modify the variable
     * in the scope where it was originally declared.
     * 
     * @param name the variable name
     * @return the environment where the variable is defined, or null if not found
     */
    public Optional<Environment> getDefiningScope(String name) {
        if (store.containsKey(name)) {
            return Optional.of(this);
        }

        if (outer != null) {
            return outer.getDefiningScope(name);
        }

        return Optional.empty();
    }

    /**
     * Creates a new block scope that inherits from this environment.
     * Block scopes are used for { } blocks in code.
     * 
     * @return a new Environment representing a block scope
     */
    public Environment newBlockScope() {
        return new Environment(this, true);
    }

    /**
     * Creates a new function scope that inherits from this environment.
     * Function scopes are used for function calls.
     * 
     * @return a new Environment representing a function scope
     */
    public Environment newFunctionScope() {
        return new Environment(this, false);
    }

    /**
     * Returns whether this is a block scope.
     */
    public boolean isBlockScope() {
        return isBlockScope;
    }

    /**
     * Returns the parent environment.
     */
    public Environment getOuter() {
        return outer;
    }

    /**
     * Returns a copy of the current environment's variables.
     * Useful for debugging and introspection.
     */
    public Map<String, BaseObject> getStore() {
        return new HashMap<>(store);
    }

    /**
     * Debugging method to dump the entire scope chain.
     * This helps visualize the environment hierarchy.
     */
    public String dumpScopeChain() {
        StringBuilder sb = new StringBuilder();
        Environment current = this;
        int level = 0;

        while (current != null) {
            sb.append("  ".repeat(level))
                    .append("Scope ")
                    .append(level)
                    .append(current.isBlockScope ? " (block)" : " (function)")
                    .append(": ")
                    .append(current.store.keySet())
                    .append("\n");

            current = current.outer;
            level++;
        }

        return sb.toString();
    }
}
