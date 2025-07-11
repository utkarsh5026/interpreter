package lang.ast.visualization;

import java.util.*;

/**
 * Renders the tree structure with proper lines and indentation
 */
public class TreeRenderer {
    private final VisualizationConfig config;
    private final NodeFormatter formatter;

    // Tree drawing characters
    private static final class TreeChars {
        final String vertical;
        final String branch;
        final String lastBranch;

        TreeChars(String vertical, String branch, String lastBranch) {
            this.vertical = vertical;
            this.branch = branch;
            this.lastBranch = lastBranch;
        }

        static final TreeChars UNICODE = new TreeChars("│   ", "├── ", "└── ");
        static final TreeChars ASCII = new TreeChars("|   ", "+-- ", "+-- ");
        static final TreeChars SIMPLE = new TreeChars("    ", "  ", "  ");
    }

    public TreeRenderer(VisualizationConfig config) {
        this.config = config;
        this.formatter = new NodeFormatter(config);
    }

    public String render(TreeNode root) {
        StringBuilder sb = new StringBuilder();

        if (config.getColorScheme() == AstTreeVisualizer.ColorScheme.HTML) {
            sb.append("<div class=\"ast-tree\">\n");
            sb.append("<pre>\n");
        }

        renderNode(sb, root, "", true, 0);

        if (config.getColorScheme() == AstTreeVisualizer.ColorScheme.HTML) {
            sb.append("</pre>\n");
            sb.append("</div>\n");
        }

        return sb.toString();
    }

    private void renderNode(StringBuilder sb, TreeNode node, String prefix, boolean isLast, int depth) {
        if (depth > config.getMaxDepth()) {
            return;
        }

        // Get tree characters based on style
        TreeChars chars = getTreeChars();

        // Render current node
        if (depth == 0) {
            // Root node - no prefix
            sb.append(formatter.formatNode(node));
        } else {
            // Child node - with tree structure
            String nodePrefix = isLast ? chars.lastBranch : chars.branch;
            sb.append(prefix).append(nodePrefix).append(formatter.formatNode(node));
        }

        sb.append("\n");

        // Render children
        List<TreeNode> children = node.getChildren();
        for (int i = 0; i < children.size(); i++) {
            boolean isLastChild = (i == children.size() - 1);
            String childPrefix = prefix + (depth == 0 ? "" : (isLast ? "    " : chars.vertical));

            renderNode(sb, children.get(i), childPrefix, isLastChild, depth + 1);
        }
    }

    private TreeChars getTreeChars() {
        return switch (config.getStyle()) {
            case UNICODE_TREE -> TreeChars.UNICODE;
            case ASCII_TREE -> TreeChars.ASCII;
            case INDENTED, COMPACT, DETAILED -> TreeChars.SIMPLE;
        };
    }
}