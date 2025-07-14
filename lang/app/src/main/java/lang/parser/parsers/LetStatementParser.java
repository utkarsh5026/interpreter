package lang.parser.parsers;

import lang.ast.statements.LetStatement;
import lang.parser.core.ParsingContext;
import lang.parser.core.StatementParse;
import lang.parser.core.TypedStatementParser;
import lang.token.TokenType;

/**
 * 📝 LetStatementParser - Specialized for let statements 📝
 */
public class LetStatementParser implements TypedStatementParser<LetStatement> {

    private final AssignmentStatementParser<LetStatement> delegate;

    public LetStatementParser(StatementParse statementParser) {
        this.delegate = new AssignmentStatementParser<>(
                statementParser,
                TokenType.LET,
                LetStatement::new);
    }

    @Override
    public boolean canParse(ParsingContext context) {
        return delegate.canParse(context);
    }

    @Override
    public LetStatement parse(ParsingContext context) {
        return delegate.parse(context);
    }
}
