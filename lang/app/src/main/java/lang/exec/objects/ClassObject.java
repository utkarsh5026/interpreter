package lang.exec.objects;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

import lang.exec.base.BaseObject;
import lang.exec.base.ObjectType;

/**
 * 🏛️ ClassObject - Runtime Class Representation 🏛️
 * 
 * Represents a class during program execution. This is the runtime equivalent
 * of the ClassStatement AST node.
 * 
 * From first principles, a class object needs to store:
 * - Class metadata (name, parent class)
 * - Constructor function
 * - Instance methods
 * - Class inheritance chain
 * 
 * The class object serves as a template for creating instances and
 * provides method resolution for inheritance.
 */
public class ClassObject implements BaseObject {
    private final String name; // 🏷️ Class name
    private final Optional<ClassObject> parentClass; // 🔗 Parent class for inheritance
    private final Optional<FunctionObject> constructor; // 🏗️ Constructor function
    private final Map<String, FunctionObject> methods; // 📋 Instance methods
    private final Environment classEnvironment; // 🌍 Class-level environment

    public ClassObject(String name, Optional<ClassObject> parentClass,
            Optional<FunctionObject> constructor,
            Map<String, FunctionObject> methods,
            Environment classEnvironment) {
        this.name = name;
        this.parentClass = parentClass;
        this.constructor = constructor;
        this.methods = new HashMap<>(methods);
        this.classEnvironment = classEnvironment;
    }

    /**
     * 🏷️ Gets the class name
     */
    public String getName() {
        return name;
    }

    /**
     * 🔗 Gets the parent class (if any)
     */
    public Optional<ClassObject> getParentClass() {
        return parentClass;
    }

    /**
     * 🏗️ Gets the constructor function
     */
    public Optional<FunctionObject> getConstructor() {
        return constructor;
    }

    /**
     * 📋 Gets all methods defined in this class
     */
    public Map<String, FunctionObject> getMethods() {
        return new HashMap<>(methods);
    }

    /**
     * 🌍 Gets the class environment
     */
    public Environment getClassEnvironment() {
        return classEnvironment;
    }

    /**
     * 🔍 Finds a method in this class or parent classes (method resolution)
     * 
     * From first principles, method resolution follows these steps:
     * 1. Check if method exists in current class
     * 2. If not found, check parent class recursively
     * 3. Continue up the inheritance chain until found or reach top
     * 
     * This implements dynamic method dispatch for inheritance.
     */
    public Optional<FunctionObject> findMethod(String methodName) {
        // Check current class first
        FunctionObject method = methods.get(methodName);
        if (method != null) {
            return Optional.of(method);
        }

        // Check parent class recursively
        if (parentClass.isPresent()) {
            return parentClass.get().findMethod(methodName);
        }

        return Optional.empty();
    }

    /**
     * ✅ Checks if this class inherits from another class
     */
    public boolean hasParentClass() {
        return parentClass.isPresent();
    }

    /**
     * ✅ Checks if this class has a constructor
     */
    public boolean hasConstructor() {
        return constructor.isPresent();
    }

    /**
     * 📜 Gets the complete inheritance chain
     * 
     * Returns a list starting with this class and going up to the root.
     * Useful for debugging and reflection.
     */
    public List<ClassObject> getInheritanceChain() {
        List<ClassObject> chain = new ArrayList<>();
        ClassObject current = this;

        while (current != null) {
            chain.add(current);
            current = current.parentClass.orElse(null);
        }

        return chain;
    }

    /**
     * ✅ Checks if this class is a subclass of another class
     */
    public boolean isSubclassOf(ClassObject otherClass) {
        if (this == otherClass) {
            return false; // A class is not a subclass of itself
        }

        ClassObject current = this.parentClass.orElse(null);
        while (current != null) {
            if (current == otherClass) {
                return true;
            }
            current = current.parentClass.orElse(null);
        }

        return false;
    }

    /**
     * 🆕 Creates a new instance of this class
     * 
     * From first principles, instance creation involves:
     * 1. Create a new InstanceObject
     * 2. Set up instance environment with class reference
     * 3. Instance is ready for constructor call
     */
    public InstanceObject createInstance() {
        return new InstanceObject(this, new Environment(classEnvironment, false));
    }

    @Override
    public ObjectType type() {
        return ObjectType.CLASS;
    }

    @Override
    public String inspect() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ").append(name);

        if (parentClass.isPresent()) {
            sb.append(" extends ").append(parentClass.get().getName());
        }

        sb.append(" { ");

        if (constructor.isPresent()) {
            sb.append("constructor, ");
        }

        sb.append(methods.size()).append(" methods }");

        return sb.toString();
    }

    @Override
    public boolean isTruthy() {
        return true; // Classes are always truthy
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof ClassObject))
            return false;
        ClassObject other = (ClassObject) obj;
        return name.equals(other.name); // Classes are equal if they have the same name
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
