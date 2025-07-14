package lang.parser.parsers;

import lang.ast.statements.ContinueStatement;
import lang.parser.core.ParsingContext;
import lang.parser.core.ParserException;
import lang.parser.core.TypedStatementParser;
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

        Token continueToken = context.consume(TokenType.CONTINUE);
        context.consume(TokenType.SEMICOLON);

        return new ContinueStatement(continueToken);
    }
}