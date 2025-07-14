package lang.parser.error;

import java.util.Optional;

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
    private final Optional<Token> token; // 🎫 The token that caused the critical error (optional)

    /**
     * 💥 Creates a parser exception with message and token context
     * 
     * @param message A clear description of what went critically wrong 💬
     * @param token   The token that caused the critical error 🎫
     */
    public ParserException(String message, Token token) {
        super(message);
        this.token = Optional.of(token);
    }

    public ParserException(String message) {
        super(message);
        this.token = Optional.empty();
    }

    /**
     * 🎫 Gets the token that caused the exception
     */
    public Optional<Token> getToken() {
        return token;
    }
}