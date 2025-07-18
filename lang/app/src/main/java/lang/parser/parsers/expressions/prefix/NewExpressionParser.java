package lang.parser.parsers.expressions.prefix;

import java.util.List;
import java.util.Set;

import lang.ast.base.Expression;
import lang.ast.expressions.NewExpression;
import lang.parser.core.ListParsingUtils;
import lang.parser.core.ParsingContext;
import lang.parser.interfaces.ExpressionParser;
import lang.parser.interfaces.PrefixExpressionParser;
import lang.parser.precedence.Precedence;
import lang.token.Token;
import lang.token.TokenType;

/**
 * 🆕 NewExpressionParser - Object Instantiation Parser 🆕
 * 
 * Parses 'new' expressions for creating class instances.
 * 
 * From first principles, parsing new expressions involves:
 * 1. Parse 'new' keyword
 * 2. Parse class name expression
 * 3. Parse argument list (like function calls)
 * 4. Create NewExpression AST node
 * 
 * Grammar:
 * ```
 * new-expression := 'new' expression '(' argument-list ')'
 * argument-list := (expression (',' expression)*)?
 * ```
 */
public class NewExpressionParser implements PrefixExpressionParser {

    private final ExpressionParser expressionParser;

    public NewExpressionParser(ExpressionParser expressionParser) {
        this.expressionParser = expressionParser;
    }

    @Override
    public Expression parsePrefix(ParsingContext context) {
        Token newToken = context.consumeCurrentToken(TokenType.NEW, "Expected 'new' keyword");
        Expression className = expressionParser.parseExpression(context, Precedence.CALL);

        context.consumeCurrentToken(TokenType.LPAREN, "Expected '(' after class name in new expression");

        List<Expression> arguments = ListParsingUtils.parseExpressionList(
                context,
                expressionParser,
                TokenType.RPAREN,
                "constructor argument");

        context.consumeCurrentToken(TokenType.RPAREN, "Expected ')' after constructor arguments");

        return new NewExpression(newToken, className, arguments);
    }

    @Override
    public Set<TokenType> getHandledTokenTypes() {
        return Set.of(TokenType.NEW);
    }
}