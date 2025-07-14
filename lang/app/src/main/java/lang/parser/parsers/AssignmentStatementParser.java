package lang.parser.parsers;

import lang.ast.base.Expression;
import lang.ast.base.Identifier;
import lang.ast.base.Statement;

import lang.parser.core.*;
import lang.parser.error.ParserException;
import lang.parser.interfaces.TypedStatementParser;
import lang.parser.interfaces.ExpressionParser;
import lang.parser.precedence.Precedence;

import lang.token.Token;
import lang.token.TokenType;

/**
 * 🔧 Generic Assignment Statement Parser - The DRY Solution 🔧
 * 
 * A reusable parser that handles both const and let statements by accepting
 * the token type and statement factory as parameters.
 * 
 * This eliminates code duplication between ConstStatementParser and
 * LetStatementParser.
 */
public class AssignmentStatementParser<T extends Statement> implements TypedStatementParser<T> {

    @FunctionalInterface
    public interface StatementFactory<T extends Statement> {
        T create(Token token, Identifier name, Expression value);
    }

    private final ExpressionParser expressionParser;
    private final TokenType expectedTokenType;
    private final StatementFactory<T> statementFactory;

    public AssignmentStatementParser(ExpressionParser expressionParser,
            TokenType expectedTokenType,
            StatementFactory<T> statementFactory) {
        this.expressionParser = expressionParser;
        this.expectedTokenType = expectedTokenType;
        this.statementFactory = statementFactory;
    }

    @Override
    public boolean canParse(ParsingContext context) {
        return context.getTokenStream().isCurrentToken(expectedTokenType);
    }

    /**
     * 🎯 Parse an assignment statement
     * 
     * Parses a statement of the form:
     * const/let identifier = expression;
     * keywordToken = let | const
     * nameToken = Identifier
     * 
     * @param context The parsing context
     * @return The parsed statement
     */
    @Override
    public T parse(ParsingContext context) throws ParserException {
        Token keywordToken = context.consumeCurrentToken(expectedTokenType);
        Token nameToken = context.consumeCurrentToken(TokenType.IDENTIFIER);
        Identifier name = new Identifier(nameToken, nameToken.literal());

        context.consumeCurrentToken(TokenType.ASSIGN);

        Expression value = expressionParser.parseExpression(context, Precedence.LOWEST);
        context.consumeCurrentToken(TokenType.SEMICOLON);
        return statementFactory.create(keywordToken, name, value);
    }
}