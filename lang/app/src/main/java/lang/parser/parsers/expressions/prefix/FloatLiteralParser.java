package lang.parser.parsers.expressions.prefix;

import lang.parser.interfaces.PrefixExpressionParser;
import lang.parser.core.ParsingContext;
import lang.parser.error.ParserException;
import lang.ast.base.Expression;
import lang.ast.literals.FloatLiteral;
import lang.token.TokenType;
import lang.token.Token;
import java.util.Set;

/**
 * 🌊 FloatLiteralParser - Floating-Point Number Parser 🌊
 * 
 * Examples of valid float literals:
 * - 3.14159 → creates FloatLiteral(3.14159)
 * - 0.5 → creates FloatLiteral(0.5)
 * - .75 → creates FloatLiteral(0.75)
 * - 2.0 → creates FloatLiteral(2.0)
 * - 1e6 → creates FloatLiteral(1000000.0)
 * - 1.23e-4 → creates FloatLiteral(0.000123)
 * 
 * Error cases:
 * - Invalid format: "3.14.15" → Parser error
 * - Out of range: "1e400" → Infinity (handled gracefully)
 * - Malformed: "3." without digits → Should be handled by lexer
 */
public class FloatLiteralParser implements PrefixExpressionParser {

    @Override
    public Expression parsePrefix(ParsingContext context) {
        Token floatToken = context.consumeCurrentToken(TokenType.FLOAT);

        try {
            double value = parseFloatValue(floatToken.literal());
            return new FloatLiteral(floatToken, value);
        } catch (NumberFormatException e) {
            throw new ParserException(
                    "Invalid float literal: " + floatToken.literal() + " - " + e.getMessage(),
                    floatToken);
        } catch (FloatParseException e) {
            throw new ParserException(e.getMessage(), floatToken);
        }
    }

    /**
     * 🔢 Parses a float value from string representation
     * 
     * From first principles, float parsing needs to handle:
     * - Standard decimal notation: 3.14, 0.5, 2.0
     * - Scientific notation: 1e6, 1.23e-4, 5E+2
     * - Numbers starting with decimal: .5, .123
     * - Numbers ending with decimal: 5., 123.
     * - Special values: Infinity, -Infinity, NaN
     */
    private double parseFloatValue(String literal) throws FloatParseException {
        if (literal == null || literal.trim().isEmpty()) {
            throw new FloatParseException("Empty float literal");
        }

        String normalized = literal.trim();

        if (normalized.startsWith(".")) {
            normalized = "0" + normalized;
        }

        if (normalized.endsWith(".")) {
            normalized = normalized + "0";
        }

        validateFloatFormat(normalized);

        try {
            double value = Double.parseDouble(normalized);

            // Check for special values and provide context
            if (Double.isInfinite(value)) {
                // This is actually valid in IEEE 754, but might want to warn
                // For now, we'll allow it as it's mathematically meaningful
                return value;
            }

            if (Double.isNaN(value)) {
                throw new FloatParseException("Float literal resulted in NaN: " + literal);
            }

            return value;

        } catch (NumberFormatException e) {
            throw new FloatParseException("Invalid float format: " + literal + " - " + e.getMessage());
        }
    }

    /**
     * ✅ Validates the basic format of a float literal
     */
    private void validateFloatFormat(String literal) throws FloatParseException {
        if (literal.trim().isEmpty()) {
            throw new FloatParseException("Empty float literal");
        }

        long decimalCount = literal.chars().filter(ch -> ch == '.').count();
        if (decimalCount > 1) {
            throw new FloatParseException("Multiple decimal points in float literal: " + literal);
        }

        if (literal.contains("e") || literal.contains("E")) {
            validateScientificNotation(literal);
        }

        boolean hasDigit = literal.chars().anyMatch(Character::isDigit);
        if (!hasDigit) {
            throw new FloatParseException("Float literal must contain at least one digit: " + literal);
        }

        for (char ch : literal.toCharArray()) {
            if (!Character.isDigit(ch) && ch != '.' && ch != '+' && ch != '-' &&
                    ch != 'e' && ch != 'E') {
                throw new FloatParseException("Invalid character in float literal: " + ch);
            }
        }
    }

    /**
     * 🔬 Validates scientific notation format
     * 
     * Scientific notation has the form: [digits][.digits][e|E][+|-][digits]
     * Examples: 1e6, 1.23e-4, 5E+2, 1.0e10
     * 
     * Rules:
     * - Must have digits before e/E
     * - Can have optional + or - after e/E
     * - Must have digits after e/E (and optional +/-)
     * - Cannot have multiple e/E indicators
     * 
     * @param literal The literal containing scientific notation
     * @throws FloatParseException If the scientific notation is malformed
     */
    private void validateScientificNotation(String literal) throws FloatParseException {
        int eIndex = Math.max(literal.indexOf('e'), literal.indexOf('E'));

        if (eIndex == -1) {
            return; // No scientific notation
        }

        // Check for multiple e/E
        String remaining = literal.substring(eIndex + 1);
        if (remaining.contains("e") || remaining.contains("E")) {
            throw new FloatParseException("Multiple scientific notation indicators: " + literal);
        }

        // Check that there's something before e/E
        if (eIndex == 0) {
            throw new FloatParseException("Scientific notation must have digits before e/E: " + literal);
        }

        // Check that there's something after e/E
        if (eIndex == literal.length() - 1) {
            throw new FloatParseException("Scientific notation must have exponent after e/E: " + literal);
        }

        // Validate the exponent part
        String exponentPart = literal.substring(eIndex + 1);
        validateExponentPart(exponentPart);
    }

    /**
     * 🔢 Validates the exponent part of scientific notation
     * 
     * The exponent part can be:
     * - Just digits: "6" in "1e6"
     * - Plus and digits: "+6" in "1e+6"
     * - Minus and digits: "-4" in "1e-4"
     * 
     * @param exponentPart The part after e/E
     * @throws FloatParseException If the exponent format is invalid
     */
    private void validateExponentPart(String exponentPart) throws FloatParseException {
        if (exponentPart.isEmpty()) {
            throw new FloatParseException("Empty exponent in scientific notation");
        }

        String digits = exponentPart;
        if (digits.startsWith("+") || digits.startsWith("-")) {
            digits = digits.substring(1);
        }

        if (digits.isEmpty()) {
            throw new FloatParseException("Exponent must have digits after sign: " + exponentPart);
        }

        for (char ch : digits.toCharArray()) {
            if (!Character.isDigit(ch)) {
                throw new FloatParseException("Invalid character in exponent: " + ch);
            }
        }
    }

    @Override
    public Set<TokenType> getHandledTokenTypes() {
        return Set.of(TokenType.FLOAT);
    }

    /**
     * 🚨 Custom exception for float parsing errors
     * 
     * This provides more specific error information than generic
     * NumberFormatException
     * and allows for better error messages in the parser.
     */
    private static class FloatParseException extends Exception {
        public FloatParseException(String message) {
            super(message);
        }
    }
}