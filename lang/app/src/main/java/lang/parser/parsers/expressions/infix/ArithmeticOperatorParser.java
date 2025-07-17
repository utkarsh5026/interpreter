package lang.parser.parsers.expressions.infix;

import lang.parser.interfaces.InfixExpressionParser;
import lang.parser.interfaces.ExpressionParser;
import lang.parser.core.ParsingContext;
import lang.ast.base.Expression;
import lang.token.TokenType;
import java.util.Set;

/**
 * ⚡ ArithmeticOperatorParser - Binary Math Operations ⚡
 * 
 * Handles arithmetic operations between two expressions.
 * These are the fundamental math operations that combine two values.
 * 
 * Supported operations:
 * - + (addition): 5 + 3 = 8
 * - - (subtraction): 10 - 4 = 6
 * - * (multiplication): 3 * 7 = 21
 * - / (division): 15 / 3 = 5
 * - % (modulus): 17 % 5 = 2
 * 
 * The parser uses the current operator's precedence to determine
 * how tightly to bind the right operand.
 */
public class ArithmeticOperatorParser implements InfixExpressionParser {

    private final BinaryOperatorParser delegate;

    public ArithmeticOperatorParser(ExpressionParser expressionParser) {
        Set<TokenType> handledTokenTypes = Set.of(
                TokenType.PLUS,
                TokenType.MINUS,
                TokenType.ASTERISK,
                TokenType.SLASH,
                TokenType.MODULUS,
                TokenType.INT_DIVISION);

        this.delegate = new BinaryOperatorParser(
                expressionParser,
                handledTokenTypes);
    }

    @Override
    public Expression parseInfix(ParsingContext context, Expression left) {
        return delegate.parseInfix(context, left);
    }

    @Override
    public Set<TokenType> getHandledTokenTypes() {
        return delegate.getHandledTokenTypes();
    }
}