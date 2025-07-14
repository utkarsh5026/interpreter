package lang.parser.parsers;

import lang.ast.statements.LetStatement;
import lang.parser.core.*;
import lang.parser.interfaces.TypedStatementParser;
import lang.token.*;
import lang.parser.interfaces.ExpressionParser;

/**
 * 📝 LetStatementParser - Specialized for let statements 📝
 */
public class LetStatementParser implements TypedStatementParser<LetStatement> {

    private final AssignmentStatementParser<LetStatement> delegate;

    public LetStatementParser(ExpressionParser expressionParser) {
        this.delegate = new AssignmentStatementParser<LetStatement>(
                expressionParser,
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
