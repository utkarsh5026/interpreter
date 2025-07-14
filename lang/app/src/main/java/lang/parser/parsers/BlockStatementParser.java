package lang.parser.parsers;

import lang.ast.statements.BlockStatement;
import lang.ast.base.Statement;

import lang.parser.core.*;
import lang.parser.error.ParserException;
import lang.parser.interfaces.TypedStatementParser;
import lang.token.TokenType;
import lang.token.Token;
import java.util.*;

/**
 * Parses block statements: { stmt1; stmt2; }
 */
public class BlockStatementParser implements TypedStatementParser<BlockStatement> {

    private final StatementParse statementParser;

    public BlockStatementParser(StatementParse statementParser) {
        this.statementParser = statementParser;
    }

    @Override
    public boolean canParse(ParsingContext context) {
        return context.getTokenStream().isCurrentToken(TokenType.LBRACE);
    }

    @Override
    public BlockStatement parse(ParsingContext context) throws ParserException {
        TokenStream tokenStream = context.getTokenStream();
        Token lbraceToken = context.consumeCurrentToken(TokenType.LBRACE, "Expected '{' at start of block");

        List<Statement> statements = new ArrayList<>();

        while (!tokenStream.isCurrentToken(TokenType.RBRACE) && !tokenStream.isAtEnd()) {
            Statement stmt = statementParser.parseStatement(context);
            if (stmt != null) {
                statements.add(stmt);
            }
        }

        context.consumeCurrentToken(TokenType.RBRACE, "Expected '}' at end of block");

        return new BlockStatement(lbraceToken, statements);
    }
}
