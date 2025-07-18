package lang.exec.objects.errors;

import lang.exec.base.ObjectType;

/**
 * Factory class for creating specific error instances.
 * Provides convenient static methods for common error scenarios.
 */
public final class ErrorFactory {

    private ErrorFactory() {
    } // Utility class

    // Identifier errors
    public static IdentifierNotFoundError identifierNotFound(String name) {
        return new IdentifierNotFoundError(name);
    }

    // Argument errors
    public static ArgumentError wrongArgumentCount(int expected, int actual) {
        return new ArgumentError(expected, actual);
    }

    public static ArgumentError wrongArgumentType(String functionName, String expectedType, String actualType) {
        return new ArgumentError(functionName, expectedType, actualType);
    }

    public static ArgumentError argumentError(String message) {
        return new ArgumentError(message);
    }

    // Type mismatch errors
    public static TypeMismatchError typeMismatch(ObjectType leftType, String operator, ObjectType rightType) {
        return new TypeMismatchError(leftType, operator, rightType);
    }

    public static TypeMismatchError invalidOperator(String operator, ObjectType leftType, ObjectType rightType) {
        return new TypeMismatchError(operator, leftType, rightType, "This operation is not supported.");
    }

    public static TypeMismatchError typeMismatch(String message) {
        return new TypeMismatchError(message);
    }

    // Index errors
    public static IndexError indexNotSupported(ObjectType containerType) {
        return new IndexError(containerType);
    }

    public static IndexError indexOutOfBounds(int index, int size) {
        return new IndexError(index, size);
    }

    public static IndexError keyNotFound(String key, String containerName) {
        return new IndexError(key, containerName);
    }

    public static IndexError indexError(String message) {
        return new IndexError(message);
    }

    // Arithmetic errors
    public static ArithmeticError divisionByZero() {
        return new ArithmeticError("division by zero");
    }

    public static ArithmeticError integerDivisionByZero() {
        return new ArithmeticError("integer division by zero");
    }

    public static ArithmeticError moduloByZero() {
        return new ArithmeticError("modulo by zero");
    }

    // Assignment errors
    public static AssignmentError constantAssignment(String variableName) {
        return AssignmentError.constantAssignment(variableName);
    }

    public static AssignmentError invalidAssignmentTarget(String targetType) {
        return AssignmentError.invalidTarget(targetType);
    }

    public static AssignmentError assignmentError(String message) {
        return new AssignmentError(message);
    }

    // Class errors
    public static ClassError classAlreadyDefined(String className) {
        return ClassError.alreadyDefined(className);
    }

    public static ClassError parentClassNotFound(String parentClassName) {
        return ClassError.parentNotFound(parentClassName);
    }

    public static ClassError notAClass(String name) {
        return ClassError.notAClass(name);
    }

    public static ClassError circularInheritance(String className, String parentClassName) {
        return ClassError.circularInheritance(className, parentClassName);
    }

    public static ClassError classError(String message) {
        return new ClassError(message);
    }

    // Property errors
    public static PropertyError propertyNotFound(String propertyName, String className) {
        return PropertyError.notFound(propertyName, className);
    }

    public static PropertyError invalidPropertyName() {
        return PropertyError.invalidName();
    }

    public static PropertyError propertyError(String message) {
        return new PropertyError(message);
    }

    // Context errors
    public static ContextError thisNotAvailable() {
        return ContextError.thisNotAvailable();
    }

    public static ContextError superNotInMethod() {
        return ContextError.superNotInMethod();
    }

    public static ContextError superNoParent(String className) {
        return ContextError.superNoParent(className);
    }

    // Instantiation errors
    public static InstantiationError cannotInstantiate(ObjectType attemptedType) {
        return new InstantiationError(attemptedType);
    }

    public static InstantiationError constructorArgumentMismatch(String className, int expected, int actual) {
        return new InstantiationError(className, expected, actual);
    }

    public static InstantiationError noConstructor(String className) {
        return InstantiationError.noConstructor(className);
    }

    public static InstantiationError instantiationError(String message) {
        return new InstantiationError(message);
    }

    // Runtime errors
    public static RuntimeError runtimeError(String message) {
        return new RuntimeError(message);
    }

    public static RuntimeError evaluationError(String message) {
        return RuntimeError.evaluationError(message);
    }

    public static RuntimeError unknownOperator(String operator, String type) {
        return RuntimeError.unknownOperator(operator, type);
    }
}