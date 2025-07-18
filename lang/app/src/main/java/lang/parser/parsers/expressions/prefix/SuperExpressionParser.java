package lang.parser.parsers.expressions.prefix;

import java.util.List;
import java.util.Set;

import lang.ast.base.Expression;
import lang.ast.base.Identifier;
import lang.ast.expressions.SuperExpression;
import lang.parser.core.*;
import lang.parser.interfaces.*;
import lang.parser.error.ParserException;
import lang.token.*;

/**
 * ⬆️ SuperExpressionParser - Parent Class Access Parser ⬆️
 * 
 * Parses 'super' expressions for accessing parent class methods.
 * 
 * From first principles, super expression parsing involves:
 * 1. Parse 'super' keyword
 * 2. Check for constructor call super() or method call super.method()
 * 3. Parse arguments if present
 * 4. Create SuperExpression AST node
 * 
 * Grammar:
 * ```
 * super-expression := 'super' ('(' argument-list ')' | '.' IDENTIFIER '('
 * argument-list ')')
 * ```
 */
public class SuperExpressionParser implements PrefixExpressionParser {

    private final ExpressionParser expressionParser;

    public SuperExpressionParser(ExpressionParser expressionParser) {
        this.expressionParser = expressionParser;
    }

    @Override
    public Expression parsePrefix(ParsingContext context) {
        Token superToken = context.consumeCurrentToken(TokenType.SUPER, "Expected 'super' keyword");

        if (context.getTokenStream().isCurrentToken(TokenType.LPAREN)) {
            // Constructor call: super(args)
            context.consumeCurrentToken(TokenType.LPAREN);

            List<Expression> arguments = ListParsingUtils.parseExpressionList(
                    context,
                    expressionParser,
                    TokenType.RPAREN,
                    "super constructor argument");

            context.consumeCurrentToken(TokenType.RPAREN, "Expected ')' after super arguments");

            return new SuperExpression(superToken, null, arguments);

        } else if (context.getTokenStream().isCurrentToken(TokenType.DOT)) {
            // Method call: super.method(args)
            context.consumeCurrentToken(TokenType.DOT, "Expected '.' after 'super'");

            Token methodToken = context.consumeCurrentToken(TokenType.IDENTIFIER,
                    "Expected method name after 'super.'");
            Identifier method = new Identifier(methodToken, methodToken.literal());

            context.consumeCurrentToken(TokenType.LPAREN, "Expected '(' after super method name");

            List<Expression> arguments = ListParsingUtils.parseExpressionList(
                    context,
                    expressionParser,
                    TokenType.RPAREN,
                    "super method argument");

            context.consumeCurrentToken(TokenType.RPAREN, "Expected ')' after super method arguments");

            return new SuperExpression(superToken, method, arguments);

        } else {
            throw new ParserException("Expected '(' or '.' after 'super'",
                    context.getTokenStream().getCurrentToken());
        }
    }

    @Override
    public Set<TokenType> getHandledTokenTypes() {
        return Set.of(TokenType.SUPER);
    }
}