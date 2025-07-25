package lang.exec.objects.env;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import lang.exec.objects.base.BaseObject;

/**
 * Environment represents a lexical scope for variable storage and retrieval.
 */
public class Environment {
    private final Map<String, BaseObject> variableBindings;
    private final Environment enclosingScope;
    private final Set<String> immutableVariableNames;
    private final boolean representsBlockScope;

    public Environment() {
        this(null, false);
    }

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
     */
    public BaseObject defineVariable(String variableName, BaseObject value) {
        variableBindings.put(variableName, value);
        return value;
    }

    /**
     * Defines a constant variable (cannot be reassigned).
     */
    public BaseObject defineConstant(String constantName, BaseObject value) {
        immutableVariableNames.add(constantName);
        return defineVariable(constantName, value);
    }

    /**
     * Checks if a variable exists in the current environment only (not parents).
     */
    public boolean containsVariableLocally(String variableName) {
        return variableBindings.containsKey(variableName);
    }

    /**
     * Checks if a variable is marked as constant in this environment or any parent.
     */
    public boolean isVariableImmutable(String variableName) {
        if (immutableVariableNames.contains(variableName)) {
            return true;
        }

        return enclosingScope != null && enclosingScope.isVariableImmutable(variableName);
    }

    /**
     * Finds the environment where a variable is defined.
     * This is used for assignment operations - we need to modify the variable
     * in the scope where it was originally declared.
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
     */
    public Environment createChildBlockScope() {
        return new Environment(this, true);
    }

    public Environment createChildFunctionScope() {
        return new Environment(this, false);
    }

    public boolean isBlockScope() {
        return representsBlockScope;
    }

    public Environment getEnclosingScope() {
        return enclosingScope;
    }

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
