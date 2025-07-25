package lang.exec.objects.base;

/**
 * Base interface for all object types in the interpreter.
 * 
 * This interface represents the foundation of the type system in your
 * interpreter.
 * Every value that can be computed, stored, or manipulated implements this
 * interface.
 * 
 * Design Philosophy:
 * - All runtime values are objects that implement this interface
 * - Objects are immutable by design (functional programming principle)
 * - Type checking is done through the ObjectType enum rather than instanceof
 * - Each object can inspect itself (toString-like functionality)
 */
public interface BaseObject {

    /**
     * Returns the type of this object.
     * This is used for type checking and dispatch in the evaluator.
     * 
     * @return the ObjectType enum value representing this object's type
     */
    ObjectType type();

    /**
     * Returns a string representation of this object.
     * This is used for debugging and displaying values to users.
     * 
     * @return a human-readable string representation
     */
    String inspect();

    /**
     * Determines if this object should be considered "truthy" in boolean contexts.
     * Different object types have different truthiness rules:
     * - Numbers: false if zero, true otherwise
     * - Strings: false if empty, true otherwise
     * - Arrays: false if empty, true otherwise
     * - Null: always false
     * - Functions: always true
     * 
     * @return true if this object is truthy, false otherwise
     */
    default boolean isTruthy() {
        return true;
    }
}
