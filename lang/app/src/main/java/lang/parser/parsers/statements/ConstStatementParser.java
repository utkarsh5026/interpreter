package lang.parser.parsers.statements;

import lang.ast.statements.ConstStatement;
import lang.parser.core.*;
import lang.parser.interfaces.TypedStatementParser;
import lang.token.*;
import lang.parser.interfaces.ExpressionParser;

/**
 * 🔒 ConstStatementParser - Specialized for const statements 🔒
 */
public class ConstStatementParser implements TypedStatementParser<ConstStatement> {

    private final AssignmentStatementParser<ConstStatement> delegate;

    public ConstStatementParser(ExpressionParser expressionParser) {
        this.delegate = new AssignmentStatementParser<ConstStatement>(
                expressionParser,
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
