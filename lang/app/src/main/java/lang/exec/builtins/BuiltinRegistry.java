package lang.exec.builtins;

import java.util.Map;
import java.util.HashMap;

import lang.exec.objects.BuiltinObject;

public class BuiltinRegistry {

    private final Map<String, BuiltinObject> builtins = new HashMap<>();

    public Map<String, BuiltinObject> getBuiltins() {
        return builtins;
    }

    public void addBuiltin(String name, BuiltinObject builtin) {
        builtins.put(name, builtin);
    }

}
