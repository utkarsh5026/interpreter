package lang.lexer.debug;

public interface DebugEventListener {
    /**
     * 🚀 Called when a debug session starts
     */
    default void onSessionStart(String sourceCode) {
    }

    /**
     * 🎫 Called when a token is created
     */
    default void onTokenCreated(DebugEvent event) {
    }

    /**
     * 🔄 Called when the lexer is reset
     */
    default void onSessionReset() {
    }

    /**
     * 🏁 Called when the debug session ends
     */
    default void onSessionEnd() {
    }
}
