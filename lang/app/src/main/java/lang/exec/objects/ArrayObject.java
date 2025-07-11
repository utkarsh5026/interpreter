package lang.exec.objects;

import java.util.List;

import lang.exec.base.BaseObject;
import lang.exec.base.ObjectType;

public class ArrayObject implements BaseObject {

    private final List<BaseObject> elements;

    public ArrayObject(List<BaseObject> elements) {
        this.elements = elements;
    }

    @Override
    public ObjectType type() {
        return ObjectType.ARRAY;
    }

    public List<BaseObject> getElements() {
        return elements;
    }

    @Override
    public String inspect() {
        List<String> elementStrings = elements.stream().map(BaseObject::inspect).toList();
        return "[" + String.join(", ", elementStrings) + "]";
    }

    @Override
    public boolean isTruthy() {
        return !elements.isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof ArrayObject))
            return false;
        ArrayObject other = (ArrayObject) obj;
        return elements.equals(other.elements);
    }
}
