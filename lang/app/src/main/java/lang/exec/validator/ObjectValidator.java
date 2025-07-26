package lang.exec.validator;

import lang.exec.objects.base.BaseObject;
import lang.exec.objects.builtins.*;
import lang.exec.objects.classes.*;
import lang.exec.objects.error.ErrorObject;
import lang.exec.objects.functions.*;
import lang.exec.objects.literals.*;
import lang.exec.objects.loop.BreakObject;
import lang.exec.objects.loop.ContinueObject;
import lang.exec.objects.structures.ArrayObject;
import lang.exec.objects.structures.HashObject;
import lang.ast.base.Identifier;
import lang.exec.objects.classes.MethodObject;

/**
 * Utility class for type checking objects without casting.
 * This replaces your TypeScript ObjectValidator with Java-specific patterns.
 */
public final class ObjectValidator {

    private ObjectValidator() {
    } // Utility class

    public static boolean isInteger(BaseObject obj) {
        return IntegerClass.isIntegerInstance(obj);
    }

    public static boolean isBoolean(BaseObject obj) {
        return BooleanClass.isBooleanInstance(obj);
    }

    public static boolean isString(BaseObject obj) {
        return StringClass.isStringInstance(obj);
    }

    public static boolean isNull(BaseObject obj) {
        return obj instanceof NullObject;
    }

    public static boolean isError(BaseObject obj) {
        return obj instanceof ErrorObject;
    }

    public static boolean isReturnValue(BaseObject obj) {
        return obj instanceof ReturnObject;
    }

    public static boolean isBreak(BaseObject obj) {
        return obj instanceof BreakObject;
    }

    public static boolean isBreakOrContinue(BaseObject obj) {
        return isBreak(obj) || isContinue(obj);
    }

    public static boolean isContinue(BaseObject obj) {
        return obj instanceof ContinueObject;
    }

    public static boolean isFunction(BaseObject obj) {
        return obj instanceof FunctionObject;
    }

    public static boolean isBuiltin(BaseObject obj) {
        return obj instanceof BuiltinObject;
    }

    public static boolean isArray(BaseObject obj) {
        return obj instanceof ArrayObject;
    }

    public static boolean isHash(BaseObject obj) {
        return obj instanceof HashObject;
    }

    public static boolean isFloat(BaseObject obj) {
        return FloatClass.isFloatInstance(obj);
    }

    public static boolean isNumeric(BaseObject obj) {
        return isInteger(obj) || isFloat(obj);
    }

    public static boolean isClass(BaseObject obj) {
        return obj instanceof ClassObject;
    }

    public static boolean isInstance(BaseObject obj) {
        return obj instanceof InstanceObject;
    }

    public static boolean isIdentifier(BaseObject obj) {
        return obj instanceof Identifier;
    }

    public static boolean isMethod(BaseObject obj) {
        return obj instanceof MethodObject;
    }

    public static boolean isInstanceBoundMethod(BaseObject obj) {
        return obj instanceof InstanceBoundMethod;
    }

    /**
     * Safe casting methods that return Optional or null.
     * These provide type-safe access to specific object types.
     */
    public static IntegerObject asInteger(BaseObject obj) {
        return IntegerClass.getIntegerObject(obj).orElse(null);
    }

    public static StringObject asString(BaseObject obj) {
        return StringClass.getStringObject(obj).orElse(null);
    }

    public static BooleanObject asBoolean(BaseObject obj) {
        return BooleanClass.getBooleanObject(obj).orElse(null);
    }

    public static ErrorObject asError(BaseObject obj) {
        return obj instanceof ErrorObject ? (ErrorObject) obj : null;
    }

    public static ReturnObject asReturnValue(BaseObject obj) {
        return obj instanceof ReturnObject ? (ReturnObject) obj : null;
    }

    public static FunctionObject asFunction(BaseObject obj) {
        return obj instanceof FunctionObject ? (FunctionObject) obj : null;
    }

    public static HashObject asHash(BaseObject obj) {
        return obj instanceof HashObject ? (HashObject) obj : null;
    }

    public static ArrayObject asArray(BaseObject obj) {
        return obj instanceof ArrayObject ? (ArrayObject) obj : null;
    }

    public static BuiltinObject asBuiltin(BaseObject obj) {
        return obj instanceof BuiltinObject ? (BuiltinObject) obj : null;
    }

    public static BreakObject asBreak(BaseObject obj) {
        return obj instanceof BreakObject ? (BreakObject) obj : null;
    }

    public static ContinueObject asContinue(BaseObject obj) {
        return obj instanceof ContinueObject ? (ContinueObject) obj : null;
    }

    public static NullObject asNull(BaseObject obj) {
        return obj instanceof NullObject ? (NullObject) obj : null;
    }

    public static FloatObject asFloat(BaseObject obj) {
        return FloatClass.getFloatObject(obj).orElse(null);
    }

    public static InstanceObject asInstance(BaseObject obj) {
        return obj instanceof InstanceObject ? (InstanceObject) obj : null;
    }

    public static Identifier asIdentifier(BaseObject obj) {
        return obj instanceof Identifier ? (Identifier) obj : null;
    }

    public static final ClassObject asClass(BaseObject obj) {
        return obj instanceof ClassObject ? (ClassObject) obj : null;
    }

    public static InstanceBoundMethod asInstanceBoundMethod(BaseObject obj) {
        return obj instanceof InstanceBoundMethod ? (InstanceBoundMethod) obj : null;
    }
}