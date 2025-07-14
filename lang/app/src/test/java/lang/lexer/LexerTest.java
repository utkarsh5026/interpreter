package lang.lexer;

import lang.token.Token;
import lang.token.TokenType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for the Lexer class.
 * 
 * This test suite covers all aspects of lexical analysis:
 * 1. Basic token recognition for all token types
 * 2. Operator precedence and associativity testing
 * 3. String literal parsing with escape sequences
 * 4. Comment handling (single-line and multi-line)
 * 5. Position tracking accuracy
 * 6. Error handling and edge cases
 * 7. Performance characteristics
 * 8. Complex input scenarios
 */
class LexerTest {

    private Lexer lexer;

    @BeforeEach
    void setUp() {
        // Reset state before each test
        lexer = null;
    }

    /**
     * Helper method to create lexer and extract all tokens from input
     */
    private List<Token> tokenizeInput(String input) {
        lexer = new Lexer(input);
        List<Token> tokens = new ArrayList<>();

        Token token;
        do {
            token = lexer.nextToken();
            tokens.add(token);
        } while (token.type() != TokenType.EOF);

        return tokens;
    }

    /**
     * Helper method to verify token properties
     */
    private void assertToken(Token token, TokenType expectedType, String expectedLiteral,
            int expectedLine, int expectedColumn) {
        assertEquals(expectedType, token.type(),
                "Token type mismatch");
        assertEquals(expectedLiteral, token.literal(),
                "Token literal mismatch");
        assertEquals(expectedLine, token.position().line(),
                "Token line position mismatch");
        assertEquals(expectedColumn, token.position().column(),
                "Token column position mismatch");
    }

    @Nested
    @DisplayName("Basic Token Recognition Tests")
    class BasicTokenTests {

        @Test
        @DisplayName("Should tokenize empty input correctly")
        void testEmptyInput() {
            List<Token> tokens = tokenizeInput("");

            System.out.println(tokens);

            assertEquals(1, tokens.size(), "Empty input should produce only EOF token");
            assertToken(tokens.get(0), TokenType.EOF, "", 1, 0);
        }

        @Test
        @DisplayName("Should tokenize whitespace-only input correctly")
        void testWhitespaceOnlyInput() {
            List<Token> tokens = tokenizeInput("   \t\n\r  ");

            assertEquals(1, tokens.size(), "Whitespace-only input should produce only EOF token");
            assertToken(tokens.get(0), TokenType.EOF, "", 3, 2);
        }

        @Test
        @DisplayName("Should tokenize single identifier")
        void testSingleIdentifier() {
            List<Token> tokens = tokenizeInput("variable");

            assertEquals(2, tokens.size());
            assertToken(tokens.get(0), TokenType.IDENTIFIER, "variable", 1, 8);
            assertToken(tokens.get(1), TokenType.EOF, "", 1, 8);
        }

        @Test
        @DisplayName("Should tokenize identifier with numbers and underscores")
        void testComplexIdentifier() {
            List<Token> tokens = tokenizeInput("_var123_name");

            assertEquals(2, tokens.size());
            assertToken(tokens.get(0), TokenType.IDENTIFIER, "_var123_name", 1, 12);
            assertToken(tokens.get(1), TokenType.EOF, "", 1, 12);
        }

        @Test
        @DisplayName("Should tokenize integer literals")
        void testIntegerLiterals() {
            List<Token> tokens = tokenizeInput("42 0 999");

            assertEquals(4, tokens.size());
            assertToken(tokens.get(0), TokenType.INT, "42", 1, 2);
            assertToken(tokens.get(1), TokenType.INT, "0", 1, 4);
            assertToken(tokens.get(2), TokenType.INT, "999", 1, 8);
            assertToken(tokens.get(3), TokenType.EOF, "", 1, 8);
        }

        @ParameterizedTest
        @ValueSource(strings = { "0", "1", "42", "999", "12345" })
        @DisplayName("Should tokenize various integer values")
        void testVariousIntegers(String number) {
            List<Token> tokens = tokenizeInput(number);

            assertEquals(2, tokens.size());
            assertToken(tokens.get(0), TokenType.INT, number, 1, number.length());
        }

        @Test
        @DisplayName("Should tokenize basic string literal")
        void testBasicStringLiteral() {
            List<Token> tokens = tokenizeInput("\"hello world\"");

            assertEquals(2, tokens.size());
            assertToken(tokens.get(0), TokenType.STRING, "hello world", 1, 13);
            assertToken(tokens.get(1), TokenType.EOF, "", 1, 13);
        }

        @Test
        @DisplayName("Should tokenize empty string literal")
        void testEmptyStringLiteral() {
            List<Token> tokens = tokenizeInput("\"\"");

            assertEquals(2, tokens.size());
            assertToken(tokens.get(0), TokenType.STRING, "", 1, 2);
            assertToken(tokens.get(1), TokenType.EOF, "", 1, 2);
        }
    }

    @Nested
    @DisplayName("Keyword Recognition Tests")
    class KeywordTests {

        @ParameterizedTest
        @CsvSource({
                "fn, FUNCTION",
                "let, LET",
                "const, CONST",
                "if, IF",
                "else, ELSE",
                "elif, ELIF",
                "return, RETURN",
                "while, WHILE",
                "for, FOR",
                "break, BREAK",
                "continue, CONTINUE",
                "true, TRUE",
                "false, FALSE",
                "null, NULL",
                "class, CLASS",
                "extends, EXTENDS",
                "super, SUPER",
                "this, THIS",
                "new, NEW"
        })
        @DisplayName("Should recognize all keywords correctly")
        void testKeywordRecognition(String keyword, String expectedTokenType) {
            List<Token> tokens = tokenizeInput(keyword);

            assertEquals(2, tokens.size());
            assertEquals(TokenType.valueOf(expectedTokenType), tokens.get(0).type());
            assertEquals(keyword, tokens.get(0).literal());
        }

        @Test
        @DisplayName("Should distinguish keywords from identifiers with similar names")
        void testKeywordVsIdentifierDistinction() {
            List<Token> tokens = tokenizeInput("if ifx xif lett");

            assertEquals(5, tokens.size());
            assertToken(tokens.get(0), TokenType.IF, "if", 1, 2);
            assertToken(tokens.get(1), TokenType.IDENTIFIER, "ifx", 1, 6);
            assertToken(tokens.get(2), TokenType.IDENTIFIER, "xif", 1, 10);
            assertToken(tokens.get(3), TokenType.IDENTIFIER, "lett", 1, 15);
            assertToken(tokens.get(4), TokenType.EOF, "", 1, 15);
        }
    }

    @Nested
    @DisplayName("Operator Recognition Tests")
    class OperatorTests {

        @Test
        @DisplayName("Should tokenize basic arithmetic operators")
        void testArithmeticOperators() {
            List<Token> tokens = tokenizeInput("+ - * / %");

            assertEquals(6, tokens.size());
            assertToken(tokens.get(0), TokenType.PLUS, "+", 1, 1);
            assertToken(tokens.get(1), TokenType.MINUS, "-", 1, 3);
            assertToken(tokens.get(2), TokenType.ASTERISK, "*", 1, 5);
            assertToken(tokens.get(3), TokenType.SLASH, "/", 1, 7);
            assertToken(tokens.get(4), TokenType.MODULUS, "%", 1, 9);
            assertToken(tokens.get(5), TokenType.EOF, "", 1, 9);
        }

        @Test
        @DisplayName("Should tokenize comparison operators")
        void testComparisonOperators() {
            List<Token> tokens = tokenizeInput("< > <= >= == !=");

            assertEquals(7, tokens.size());
            assertToken(tokens.get(0), TokenType.LESS_THAN, "<", 1, 1);
            assertToken(tokens.get(1), TokenType.GREATER_THAN, ">", 1, 3);
            assertToken(tokens.get(2), TokenType.LESS_THAN_OR_EQUAL, "<=", 1, 6);
            assertToken(tokens.get(3), TokenType.GREATER_THAN_OR_EQUAL, ">=", 1, 9);
            assertToken(tokens.get(4), TokenType.EQ, "==", 1, 12);
            assertToken(tokens.get(5), TokenType.NOT_EQ, "!=", 1, 15);
            assertToken(tokens.get(6), TokenType.EOF, "", 1, 15);
        }

        @Test
        @DisplayName("Should tokenize logical operators")
        void testLogicalOperators() {
            List<Token> tokens = tokenizeInput("! && ||");

            assertEquals(4, tokens.size());
            assertToken(tokens.get(0), TokenType.BANG, "!", 1, 1);
            assertToken(tokens.get(1), TokenType.AND, "&&", 1, 4);
            assertToken(tokens.get(2), TokenType.OR, "||", 1, 7);
            assertToken(tokens.get(3), TokenType.EOF, "", 1, 7);
        }

        @Test
        @DisplayName("Should tokenize assignment operators")
        void testAssignmentOperators() {
            List<Token> tokens = tokenizeInput("= += -= *= /=");

            assertEquals(6, tokens.size());
            assertToken(tokens.get(0), TokenType.ASSIGN, "=", 1, 1);
            assertToken(tokens.get(1), TokenType.PLUS_ASSIGN, "+=", 1, 4);
            assertToken(tokens.get(2), TokenType.MINUS_ASSIGN, "-=", 1, 7);
            assertToken(tokens.get(3), TokenType.ASTERISK_ASSIGN, "*=", 1, 10);
            assertToken(tokens.get(4), TokenType.SLASH_ASSIGN, "/=", 1, 13);
            assertToken(tokens.get(5), TokenType.EOF, "", 1, 13);
        }

        @Test
        @DisplayName("Should tokenize bitwise operators")
        void testBitwiseOperators() {
            List<Token> tokens = tokenizeInput("& | ^ ~");

            assertEquals(5, tokens.size());
            assertToken(tokens.get(0), TokenType.BITWISE_AND, "&", 1, 1);
            assertToken(tokens.get(1), TokenType.BITWISE_OR, "|", 1, 3);
            assertToken(tokens.get(2), TokenType.BITWISE_XOR, "^", 1, 5);
            assertToken(tokens.get(3), TokenType.BITWISE_NOT, "~", 1, 7);
            assertToken(tokens.get(4), TokenType.EOF, "", 1, 7);
        }

        @Test
        @DisplayName("Should distinguish single and double character operators")
        void testOperatorDistinction() {
            List<Token> tokens = tokenizeInput("= == ! != < <= > >=");

            assertEquals(9, tokens.size());
            assertToken(tokens.get(0), TokenType.ASSIGN, "=", 1, 1);
            assertToken(tokens.get(1), TokenType.EQ, "==", 1, 4);
            assertToken(tokens.get(2), TokenType.BANG, "!", 1, 6);
            assertToken(tokens.get(3), TokenType.NOT_EQ, "!=", 1, 9);
            assertToken(tokens.get(4), TokenType.LESS_THAN, "<", 1, 11);
            assertToken(tokens.get(5), TokenType.LESS_THAN_OR_EQUAL, "<=", 1, 14);
            assertToken(tokens.get(6), TokenType.GREATER_THAN, ">", 1, 16);
            assertToken(tokens.get(7), TokenType.GREATER_THAN_OR_EQUAL, ">=", 1, 19);
            assertToken(tokens.get(8), TokenType.EOF, "", 1, 19);
        }
    }

    @Nested
    @DisplayName("Delimiter Recognition Tests")
    class DelimiterTests {

        @Test
        @DisplayName("Should tokenize parentheses and brackets")
        void testBracketsAndParentheses() {
            List<Token> tokens = tokenizeInput("() [] {}");

            assertEquals(7, tokens.size());
            assertToken(tokens.get(0), TokenType.LPAREN, "(", 1, 1);
            assertToken(tokens.get(1), TokenType.RPAREN, ")", 1, 2);
            assertToken(tokens.get(2), TokenType.LBRACKET, "[", 1, 4);
            assertToken(tokens.get(3), TokenType.RBRACKET, "]", 1, 5);
            assertToken(tokens.get(4), TokenType.LBRACE, "{", 1, 7);
            assertToken(tokens.get(5), TokenType.RBRACE, "}", 1, 8);
            assertToken(tokens.get(6), TokenType.EOF, "", 1, 8);
        }

        @Test
        @DisplayName("Should tokenize punctuation marks")
        void testPunctuation() {
            List<Token> tokens = tokenizeInput(", ; : .");

            assertEquals(5, tokens.size());
            assertToken(tokens.get(0), TokenType.COMMA, ",", 1, 1);
            assertToken(tokens.get(1), TokenType.SEMICOLON, ";", 1, 3);
            assertToken(tokens.get(2), TokenType.COLON, ":", 1, 5);
            assertToken(tokens.get(3), TokenType.DOT, ".", 1, 7);
            assertToken(tokens.get(4), TokenType.EOF, "", 1, 7);
        }
    }

    @Nested
    @DisplayName("String Literal Tests")
    class StringLiteralTests {

        @Test
        @DisplayName("Should handle escape sequences in strings")
        void testStringEscapeSequences() {
            List<Token> tokens = tokenizeInput("\"Hello\\nWorld\\t!\"");

            assertEquals(2, tokens.size());
            assertToken(tokens.get(0), TokenType.STRING, "Hello\nWorld\t!", 1, 17);
            assertToken(tokens.get(1), TokenType.EOF, "", 1, 17);
        }

        @Test
        @DisplayName("Should handle all supported escape sequences")
        void testAllEscapeSequences() {
            String input = "\"\\n\\t\\r\\f\\b\\'\\\"\\\\\"";
            List<Token> tokens = tokenizeInput(input);

            assertEquals(2, tokens.size());
            assertToken(tokens.get(0), TokenType.STRING, "\n\t\r\f\b'\"\\", 1, input.length());
        }

        @Test
        @DisplayName("Should handle strings with quotes inside")
        void testStringWithQuotes() {
            List<Token> tokens = tokenizeInput("\"He said \\\"Hello\\\"\"");

            assertEquals(2, tokens.size());
            assertToken(tokens.get(0), TokenType.STRING, "He said \"Hello\"", 1, 19);
        }

        @Test
        @DisplayName("Should throw exception for unterminated string")
        void testUnterminatedString() {
            assertThrows(RuntimeException.class, () -> {
                tokenizeInput("\"unterminated string");
            }, "Should throw exception for unterminated string");
        }

        @Test
        @DisplayName("Should handle string with special characters")
        void testStringWithSpecialChars() {
            List<Token> tokens = tokenizeInput("\"@#$%^&*()_+-=[]{}|;':,.<>?\"");

            assertEquals(2, tokens.size());
            assertToken(tokens.get(0), TokenType.STRING, "@#$%^&*()_+-=[]{}|;':,.<>?", 1, 28);
        }
    }

    @Nested
    @DisplayName("Position Tracking Tests")
    class PositionTrackingTests {

        @Test
        @DisplayName("Should track line numbers correctly")
        void testLineNumberTracking() {
            String input = "let x = 5;\nlet y = 10;\nlet z = 15;";
            List<Token> tokens = tokenizeInput(input);

            // First line tokens
            assertEquals(1, tokens.get(0).position().line()); // let
            assertEquals(1, tokens.get(1).position().line()); // x
            assertEquals(1, tokens.get(2).position().line()); // =
            assertEquals(1, tokens.get(3).position().line()); // 5
            assertEquals(1, tokens.get(4).position().line()); // ;

            // Second line tokens
            assertEquals(2, tokens.get(5).position().line()); // let
            assertEquals(2, tokens.get(6).position().line()); // y
            assertEquals(2, tokens.get(7).position().line()); // =
            assertEquals(2, tokens.get(8).position().line()); // 10
            assertEquals(2, tokens.get(9).position().line()); // ;

            // Third line tokens
            assertEquals(3, tokens.get(10).position().line()); // let
            assertEquals(3, tokens.get(11).position().line()); // z
            assertEquals(3, tokens.get(12).position().line()); // =
            assertEquals(3, tokens.get(13).position().line()); // 15
            assertEquals(3, tokens.get(14).position().line()); // ;
        }

        @Test
        @DisplayName("Should track column numbers correctly")
        void testColumnNumberTracking() {
            String input = "let x = 5;";
            List<Token> tokens = tokenizeInput(input);

            assertToken(tokens.get(0), TokenType.LET, "let", 1, 3); // columns 1-3
            assertToken(tokens.get(1), TokenType.IDENTIFIER, "x", 1, 5); // column 5
            assertToken(tokens.get(2), TokenType.ASSIGN, "=", 1, 7); // column 7
            assertToken(tokens.get(3), TokenType.INT, "5", 1, 9); // column 9
            assertToken(tokens.get(4), TokenType.SEMICOLON, ";", 1, 10); // column 10
        }

        @Test
        @DisplayName("Should handle different line ending types")
        void testDifferentLineEndings() {
            // Test \n, \r\n, and \r line endings
            List<Token> tokens1 = tokenizeInput("let x = 5;\nlet y = 10;");
            List<Token> tokens2 = tokenizeInput("let x = 5;\r\nlet y = 10;");
            List<Token> tokens3 = tokenizeInput("let x = 5;\rlet y = 10;");

            // All should produce same line numbers for second statement
            assertEquals(2, tokens1.get(5).position().line());
            assertEquals(2, tokens2.get(5).position().line());
            assertEquals(2, tokens3.get(5).position().line());
        }

        @Test
        @DisplayName("Should handle mixed whitespace correctly")
        void testMixedWhitespace() {
            String input = "let\t\tx\n  =\t 5  ;";
            List<Token> tokens = tokenizeInput(input);

            assertEquals(6, tokens.size());
            assertToken(tokens.get(0), TokenType.LET, "let", 1, 3);
            assertToken(tokens.get(1), TokenType.IDENTIFIER, "x", 1, 6);
            assertToken(tokens.get(2), TokenType.ASSIGN, "=", 2, 3);
            assertToken(tokens.get(3), TokenType.INT, "5", 2, 6);
            assertToken(tokens.get(4), TokenType.SEMICOLON, ";", 2, 9);
            assertToken(tokens.get(5), TokenType.EOF, "", 2, 9);
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle illegal characters")
        void testIllegalCharacters() {
            List<Token> tokens = tokenizeInput("let x = 5 @ y = 10;");

            // Should produce ILLEGAL token for '@'
            boolean foundIllegal = tokens.stream()
                    .anyMatch(token -> token.type() == TokenType.ILLEGAL);
            assertTrue(foundIllegal, "Should produce ILLEGAL token for '@' character");
        }

        @Test
        @DisplayName("Should handle Unicode characters")
        void testUnicodeCharacters() {
            List<Token> tokens = tokenizeInput("let α = 5;"); // Greek alpha

            // Should treat unicode as illegal character
            boolean foundIllegal = tokens.stream()
                    .anyMatch(token -> token.type() == TokenType.ILLEGAL);
            assertTrue(foundIllegal, "Should produce ILLEGAL token for Unicode character");
        }

        @Test
        @DisplayName("Should handle very long identifiers")
        void testVeryLongIdentifier() {
            StringBuilder longId = new StringBuilder("a");
            for (int i = 0; i < 1000; i++) {
                longId.append("b");
            }

            List<Token> tokens = tokenizeInput(longId.toString());

            assertEquals(2, tokens.size());
            assertToken(tokens.get(0), TokenType.IDENTIFIER, longId.toString(), 1, longId.length());
        }

        @Test
        @DisplayName("Should handle very long numbers")
        void testVeryLongNumber() {
            StringBuilder longNumber = new StringBuilder("1");
            for (int i = 0; i < 100; i++) {
                longNumber.append("2");
            }

            List<Token> tokens = tokenizeInput(longNumber.toString());

            assertEquals(2, tokens.size());
            assertToken(tokens.get(0), TokenType.INT, longNumber.toString(), 1, longNumber.length());
        }
    }

    @Nested
    @DisplayName("Complex Input Tests")
    class ComplexInputTests {

        @Test
        @DisplayName("Should tokenize complete function definition")
        void testCompleteFunctionDefinition() {
            String input = """
                    fn fibonacci(n) {
                        if (n <= 1) {
                            return n;
                        }
                        return fibonacci(n - 1) + fibonacci(n - 2);
                    }
                    """;

            List<Token> tokens = tokenizeInput(input);

            // Verify key tokens are present
            assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.FUNCTION));
            assertTrue(
                    tokens.stream().anyMatch(t -> t.type() == TokenType.IDENTIFIER && t.literal().equals("fibonacci")));
            assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.IF));
            assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.RETURN));
            assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.LESS_THAN_OR_EQUAL));
        }

        @Test
        @DisplayName("Should tokenize complex expression")
        void testComplexExpression() {
            String input = "result = (a + b) * c / d - e % f;";
            List<Token> tokens = tokenizeInput(input);

            assertEquals(17, tokens.size());
            assertToken(tokens.get(0), TokenType.IDENTIFIER, "result", 1, 6);
            assertToken(tokens.get(1), TokenType.ASSIGN, "=", 1, 8);
            assertToken(tokens.get(2), TokenType.LPAREN, "(", 1, 10);
            assertToken(tokens.get(3), TokenType.IDENTIFIER, "a", 1, 11);
            assertToken(tokens.get(4), TokenType.PLUS, "+", 1, 13);
            assertToken(tokens.get(5), TokenType.IDENTIFIER, "b", 1, 15);
            assertToken(tokens.get(6), TokenType.RPAREN, ")", 1, 16);
            assertToken(tokens.get(7), TokenType.ASTERISK, "*", 1, 18);
            assertToken(tokens.get(8), TokenType.IDENTIFIER, "c", 1, 20);
            assertToken(tokens.get(9), TokenType.SLASH, "/", 1, 22);
            assertToken(tokens.get(10), TokenType.IDENTIFIER, "d", 1, 24);
            assertToken(tokens.get(11), TokenType.MINUS, "-", 1, 26);
            assertToken(tokens.get(12), TokenType.IDENTIFIER, "e", 1, 28);
            assertToken(tokens.get(13), TokenType.MODULUS, "%", 1, 30);
            assertToken(tokens.get(14), TokenType.IDENTIFIER, "f", 1, 32);
            assertToken(tokens.get(15), TokenType.SEMICOLON, ";", 1, 33);
        }

        @Test
        @DisplayName("Should tokenize array and hash literals")
        void testArrayAndHashLiterals() {
            String input = """
                    let arr = [1, 2, 3];
                    let hash = {"key": "value", "num": 42};
                    """;

            List<Token> tokens = tokenizeInput(input);

            // Verify array literal tokens
            assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.LBRACKET));
            assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.RBRACKET));

            // Verify hash literal tokens
            assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.LBRACE));
            assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.RBRACE));
            assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.COLON));
            assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.STRING && t.literal().equals("key")));
            assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.STRING && t.literal().equals("value")));
        }

        @Test
        @DisplayName("Should tokenize nested structures")
        void testNestedStructures() {
            String input = "let nested = {\"array\": [1, {\"inner\": true}]};";
            List<Token> tokens = tokenizeInput(input);

            // Should handle deeply nested structures correctly
            long braceCount = tokens.stream()
                    .mapToLong(t -> t.type() == TokenType.LBRACE ? 1 : (t.type() == TokenType.RBRACE ? -1 : 0)).sum();

            assertEquals(0, braceCount, "Braces should be balanced");

            long bracketCount = tokens.stream()
                    .mapToLong(t -> t.type() == TokenType.LBRACKET ? 1 : (t.type() == TokenType.RBRACKET ? -1 : 0))
                    .sum();

            assertEquals(0, bracketCount, "Brackets should be balanced");
        }

        @Test
        @DisplayName("Should handle mixed comments and code")
        void testMixedCommentsAndCode() {
            String input = """
                    // Main function
                    fn main() { /* entry point */
                        let x = 5; // initialize x
                        /*
                         * Multi-line calculation
                         */
                        let y = x * 2; // double x
                    } // end main
                    """;

            List<Token> tokens = tokenizeInput(input);

            // Should properly skip all comments and tokenize code
            assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.FUNCTION));
            assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.IDENTIFIER && t.literal().equals("main")));
            assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.LET));
            assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.IDENTIFIER && t.literal().equals("x")));
            assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.INT && t.literal().equals("5")));
            assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.IDENTIFIER && t.literal().equals("y")));
            assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.ASTERISK));
            assertTrue(tokens.stream().anyMatch(t -> t.type() == TokenType.INT && t.literal().equals("2")));
        }
    }

    @Nested
    @DisplayName("Lexer State Management Tests")
    class StateManagementTests {

        @Test
        @DisplayName("Should handle reset functionality")
        void testLexerReset() {
            lexer = new Lexer("let x = 5;");

            // Consume some tokens
            Token token1 = lexer.nextToken();
            Token token2 = lexer.nextToken();

            assertEquals(TokenType.LET, token1.type());
            assertEquals(TokenType.IDENTIFIER, token2.type());

            // Reset lexer
            lexer.reset();

            // Should start from beginning again
            Token resetToken1 = lexer.nextToken();
            Token resetToken2 = lexer.nextToken();

            assertEquals(TokenType.LET, resetToken1.type());
            assertEquals(TokenType.IDENTIFIER, resetToken2.type());
            assertEquals("x", resetToken2.literal());
        }

        @Test
        @DisplayName("Should handle getCurrentChar method")
        void testGetCurrentChar() {
            lexer = new Lexer("abc");

            // getCurrentChar should return current character
            char currentChar = lexer.getCurrentChar();
            assertTrue(Character.isLetter(currentChar) || currentChar == '\0');
        }

        @Test
        @DisplayName("Should handle multiple lexer instances")
        void testMultipleLexerInstances() {
            Lexer lexer1 = new Lexer("let x = 5;");
            Lexer lexer2 = new Lexer("const y = 10;");

            Token token1 = lexer1.nextToken();
            Token token2 = lexer2.nextToken();

            assertEquals(TokenType.LET, token1.type());
            assertEquals(TokenType.CONST, token2.type());

            // Instances should be independent
            assertNotEquals(token1.type(), token2.type());
        }
    }

    @Nested
    @DisplayName("Performance and Edge Case Tests")
    class PerformanceAndEdgeCaseTests {

        @Test
        @DisplayName("Should handle very large input efficiently")
        void testLargeInputPerformance() {
            StringBuilder largeInput = new StringBuilder();
            for (int i = 0; i < 1000; i++) {
                largeInput.append("let var").append(i).append(" = ").append(i).append("; ");
            }

            long startTime = System.currentTimeMillis();
            List<Token> tokens = tokenizeInput(largeInput.toString());
            long endTime = System.currentTimeMillis();

            // Should complete within reasonable time (less than 1 second)
            assertTrue(endTime - startTime < 1000, "Large input should be processed quickly");

            // Should produce expected number of tokens (let, identifier, =, number, ; for
            // each iteration)
            assertTrue(tokens.size() > 5000, "Should produce many tokens for large input");
        }

        @Test
        @DisplayName("Should handle input with only delimiters")
        void testOnlyDelimiters() {
            List<Token> tokens = tokenizeInput("()[]{},:;.");

            assertEquals(11, tokens.size());
            assertEquals(TokenType.LPAREN, tokens.get(0).type());
            assertEquals(TokenType.RPAREN, tokens.get(1).type());
            assertEquals(TokenType.LBRACKET, tokens.get(2).type());
            assertEquals(TokenType.RBRACKET, tokens.get(3).type());
            assertEquals(TokenType.LBRACE, tokens.get(4).type());
            assertEquals(TokenType.RBRACE, tokens.get(5).type());
            assertEquals(TokenType.COMMA, tokens.get(6).type());
            assertEquals(TokenType.COLON, tokens.get(7).type());
            assertEquals(TokenType.SEMICOLON, tokens.get(8).type());
        }

        @Test
        @DisplayName("Should handle alternating tokens and whitespace")
        void testAlternatingTokensAndWhitespace() {
            String input = " let   x   =   5   ; ";
            List<Token> tokens = tokenizeInput(input);

            assertEquals(6, tokens.size());
            assertToken(tokens.get(0), TokenType.LET, "let", 1, 4);
            assertToken(tokens.get(1), TokenType.IDENTIFIER, "x", 1, 8);
            assertToken(tokens.get(2), TokenType.ASSIGN, "=", 1, 12);
            assertToken(tokens.get(3), TokenType.INT, "5", 1, 16);
            assertToken(tokens.get(4), TokenType.SEMICOLON, ";", 1, 20);
            assertToken(tokens.get(5), TokenType.EOF, "", 1, 21);
        }
    }
}