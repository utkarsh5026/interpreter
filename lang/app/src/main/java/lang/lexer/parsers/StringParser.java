package lang.lexer.parsers;

import lang.lexer.core.CharacterStream;
import lang.lexer.core.TokenDescription;
import lang.lexer.core.TokenFactory;
import lang.token.Token;
import lang.token.TokenType;

/**
 * 📜 StringParser - Handles String and F-String Literals 📜
 * 
 * From first principles, string parsing involves:
 * 1. Finding string boundaries (quotes)
 * 2. Handling escape sequences (\n, \t, etc.)
 * 3. Supporting different quote types (", ')
 * 4. Special handling for f-strings
 */
public class StringParser extends AbstractTokenParser {

    public StringParser(CharacterStream stream, TokenFactory tokenFactory) {
        super(stream, tokenFactory);
    }

    @Override
    public boolean canParse(char ch) {
        return ch == TokenDescription.STRING_START_DOUBLE_QUOTE
                || ch == TokenDescription.STRING_START_SINGLE_QUOTE;
    }

    @Override
    public Token parse() {
        char quoteChar = stream.currentCharacter();
        StringBuilder content = new StringBuilder();

        stream.advance(); // Skip opening quote

        while (stream.currentCharacter() != TokenDescription.EOF
                && stream.currentCharacter() != quoteChar) {
            if (stream.currentCharacter() == '\\') {
                stream.advance();
                content.append(handleEscapeSequence());
            } else {
                content.append(stream.currentCharacter());
            }
            stream.advance();
        }

        if (stream.currentCharacter() != quoteChar) {
            throw new RuntimeException("Unterminated string literal");
        }

        return tokenFactory.createToken(TokenType.STRING, content.toString());
    }

    private String handleEscapeSequence() {
        return switch (stream.currentCharacter()) {
            case 'n' -> "\n";
            case 't' -> "\t";
            case 'r' -> "\r";
            case 'f' -> "\f";
            case 'b' -> "\b";
            case '\'' -> "\'";
            case '"' -> "\"";
            case '\\' -> "\\";
            default -> String.valueOf(stream.currentCharacter());
        };
    }
}