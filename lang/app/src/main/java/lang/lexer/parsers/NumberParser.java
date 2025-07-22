package lang.lexer.parsers;

import lang.lexer.core.CharacterStream;
import lang.lexer.core.TokenDescription;
import lang.lexer.core.TokenFactory;
import lang.token.Token;
import lang.token.TokenType;

/**
 * 🔢 NumberParser - Handles Integer and Float Literals 🔢
 * 
 * From first principles, numbers can be:
 * 1. Integers: 123, 0, 999
 * 2. Floats: 12.34, 0.5, .5, 5.
 * 
 * Key insight: A decimal point might start a number (.5)
 * or be part of a number (3.14) or be a separate operator (obj.prop)
 */
public class NumberParser extends AbstractTokenParser {

    public NumberParser(CharacterStream stream, TokenFactory tokenFactory) {
        super(stream, tokenFactory);
    }

    @Override
    public boolean canParse(char ch) {
        return isDigit(ch)
                || (ch == TokenDescription.DOT && isDigit(stream.peekCharacter()));
    }

    @Override
    public Token parse() {
        int startPos = stream.getCurrentPosition();
        boolean isFloat = false;

        if (stream.currentCharacter() == TokenDescription.DOT) {
            isFloat = true;
            stream.advance();

            while (isDigit(stream.currentCharacter())) {
                stream.advance();
            }
        } else {
            while (isDigit(stream.currentCharacter())) {
                stream.advance();
            }

            if (stream.currentCharacter() == TokenDescription.DOT
                    && isDigit(stream.peekCharacter())) {
                isFloat = true;
                stream.advance();

                while (isDigit(stream.currentCharacter())) {
                    stream.advance();
                }
            }
        }

        String numberStr = stream.getSubstring(startPos);
        stream.backtrack(1);

        TokenType type = isFloat ? TokenType.FLOAT : TokenType.INT;
        return tokenFactory.createToken(type, numberStr);
    }
}