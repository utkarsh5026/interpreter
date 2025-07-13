package lang.parser.parsers;

import lang.ast.statements.BlockStatement;
import lang.ast.base.Statement;

import lang.parser.core.ParsingContext;
import lang.parser.core.TokenStream;
import lang.parser.core.StatementParse;

import lang.token.TokenType;
import lang.token.Token;
import java.util.*;

/**
 * Parses block statements: { stmt1; stmt2; }
 */
public class BlockStatementParser implements StatementParser<BlockStatement> {

    private final StatementParse statementParser;

    public BlockStatementParser(StatementParse statementParser) {
        this.statementParser = statementParser;
    }

    @Override
    public boolean canParse(ParsingContext context) {
        return context.getTokenStream().isCurrentToken(TokenType.LBRACE);
    }

    @Override
    public BlockStatement parse(ParsingContext context) {
        TokenStream tokens = context.getTokenStream();

        Token braceToken = tokens.getCurrentToken();
        tokens.advance(); // consume '{'

        List<Statement> statements = new ArrayList<>();

        while (!tokens.isCurrentToken(TokenType.RBRACE) && !tokens.isAtEnd()) {
            Statement stmt = statementParser.parseStatement(context);
            if (stmt != null) {
                statements.add(stmt);
            }

            // Advance to next statement
            tokens.advance();
        }

        // Consume '}'
        if (tokens.isCurrentToken(TokenType.RBRACE)) {
            tokens.advance();
        } else {
            context.addError("Expected '}' to close block", tokens.getCurrentToken());
        }

        return new BlockStatement(braceToken, statements);
    }
}
