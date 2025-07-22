package lang.lexer.parsers;

import lang.lexer.core.CharacterStream;
import lang.lexer.core.TokenFactory;
import lang.token.Token;
import lang.token.TokenType;
import java.util.Map;

/**
 * ⚙️ OperatorParser - Handles All Operator Tokens ⚙️
 * 
 * From first principles, operators can be:
 * 1. Single character: +, -, *, /, etc.
 * 2. Multi-character: ==, !=, <=, +=, etc.
 * 
 * The key insight: we need to look ahead to distinguish
 * between similar operators (= vs ==, < vs <=, etc.)
 */
public class OperatorParser extends AbstractTokenParser {

    // Maps single characters to their potential multi-character operators
    private static final Map<Character, Map<Character, TokenType>> MULTI_CHAR_OPERATORS = Map.ofEntries(
            Map.entry('=', Map.of('=', TokenType.EQ)),
            Map.entry('!', Map.of('=', TokenType.NOT_EQ)),
            Map.entry('<', Map.of('=', TokenType.LESS_THAN_OR_EQUAL, '<', TokenType.BITWISE_LEFT_SHIFT)),
            Map.entry('>', Map.of('=', TokenType.GREATER_THAN_OR_EQUAL, '>', TokenType.BITWISE_RIGHT_SHIFT)),
            Map.entry('+', Map.of('=', TokenType.PLUS_ASSIGN)),
            Map.entry('-', Map.of('=', TokenType.MINUS_ASSIGN)),
            Map.entry('*', Map.of('=', TokenType.ASTERISK_ASSIGN)),
            Map.entry('/', Map.of('=', TokenType.SLASH_ASSIGN, '/', TokenType.INT_DIVISION)),
            Map.entry('%', Map.of('=', TokenType.MODULUS_ASSIGN)),
            Map.entry('&', Map.of('&', TokenType.AND)),
            Map.entry('|', Map.of('|', TokenType.OR)));

    private static final Map<Character, TokenType> SINGLE_CHAR_OPERATORS = Map.<Character, TokenType>ofEntries(
            Map.entry('=', TokenType.ASSIGN),
            Map.entry('!', TokenType.BANG),
            Map.entry('<', TokenType.LESS_THAN),
            Map.entry('>', TokenType.GREATER_THAN),
            Map.entry('+', TokenType.PLUS),
            Map.entry('-', TokenType.MINUS),
            Map.entry('*', TokenType.ASTERISK),
            Map.entry('/', TokenType.SLASH),
            Map.entry('%', TokenType.MODULUS),
            Map.entry('&', TokenType.BITWISE_AND),
            Map.entry('|', TokenType.BITWISE_OR),
            Map.entry('^', TokenType.BITWISE_XOR),
            Map.entry('~', TokenType.BITWISE_NOT),
            Map.entry(';', TokenType.SEMICOLON),
            Map.entry(',', TokenType.COMMA),
            Map.entry(':', TokenType.COLON),
            Map.entry('.', TokenType.DOT),
            Map.entry('(', TokenType.LPAREN),
            Map.entry(')', TokenType.RPAREN),
            Map.entry('{', TokenType.LBRACE),
            Map.entry('}', TokenType.RBRACE),
            Map.entry('[', TokenType.LBRACKET),
            Map.entry(']', TokenType.RBRACKET));

    public OperatorParser(CharacterStream stream, TokenFactory tokenFactory) {
        super(stream, tokenFactory);
    }

    @Override
    public boolean canParse(char ch) {
        return SINGLE_CHAR_OPERATORS.containsKey(ch);
    }

    @Override
    public Token parse() {
        char current = stream.currentCharacter();

        Map<Character, TokenType> multiCharMap = MULTI_CHAR_OPERATORS.get(current);
        if (multiCharMap != null) {
            char next = stream.peekCharacter();
            TokenType multiCharType = multiCharMap.get(next);

            if (multiCharType != null) {
                String literal = String.valueOf(current) + next;
                stream.advance();
                return tokenFactory.createToken(multiCharType, literal);
            }
        }

        TokenType tokenType = SINGLE_CHAR_OPERATORS.get(current);
        return tokenFactory.createToken(tokenType, current);
    }
}