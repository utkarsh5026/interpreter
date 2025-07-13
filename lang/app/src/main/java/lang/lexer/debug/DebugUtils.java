package lang.lexer.debug;

public final class DebugUtils {

    private DebugUtils() {
    } // Utility class

    /**
     * 🔤 Escapes special characters for display
     */
    public static String escapeForDisplay(String text) {
        return text.replace("\n", "\\n")
                .replace("\t", "\\t")
                .replace("\r", "\\r")
                .replace(" ", "·");
    }

    /**
     * 📏 Extracts source context around a position
     */
    public static String extractContext(String source, int position, int radius) {
        int start = Math.max(0, position - radius);
        int end = Math.min(source.length(), position + radius + 1);

        String before = source.substring(start, Math.min(position, source.length()));
        String at = position < source.length() ? String.valueOf(source.charAt(position)) : "";
        String after = position + 1 < end ? source.substring(position + 1, end) : "";

        return escapeForDisplay(before) + "|" + escapeForDisplay(at) + "|" + escapeForDisplay(after);
    }

    /**
     * 📊 Formats duration in human-readable form
     */
    public static String formatDuration(long nanoseconds) {
        if (nanoseconds < 1_000) {
            return nanoseconds + "ns";
        } else if (nanoseconds < 1_000_000) {
            return String.format("%.1fμs", nanoseconds / 1_000.0);
        } else if (nanoseconds < 1_000_000_000) {
            return String.format("%.2fms", nanoseconds / 1_000_000.0);
        } else {
            return String.format("%.3fs", nanoseconds / 1_000_000_000.0);
        }
    }

    /**
     * 📈 Formats numbers with thousand separators
     */
    public static String formatNumber(long number) {
        return java.text.NumberFormat.getInstance().format(number);
    }
}