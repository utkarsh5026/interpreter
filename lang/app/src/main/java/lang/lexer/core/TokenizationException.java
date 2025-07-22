package lang.lexer.core;

public class TokenizationException extends RuntimeException {

    public TokenizationException(String message) {
        super(message);
    }

    public TokenizationException(String message, Throwable cause) {
        super(message, cause);
    }

}
