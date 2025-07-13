package lang.exec.builtins;

import java.util.Map;
import java.util.HashMap;

import lang.exec.objects.BuiltinObject;

public class BuiltinRegistry {

    private static final Map<String, BuiltinObject> builtins = new HashMap<>();

    public static Map<String, BuiltinObject> getBuiltins() {
        return builtins;
    }

    public static void addBuiltin(String name, BuiltinObject builtin) {
        builtins.put(name, builtin);
    }

    public static boolean isBuiltin(String name) {
        return builtins.containsKey(name);
    }

    public static BuiltinObject getBuiltin(String name) {
        return builtins.get(name);
    }
}
