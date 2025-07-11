package lang.ast.visualization;

public class VisualizationConfig {
    private AstTreeVisualizer.Style style = AstTreeVisualizer.Style.UNICODE_TREE;
    private AstTreeVisualizer.ColorScheme colorScheme = AstTreeVisualizer.ColorScheme.TERMINAL;
    private boolean showMetadata = true;
    private boolean showPositions = true;
    private boolean showIndices = true;
    private boolean showTypes = true;
    private boolean showValues = true;
    private boolean compactMode = false;
    private int maxDepth = Integer.MAX_VALUE;
    private int maxWidth = 120;

    // Builder pattern for easy configuration
    public static class Builder {
        private final VisualizationConfig config = new VisualizationConfig();

        public Builder style(AstTreeVisualizer.Style style) {
            config.style = style;
            return this;
        }

        public Builder colorScheme(AstTreeVisualizer.ColorScheme colorScheme) {
            config.colorScheme = colorScheme;
            return this;
        }

        public Builder showMetadata(boolean show) {
            config.showMetadata = show;
            return this;
        }

        public Builder showPositions(boolean show) {
            config.showPositions = show;
            return this;
        }

        public Builder showIndices(boolean show) {
            config.showIndices = show;
            return this;
        }

        public Builder showTypes(boolean show) {
            config.showTypes = show;
            return this;
        }

        public Builder showValues(boolean show) {
            config.showValues = show;
            return this;
        }

        public Builder compactMode(boolean compact) {
            config.compactMode = compact;
            return this;
        }

        public Builder maxDepth(int depth) {
            config.maxDepth = depth;
            return this;
        }

        public Builder maxWidth(int width) {
            config.maxWidth = width;
            return this;
        }

        public VisualizationConfig build() {
            return config;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public AstTreeVisualizer.Style getStyle() {
        return style;
    }

    public AstTreeVisualizer.ColorScheme getColorScheme() {
        return colorScheme;
    }

    public boolean isShowMetadata() {
        return showMetadata;
    }

    public boolean isShowPositions() {
        return showPositions;
    }

    public boolean isShowIndices() {
        return showIndices;
    }

    public boolean isShowTypes() {
        return showTypes;
    }

    public boolean isShowValues() {
        return showValues;
    }

    public boolean isCompactMode() {
        return compactMode;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public int getMaxWidth() {
        return maxWidth;
    }
}
