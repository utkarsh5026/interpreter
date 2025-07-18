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

    // Maps variable names to their values in this scope
    private final Map<String, BaseObject> variableBindings;

    // Reference to the enclosing scope (null for global scope)
    private final Environment enclosingScope;

    // Set of variable names that are constants (cannot be reassigned)
    private final Set<String> immutableVariableNames;

    // Flag indicating if this represents a block scope (for let/const scoping
    // rules)
    private final boolean representsBlockScope;

    /**
     * Creates a new global environment (no parent).
     */
    public Environment() {
        this(null, false);
    }

    /**
     * Creates a new environment with an optional parent.
     * 
     * @param enclosingScope       the parent environment, or null for global scope
     * @param representsBlockScope true if this represents a block scope (like { })
     */
    public Environment(Environment enclosingScope, boolean representsBlockScope) {
        this.variableBindings = new HashMap<>();
        this.enclosingScope = enclosingScope;
        this.immutableVariableNames = new HashSet<>();
        this.representsBlockScope = representsBlockScope;
    }

    /**
     * Retrieves a variable from this environment or any parent environment.
     * 
     * This implements the SCOPE RESOLUTION algorithm:
     * 1. Check current environment
     * 2. If not found and there's a parent, recursively check parent
     * 3. Return null if not found anywhere
     * 
     * @param variableName the variable name to look up
     * @return the value, or null if not found
     */
    public Optional<BaseObject> resolveVariable(String variableName) {
        BaseObject value = variableBindings.get(variableName);

        if (value == null && enclosingScope != null) {
            return enclosingScope.resolveVariable(variableName);
        }

        return Optional.ofNullable(value);
    }

    /**
     * Defines a variable in the current environment.
     * 
     * @param variableName the variable name
     * @param value        the value to store
     * @return the value that was stored
     */
    public BaseObject defineVariable(String variableName, BaseObject value) {
        variableBindings.put(variableName, value);
        return value;
    }

    /**
     * Defines a constant variable (cannot be reassigned).
     * 
     * @param constantName the constant name
     * @param value        the value to store
     * @return the value that was stored
     */
    public BaseObject defineConstant(String constantName, BaseObject value) {
        immutableVariableNames.add(constantName);
        return defineVariable(constantName, value);
    }

    /**
     * Checks if a variable exists in the current environment only (not parents).
     * 
     * @param variableName the variable name to check
     * @return true if the variable exists in this environment
     */
    public boolean containsVariableLocally(String variableName) {
        return variableBindings.containsKey(variableName);
    }

    /**
     * Checks if a variable is marked as constant in this environment or any parent.
     * 
     * @param variableName the variable name to check
     * @return true if the variable is a constant
     */
    public boolean isVariableImmutable(String variableName) {
        if (immutableVariableNames.contains(variableName)) {
            return true;
        }

        // Check parent environments
        return enclosingScope != null && enclosingScope.isVariableImmutable(variableName);
    }

    /**
     * Finds the environment where a variable is defined.
     * This is used for assignment operations - we need to modify the variable
     * in the scope where it was originally declared.
     * 
     * @param variableName the variable name
     * @return the environment where the variable is defined, or empty if not found
     */
    public Optional<Environment> findVariableDeclarationScope(String variableName) {
        if (variableBindings.containsKey(variableName)) {
            return Optional.of(this);
        }

        if (enclosingScope != null) {
            return enclosingScope.findVariableDeclarationScope(variableName);
        }

        return Optional.empty();
    }

    /**
     * Creates a new block scope that inherits from this environment.
     * Block scopes are used for { } blocks in code.
     * 
     * @return a new Environment representing a block scope
     */
    public Environment createChildBlockScope() {
        return new Environment(this, true);
    }

    /**
     * Creates a new function scope that inherits from this environment.
     * Function scopes are used for function calls.
     * 
     * @return a new Environment representing a function scope
     */
    public Environment createChildFunctionScope() {
        return new Environment(this, false);
    }

    /**
     * Returns whether this represents a block scope.
     */
    public boolean isBlockScope() {
        return representsBlockScope;
    }

    /**
     * Returns the parent environment.
     */
    public Environment getEnclosingScope() {
        return enclosingScope;
    }

    /**
     * Returns a copy of the current environment's variable bindings.
     * Useful for debugging and introspection.
     */
    public Map<String, BaseObject> getLocalVariableBindings() {
        return new HashMap<>(variableBindings);
    }

    /**
     * Debugging method to visualize the entire scope hierarchy.
     * This helps understand the environment chain structure.
     */
    public String debugScopeHierarchy() {
        StringBuilder hierarchyDescription = new StringBuilder();
        Environment currentScope = this;
        int scopeDepth = 0;

        while (currentScope != null) {
            hierarchyDescription.append("  ".repeat(scopeDepth))
                    .append("Scope Level ")
                    .append(scopeDepth)
                    .append(currentScope.representsBlockScope ? " (block scope)" : " (function scope)")
                    .append(": ")
                    .append(currentScope.variableBindings.keySet())
                    .append("\n");

            currentScope = currentScope.enclosingScope;
            scopeDepth++;
        }

        return hierarchyDescription.toString();
    }
}
