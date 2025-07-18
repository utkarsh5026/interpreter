package lang.exec.objects.errors;

import lang.exec.base.ObjectType;
import lang.exec.objects.ErrorObject;

/**
 * Error for array/hash indexing issues.
 * Examples: "array index out of bounds", "Index operator not supported"
 */
public class IndexError extends ErrorObject {
    private final ObjectType containerType;
    private final int index;

    public IndexError(String message) {
        super(message);
        this.containerType = null;
        this.index = -1;
    }

    public IndexError(ObjectType containerType) {
        super(String.format("Index operator not supported for type: %s", containerType));
        this.containerType = containerType;
        this.index = -1;
    }

    public IndexError(int index, int size) {
        super(String.format("array index out of bounds: index %d, length %d", index, size));
        this.containerType = null;
        this.index = index;
    }

    public IndexError(String key, String containerName) {
        super(String.format("key '%s' not found in %s", key, containerName));
        this.containerType = null;
        this.index = -1;
    }

    public ObjectType getContainerType() {
        return containerType;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String inspect() {
        return "INDEX_ERROR: " + getMessage();
    }
}