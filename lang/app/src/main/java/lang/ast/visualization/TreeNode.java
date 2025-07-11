package lang.ast.visualization;

import java.util.*;

/**
 * Represents a node in the visualization tree
 */
public class TreeNode {
    private final String type;
    private final String category;
    private String label;
    private String value;
    private int index = -1;
    private final List<TreeNode> children = new ArrayList<>();
    private final Map<String, Object> metadata = new LinkedHashMap<>();

    public TreeNode(String type, String category) {
        this.type = type;
        this.category = category;
    }

    // Getters and setters
    public String getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public List<TreeNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public Map<String, Object> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    public void addChild(TreeNode child) {
        children.add(child);
    }

    public void addMetadata(String key, Object value) {
        metadata.put(key, value);
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }
}
