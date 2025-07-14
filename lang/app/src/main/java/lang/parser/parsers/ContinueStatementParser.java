package lang.parser.parsers;

import lang.ast.statements.ContinueStatement;
import lang.parser.core.*;
import lang.parser.error.ParserException;
import lang.parser.interfaces.TypedStatementParser;
import lang.token.*;

/**
 * Parses continue statements: continue;
 */
public class ContinueStatementParser implements TypedStatementParser<ContinueStatement> {

    @Override
    public boolean canParse(ParsingContext context) {
        return context.getTokenStream().isCurrentToken(TokenType.CONTINUE);
    }

    @Override
    public ContinueStatement parse(ParsingContext context) {
        if (!context.isInLoop()) {
            throw new ParserException("Continue statement must be inside a loop",
                    context.getTokenStream().getCurrentToken());
        }

        Token continueToken = context.consumeCurrentToken(TokenType.CONTINUE);
        context.consumeCurrentToken(TokenType.SEMICOLON);

        return new ContinueStatement(continueToken);
    }
}