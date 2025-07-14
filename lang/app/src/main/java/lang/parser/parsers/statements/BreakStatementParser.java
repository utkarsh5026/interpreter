package lang.parser.parsers.statements;

import lang.ast.statements.BreakStatement;
import lang.parser.core.*;
import lang.parser.error.ParserException;
import lang.parser.interfaces.TypedStatementParser;
import lang.token.*;

/**
 * Parses break statements: break;
 */
public class BreakStatementParser implements TypedStatementParser<BreakStatement> {

    @Override
    public boolean canParse(ParsingContext context) {
        return context.getTokenStream().isCurrentToken(TokenType.BREAK);
    }

    @Override
    public BreakStatement parse(ParsingContext context) throws ParserException {
        if (!context.isInLoop()) {
            throw new ParserException("Break statement must be inside a loop",
                    context.getTokenStream().getCurrentToken());
        }

        Token breakToken = context.consumeCurrentToken(TokenType.BREAK);
        context.consumeCurrentToken(TokenType.SEMICOLON);
        return new BreakStatement(breakToken);
    }
}