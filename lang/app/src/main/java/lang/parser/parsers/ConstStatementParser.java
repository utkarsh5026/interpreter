package lang.parser.parsers;

import lang.ast.statements.ConstStatement;
import lang.parser.core.ParsingContext;
import lang.parser.core.StatementParse;
import lang.parser.core.TypedStatementParser;
import lang.token.TokenType;

/**
 * 🔒 ConstStatementParser - Specialized for const statements 🔒
 */
public class ConstStatementParser implements TypedStatementParser<ConstStatement> {

    private final AssignmentStatementParser<ConstStatement> delegate;

    public ConstStatementParser(StatementParse statementParser) {
        this.delegate = new AssignmentStatementParser<>(
                statementParser,
                TokenType.CONST,
                ConstStatement::new);
    }

    @Override
    public boolean canParse(ParsingContext context) {
        return delegate.canParse(context);
    }

    @Override
    public ConstStatement parse(ParsingContext context) {
        return delegate.parse(context);
    }
}
