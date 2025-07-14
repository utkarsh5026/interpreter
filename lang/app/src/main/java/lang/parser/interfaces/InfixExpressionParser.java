package lang.parser.interfaces;

import lang.ast.base.Expression;
import lang.parser.core.ParsingContext;
import lang.token.TokenType;
import java.util.Set;
import java.util.HashSet;

/**
 * ⚡ InfixExpressionParser - Expression Combiner Interface ⚡
 * 
 * Interface for parsers that handle operators and combinators that appear
 * between expressions. These are the "expression combiners" that take a
 * left expression and combine it with something on the right! 🔗
 * 
 * Examples:
 * - ArithmeticParser handles +, -, *, / between expressions
 * - ComparisonParser handles ==, !=, <, > between expressions
 * - CallExpressionParser handles function calls: func(args)
 * - IndexExpressionParser handles array access: array[index]
 * - AssignmentParser handles variable assignment: x = value
 */
@FunctionalInterface
public interface InfixExpressionParser {
    /**
     * ⚡ Parses an infix expression with a left operand
     */
    Expression parseInfix(ParsingContext context, Expression left);

    /**
     * 🔍 Gets the token types this parser can handle as infix operators
     */
    default Set<TokenType> getHandledTokenTypes() {
        return new HashSet<>();
    }
}
