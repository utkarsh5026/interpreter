package lang.exec.objects.loop;

import lang.exec.objects.base.BaseObject;
import lang.exec.objects.base.ObjectType;

public class BreakObject implements BaseObject {
    public static final BreakObject INSTANCE = new BreakObject();

    @Override
    public ObjectType type() {
        return ObjectType.BREAK;
    }

    @Override
    public String inspect() {
        return "break";
    }

}
