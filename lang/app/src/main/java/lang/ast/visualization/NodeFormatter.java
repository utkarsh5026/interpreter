package lang.ast.visualization;

import java.util.*;

public class NodeFormatter {
    private final VisualizationConfig config;

    // ANSI color codes
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    private static final String DIM = "\u001B[2m";

    // Node type colors
    private static final Map<String, String> CATEGORY_COLORS = Map.ofEntries(
            Map.entry("ROOT", "\u001B[35m"), // Magenta
            Map.entry("DECLARATION", "\u001B[34m"), // Blue
            Map.entry("CONTROL", "\u001B[31m"), // Red
            Map.entry("STATEMENT", "\u001B[36m"), // Cyan
            Map.entry("BLOCK", "\u001B[33m"), // Yellow
            Map.entry("LOOP", "\u001B[32m"), // Green
            Map.entry("VARIABLE", "\u001B[94m"), // Light Blue
            Map.entry("OPERATOR", "\u001B[93m"), // Light Yellow
            Map.entry("LITERAL", "\u001B[92m"), // Light Green
            Map.entry("CONDITIONAL", "\u001B[95m"), // Light Magenta
            Map.entry("CALL", "\u001B[96m"), // Light Cyan
            Map.entry("ACCESS", "\u001B[91m"), // Light Red
            Map.entry("ASSIGNMENT", "\u001B[97m"), // White
            Map.entry("COLLECTION", "\u001B[90m"), // Dark Gray
            Map.entry("FUNCTION", "\u001B[35m") // Magenta
    );

    public NodeFormatter(VisualizationConfig config) {
        this.config = config;
    }

    public String formatNode(TreeNode node) {
        StringBuilder sb = new StringBuilder();

        // Add label if present
        if (node.getLabel() != null) {
            sb.append(formatLabel(node.getLabel())).append(": ");
        }

        // Add type
        if (config.isShowTypes()) {
            sb.append(formatType(node.getType(), node.getCategory()));
        }

        // Add value if present
        if (config.isShowValues() && node.getValue() != null) {
            sb.append(" ").append(formatValue(node.getValue(), node.getCategory()));
        }

        // Add metadata
        if (config.isShowMetadata() && !node.getMetadata().isEmpty()) {
            sb.append(" ").append(formatMetadata(node.getMetadata()));
        }

        // Add position
        if (config.isShowPositions() && node.getMetadata().containsKey("line")) {
            sb.append(" ").append(formatPosition(node.getMetadata()));
        }

        return sb.toString();
    }

    private String formatType(String type, String category) {
        String formatted = type;

        if (config.getColorScheme() == AstTreeVisualizer.ColorScheme.TERMINAL) {
            String color = CATEGORY_COLORS.getOrDefault(category, "");
            formatted = color + BOLD + type + RESET;
        } else if (config.getColorScheme() == AstTreeVisualizer.ColorScheme.HTML) {
            formatted = String.format("<span class=\"ast-node ast-%s\">%s</span>",
                    category.toLowerCase(), type);
        } else if (config.getColorScheme() == AstTreeVisualizer.ColorScheme.MARKDOWN) {
            formatted = "**" + type + "**";
        }

        return formatted;
    }

    private String formatValue(String value, String category) {
        String formatted = value;

        if (config.getColorScheme() == AstTreeVisualizer.ColorScheme.TERMINAL) {
            if ("LITERAL".equals(category)) {
                formatted = "\u001B[92m" + value + RESET; // Light green
            } else if ("OPERATOR".equals(category)) {
                formatted = "\u001B[93m" + value + RESET; // Light yellow
            } else {
                formatted = "\u001B[96m" + value + RESET; // Light cyan
            }
        } else if (config.getColorScheme() == AstTreeVisualizer.ColorScheme.HTML) {
            formatted = String.format("<code class=\"ast-value\">%s</code>", value);
        } else if (config.getColorScheme() == AstTreeVisualizer.ColorScheme.MARKDOWN) {
            formatted = "`" + value + "`";
        }

        return formatted;
    }

    private String formatLabel(String label) {
        if (config.getColorScheme() == AstTreeVisualizer.ColorScheme.TERMINAL) {
            return DIM + label + RESET;
        } else if (config.getColorScheme() == AstTreeVisualizer.ColorScheme.HTML) {
            return String.format("<span class=\"ast-label\">%s</span>", label);
        } else if (config.getColorScheme() == AstTreeVisualizer.ColorScheme.MARKDOWN) {
            return "*" + label + "*";
        }
        return label;
    }

    private String formatMetadata(Map<String, Object> metadata) {
        if (config.isCompactMode()) {
            return "";
        }

        List<String> parts = new ArrayList<>();

        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            if ("line".equals(entry.getKey()) || "column".equals(entry.getKey())) {
                continue; // Handle separately
            }

            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Boolean) {
                if ((Boolean) value) {
                    parts.add(key);
                }
            } else {
                parts.add(key + "=" + value);
            }
        }

        if (parts.isEmpty()) {
            return "";
        }

        String metadata_str = String.join(", ", parts);

        if (config.getColorScheme() == AstTreeVisualizer.ColorScheme.TERMINAL) {
            return DIM + "{" + metadata_str + "}" + RESET;
        } else if (config.getColorScheme() == AstTreeVisualizer.ColorScheme.HTML) {
            return String.format("<span class=\"ast-metadata\">{%s}</span>", metadata_str);
        } else if (config.getColorScheme() == AstTreeVisualizer.ColorScheme.MARKDOWN) {
            return "*{" + metadata_str + "}*";
        }

        return "{" + metadata_str + "}";
    }

    private String formatPosition(Map<String, Object> metadata) {
        Object line = metadata.get("line");
        Object column = metadata.get("column");

        if (line == null || column == null) {
            return "";
        }

        String position = "@" + line + ":" + column;

        if (config.getColorScheme() == AstTreeVisualizer.ColorScheme.TERMINAL) {
            return DIM + position + RESET;
        } else if (config.getColorScheme() == AstTreeVisualizer.ColorScheme.HTML) {
            return String.format("<span class=\"ast-position\">%s</span>", position);
        }

        return position;
    }
}
