package lang.parser.parsers;

import lang.ast.base.Expression;
import lang.ast.base.Identifier;
import lang.ast.base.Statement;
import lang.parser.core.ParsingContext;
import lang.parser.core.PrecedenceTable;
import lang.parser.core.StatementParse;
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
public class AssignmentStatementParser<T extends Statement> implements StatementParser<T> {

    @FunctionalInterface
    public interface StatementFactory<T extends Statement> {
        T create(Token token, Identifier name, Expression value);
    }

    private final StatementParse statementParser;
    private final TokenType expectedTokenType;
    private final StatementFactory<T> statementFactory;

    public AssignmentStatementParser(StatementParse statementParser,
            TokenType expectedTokenType,
            StatementFactory<T> statementFactory) {
        this.statementParser = statementParser;
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
    public T parse(ParsingContext context) {
        Token keywordToken = context.consume(expectedTokenType);
        Token nameToken = context.consume(TokenType.IDENTIFIER);
        Identifier name = new Identifier(nameToken, nameToken.literal());

        context.consume(TokenType.ASSIGN);

        ExpressionParser expressionParser = new ExpressionParser(statementParser);
        Expression value = expressionParser.parseExpression(context, PrecedenceTable.Precedence.LOWEST);

        if (value == null) {
            context.addError("Expected expression after '='", context.getTokenStream().getCurrentToken().copy());
            return null;
        }

        context.consume(TokenType.SEMICOLON);
        return statementFactory.create(keywordToken, name, value);
    }
}