package lang.exec.base;

/**
 * Enumeration of all object types in the interpreter.
 * This enum serves multiple purposes:
 * 1. Type identification without reflection
 * 2. Pattern matching in switch statements
 * 3. Runtime type checking
 */
public enum ObjectType {
    INTEGER,
    BOOLEAN,
    STRING,
    NULL,
    RETURN_VALUE,
    ERROR,
    FUNCTION,
    BUILTIN,
    ARRAY,
    HASH,
    CLASS,
    INSTANCE,
    BREAK,
    CONTINUE
}
