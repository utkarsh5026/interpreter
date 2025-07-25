package lang.exec.objects.functions;

import lang.exec.builtins.BuiltinFunction;
import lang.exec.base.BaseObject;
import lang.exec.base.ObjectType;

/**
 * BuiltinObject represents a built-in function in the language.
 * 
 * This class is used to store built-in functions and provide a way to call
 * them.
 */
public class BuiltinObject implements BaseObject {
    private final BuiltinFunction function;
    private final String name;
    private final String description;

    public BuiltinObject(BuiltinFunction function, String name, String description) {
        this.function = function;
        this.name = name;
        this.description = description;
    }

    public BuiltinFunction getFunction() {
        return function;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public ObjectType type() {
        return ObjectType.BUILTIN;
    }

    @Override
    public String inspect() {
        return String.format("Builtin Function: %s\nDescription: %s", name, description);
    }

}
