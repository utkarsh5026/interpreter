package lang.parser.interfaces;

import lang.ast.base.Expression;
import lang.parser.core.ParsingContext;
import lang.token.TokenType;

/**
 * 🎯 PrefixExpressionParser - Expression Starter Interface 🎯
 * 
 * Interface for parsers that handle expressions starting with specific tokens.
 * Think of these as "expression beginners" - they know how to start parsing
 * when they see their trigger token! 🚀
 * 
 * Examples:
 * - IdentifierParser starts when it sees IDENTIFIER tokens
 * - IntegerParser starts when it sees INT tokens
 * - PrefixOperatorParser starts when it sees MINUS or BANG tokens
 * - GroupedExpressionParser starts when it sees LPAREN tokens
 */
@FunctionalInterface
public interface PrefixExpressionParser {
    /**
     * 🎯 Parses an expression that starts with a specific token type
     */
    Expression parsePrefix(ParsingContext context);

    /**
     * 🔍 Gets the token types this parser can handle as prefix
     */
    default TokenType[] getHandledTokenTypes() {
        return new TokenType[0];
    }
}