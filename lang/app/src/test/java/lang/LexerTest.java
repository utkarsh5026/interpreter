package lang;

import lang.lexer.Lexer;
import lang.token.Token;
import lang.token.TokenType;
import lang.token.TokenPosition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LexerTest {

    @Nested
    @DisplayName("Single Character Tokens")
    class SingleCharacterTokens {

        @ParameterizedTest
        @CsvSource({
                "=, ASSIGN",
                "!, BANG",
                ";, SEMICOLON",
                "(, LPAREN",
                "), RPAREN",
                ",, COMMA",
                "+, PLUS",
                "-, MINUS",
                "*, ASTERISK",
                "%, MODULUS",
                "/, SLASH",
                "<, LESS_THAN",
                ">, GREATER_THAN",
                "{, LBRACE",
                "}, RBRACE",
                "[, LBRACKET",
                "], RBRACKET",
                ":, COLON",
                "., DOT",
                "&, BITWISE_AND",
                "|, BITWISE_OR",
                "^, BITWISE_XOR",
                "~, BITWISE_NOT"
        })
        void testSingleCharacterTokens(String input, String expectedTokenType) {
            Lexer lexer = new Lexer(input);
            Token token = lexer.nextToken();

            assertEquals(TokenType.valueOf(expectedTokenType), token.type());
            assertEquals(input, token.literal());
            assertEquals(new TokenPosition(1, 1), token.position());
        }
    }

    @Nested
    @DisplayName("Two Character Operators")
    class TwoCharacterOperators {

        @ParameterizedTest
        @CsvSource({
                "==, EQ",
                "!=, NOT_EQ",
                "+=, PLUS_ASSIGN",
                "-=, MINUS_ASSIGN",
                "*=, ASTERISK_ASSIGN",
                "/=, SLASH_ASSIGN",
                "<=, LESS_THAN_OR_EQUAL",
                ">=, GREATER_THAN_OR_EQUAL",
                "&&, AND",
                "||, OR"
        })
        void testTwoCharacterOperators(String input, String expectedTokenType) {
            Lexer lexer = new Lexer(input);
            Token token = lexer.nextToken();

            assertEquals(TokenType.valueOf(expectedTokenType), token.type());
            assertEquals(input, token.literal());
            assertEquals(new TokenPosition(1, 1), token.position());
        }

        @Test
        @DisplayName("Single character when next char doesn't match")
        void testSingleCharacterWhenNextCharDoesntMatch() {
            Lexer lexer = new Lexer("= + != - &&");

            List<Token> expectedTokens = List.of(
                    new Token(TokenType.ASSIGN, "=", new TokenPosition(1, 1)),
                    new Token(TokenType.PLUS, "+", new TokenPosition(1, 3)),
                    new Token(TokenType.NOT_EQ, "!=", new TokenPosition(1, 5)),
                    new Token(TokenType.MINUS, "-", new TokenPosition(1, 8)),
                    new Token(TokenType.AND, "&&", new TokenPosition(1, 10)));

            List<Token> actualTokens = new ArrayList<>();
            Token token;
            while ((token = lexer.nextToken()).type() != TokenType.EOF) {
                actualTokens.add(token);
            }

            assertEquals(expectedTokens, actualTokens);
        }
    }

    @Nested
    @DisplayName("String Literals")
    class StringLiterals {

        @Test
        @DisplayName("Simple string literal")
        void testSimpleStringLiteral() {
            Lexer lexer = new Lexer("\"hello world\"");
            Token token = lexer.nextToken();

            assertEquals(TokenType.STRING, token.type());
            assertEquals("hello world", token.literal());
            assertEquals(new TokenPosition(1, 1), token.position());
        }

        @Test
        @DisplayName("Empty string literal")
        void testEmptyStringLiteral() {
            Lexer lexer = new Lexer("\"\"");
            Token token = lexer.nextToken();

            assertEquals(TokenType.STRING, token.type());
            assertEquals("", token.literal());
            assertEquals(new TokenPosition(1, 1), token.position());
        }

        @ParameterizedTest
        @CsvSource({
                "\"hello\\nworld\", hello\nworld",
                "\"tab\\there\", tab\there",
                "\"carriage\\rreturn\", carriage\rreturn",
                "\"form\\ffeed\", form\ffeed",
                "\"back\\bspace\", back\bspace",
                "\"single\\\'quote\", single'quote",
                "\"double\\\"quote\", double\"quote"
        })
        void testStringLiteralsWithEscapeSequences(String input, String expected) {
            Lexer lexer = new Lexer(input);
            Token token = lexer.nextToken();

            assertEquals(TokenType.STRING, token.type());
            assertEquals(expected, token.literal());
        }

        @Test
        @DisplayName("Unterminated string throws exception")
        void testUnterminatedStringThrowsException() {
            Lexer lexer = new Lexer("\"unterminated string");

            assertThrows(RuntimeException.class, () -> lexer.nextToken());
        }

        @Test
        @DisplayName("String with unknown escape sequence")
        void testStringWithUnknownEscapeSequence() {
            Lexer lexer = new Lexer("\"test\\z\"");
            Token token = lexer.nextToken();

            assertEquals(TokenType.STRING, token.type());
            assertEquals("testz", token.literal());
        }
    }

    @Nested
    @DisplayName("Numbers")
    class Numbers {

        @ParameterizedTest
        @ValueSource(strings = { "0", "1", "42", "123", "999" })
        void testIntegerLiterals(String input) {
            System.out.println("input: " + input);
            Lexer lexer = new Lexer(input);
            Token token = lexer.nextToken();

            assertEquals(TokenType.INT, token.type());
            assertEquals(input, token.literal());
            assertEquals(new TokenPosition(1, 1), token.position());
        }

        @Test
        @DisplayName("Multiple numbers separated by spaces")
        void testMultipleNumbers() {
            Lexer lexer = new Lexer("123 456 789");

            List<Token> expectedTokens = List.of(
                    new Token(TokenType.INT, "123", new TokenPosition(1, 1)),
                    new Token(TokenType.INT, "456", new TokenPosition(1, 5)),
                    new Token(TokenType.INT, "789", new TokenPosition(1, 9)));

            List<Token> actualTokens = new ArrayList<>();
            Token token;
            while ((token = lexer.nextToken()).type() != TokenType.EOF) {
                actualTokens.add(token);
            }

            assertEquals(expectedTokens, actualTokens);
        }
    }

    @Nested
    @DisplayName("Identifiers and Keywords")
    class IdentifiersAndKeywords {

        @ParameterizedTest
        @ValueSource(strings = { "variable", "myVar", "test_var", "_private", "CamelCase", "snake_case" })
        void testValidIdentifiers(String input) {
            Lexer lexer = new Lexer(input);
            Token token = lexer.nextToken();

            assertEquals(TokenType.IDENTIFIER, token.type());
            assertEquals(input, token.literal());
            assertEquals(new TokenPosition(1, 1), token.position());
        }

        @ParameterizedTest
        @CsvSource({
                "fn, FUNCTION",
                "let, LET",
                "true, TRUE",
                "false, FALSE",
                "if, IF",
                "elif, ELIF",
                "else, ELSE",
                "return, RETURN",
                "while, WHILE",
                "break, BREAK",
                "continue, CONTINUE",
                "for, FOR",
                "const, CONST",
                "class, CLASS",
                "extends, EXTENDS",
                "super, SUPER",
                "this, THIS",
                "new, NEW",
                "null, NULL"
        })
        void testKeywords(String input, String expectedTokenType) {
            Lexer lexer = new Lexer(input);
            Token token = lexer.nextToken();

            assertEquals(TokenType.valueOf(expectedTokenType), token.type());
            assertEquals(input, token.literal());
            assertEquals(new TokenPosition(1, 1), token.position());
        }

        @Test
        @DisplayName("Identifier with numbers")
        void testIdentifierWithNumbers() {
            Lexer lexer = new Lexer("var123");
            Token token = lexer.nextToken();

            assertEquals(TokenType.IDENTIFIER, token.type());
            assertEquals("var123", token.literal());
        }
    }

    @Nested
    @DisplayName("Comments")
    class Comments {

        @Test
        @DisplayName("Single line comment")
        void testSingleLineComment() {
            Lexer lexer = new Lexer("// this is a comment\nlet x = 5;");

            Token token = lexer.nextToken();
            assertEquals(TokenType.LET, token.type());
            assertEquals("let", token.literal());
            assertEquals(new TokenPosition(2, 1), token.position());
        }

        @Test
        @DisplayName("Multi-line comment")
        void testMultiLineComment() {
            Lexer lexer = new Lexer("/* this is a\nmulti-line comment */\nlet x = 5;");

            Token token = lexer.nextToken();
            assertEquals(TokenType.LET, token.type());
            assertEquals("let", token.literal());
            assertEquals(new TokenPosition(3, 1), token.position());
        }

        @Test
        @DisplayName("Nested multi-line comments")
        void testNestedMultiLineComments() {
            Lexer lexer = new Lexer("/* outer /* inner */ outer */\nlet x = 5;");

            Token token = lexer.nextToken();
            assertEquals(TokenType.LET, token.type());
            assertEquals("let", token.literal());
        }

        @Test
        @DisplayName("Comment at end of file")
        void testCommentAtEndOfFile() {
            Lexer lexer = new Lexer("let x = 5; // comment at end");

            List<Token> tokens = new ArrayList<>();
            Token token;
            while ((token = lexer.nextToken()).type() != TokenType.EOF) {
                tokens.add(token);
            }

            assertEquals(4, tokens.size());
            assertEquals(TokenType.LET, tokens.get(0).type());
            assertEquals(TokenType.IDENTIFIER, tokens.get(1).type());
            assertEquals(TokenType.ASSIGN, tokens.get(2).type());
            assertEquals(TokenType.INT, tokens.get(3).type());
        }

        @Test
        @DisplayName("Multiple consecutive comments")
        void testMultipleConsecutiveComments() {
            Lexer lexer = new Lexer("// first comment\n// second comment\n/* third comment */\nlet x = 5;");

            Token token = lexer.nextToken();
            assertEquals(TokenType.LET, token.type());
            assertEquals("let", token.literal());
        }
    }

    @Nested
    @DisplayName("Whitespace Handling")
    class WhitespaceHandling {

        @Test
        @DisplayName("Various whitespace characters")
        void testVariousWhitespaceCharacters() {
            Lexer lexer = new Lexer("  \t\n\r\f  let   x   =   5   ;  ");

            List<Token> expectedTokens = List.of(
                    new Token(TokenType.LET, "let", new TokenPosition(2, 3)),
                    new Token(TokenType.IDENTIFIER, "x", new TokenPosition(2, 9)),
                    new Token(TokenType.ASSIGN, "=", new TokenPosition(2, 13)),
                    new Token(TokenType.INT, "5", new TokenPosition(2, 17)),
                    new Token(TokenType.SEMICOLON, ";", new TokenPosition(2, 21)));

            List<Token> actualTokens = new ArrayList<>();
            Token token;
            while ((token = lexer.nextToken()).type() != TokenType.EOF) {
                actualTokens.add(token);
            }

            assertEquals(expectedTokens, actualTokens);
        }

        @Test
        @DisplayName("Tokens separated by newlines")
        void testTokensSeparatedByNewlines() {
            Lexer lexer = new Lexer("let\nx\n=\n5\n;");

            List<Token> expectedTokens = List.of(
                    new Token(TokenType.LET, "let", new TokenPosition(1, 1)),
                    new Token(TokenType.IDENTIFIER, "x", new TokenPosition(2, 1)),
                    new Token(TokenType.ASSIGN, "=", new TokenPosition(3, 1)),
                    new Token(TokenType.INT, "5", new TokenPosition(4, 1)),
                    new Token(TokenType.SEMICOLON, ";", new TokenPosition(5, 1)));

            List<Token> actualTokens = new ArrayList<>();
            Token token;
            while ((token = lexer.nextToken()).type() != TokenType.EOF) {
                actualTokens.add(token);
            }

            assertEquals(expectedTokens, actualTokens);
        }
    }

    @Nested
    @DisplayName("Position Tracking")
    class PositionTracking {

        @Test
        @DisplayName("Line and column tracking")
        void testLineAndColumnTracking() {
            Lexer lexer = new Lexer("let x = 5;\nlet y = 10;");

            List<Token> expectedTokens = List.of(
                    new Token(TokenType.LET, "let", new TokenPosition(1, 1)),
                    new Token(TokenType.IDENTIFIER, "x", new TokenPosition(1, 5)),
                    new Token(TokenType.ASSIGN, "=", new TokenPosition(1, 7)),
                    new Token(TokenType.INT, "5", new TokenPosition(1, 9)),
                    new Token(TokenType.SEMICOLON, ";", new TokenPosition(1, 10)),
                    new Token(TokenType.LET, "let", new TokenPosition(2, 1)),
                    new Token(TokenType.IDENTIFIER, "y", new TokenPosition(2, 5)),
                    new Token(TokenType.ASSIGN, "=", new TokenPosition(2, 7)),
                    new Token(TokenType.INT, "10", new TokenPosition(2, 9)),
                    new Token(TokenType.SEMICOLON, ";", new TokenPosition(2, 11)));

            List<Token> actualTokens = new ArrayList<>();
            Token token;
            while ((token = lexer.nextToken()).type() != TokenType.EOF) {
                actualTokens.add(token);
            }

            assertEquals(expectedTokens, actualTokens);
        }

        @Test
        @DisplayName("Position tracking with strings")
        void testPositionTrackingWithStrings() {
            Lexer lexer = new Lexer("let message = \"Hello\\nWorld\";");

            List<Token> expectedTokens = List.of(
                    new Token(TokenType.LET, "let", new TokenPosition(1, 1)),
                    new Token(TokenType.IDENTIFIER, "message", new TokenPosition(1, 5)),
                    new Token(TokenType.ASSIGN, "=", new TokenPosition(1, 13)),
                    new Token(TokenType.STRING, "Hello\nWorld", new TokenPosition(1, 15)),
                    new Token(TokenType.SEMICOLON, ";", new TokenPosition(1, 29)));

            List<Token> actualTokens = new ArrayList<>();
            Token token;
            while ((token = lexer.nextToken()).type() != TokenType.EOF) {
                actualTokens.add(token);
            }

            assertEquals(expectedTokens, actualTokens);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Empty input")
        void testEmptyInput() {
            Lexer lexer = new Lexer("");
            Token token = lexer.nextToken();

            assertEquals(TokenType.EOF, token.type());
            assertEquals("", token.literal());
        }

        @Test
        @DisplayName("Only whitespace")
        void testOnlyWhitespace() {
            Lexer lexer = new Lexer("   \t\n\r\f   ");
            Token token = lexer.nextToken();

            assertEquals(TokenType.EOF, token.type());
            assertEquals("", token.literal());
        }

        @Test
        @DisplayName("Illegal character")
        void testIllegalCharacter() {
            Lexer lexer = new Lexer("@");
            Token token = lexer.nextToken();

            assertEquals(TokenType.ILLEGAL, token.type());
            assertEquals("@", token.literal());
        }

        @Test
        @DisplayName("Multiple illegal characters")
        void testMultipleIllegalCharacters() {
            Lexer lexer = new Lexer("@#$");

            List<Token> expectedTokens = List.of(
                    new Token(TokenType.ILLEGAL, "@", new TokenPosition(1, 1)),
                    new Token(TokenType.ILLEGAL, "#", new TokenPosition(1, 2)),
                    new Token(TokenType.ILLEGAL, "$", new TokenPosition(1, 3)));

            List<Token> actualTokens = new ArrayList<>();
            Token token;
            while ((token = lexer.nextToken()).type() != TokenType.EOF) {
                actualTokens.add(token);
            }

            assertEquals(expectedTokens, actualTokens);
        }
    }

    @Nested
    @DisplayName("Complex Examples")
    class ComplexExamples {

        @Test
        @DisplayName("Simple function definition")
        void testSimpleFunctionDefinition() {
            String input = "fn add(x, y) { return x + y; }";
            Lexer lexer = new Lexer(input);

            List<Token> expectedTokens = List.of(
                    new Token(TokenType.FUNCTION, "fn", new TokenPosition(1, 1)),
                    new Token(TokenType.IDENTIFIER, "add", new TokenPosition(1, 4)),
                    new Token(TokenType.LPAREN, "(", new TokenPosition(1, 7)),
                    new Token(TokenType.IDENTIFIER, "x", new TokenPosition(1, 8)),
                    new Token(TokenType.COMMA, ",", new TokenPosition(1, 9)),
                    new Token(TokenType.IDENTIFIER, "y", new TokenPosition(1, 11)),
                    new Token(TokenType.RPAREN, ")", new TokenPosition(1, 12)),
                    new Token(TokenType.LBRACE, "{", new TokenPosition(1, 14)),
                    new Token(TokenType.RETURN, "return", new TokenPosition(1, 16)),
                    new Token(TokenType.IDENTIFIER, "x", new TokenPosition(1, 23)),
                    new Token(TokenType.PLUS, "+", new TokenPosition(1, 25)),
                    new Token(TokenType.IDENTIFIER, "y", new TokenPosition(1, 27)),
                    new Token(TokenType.SEMICOLON, ";", new TokenPosition(1, 28)),
                    new Token(TokenType.RBRACE, "}", new TokenPosition(1, 30)));

            List<Token> actualTokens = new ArrayList<>();
            Token token;
            while ((token = lexer.nextToken()).type() != TokenType.EOF) {
                actualTokens.add(token);
            }

            assertEquals(expectedTokens, actualTokens);
        }

        @Test
        @DisplayName("If statement with comparison")
        void testIfStatementWithComparison() {
            String input = "if (x >= 10 && y != 0) { let z = x / y; }";
            Lexer lexer = new Lexer(input);

            List<Token> expectedTokens = List.of(
                    new Token(TokenType.IF, "if", new TokenPosition(1, 1)),
                    new Token(TokenType.LPAREN, "(", new TokenPosition(1, 4)),
                    new Token(TokenType.IDENTIFIER, "x", new TokenPosition(1, 5)),
                    new Token(TokenType.GREATER_THAN_OR_EQUAL, ">=", new TokenPosition(1, 7)),
                    new Token(TokenType.INT, "10", new TokenPosition(1, 10)),
                    new Token(TokenType.AND, "&&", new TokenPosition(1, 13)),
                    new Token(TokenType.IDENTIFIER, "y", new TokenPosition(1, 16)),
                    new Token(TokenType.NOT_EQ, "!=", new TokenPosition(1, 18)),
                    new Token(TokenType.INT, "0", new TokenPosition(1, 21)),
                    new Token(TokenType.RPAREN, ")", new TokenPosition(1, 22)),
                    new Token(TokenType.LBRACE, "{", new TokenPosition(1, 24)),
                    new Token(TokenType.LET, "let", new TokenPosition(1, 26)),
                    new Token(TokenType.IDENTIFIER, "z", new TokenPosition(1, 30)),
                    new Token(TokenType.ASSIGN, "=", new TokenPosition(1, 32)),
                    new Token(TokenType.IDENTIFIER, "x", new TokenPosition(1, 34)),
                    new Token(TokenType.SLASH, "/", new TokenPosition(1, 36)),
                    new Token(TokenType.IDENTIFIER, "y", new TokenPosition(1, 38)),
                    new Token(TokenType.SEMICOLON, ";", new TokenPosition(1, 39)),
                    new Token(TokenType.RBRACE, "}", new TokenPosition(1, 41)));

            List<Token> actualTokens = new ArrayList<>();
            Token token;
            while ((token = lexer.nextToken()).type() != TokenType.EOF) {
                actualTokens.add(token);
            }

            assertEquals(expectedTokens, actualTokens);
        }

        @Test
        @DisplayName("Array indexing with assignment")
        void testArrayIndexingWithAssignment() {
            String input = "arr[0] += 5;";
            Lexer lexer = new Lexer(input);

            List<Token> expectedTokens = List.of(
                    new Token(TokenType.IDENTIFIER, "arr", new TokenPosition(1, 1)),
                    new Token(TokenType.LBRACKET, "[", new TokenPosition(1, 4)),
                    new Token(TokenType.INT, "0", new TokenPosition(1, 5)),
                    new Token(TokenType.RBRACKET, "]", new TokenPosition(1, 6)),
                    new Token(TokenType.PLUS_ASSIGN, "+=", new TokenPosition(1, 8)),
                    new Token(TokenType.INT, "5", new TokenPosition(1, 11)),
                    new Token(TokenType.SEMICOLON, ";", new TokenPosition(1, 12)));

            List<Token> actualTokens = new ArrayList<>();
            Token token;
            while ((token = lexer.nextToken()).type() != TokenType.EOF) {
                actualTokens.add(token);
            }

            assertEquals(expectedTokens, actualTokens);
        }
    }

    @Nested
    @DisplayName("Lexer State Management")
    class LexerStateManagement {

        @Test
        @DisplayName("Reset functionality")
        void testResetFunctionality() {
            Lexer lexer = new Lexer("let x = 5;");

            // Read first token
            Token firstToken = lexer.nextToken();
            assertEquals(TokenType.LET, firstToken.type());

            // Reset and read again
            lexer.reset();
            Token resetToken = lexer.nextToken();
            assertEquals(TokenType.LET, resetToken.type());
            assertEquals(firstToken, resetToken);
        }

        @Test
        @DisplayName("Current character access")
        void testCurrentCharacterAccess() {
            Lexer lexer = new Lexer("abc");

            // Initial state after constructor
            assertEquals('a', lexer.getCurrentChar());

            // After reading first token
            lexer.nextToken();
            // Should be at end since identifier consumed all characters
            assertEquals('\0', lexer.getCurrentChar());
        }

        @Test
        @DisplayName("EOF handling")
        void testEOFHandling() {
            Lexer lexer = new Lexer("x");

            // Read identifier
            Token token1 = lexer.nextToken();
            assertEquals(TokenType.IDENTIFIER, token1.type());

            // Read EOF
            Token token2 = lexer.nextToken();
            assertEquals(TokenType.EOF, token2.type());

            // Multiple EOF reads should work
            Token token3 = lexer.nextToken();
            assertEquals(TokenType.EOF, token3.type());
        }
    }

    @Nested
    @DisplayName("Comment Edge Cases")
    class CommentEdgeCases {

        @Test
        @DisplayName("Comment immediately followed by slash")
        void testCommentImmediatelyFollowedBySlash() {
            Lexer lexer = new Lexer("// comment\n/");

            Token token = lexer.nextToken();
            assertEquals(TokenType.SLASH, token.type());
            assertEquals("/", token.literal());
        }

        @Test
        @DisplayName("Division vs comment detection")
        void testDivisionVsCommentDetection() {
            Lexer lexer = new Lexer("x / y // comment");

            List<Token> expectedTokens = List.of(
                    new Token(TokenType.IDENTIFIER, "x", new TokenPosition(1, 1)),
                    new Token(TokenType.SLASH, "/", new TokenPosition(1, 3)),
                    new Token(TokenType.IDENTIFIER, "y", new TokenPosition(1, 5)));

            List<Token> actualTokens = new ArrayList<>();
            Token token;
            while ((token = lexer.nextToken()).type() != TokenType.EOF) {
                actualTokens.add(token);
            }

            assertEquals(expectedTokens, actualTokens);
        }

        @Test
        @DisplayName("Unterminated multi-line comment")
        void testUnterminatedMultiLineComment() {
            Lexer lexer = new Lexer("/* unterminated comment");

            // Should handle gracefully and return EOF
            Token token = lexer.nextToken();
            assertEquals(TokenType.EOF, token.type());
        }

        @Test
        @DisplayName("Multi-line comment with slash assign")
        void testMultiLineCommentWithSlashAssign() {
            Lexer lexer = new Lexer("x /* comment */ /= 5");

            List<Token> expectedTokens = List.of(
                    new Token(TokenType.IDENTIFIER, "x", new TokenPosition(1, 1)),
                    new Token(TokenType.SLASH_ASSIGN, "/=", new TokenPosition(1, 17)),
                    new Token(TokenType.INT, "5", new TokenPosition(1, 20)));

            List<Token> actualTokens = new ArrayList<>();
            Token token;
            while ((token = lexer.nextToken()).type() != TokenType.EOF) {
                actualTokens.add(token);
            }

            assertEquals(expectedTokens, actualTokens);
        }
    }
}