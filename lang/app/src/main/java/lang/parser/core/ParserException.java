package lang.parser.core;

import lang.token.Token;

/**
 * 💥 ParserException - The Emergency Stop Signal 💥
 * 
 * A runtime exception thrown when parsing encounters a critical error.
 * Like an emergency brake that stops everything when something goes seriously
 * wrong! 🚨🛑
 * 
 * This exception is used for:
 * - Unrecoverable parsing errors 🚫
 * - Critical syntax violations 💥
 * - Unexpected parser states 🤯
 * - Token stream corruption 📡❌
 * 
 * Unlike ParseError (which collects issues), ParserException immediately
 * stops parsing and signals that something is critically wrong! ⚡🛑
 * 
 * Example scenarios:
 * - "Expected semicolon but reached end of file" 📄❌
 * - "Malformed expression structure" 🏗️💥
 * - "Invalid token sequence" 🎫❌
 */
public class ParserException extends RuntimeException {
    private final Token token; // 🎫 The token that caused the critical error (optional)

    /**
     * 💥 Creates a parser exception with just a message
     * 
     * Used for general parsing errors that don't relate to a specific token.
     * Like declaring a general emergency without pointing to a specific cause! 🚨
     * 
     * @param message A clear description of what went critically wrong 💬
     */
    public ParserException(String message) {
        super(message);
        this.token = null;
    }

    /**
     * 💥 Creates a parser exception with message and token context
     * 
     * Used when a specific token caused the critical error.
     * Like pointing to the exact cause of the emergency! 🚨🎯
     * 
     * @param message A clear description of what went critically wrong 💬
     * @param token   The token that caused the critical error 🎫
     */
    public ParserException(String message, Token token) {
        super(message);
        this.token = token;
    }

    /**
     * 🎫 Gets the token that caused the exception
     * 
     * Returns the problematic token if one was specified, null otherwise.
     * Like getting the evidence from the scene of the crash! 🔍🎫
     * 
     * This is useful for:
     * - Showing exactly where the critical error occurred 📍
     * - Providing context for error messages 💬
     * - Debugging parser issues 🐛
     * 
     * @return The token that caused the exception, or null if not specified 🎫
     */
    public Token getToken() {
        return token;
    }
}