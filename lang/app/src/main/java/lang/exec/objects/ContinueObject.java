package lang.exec.objects;

import lang.exec.base.BaseObject;
import lang.exec.base.ObjectType;

public class ContinueObject implements BaseObject {
    @Override
    public ObjectType type() {
        return ObjectType.CONTINUE;
    }

    @Override
    public String inspect() {
        return "continue";
    }
}
