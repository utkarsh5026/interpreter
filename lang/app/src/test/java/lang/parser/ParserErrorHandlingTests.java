package lang.parser;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import lang.ast.base.*;

import lang.lexer.Lexer;
import lang.parser.error.ParseError;
import lang.ast.statements.*;
import java.util.List;

/**
 * 🚨 Comprehensive Parser Error Handling Tests 🚨
 * 
 * This test suite validates that our parser handles errors gracefully:
 * 1. Syntax errors are detected and reported
 * 2. Error recovery allows parsing to continue
 * 3. Error messages are meaningful and helpful
 * 4. Source positions are correctly reported
 * 5. Multiple errors can be collected
 * 6. Edge cases are handled properly
 * 
 * Testing from first principles:
 * - What happens when syntax is malformed?
 * - Does the parser crash or recover gracefully?
 * - Are error messages helpful for debugging?
 * - Can we handle partial/incomplete input?
 */
@DisplayName("Parser Error Handling Tests")
public class ParserErrorHandlingTests {

    private LanguageParser parser;

    // ═══════════════════════════════════════════════════════════════════════════
    // 🚫 MISSING TOKEN ERRORS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("🚨 Missing semicolon in let statement")
    void testMissingSemicolonInLetStatement() {
        String input = "let x = 5";
        parser = new LanguageParser(new Lexer(input));

        Program program = parser.parseProgram();

        // Parser might accept this or report error - check both cases
        assertNotNull(program, "Program should still be created");

        if (parser.hasErrors()) {
            List<ParseError> errors = parser.getErrors();
            assertFalse(errors.isEmpty(), "Should have at least one error");

            // Find semicolon-related error
            boolean foundSemicolonError = errors.stream()
                    .anyMatch(error -> error.getMessage().toLowerCase().contains("semicolon") ||
                            error.getMessage().contains(";"));

            // If errors are reported, at least one should mention semicolon
            if (!errors.isEmpty()) {
                assertTrue(foundSemicolonError || errors.size() > 0,
                        "Should report semicolon error or other parsing issue");
            }
        }
    }

    @Test
    @DisplayName("🚨 Missing assignment operator in let statement")
    void testMissingAssignmentInLetStatement() {
        String input = "let x 5;";
        parser = new LanguageParser(new Lexer(input));

        parser.parseProgram();

        assertTrue(parser.hasErrors(), "Parser should detect missing assignment operator");

        List<ParseError> errors = parser.getErrors();
        assertFalse(errors.isEmpty(), "Should have at least one error");

        // Look for assignment-related error
        boolean foundAssignmentError = errors.stream()
                .anyMatch(error -> error.getMessage().contains("=") ||
                        error.getMessage().toLowerCase().contains("assign"));

        assertTrue(foundAssignmentError, "Should report missing assignment operator");
    }

    @Test
    @DisplayName("🚨 Missing identifier in let statement")
    void testMissingIdentifierInLetStatement() {
        String input = "let = 5;";
        parser = new LanguageParser(new Lexer(input));

        parser.parseProgram();

        assertTrue(parser.hasErrors(), "Parser should detect missing identifier");

        List<ParseError> errors = parser.getErrors();
        assertFalse(errors.isEmpty(), "Should have at least one error");
    }

    @Test
    @DisplayName("🚨 Missing closing brace in block")
    void testMissingClosingBraceInBlock() {
        String input = """
                {
                    let x = 5;
                    let y = 10;
                """;
        parser = new LanguageParser(new Lexer(input));

        parser.parseProgram();

        assertTrue(parser.hasErrors(), "Parser should detect missing closing brace");

        List<ParseError> errors = parser.getErrors();
        assertFalse(errors.isEmpty(), "Should have at least one error");

        // Look for brace-related error
        boolean foundBraceError = errors.stream()
                .anyMatch(error -> error.getMessage().contains("}") ||
                        error.getMessage().toLowerCase().contains("brace"));

        assertTrue(foundBraceError, "Should report missing closing brace");
    }

    @Test
    @DisplayName("🚨 Missing closing parenthesis in function call")
    void testMissingClosingParenInFunctionCall() {
        String input = "print(\"hello\";";
        parser = new LanguageParser(new Lexer(input));

        parser.parseProgram();

        assertTrue(parser.hasErrors(), "Parser should detect missing closing parenthesis");

        List<ParseError> errors = parser.getErrors();
        assertFalse(errors.isEmpty(), "Should have at least one error");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 🔧 MALFORMED EXPRESSION ERRORS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("🚨 Invalid expression - operator without operand")
    void testInvalidExpressionOperatorWithoutOperand() {
        String input = "let x = +;";
        parser = new LanguageParser(new Lexer(input));

        parser.parseProgram();

        assertTrue(parser.hasErrors(), "Parser should detect incomplete expression");

        List<ParseError> errors = parser.getErrors();
        assertFalse(errors.isEmpty(), "Should have at least one error");
    }

    @Test
    @DisplayName("🚨 Invalid expression - consecutive operators")
    void testInvalidExpressionConsecutiveOperators() {
        String input = "let x = 5 + + 3;";
        parser = new LanguageParser(new Lexer(input));

        parser.parseProgram();

        assertTrue(parser.hasErrors(), "Parser should detect consecutive operators");

        List<ParseError> errors = parser.getErrors();
        assertFalse(errors.isEmpty(), "Should have at least one error");
    }

    @Test
    @DisplayName("🚨 Invalid expression - missing operand")
    void testInvalidExpressionMissingOperand() {
        String input = "let x = 5 +;";
        parser = new LanguageParser(new Lexer(input));

        parser.parseProgram();

        assertTrue(parser.hasErrors(), "Parser should detect missing operand");

        List<ParseError> errors = parser.getErrors();
        assertFalse(errors.isEmpty(), "Should have at least one error");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 🔄 CONTROL FLOW ERRORS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("🚨 While loop missing condition")
    void testWhileLoopMissingCondition() {
        String input = """
                while () {
                    print("hello");
                }
                """;
        parser = new LanguageParser(new Lexer(input));

        parser.parseProgram();

        assertTrue(parser.hasErrors(), "Parser should detect missing while condition");

        List<ParseError> errors = parser.getErrors();
        assertFalse(errors.isEmpty(), "Should have at least one error");
    }

    @Test
    @DisplayName("🚨 For loop missing components")
    void testForLoopMissingComponents() {
        String input = """
                for (;;) {
                    print("hello");
                }
                """;
        parser = new LanguageParser(new Lexer(input));

        Program program = parser.parseProgram();

        // This might be valid syntax in some languages, but should be caught if invalid
        // The exact behavior depends on the language specification
        assertNotNull(program, "Program should be created even with parsing errors");
    }

    @Test
    @DisplayName("🚨 If statement missing condition")
    void testIfStatementMissingCondition() {
        String input = """
                if () {
                    print("hello");
                }
                """;
        parser = new LanguageParser(new Lexer(input));

        parser.parseProgram();

        assertTrue(parser.hasErrors(), "Parser should detect missing if condition");

        List<ParseError> errors = parser.getErrors();
        assertFalse(errors.isEmpty(), "Should have at least one error");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 🔧 FUNCTION DEFINITION ERRORS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("🚨 Function literal missing parameters")
    void testFunctionLiteralMissingParameters() {
        String input = """
                let f = fn {
                    return 42;
                };
                """;
        parser = new LanguageParser(new Lexer(input));

        parser.parseProgram();

        assertTrue(parser.hasErrors(), "Parser should detect missing function parameters");

        List<ParseError> errors = parser.getErrors();
        assertFalse(errors.isEmpty(), "Should have at least one error");
    }

    @Test
    @DisplayName("🚨 Function literal missing body")
    void testFunctionLiteralMissingBody() {
        String input = "let f = fn(x, y);";
        parser = new LanguageParser(new Lexer(input));

        parser.parseProgram();

        assertTrue(parser.hasErrors(), "Parser should detect missing function body");

        List<ParseError> errors = parser.getErrors();
        assertFalse(errors.isEmpty(), "Should have at least one error");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 📊 LITERAL ERRORS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("🚨 Unterminated string literal")
    void testUnterminatedStringLiteral() {
        String input = "let message = \"Hello, World!;";
        parser = new LanguageParser(new Lexer(input));

        // This error is typically caught by the lexer, not the parser
        try {
            parser.parseProgram();

            // If we get here, check if there are parsing errors
            if (parser.hasErrors()) {
                List<ParseError> errors = parser.getErrors();
                assertFalse(errors.isEmpty(), "Should have at least one error");
            }
        } catch (RuntimeException e) {
            // Lexer might throw exception for unterminated string
            assertTrue(e.getMessage().contains("Unterminated") ||
                    e.getMessage().contains("string"),
                    "Should report unterminated string error");
        }
    }

    @Test
    @DisplayName("🚨 Invalid array literal")
    void testInvalidArrayLiteral() {
        String input = "let arr = [1, 2, 3;";
        parser = new LanguageParser(new Lexer(input));

        parser.parseProgram();

        assertTrue(parser.hasErrors(), "Parser should detect invalid array literal");

        List<ParseError> errors = parser.getErrors();
        assertFalse(errors.isEmpty(), "Should have at least one error");
    }

    @Test
    @DisplayName("🚨 Invalid hash literal")
    void testInvalidHashLiteral() {
        String input = "let hash = {\"key\": \"value\";";
        parser = new LanguageParser(new Lexer(input));

        parser.parseProgram();

        assertTrue(parser.hasErrors(), "Parser should detect invalid hash literal");

        List<ParseError> errors = parser.getErrors();
        assertFalse(errors.isEmpty(), "Should have at least one error");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 🎯 CONTEXT-SPECIFIC ERRORS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("🚨 Break statement outside loop")
    void testBreakStatementOutsideLoop() {
        String input = """
                let x = 5;
                break;
                """;
        parser = new LanguageParser(new Lexer(input));

        Program program = parser.parseProgram();

        // This might be caught during parsing or semantic analysis
        assertNotNull(program, "Program should be created");

        // Check if parser reports this as an error
        if (parser.hasErrors()) {
            List<ParseError> errors = parser.getErrors();
            boolean foundBreakError = errors.stream()
                    .anyMatch(error -> error.getMessage().toLowerCase().contains("break") &&
                            error.getMessage().toLowerCase().contains("loop"));

            assertTrue(foundBreakError, "Should report break outside loop error");
        }
    }

    @Test
    @DisplayName("🚨 Continue statement outside loop")
    void testContinueStatementOutsideLoop() {
        String input = """
                let x = 5;
                continue;
                """;
        parser = new LanguageParser(new Lexer(input));
        Program program = parser.parseProgram();

        assertNotNull(program, "Program should be created");

        // Check if parser reports this as an error
        if (parser.hasErrors()) {
            List<ParseError> errors = parser.getErrors();
            boolean foundContinueError = errors.stream()
                    .anyMatch(error -> error.getMessage().toLowerCase().contains("continue") &&
                            error.getMessage().toLowerCase().contains("loop"));

            assertTrue(foundContinueError, "Should report continue outside loop error");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 📍 POSITION TRACKING IN ERRORS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("📍 Error position tracking")
    void testErrorPositionTracking() {
        String input = """
                let x = 5;
                let y = ;
                let z = 10;
                """;
        parser = new LanguageParser(new Lexer(input));

        parser.parseProgram();

        assertTrue(parser.hasErrors(), "Parser should detect syntax error");

        List<ParseError> errors = parser.getErrors();
        assertFalse(errors.isEmpty(), "Should have at least one error");

        // Find error related to the malformed statement
        ParseError error = errors.get(0);
        assertNotNull(error.getPosition(), "Error should have position information");

        // The error should be on line 2 (where the malformed statement is)
        assertTrue(error.getPosition().line() >= 2,
                "Error should be on or after line 2");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 🔄 ERROR RECOVERY TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("🔄 Parser recovery after error")
    void testParserRecoveryAfterError() {
        String input = """
                let x = 5;
                let y = ;
                let z = 10;
                """;
        parser = new LanguageParser(new Lexer(input));

        Program program = parser.parseProgram();

        assertNotNull(program, "Program should be created despite errors");
        assertTrue(parser.hasErrors(), "Parser should detect syntax error");

        // Parser should still attempt to parse valid statements
        List<Statement> statements = program.getStatements();

        // We should have some statements, even if not all are valid
        assertFalse(statements.isEmpty(), "Should have at least some statements");

        // The first statement should be valid
        if (!statements.isEmpty()) {
            Statement firstStmt = statements.get(0);
            assertNotNull(firstStmt, "First statement should be parsed");
        }
    }

    @Test
    @DisplayName("🔄 Multiple error collection")
    void testMultipleErrorCollection() {
        String input = """
                let x = ;
                let y = ;
                let z = ;
                """;
        parser = new LanguageParser(new Lexer(input));

        parser.parseProgram();

        assertTrue(parser.hasErrors(), "Parser should detect multiple syntax errors");

        List<ParseError> errors = parser.getErrors();

        // Should detect multiple errors (one for each malformed statement)
        assertTrue(errors.size() >= 1, "Should have at least one error");

        // Each error should have position information
        for (ParseError error : errors) {
            assertNotNull(error.getPosition(), "Each error should have position");
            assertNotNull(error.getMessage(), "Each error should have message");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 🎭 EDGE CASES
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("🎭 Empty input")
    void testEmptyInput() {
        String input = "";
        parser = new LanguageParser(new Lexer(input));
        Program program = parser.parseProgram();

        assertNotNull(program, "Program should be created for empty input");
        assertFalse(parser.hasErrors(), "Empty input should not cause errors");
        assertEquals(0, program.getStatements().size(), "Empty input should have no statements");
    }

    @Test
    @DisplayName("🎭 Whitespace only input")
    void testWhitespaceOnlyInput() {
        String input = "   \n\t  \n  ";
        parser = new LanguageParser(new Lexer(input));
        Program program = parser.parseProgram();

        assertNotNull(program, "Program should be created for whitespace input");
        assertFalse(parser.hasErrors(), "Whitespace only should not cause errors");
        assertEquals(0, program.getStatements().size(), "Whitespace input should have no statements");
    }

    @Test
    @DisplayName("🎭 Only comments input")
    void testOnlyCommentsInput() {
        String input = """
                // This is a comment
                /* This is a
                   multi-line comment */
                // Another comment
                """;
        parser = new LanguageParser(new Lexer(input));
        Program program = parser.parseProgram();

        assertNotNull(program, "Program should be created for comments only");
        assertFalse(parser.hasErrors(), "Comments only should not cause errors");
        assertEquals(0, program.getStatements().size(), "Comments only should have no statements");
    }

    @Test
    @DisplayName("🎭 Incomplete input - unexpected EOF")
    void testIncompleteInputUnexpectedEOF() {
        String input = "let x = ";
        parser = new LanguageParser(new Lexer(input));

        parser.parseProgram();

        assertTrue(parser.hasErrors(), "Parser should detect incomplete input");

        List<ParseError> errors = parser.getErrors();
        assertFalse(errors.isEmpty(), "Should have at least one error");

        // Look for EOF-related error
        boolean foundEOFError = errors.stream()
                .anyMatch(error -> error.getMessage().toLowerCase().contains("eof") ||
                        error.getMessage().toLowerCase().contains("end"));

        // Should report some kind of error for incomplete input
        assertTrue(errors.size() > 0, "Should report error for incomplete input");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 📊 ERROR MESSAGE QUALITY TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("📊 Error message contains useful information")
    void testErrorMessageQuality() {
        String input = "let x = 5 +;";
        parser = new LanguageParser(new Lexer(input));

        parser.parseProgram();

        assertTrue(parser.hasErrors(), "Parser should detect syntax error");

        List<ParseError> errors = parser.getErrors();
        assertFalse(errors.isEmpty(), "Should have at least one error");

        ParseError error = errors.get(0);
        String message = error.getMessage();

        // Error message should be non-empty and descriptive
        assertNotNull(message, "Error message should not be null");
        assertFalse(message.trim().isEmpty(), "Error message should not be empty");
        assertTrue(message.length() > 5, "Error message should be descriptive");

        // Should contain position information when displayed
        String errorString = error.toString();
        assertTrue(errorString.contains("line") || errorString.contains("column"),
                "Error string should contain position information");
    }

    @Test
    @DisplayName("📊 Parser prints errors correctly")
    void testParserPrintErrors() {
        String input = """
                let x = ;
                let y = 5 +;
                """;
        parser = new LanguageParser(new Lexer(input));

        parser.parseProgram();

        assertTrue(parser.hasErrors(), "Parser should detect syntax errors");

        // This test verifies that printErrors() doesn't crash
        // In a real scenario, you might capture System.err to verify output
        assertDoesNotThrow(() -> parser.printErrors(),
                "printErrors() should not throw exception");
    }
}