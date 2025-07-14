package lang.parser.interfaces;

import lang.ast.base.Expression;
import lang.parser.core.ParsingContext;
import lang.parser.precedence.Precedence;

public interface ExpressionParser {
    Expression parseExpression(ParsingContext context, Precedence minPrecedence);
}
