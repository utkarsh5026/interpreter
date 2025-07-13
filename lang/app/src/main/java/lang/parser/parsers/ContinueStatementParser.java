package lang.parser.parsers;

import lang.ast.statements.ContinueStatement;
import lang.parser.core.ParsingContext;
import lang.token.TokenType;
import lang.token.Token;

/**
 * Parses continue statements: continue;
 */
public class ContinueStatementParser implements StatementParser<ContinueStatement> {

    @Override
    public boolean canParse(ParsingContext context) {
        return context.getTokenStream().isCurrentToken(TokenType.CONTINUE);
    }

    @Override
    public ContinueStatement parse(ParsingContext context) {
        if (!context.isInLoop()) {
            context.addError("Continue statement must be inside a loop",
                    context.getTokenStream().getCurrentToken());
            return null;
        }

        Token continueToken = context.getTokenStream().getCurrentToken();
        context.getTokenStream().consume(TokenType.SEMICOLON);

        return new ContinueStatement(continueToken);
    }
}