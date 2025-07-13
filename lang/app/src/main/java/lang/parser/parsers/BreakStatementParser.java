package lang.parser.parsers;

import lang.ast.statements.BreakStatement;
import lang.parser.core.ParsingContext;
import lang.token.TokenType;
import lang.token.Token;

/**
 * Parses break statements: break;
 */
public class BreakStatementParser implements StatementParser<BreakStatement> {

    @Override
    public boolean canParse(ParsingContext context) {
        return context.getTokenStream().isCurrentToken(TokenType.BREAK);
    }

    @Override
    public BreakStatement parse(ParsingContext context) {
        if (!context.isInLoop()) {
            context.addError("Break statement must be inside a loop",
                    context.getTokenStream().getCurrentToken());
            return null;
        }

        Token breakToken = context.getTokenStream().getCurrentToken();
        context.getTokenStream().consume(TokenType.SEMICOLON);

        return new BreakStatement(breakToken);
    }
}