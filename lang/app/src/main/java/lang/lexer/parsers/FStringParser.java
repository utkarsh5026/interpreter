package lang.lexer.parsers;

import lang.lexer.core.*;
import lang.token.Token;
import lang.token.TokenType;

/**
 * 🎯 FStringParser - Handles F-String Literals 🎯
 * 
 * F-strings are complex because they contain both:
 * 1. Static text content
 * 2. Embedded expressions in braces
 * 
 * The lexer treats the entire f-string as one token.
 * The parser will later break down the internal structure.
 */
public class FStringParser extends AbstractTokenParser {

    public FStringParser(CharacterStream stream, TokenFactory tokenFactory) {
        super(stream, tokenFactory);
    }

    @Override
    public boolean canParse(char ch) {
        return ch == TokenDescription.F_STRING_START
                && stream.peekCharacter() == TokenDescription.STRING_START_DOUBLE_QUOTE;
    }

    @Override
    public Token parse() {
        stream.advance(); // skip 'f'
        stream.advance(); // skip '"'

        StringBuilder content = new StringBuilder();
        int braceDepth = 0;

        while (stream.currentCharacter() != TokenDescription.EOF) {
            if (stream.currentCharacter() == TokenDescription.STRING_START_DOUBLE_QUOTE
                    && braceDepth == 0) {
                break; // End of f-string
            }

            if (stream.currentCharacter() == TokenDescription.LBRACE) {
                braceDepth++;
            } else if (stream.currentCharacter() == TokenDescription.RBRACE) {
                braceDepth--;
                if (braceDepth < 0) {
                    throw new TokenizationException("Unmatched '}' in f-string");
                }
            }

            if (stream.currentCharacter() == '\\') {
                stream.advance();
                content.append(handleEscapeSequence());
            } else {
                content.append(stream.currentCharacter());
            }

            stream.advance();
        }

        if (stream.currentCharacter() != TokenDescription.STRING_START_DOUBLE_QUOTE) {
            throw new TokenizationException("Unterminated f-string");
        }

        if (braceDepth > 0) {
            throw new TokenizationException("Unclosed '{' in f-string");
        }

        return tokenFactory.createToken(TokenType.F_STRING, content.toString());
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
