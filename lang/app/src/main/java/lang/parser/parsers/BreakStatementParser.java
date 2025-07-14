package lang.parser.parsers;

import lang.ast.statements.BreakStatement;
import lang.parser.core.ParsingContext;
import lang.parser.core.ParserException;
import lang.parser.core.TypedStatementParser;
import lang.token.TokenType;
import lang.token.Token;

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

        Token breakToken = context.consume(TokenType.BREAK);
        context.consume(TokenType.SEMICOLON);
        return new BreakStatement(breakToken);
    }
}