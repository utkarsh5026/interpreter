package lang.lexer.parsers;

import lang.lexer.core.*;
import lang.token.*;

/**
 * 🔤 IdentifierParser - Handles Identifiers and Keywords 🔤
 * 
 * From first principles, identifiers:
 * 1. Start with letter or underscore
 * 2. Can contain letters, digits, underscores
 * 3. May be keywords (reserved words)
 */
public class IdentifierParser extends AbstractTokenParser {

    public IdentifierParser(CharacterStream stream, TokenFactory tokenFactory) {
        super(stream, tokenFactory);
    }

    @Override
    public boolean canParse(char ch) {
        return isLetter(ch);
    }

    @Override
    public Token parse() {
        int startPos = stream.getCurrentPosition();

        while (isLetter(stream.currentCharacter()) || isDigit(stream.currentCharacter())) {
            stream.advance();
        }

        String identifier = stream.getSubstring(startPos);
        stream.backtrack(1);

        TokenType type = Keywords.lookupIdentifier(identifier);
        return tokenFactory.createToken(type, identifier);
    }
}
