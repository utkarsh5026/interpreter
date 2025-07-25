package lang.exec.objects.classes;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import lang.exec.objects.base.BaseObject;
import lang.exec.objects.env.Environment;
import lang.exec.objects.error.ErrorObject;

import java.util.Collections;

import lang.exec.objects.literals.*;
import lang.exec.validator.ObjectValidator;

/**
 * 🌟 BaseObjectClass - The Root of All Class Hierarchies 🌟
 * 
 * From first principles, every object-oriented language needs a root class
 * that:
 * 1. Provides fundamental methods all objects should have
 * 2. Serves as the ultimate parent for inheritance chains
 * 3. Defines the basic contract for object behavior
 * 
 * This is similar to:
 * - Object in Java
 * - object in Python
 * - Object in JavaScript
 * 
 * All user-defined classes will automatically inherit from this base class
 * unless they explicitly extend another class.
 */
public class BaseObjectClass extends ClassObject {

    public static final String BASE_OBJECT_CLASS_NAME = "Object";

    private static BaseObjectClass instance;

    private BaseObjectClass() {
        super(
                BASE_OBJECT_CLASS_NAME,
                Optional.empty(), // No parent - this IS the root
                Optional.empty(), // No constructor needed
                createBaseMethods(),
                new Environment() // Root environment
        );
    }

    private static Map<String, MethodObject> createBaseMethods() {
        var env = new Environment();
        return Map.of(
                "getClass", createGetClassMethod(env),
                "hashCode", createHashCodeMethod(env),
                "equals", createEqualsMethod(env),
                "toString", createToStringMethod(env));
    }

    /**
     * 🏗️ Gets the singleton instance of BaseObjectClass
     */
    public static BaseObjectClass getInstance() {
        if (instance == null) {
            instance = new BaseObjectClass();
        }
        return instance;
    }

    /**
     * ⚖️ Creates the equals(other) method
     */
    private static MethodObject createEqualsMethod(Environment env) {
        return new BuiltInMethod(
                "equals",
                "Compares this object with another for equality",
                List.of("other"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("equals() takes exactly 1 argument, got " + args.length);
                    }

                    BaseObject other = args[0];

                    // Default implementation: reference equality
                    boolean isEqual = (instance == other);
                    return new BooleanObject(isEqual);
                },

                env);
    }

    /**
     * 📝 Creates the toString() method
     * 
     * From first principles, toString should:
     * 1. Return a string representation of the object
     * 2. Be meaningful for debugging and display
     * 3. Include class name and key properties
     */
    private static MethodObject createToStringMethod(Environment env) {
        return new BuiltInMethod(
                "toString",
                "Returns a string representation of this object",
                Collections.emptyList(),
                (instance, args) -> {

                    if (args.length != 0) {
                        return new ErrorObject("toString() takes no arguments, got " + args.length);
                    }

                    // Default implementation: "ClassName@hashcode"
                    if (!ObjectValidator.isInstance(instance)) {
                        return new ErrorObject("toString() can only be called on instances");
                    }

                    var instanceObject = ObjectValidator.asInstance(instance);
                    String className = instanceObject.getClassObject().getName();
                    int hashCode = System.identityHashCode(instance);
                    String result = className + "@" + Integer.toHexString(hashCode);

                    return new StringObject(result);
                },
                env);
    }

    /**
     * 🏷️ Creates the getClass() method
     */
    private static MethodObject createGetClassMethod(Environment env) {
        return new BuiltInMethod(
                "getClass",
                "Returns the Class object representing the class of this object",
                Collections.emptyList(),
                (instance, args) -> {
                    if (args.length != 0) {
                        return new ErrorObject("getClass() takes no arguments, got " + args.length);
                    }

                    if (!ObjectValidator.isInstance(instance)) {
                        return new ErrorObject("getClass() can only be called on instances");
                    }

                    var instanceObject = ObjectValidator.asInstance(instance);
                    return instanceObject.getClassObject();
                },
                env);
    }

    /**
     * 🔢 Creates the hashCode() method
     */
    private static MethodObject createHashCodeMethod(Environment env) {
        return new BuiltInMethod(
                "hashCode",
                "Returns a hash code for this object",
                Collections.emptyList(),
                (instance, args) -> {
                    if (args.length != 0) {
                        return new ErrorObject("hashCode() takes no arguments, got " + args.length);
                    }

                    if (!ObjectValidator.isInstance(instance)) {
                        return new ErrorObject("hashCode() can only be called on instances");
                    }

                    // Default implementation: identity hash code
                    int hash = System.identityHashCode(instance);
                    return new IntegerObject(hash);
                },
                env);
    }

    /**
     * ✅ Checks if a class should automatically inherit from Object
     */
    public static boolean shouldInheritFromObject(Optional<ClassObject> parentClass) {
        // If no parent is specified, inherit from Object
        // If parent is already Object, don't create circular inheritance
        return parentClass.isEmpty() ||
                !parentClass.get().getName().equals(BASE_OBJECT_CLASS_NAME);
    }

}
