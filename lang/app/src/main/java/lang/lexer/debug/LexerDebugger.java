package lang.lexer.debug;

import java.io.PrintStream;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import lang.token.Token;
import lang.token.TokenType;

public class LexerDebugger implements DebugEventListener {

    /**
     * 📦 Complete debug session data
     */
    public record DebugSessionData(
            String sourceCode,
            long sessionStartTime,
            List<DebugEvent> events,
            Map<TokenType, Integer> tokenTypeCounts,
            DebugConfig config) {
    }

    /**
     * ⏱️ Timing information record
     */
    public record TimingInfo(
            long totalSessionTime,
            long firstTokenTime,
            long lastTokenTime,
            int tokenCount,
            int sourceLength) {
        public double getTokensPerSecond() {
            return tokenCount / (totalSessionTime / 1_000_000_000.0);
        }

        public double getCharactersPerSecond() {
            return sourceLength / (totalSessionTime / 1_000_000_000.0);
        }

        public double getAverageTokenTime() {
            return totalSessionTime / (double) tokenCount;
        }
    }

    private final DebugConfig config;
    private final PrintStream output;

    private String sourceCode;
    private long sessionStartTime;
    private int tokenCount = 0;

    private final List<DebugEvent> events;
    private final Map<Integer, List<DebugEvent>> eventsByLine;
    private final Map<TokenType, Integer> tokenTypeCounts;
    private final List<DebugEventListener> additionalListeners;

    /**
     * 🏗️ Creates a debugger with custom configuration
     */
    public LexerDebugger(DebugConfig config) {
        this.config = config;
        this.output = config.getOutputStream();

        // Initialize collections based on config
        this.events = config.isCollectHistory() ? new ArrayList<>() : null;
        this.eventsByLine = config.isCollectHistory() ? new LinkedHashMap<>() : null;
        this.tokenTypeCounts = new LinkedHashMap<>();
        this.additionalListeners = new ArrayList<>();
    }

    /**
     * 🎧 Adds an additional debug listener
     */
    public void addListener(DebugEventListener listener) {
        additionalListeners.add(listener);
    }

    /**
     * 🎧 Removes a debug listener
     */
    public void removeListener(DebugEventListener listener) {
        additionalListeners.remove(listener);
    }

    @Override
    public void onSessionStart(String sourceCode) {
        this.sourceCode = sourceCode;
        this.sessionStartTime = System.nanoTime();
        this.tokenCount = 0;

        // Clear previous session data
        if (events != null)
            events.clear();
        if (eventsByLine != null)
            eventsByLine.clear();
        tokenTypeCounts.clear();

        printSessionHeader();

        // Notify additional listeners
        additionalListeners.forEach(listener -> listener.onSessionStart(sourceCode));
    }

    @Override
    public void onSessionReset() {
        printMessage("🔄 Lexer session reset");
        onSessionStart(this.sourceCode); // Re-initialize
        additionalListeners.forEach(DebugEventListener::onSessionReset);
    }

    @Override
    public void onSessionEnd() {
        printSessionSummary();
        additionalListeners.forEach(DebugEventListener::onSessionEnd);
    }

    @Override
    public void onTokenCreated(DebugEvent event) {
        tokenCount++;

        if (events != null) {
            events.add(event);
        }

        if (eventsByLine != null) {
            int line = event.token().position().line();
            eventsByLine.computeIfAbsent(line, _ -> new ArrayList<>()).add(event);
        }

        TokenType type = event.token().type();

        int tokenCount = tokenTypeCounts.getOrDefault(type, 0);
        tokenTypeCounts.put(type, tokenCount + 1);

        if (config.isLiveTokenOutput()) {
            printTokenEvent(event);
        }

        additionalListeners.forEach(listener -> listener.onTokenCreated(event));
    }

    /**
     * 🔍 Finds tokens of specific types
     */
    public List<DebugEvent> findTokens(TokenType... types) {
        if (events == null)
            return Collections.emptyList();

        Set<TokenType> targetTypes = Set.of(types);
        return events.stream()
                .filter(event -> targetTypes.contains(event.token().type()))
                .toList();
    }

    /**
     * 📊 Gets token count for a specific type
     */
    public int getTokenCount(TokenType type) {
        return tokenTypeCounts.getOrDefault(type, 0);
    }

    /**
     * 📋 Prints session header information
     */
    private void printSessionHeader() {
        String title = "🔍 LEXER DEBUG SESSION STARTED 🔍";
        output.println(colorBold(title));
        output.println(colorBold("Source length: " + sourceCode.length() + " characters"));
        output.println(colorBold("Debug config: " + config.getSummary()));
        output.println(colorBold("=" + "=".repeat(60)));
        output.println();
    }

    /**
     * ⏱️ Gets timing information (if timing is enabled)
     */
    public Optional<TimingInfo> getTimingInfo() {
        if (!config.isMeasureTiming() || events == null || events.isEmpty()) {
            return Optional.empty();
        }

        long totalTime = System.nanoTime() - sessionStartTime;
        long firstTokenTime = events.get(0).getRelativeTimestamp(sessionStartTime);
        long lastTokenTime = events.get(events.size() - 1).getRelativeTimestamp(sessionStartTime);

        return Optional.of(new TimingInfo(
                totalTime,
                firstTokenTime,
                lastTokenTime,
                tokenCount,
                sourceCode.length()));
    }

    /**
     * 📈 Gets tokens per line statistics
     */
    public Map<Integer, Integer> getTokensPerLine() {
        if (eventsByLine == null)
            return Collections.emptyMap();

        return eventsByLine.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().size()));
    }

    private String colorBold(String text) {
        return DebugColors.bold(text, config.isUseColors());
    }

    private String colorInfo(String text) {
        return DebugColors.colorize(text, DebugColors.INFO, config.isUseColors());
    }

    /**
     * 💬 Prints a general message
     */
    private void printMessage(String message) {
        output.println(colorInfo(message));
    }

    /**
     * 🏁 Prints complete session summary
     */
    private void printSessionSummary() {
        printLineByLineSummary();
        printStatistics();

        output.println();
        output.println(colorBold("🏁 LEXER DEBUG SESSION COMPLETE 🏁"));
    }

    /**
     * 🎫 Prints information about a token creation event
     */
    private void printTokenEvent(DebugEvent event) {
        Token token = event.token();
        String tokenColor = DebugColors.getTokenColor(token.type());
        String posColor = config.isUseColors() ? DebugColors.POSITION : "";
        String reset = config.isUseColors() ? DebugColors.RESET : "";

        if (config.isCompactMode()) {
            // Compact format
            output.printf("%s[%d:%d]%s %s%s%s:'%s'",
                    posColor, token.position().line(), token.position().column(), reset,
                    tokenColor, token.type(), reset,
                    DebugUtils.escapeForDisplay(token.literal()));
        } else {
            // Full format
            output.printf("%s[%3d:%2d]%s %s%-12s%s: '%s'",
                    posColor, token.position().line(), token.position().column(), reset,
                    tokenColor, token.type(), reset,
                    DebugUtils.escapeForDisplay(token.literal()));
        }

        // Add character details if configured
        if (config.isCharacterDetails()) {
            output.printf(" (trigger: %s)", event.getTriggerCharDisplay());
        }

        // Add timing if configured
        if (config.isMeasureTiming()) {
            long relativeTime = event.getRelativeTimestamp(sessionStartTime);
            output.printf(" [%s]", DebugUtils.formatDuration(relativeTime));
        }

        // Add source context if configured
        if (config.isSourceContext()) {
            String context = DebugUtils.extractContext(sourceCode, event.sourcePosition(), config.getContextRadius());
            String contextColor = config.isUseColors() ? DebugColors.CONTEXT : "";
            output.printf(" → %s%s%s", contextColor, context, reset);
        }

        output.println();
    }

    /**
     * 📊 Prints tokenization statistics
     */
    public void printStatistics() {
        if (!config.isStatistics())
            return;

        output.println();
        output.println(colorBold("📊 TOKENIZATION STATISTICS"));
        output.println(colorBold("=" + "=".repeat(30)));

        // Basic stats
        output.println("Total tokens: " + colorInfo(String.valueOf(tokenCount)));
        if (eventsByLine != null) {
            output.println("Lines processed: " + colorInfo(String.valueOf(eventsByLine.size())));
        }
        output.println("Source characters: " + colorInfo(String.valueOf(sourceCode.length())));

        // Performance stats
        if (config.isMeasureTiming()) {
            long totalTime = System.nanoTime() - sessionStartTime;
            output.println("Total time: " + colorInfo(DebugUtils.formatDuration(totalTime)));

            double tokensPerSecond = tokenCount / (totalTime / 1_000_000_000.0);
            output.println("Throughput: " + colorInfo(String.format("%.0f tokens/sec", tokensPerSecond)));
        }

        // Token distribution
        output.println("\nToken type distribution:");
        tokenTypeCounts.entrySet().stream()
                .sorted(Map.Entry.<TokenType, Integer>comparingByValue().reversed())
                .forEach(entry -> {
                    String tokenColor = DebugColors.getTokenColor(entry.getKey());
                    String reset = config.isUseColors() ? DebugColors.RESET : "";
                    double percentage = (entry.getValue() * 100.0) / tokenCount;

                    output.printf("  %s%-15s%s: %s (%s%%)\n",
                            config.isUseColors() ? tokenColor : "", entry.getKey(), reset,
                            colorInfo(String.valueOf(entry.getValue())),
                            colorInfo(String.format("%.1f", percentage)));
                });
    }

    /**
     * 📄 Prints line-by-line token summary
     */
    public void printLineByLineSummary() {
        if (!config.isLineByLineSummary() || eventsByLine == null)
            return;

        output.println();
        output.println(colorBold("📄 LINE-BY-LINE TOKEN SUMMARY"));
        output.println(colorBold("=" + "=".repeat(40)));

        String[] lines = sourceCode.split("\n");

        for (Map.Entry<Integer, List<DebugEvent>> entry : eventsByLine.entrySet()) {
            int lineNum = entry.getKey();
            List<DebugEvent> lineEvents = entry.getValue();

            // Show source line
            String sourceLine = lineNum <= lines.length ? lines[lineNum - 1] : "";
            output.printf("%sLine %d:%s %s\n",
                    colorBold(""), lineNum, config.isUseColors() ? DebugColors.RESET : "", sourceLine);

            // Show tokens for this line
            for (DebugEvent event : lineEvents) {
                Token token = event.token();
                String tokenColor = DebugColors.getTokenColor(token.type());
                String reset = config.isUseColors() ? DebugColors.RESET : "";

                output.printf("  %s%-12s%s: '%s' at column %d",
                        config.isUseColors() ? tokenColor : "", token.type(), reset,
                        DebugUtils.escapeForDisplay(token.literal()),
                        token.position().column());

                if (config.isMeasureTiming()) {
                    long relativeTime = event.getRelativeTimestamp(sessionStartTime);
                    output.printf(" [%s]", DebugUtils.formatDuration(relativeTime));
                }

                output.println();
            }
            output.println();
        }
    }
}
