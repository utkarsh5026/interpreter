package lang.parser.parsers.expressions.prefix;

import lang.parser.interfaces.PrefixExpressionParser;
import lang.parser.core.ParsingContext;
import lang.ast.base.Expression;
import lang.ast.literals.FStringLiteral;
import lang.parser.error.ParserException;
import lang.lexer.Lexer;
import lang.parser.LanguageParser;

import java.util.*;
import lang.token.*;

/**
 * 🎯 FStringLiteralParser - F-String Parsing Specialist 🎯
 * 
 * Handles f-string literal expressions that contain both static text and
 * embedded expressions.
 * 
 * From first principles, f-strings work like this:
 * 1. Static text is preserved as-is
 * 2. Expressions in {braces} are parsed as normal expressions
 * 3. At evaluation time, expressions are computed and interpolated
 * 
 * Examples:
 * - f"Hello {name}!" → static=["Hello ", "!"], expressions=[name]
 * - f"{x} + {y} = {x + y}" → static=["", " + ", " = ", ""], expressions=[x, y,
 * x+y]
 * 
 * Parsing Strategy:
 * 1. Scan through f-string content character by character
 * 2. Build static text until we hit '{'
 * 3. Parse the expression inside braces
 * 4. Continue until end of string
 * 5. Construct FStringLiteral with alternating static/expression parts
 */
public class FStringLiteralParser implements PrefixExpressionParser {

    @Override
    public Expression parsePrefix(ParsingContext context) {
        Token fStringToken = context.consumeCurrentToken(TokenType.F_STRING);
        String content = fStringToken.literal();

        // Parse the f-string content to separate static parts from expressions
        FStringParseResult result = parseFStringContent(content, context);

        return new FStringLiteral(fStringToken, result.staticParts, result.expressions);
    }

    /**
     * 📋 Container for f-string parsing results
     */
    private static class FStringParseResult {
        final List<String> staticParts;
        final List<Expression> expressions;

        FStringParseResult(List<String> staticParts, List<Expression> expressions) {
            this.staticParts = staticParts;
            this.expressions = expressions;
        }
    }

    /**
     * 🔍 Parses f-string content into static parts and expressions
     * 
     * This method implements a state machine that tracks:
     * - Current position in the string
     * - Whether we're inside or outside braces
     * - Brace nesting level (for expressions with nested braces)
     * 
     * @param content The f-string content (without f" and closing ")
     * @param context Parsing context for error reporting
     * @return Parsed static parts and expressions
     */
    private FStringParseResult parseFStringContent(String content, ParsingContext context) {
        List<String> staticParts = new ArrayList<>();
        List<Expression> expressions = new ArrayList<>();

        StringBuilder currentStatic = new StringBuilder();
        int pos = 0;

        while (pos < content.length()) {
            char ch = content.charAt(pos);

            if (ch == '{') {
                // Found start of expression
                staticParts.add(currentStatic.toString());
                currentStatic = new StringBuilder();

                // Parse the expression inside braces
                ExpressionParseResult exprResult = parseExpressionInBraces(content, pos + 1, context);
                expressions.add(exprResult.expression);
                pos = exprResult.endPosition;

            } else if (ch == '}') {
                // Unmatched closing brace
                throw new ParserException("Unmatched '}' in f-string",
                        context.getTokenStream().getCurrentToken());

            } else {
                // Regular character - add to current static part
                currentStatic.append(ch);
                pos++;
            }
        }

        // Add the final static part (might be empty)
        staticParts.add(currentStatic.toString());

        return new FStringParseResult(staticParts, expressions);
    }

    /**
     * 📦 Container for expression parsing results
     */
    private static class ExpressionParseResult {
        final Expression expression;
        final int endPosition;

        ExpressionParseResult(Expression expression, int endPosition) {
            this.expression = expression;
            this.endPosition = endPosition;
        }
    }

    /**
     * ⚡ Parses an expression inside braces: {expression}
     * 
     * This is complex because expressions can contain:
     * - Nested braces: {array[{index}]}
     * - String literals with braces: {f"hello {name}"}
     * - Function calls: {func(x, y)}
     * 
     * We need to track brace depth and string context to find the matching '}'.
     * 
     * @param content  The full f-string content
     * @param startPos Position after the opening '{'
     * @param context  Parsing context
     * @return Parsed expression and position after closing '}'
     */
    private ExpressionParseResult parseExpressionInBraces(String content, int startPos,
            ParsingContext context) {
        // Find the matching closing brace
        int braceDepth = 1;
        int pos = startPos;
        boolean inString = false;
        boolean inChar = false;
        boolean escaped = false;

        while (pos < content.length() && braceDepth > 0) {
            char ch = content.charAt(pos);

            if (escaped) {
                escaped = false;
                pos++;
                continue;
            }

            if (ch == '\\') {
                escaped = true;
                pos++;
                continue;
            }

            if (!inString && !inChar) {
                if (ch == '"') {
                    inString = true;
                } else if (ch == '\'') {
                    inChar = true;
                } else if (ch == '{') {
                    braceDepth++;
                } else if (ch == '}') {
                    braceDepth--;
                }
            } else if (inString && ch == '"') {
                inString = false;
            } else if (inChar && ch == '\'') {
                inChar = false;
            }

            pos++;
        }

        if (braceDepth > 0) {
            throw new ParserException("Unclosed '{' in f-string expression",
                    context.getTokenStream().getCurrentToken());
        }

        // Extract the expression text
        String expressionText = content.substring(startPos, pos - 1);

        if (expressionText.trim().isEmpty()) {
            throw new ParserException("Empty expression in f-string",
                    context.getTokenStream().getCurrentToken());
        }

        // Parse the expression using a new parser instance
        Expression expression = parseExpressionFromString(expressionText.trim());

        return new ExpressionParseResult(expression, pos);
    }

    /**
     * 🔧 Parses an expression from a string
     * 
     * Creates a temporary lexer and parser to parse the expression text.
     * This is necessary because the expression might be complex (function calls,
     * etc.)
     * 
     * @param expressionText The expression text to parse
     * @return Parsed expression
     */
    private Expression parseExpressionFromString(String expressionText) {
        try {
            Lexer lexer = new Lexer(expressionText);
            LanguageParser parser = new LanguageParser(lexer);
            Expression expression = parser.parseExpression();

            if (parser.hasErrors()) {
                throw new ParserException("Invalid expression in f-string: " + expressionText,
                        null);
            }

            return expression;
        } catch (Exception e) {
            throw new ParserException("Failed to parse expression in f-string: " + expressionText,
                    null);
        }
    }

    @Override
    public Set<TokenType> getHandledTokenTypes() {
        return Set.of(TokenType.F_STRING);
    }
}