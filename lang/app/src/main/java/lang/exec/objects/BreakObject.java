package lang.exec.objects;

import lang.exec.base.BaseObject;
import lang.exec.base.ObjectType;

public class BreakObject implements BaseObject {
    @Override
    public ObjectType type() {
        return ObjectType.BREAK;
    }

    @Override
    public String inspect() {
        return "break";
    }

}
