package lang.exec.objects;

import lang.exec.base.BaseObject;
import lang.exec.base.ObjectType;

/**
 * ReturnObject represents a return value from a function.
 * 
 * This class is used to store the return value of a function and provide a way
 * to inspect it.
 */
public class ReturnObject implements BaseObject {
    private final BaseObject value;

    public ReturnObject(BaseObject value) {
        this.value = value;
    }

    public BaseObject getValue() {
        return value;
    }

    @Override
    public ObjectType type() {
        return ObjectType.RETURN_VALUE;
    }

    @Override
    public String inspect() {
        return value.inspect();
    }

    @Override
    public boolean isTruthy() {
        return true;
    }
}
