package lang.lexer.debug;

import lang.token.Token;

/**
 * 📡 DebugEvent - Token Creation Event 📡
 * 
 * Immutable event object containing all information about a token creation.
 * This is the data contract between the lexer and debug system.
 */
public record DebugEvent(
        Token token, // 🎫 The token that was created
        int sourcePosition, // 📍 Position in source where tokenization started
        char triggerChar, // 🔤 Character that triggered this token
        int endPosition, // 📍 Position where tokenization ended
        long timestamp // ⏰ When this event occurred (nanoseconds)
) {

    /**
     * 📏 Gets the length of the token in source code
     */
    public int getTokenLength() {
        return endPosition - sourcePosition;
    }

    /**
     * 🎯 Gets a display-friendly trigger character
     */
    public String getTriggerCharDisplay() {
        return switch (triggerChar) {
            case '\0' -> "EOF";
            case '\n' -> "\\n";
            case '\t' -> "\\t";
            case '\r' -> "\\r";
            case ' ' -> "SPACE";
            default -> String.valueOf(triggerChar);
        };
    }

    /**
     * 📊 Gets relative timestamp from session start
     */
    public long getRelativeTimestamp(long sessionStartTime) {
        return timestamp - sessionStartTime;
    }
}
