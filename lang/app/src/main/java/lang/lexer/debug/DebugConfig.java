package lang.lexer.debug;

/**
 * ⚙️ DebugConfig - Debug Behavior Configuration ⚙️
 * 
 * Centralized configuration for all debug behavior.
 * Uses builder pattern for clean, fluent configuration.
 */
public class DebugConfig {
    // Output control
    private boolean liveTokenOutput = true;
    private boolean lineByLineSummary = true;
    private boolean sourceContext = true;
    private boolean statistics = true;
    private boolean characterDetails = false;

    // Formatting control
    private boolean useColors = true;
    private boolean compactMode = false;
    private int contextRadius = 10;

    // Performance control
    private boolean collectHistory = true;
    private boolean measureTiming = false;

    // Output destination
    private java.io.PrintStream outputStream = System.err;

    private DebugConfig() {
    }

    /**
     * 🏗️ Creates a new debug configuration builder
     */
    public static DebugConfig builder() {
        return new DebugConfig();
    }

    /**
     * 🏗️ Creates a default configuration
     */
    public static DebugConfig defaultConfig() {
        return new DebugConfig();
    }

    /**
     * 🏗️ Creates a minimal configuration (performance-focused)
     */
    public static DebugConfig minimal() {
        return builder()
                .liveTokenOutput(false)
                .lineByLineSummary(false)
                .sourceContext(false)
                .statistics(true)
                .useColors(false)
                .collectHistory(false);
    }

    /**
     * 🏗️ Creates a verbose configuration (full debugging)
     */
    public static DebugConfig verbose() {
        return builder()
                .liveTokenOutput(true)
                .lineByLineSummary(true)
                .sourceContext(true)
                .statistics(true)
                .characterDetails(true)
                .useColors(true)
                .measureTiming(true)
                .contextRadius(15);
    }

    // Builder methods
    public DebugConfig liveTokenOutput(boolean enabled) {
        this.liveTokenOutput = enabled;
        return this;
    }

    public DebugConfig lineByLineSummary(boolean enabled) {
        this.lineByLineSummary = enabled;
        return this;
    }

    public DebugConfig sourceContext(boolean enabled) {
        this.sourceContext = enabled;
        return this;
    }

    public DebugConfig statistics(boolean enabled) {
        this.statistics = enabled;
        return this;
    }

    public DebugConfig characterDetails(boolean enabled) {
        this.characterDetails = enabled;
        return this;
    }

    public DebugConfig useColors(boolean enabled) {
        this.useColors = enabled;
        return this;
    }

    public DebugConfig compactMode(boolean enabled) {
        this.compactMode = enabled;
        return this;
    }

    public DebugConfig contextRadius(int radius) {
        this.contextRadius = Math.max(0, radius);
        return this;
    }

    public DebugConfig collectHistory(boolean enabled) {
        this.collectHistory = enabled;
        return this;
    }

    public DebugConfig measureTiming(boolean enabled) {
        this.measureTiming = enabled;
        return this;
    }

    public DebugConfig outputTo(java.io.PrintStream stream) {
        this.outputStream = stream;
        return this;
    }

    // Getters
    public boolean isLiveTokenOutput() {
        return liveTokenOutput;
    }

    public boolean isLineByLineSummary() {
        return lineByLineSummary;
    }

    public boolean isSourceContext() {
        return sourceContext;
    }

    public boolean isStatistics() {
        return statistics;
    }

    public boolean isCharacterDetails() {
        return characterDetails;
    }

    public boolean isUseColors() {
        return useColors;
    }

    public boolean isCompactMode() {
        return compactMode;
    }

    public int getContextRadius() {
        return contextRadius;
    }

    public boolean isCollectHistory() {
        return collectHistory;
    }

    public boolean isMeasureTiming() {
        return measureTiming;
    }

    public java.io.PrintStream getOutputStream() {
        return outputStream;
    }

    /**
     * 📋 Gets a summary of this configuration
     */
    public String getSummary() {
        java.util.List<String> features = new java.util.ArrayList<>();
        if (liveTokenOutput)
            features.add("live-output");
        if (lineByLineSummary)
            features.add("line-summary");
        if (sourceContext)
            features.add("context");
        if (statistics)
            features.add("stats");
        if (characterDetails)
            features.add("char-details");
        if (useColors)
            features.add("colors");
        if (compactMode)
            features.add("compact");
        if (measureTiming)
            features.add("timing");

        return String.join(", ", features);
    }
}