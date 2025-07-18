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

    public boolean remove(BaseObject element) {
        return elements.remove(element);
    }

    @Override
    public boolean isTruthy() {
        return !elements.isEmpty();
    }

    public BaseObject set(int index, BaseObject value) throws IndexOutOfBoundsException {
        if (index < 0 || index >= elements.size()) {
            throw new IndexOutOfBoundsException(
                    String.format("Index %d out of bounds for array of size %d", index, elements.size()));
        }
        elements.set(index, value);
        return value;
    }

    public BaseObject get(int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= elements.size()) {
            throw new IndexOutOfBoundsException(
                    String.format("Index %d out of bounds for array of size %d", index, elements.size()));
        }
        return elements.get(index);
    }

    public boolean append(BaseObject element) {
        return elements.add(element);
    }

    public void insert(int index, BaseObject element) {
        elements.add(index, element);
    }

    public BaseObject removeAt(int index) {
        return elements.remove(index);
    }

    public int size() {
        return elements.size();
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public boolean isValidIndex(int index) {
        return index >= 0 && index < elements.size();
    }

    public void clear() {
        elements.clear();
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

    @Override
    public int hashCode() {
        return elements.hashCode();
    }
}
