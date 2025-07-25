package lang.exec.objects;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import lang.exec.base.BaseObject;
import lang.exec.base.ObjectType;
import lang.exec.objects.functions.FunctionObject;

/**
 * 🎭 InstanceObject - Runtime Instance Representation 🎭
 * 
 * Represents an instance of a class during program execution.
 * 
 * From first principles, an instance needs:
 * - Reference to its class (for method lookup)
 * - Instance variables (unique to this instance)
 * - Instance environment (for variable storage)
 * - Method binding (this pointer for method calls)
 * 
 * Each instance is independent - they can have different values
 * for instance variables while sharing the same methods from their class.
 */
public class InstanceObject implements BaseObject {
    private final ClassObject classObject; // 🏛️ The class this is an instance of
    private final Map<String, BaseObject> properties; // 📦 Instance variables
    private final Environment instanceEnvironment; // 🌍 Instance-level environment

    public InstanceObject(ClassObject classObject, Environment instanceEnvironment) {
        this.classObject = classObject;
        this.properties = new HashMap<>();
        this.instanceEnvironment = instanceEnvironment;
        this.instanceEnvironment.defineVariable("this", this);
    }

    /**
     * 🏛️ Gets the class this is an instance of
     */
    public ClassObject getClassObject() {
        return classObject;
    }

    /**
     * 🌍 Gets the instance environment
     */
    public Environment getInstanceEnvironment() {
        return instanceEnvironment;
    }

    /**
     * 🔧 Sets an instance property
     * 
     * Instance properties are specific to each object instance.
     * Setting a property only affects this instance, not others.
     */
    public BaseObject setProperty(String name, BaseObject value) {
        properties.put(name, value);
        return value;
    }

    /**
     * 🔍 Gets an instance property
     * 
     * Property lookup follows these steps:
     * 1. Check instance properties first
     * 2. If not found, could potentially check class for static properties
     * (not implemented in this basic version)
     */
    public Optional<BaseObject> getProperty(String name) {
        return Optional.ofNullable(properties.get(name));
    }

    /**
     * ❓ Checks if this instance has a specific property
     */
    public boolean hasProperty(String name) {
        return properties.containsKey(name);
    }

    /**
     * 📋 Gets all property names for this instance
     */
    public java.util.Set<String> getPropertyNames() {
        return properties.keySet();
    }

    /**
     * 🔍 Finds a method in this instance's class hierarchy
     * 
     * Method lookup delegates to the class object, which handles
     * the inheritance chain traversal.
     */
    public Optional<FunctionObject> findMethod(String methodName) {
        return classObject.findMethod(methodName);
    }

    /**
     * 🏗️ Gets the constructor for this instance's class
     */
    public Optional<FunctionObject> getConstructor() {
        return classObject.getConstructor();
    }

    /**
     * ✅ Checks if this instance is of a specific class
     */
    public boolean isInstanceOf(ClassObject classObj) {
        return classObject == classObj || classObject.isSubclassOf(classObj);
    }

    /**
     * ✅ Checks if this instance is of a class with a specific name
     */
    public boolean isInstanceOf(String className) {
        return classObject.getName().equals(className) ||
                classObject.getInheritanceChain().stream()
                        .anyMatch(cls -> cls.getName().equals(className));
    }

    @Override
    public ObjectType type() {
        return ObjectType.INSTANCE;
    }

    @Override
    public String inspect() {
        StringBuilder sb = new StringBuilder();
        sb.append("instance of ").append(classObject.getName()).append(" {");

        boolean first = true;
        for (Map.Entry<String, BaseObject> entry : properties.entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(entry.getKey()).append(": ").append(entry.getValue().inspect());
            first = false;
        }

        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean isTruthy() {
        return true; // Instances are always truthy
    }

    @Override
    public boolean equals(Object obj) {
        // Instances are equal only if they're the same object (reference equality)
        return this == obj;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }
}