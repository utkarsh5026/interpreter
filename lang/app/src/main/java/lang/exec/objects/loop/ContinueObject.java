package lang.exec.objects.loop;

import lang.exec.objects.base.BaseObject;
import lang.exec.objects.base.ObjectType;

public class ContinueObject implements BaseObject {
    public static final ContinueObject INSTANCE = new ContinueObject();

    @Override
    public ObjectType type() {
        return ObjectType.CONTINUE;
    }

    @Override
    public String inspect() {
        return "continue";
    }
}
