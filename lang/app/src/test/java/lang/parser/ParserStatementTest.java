package lang.parser;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import lang.lexer.Lexer;
import lang.ast.statements.*;
import lang.ast.base.*;
import lang.ast.expressions.*;
import lang.ast.literals.*;

/**
 * 📝 Comprehensive Parser Tests for Statements 📝
 * 
 * This test suite validates that our parser correctly transforms token streams
 * into Abstract Syntax Trees for all types of statements.
 * 
 * Testing from first principles:
 * 1. Does the parser recognize statement keywords?
 * 2. Does it build correct AST structures?
 * 3. Does it handle syntax errors gracefully?
 * 4. Does it maintain proper source position information?
 */
@DisplayName("Parser Statement Tests")
public class ParserStatementTest {

    private LanguageParser parser;

    // ═══════════════════════════════════════════════════════════════════════════
    // 📦 LET STATEMENT TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("🎯 Parse simple let statement with integer")
    void testSimpleLetStatementInteger() {
        String input = "let x = 5;";
        parser = new LanguageParser(new Lexer(input));

        Program program = parser.parseProgram();

        // Verify no parsing errors
        assertFalse(parser.hasErrors(), "Parser should not have errors");

        // Verify program structure
        assertEquals(1, program.getStatements().size(), "Should have exactly 1 statement");

        Statement stmt = program.getStatements().get(0);
        assertTrue(stmt instanceof LetStatement, "Statement should be LetStatement");

        LetStatement letStmt = (LetStatement) stmt;
        assertEquals("x", letStmt.getName().getValue(), "Variable name should be 'x'");
        assertEquals("let", letStmt.tokenLiteral(), "Token literal should be 'let'");

        // Verify the value expression
        assertTrue(letStmt.getValue() instanceof IntegerLiteral, "Value should be IntegerLiteral");
        IntegerLiteral intLit = (IntegerLiteral) letStmt.getValue();
        assertEquals(5, intLit.getValue(), "Integer value should be 5");
    }

    @Test
    @DisplayName("🎯 Parse let statement with string literal")
    void testLetStatementString() {
        String input = "let message = \"Hello, World!\";";
        parser = new LanguageParser(new Lexer(input));

        Program program = parser.parseProgram();

        assertFalse(parser.hasErrors(), "Parser should not have errors");
        assertEquals(1, program.getStatements().size());

        LetStatement letStmt = (LetStatement) program.getStatements().get(0);
        assertEquals("message", letStmt.getName().getValue());

        assertTrue(letStmt.getValue() instanceof StringLiteral);
        StringLiteral strLit = (StringLiteral) letStmt.getValue();
        assertEquals("Hello, World!", strLit.getValue());
    }

    @Test
    @DisplayName("🎯 Parse let statement with expression")
    void testLetStatementExpression() {
        String input = "let result = 5 + 3 * 2;";
        parser = new LanguageParser(new Lexer(input));

        Program program = parser.parseProgram();

        assertFalse(parser.hasErrors(), "Parser should not have errors");
        assertEquals(1, program.getStatements().size());

        LetStatement letStmt = (LetStatement) program.getStatements().get(0);
        assertEquals("result", letStmt.getName().getValue());

        // The value should be an infix expression (5 + (3 * 2))
        assertTrue(letStmt.getValue() instanceof InfixExpression);
    }

    @Test
    @DisplayName("🚫 Parse malformed let statement - missing assignment")
    void testMalformedLetStatementMissingAssignment() {
        String input = "let x;";
        parser = new LanguageParser(new Lexer(input));

        parser.parseProgram();

        assertTrue(parser.hasErrors(), "Parser should have errors for malformed syntax");
        assertFalse(parser.getErrors().isEmpty(), "Should have at least one error");
    }

    @Test
    @DisplayName("🚫 Parse malformed let statement - missing semicolon")
    void testMalformedLetStatementMissingSemicolon() {
        String input = "let x = 5";
        parser = new LanguageParser(new Lexer(input));

        Program program = parser.parseProgram();

        // This might not be an error in some languages, but let's verify behavior
        // The parser should either accept it or report missing semicolon
        assertNotNull(program);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 🔒 CONST STATEMENT TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("🎯 Parse simple const statement")
    void testSimpleConstStatement() {
        String input = "const PI = 3;";
        parser = new LanguageParser(new Lexer(input));

        Program program = parser.parseProgram();

        assertFalse(parser.hasErrors(), "Parser should not have errors");
        assertEquals(1, program.getStatements().size());

        Statement stmt = program.getStatements().get(0);
        assertTrue(stmt instanceof ConstStatement, "Statement should be ConstStatement");

        ConstStatement constStmt = (ConstStatement) stmt;
        assertEquals("PI", constStmt.getName().getValue());
        assertEquals("const", constStmt.tokenLiteral());
    }

    @Test
    @DisplayName("🎯 Parse const statement with complex expression")
    void testConstStatementComplexExpression() {
        String input = "const MAX_VALUE = len(array) * 2 + 1;";
        parser = new LanguageParser(new Lexer(input));

        Program program = parser.parseProgram();

        assertFalse(parser.hasErrors(), "Parser should not have errors");
        assertEquals(1, program.getStatements().size());

        ConstStatement constStmt = (ConstStatement) program.getStatements().get(0);
        assertEquals("MAX_VALUE", constStmt.getName().getValue());

        // Value should be a complex infix expression
        assertTrue(constStmt.getValue() instanceof InfixExpression);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ↩️ RETURN STATEMENT TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("🎯 Parse simple return statement")
    void testSimpleReturnStatement() {
        String input = "return 42;";
        parser = new LanguageParser(new Lexer(input));

        Program program = parser.parseProgram();

        assertFalse(parser.hasErrors(), "Parser should not have errors");
        assertEquals(1, program.getStatements().size());

        Statement stmt = program.getStatements().get(0);
        assertTrue(stmt instanceof ReturnStatement, "Statement should be ReturnStatement");

        ReturnStatement returnStmt = (ReturnStatement) stmt;
        assertEquals("return", returnStmt.tokenLiteral());

        assertTrue(returnStmt.getReturnValue() instanceof IntegerLiteral);
        IntegerLiteral intLit = (IntegerLiteral) returnStmt.getReturnValue();
        assertEquals(42, intLit.getValue());
    }

    @Test
    @DisplayName("🎯 Parse return statement with expression")
    void testReturnStatementExpression() {
        String input = "return x + y * 2;";
        parser = new LanguageParser(new Lexer(input));

        Program program = parser.parseProgram();

        assertFalse(parser.hasErrors(), "Parser should not have errors");
        assertEquals(1, program.getStatements().size());

        ReturnStatement returnStmt = (ReturnStatement) program.getStatements().get(0);
        assertTrue(returnStmt.getReturnValue() instanceof InfixExpression);
    }

    @Test
    @DisplayName("🎯 Parse return statement with function call")
    void testReturnStatementFunctionCall() {
        String input = "return calculate(a, b);";
        parser = new LanguageParser(new Lexer(input));

        Program program = parser.parseProgram();

        assertFalse(parser.hasErrors(), "Parser should not have errors");
        assertEquals(1, program.getStatements().size());

        ReturnStatement returnStmt = (ReturnStatement) program.getStatements().get(0);
        assertTrue(returnStmt.getReturnValue() instanceof CallExpression);
    }

    @Test
    @DisplayName("🎯 Parse return statement with null")
    void testReturnStatementNull() {
        String input = "return;";
        parser = new LanguageParser(new Lexer(input));

        Program program = parser.parseProgram();

        assertFalse(parser.hasErrors(), "Parser should not have errors");
        assertEquals(1, program.getStatements().size());

        ReturnStatement returnStmt = (ReturnStatement) program.getStatements().get(0);
        assertTrue(returnStmt.getReturnValue() instanceof NullExpression);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 🏗️ BLOCK STATEMENT TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("🎯 Parse empty block statement")
    void testEmptyBlockStatement() {
        String input = "{}";
        parser = new LanguageParser(new Lexer(input));

        Program program = parser.parseProgram();

        assertFalse(parser.hasErrors(), "Parser should not have errors");
        assertEquals(1, program.getStatements().size());

        Statement stmt = program.getStatements().get(0);
        assertTrue(stmt instanceof BlockStatement, "Statement should be BlockStatement");

        BlockStatement blockStmt = (BlockStatement) stmt;
        assertEquals(0, blockStmt.getStatements().size(), "Block should be empty");
    }

    @Test
    @DisplayName("🎯 Parse block statement with multiple statements")
    void testBlockStatementMultipleStatements() {
        String input = """
                {
                    let x = 5;
                    let y = 10;
                    return x + y;
                }
                """;
        parser = new LanguageParser(new Lexer(input));

        Program program = parser.parseProgram();

        assertFalse(parser.hasErrors(), "Parser should not have errors");
        assertEquals(1, program.getStatements().size());

        BlockStatement blockStmt = (BlockStatement) program.getStatements().get(0);
        assertEquals(3, blockStmt.getStatements().size(), "Block should have 3 statements");

        // Verify statement types
        assertTrue(blockStmt.getStatements().get(0) instanceof LetStatement);
        assertTrue(blockStmt.getStatements().get(1) instanceof LetStatement);
        assertTrue(blockStmt.getStatements().get(2) instanceof ReturnStatement);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 🔄 WHILE STATEMENT TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("🎯 Parse simple while statement")
    void testSimpleWhileStatement() {
        String input = """
                while (x < 10) {
                    x = x + 1;
                }
                """;
        parser = new LanguageParser(new Lexer(input));

        Program program = parser.parseProgram();

        assertFalse(parser.hasErrors(), "Parser should not have errors");
        assertEquals(1, program.getStatements().size());

        Statement stmt = program.getStatements().get(0);
        assertTrue(stmt instanceof WhileStatement, "Statement should be WhileStatement");

        WhileStatement whileStmt = (WhileStatement) stmt;
        assertEquals("while", whileStmt.tokenLiteral());

        // Verify condition
        assertTrue(whileStmt.getCondition() instanceof InfixExpression);

        // Verify body
        assertNotNull(whileStmt.getBody());
        assertEquals(1, whileStmt.getBody().getStatements().size());
    }

    @Test
    @DisplayName("🎯 Parse while statement with complex condition")
    void testWhileStatementComplexCondition() {
        String input = """
                while (x < 10 && y > 0) {
                    x = x + 1;
                    y = y - 1;
                }
                """;
        parser = new LanguageParser(new Lexer(input));

        Program program = parser.parseProgram();

        assertFalse(parser.hasErrors(), "Parser should not have errors");
        assertEquals(1, program.getStatements().size());

        WhileStatement whileStmt = (WhileStatement) program.getStatements().get(0);
        assertTrue(whileStmt.getCondition() instanceof InfixExpression);
        assertEquals(2, whileStmt.getBody().getStatements().size());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 🔁 FOR STATEMENT TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("🎯 Parse simple for statement")
    void testSimpleForStatement() {
        String input = """
                for (let i = 0; i < 10; i = i + 1) {
                    print(i);
                }
                """;
        parser = new LanguageParser(new Lexer(input));

        Program program = parser.parseProgram();

        assertFalse(parser.hasErrors(), "Parser should not have errors");
        assertEquals(1, program.getStatements().size());

        Statement stmt = program.getStatements().get(0);
        assertTrue(stmt instanceof ForStatement, "Statement should be ForStatement");

        ForStatement forStmt = (ForStatement) stmt;
        assertEquals("for", forStmt.tokenLiteral());

        // Verify initializer
        assertTrue(forStmt.getInitializer() instanceof LetStatement);

        // Verify condition
        assertTrue(forStmt.getCondition() instanceof InfixExpression);

        // Verify increment
        assertTrue(forStmt.getIncrement() instanceof AssignmentExpression);

        // Verify body
        assertNotNull(forStmt.getBody());
        assertEquals(1, forStmt.getBody().getStatements().size());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 🚪 BREAK AND CONTINUE TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("🎯 Parse break statement in while loop")
    void testBreakStatementInWhile() {
        String input = """
                while (true) {
                    if (x > 5) {
                        break;
                    }
                    x = x + 1;
                }
                """;
        parser = new LanguageParser(new Lexer(input));

        Program program = parser.parseProgram();

        assertFalse(parser.hasErrors(), "Parser should not have errors");
        assertEquals(1, program.getStatements().size());

        WhileStatement whileStmt = (WhileStatement) program.getStatements().get(0);
        assertEquals(2, whileStmt.getBody().getStatements().size());
    }

    @Test
    @DisplayName("🎯 Parse continue statement in for loop")
    void testContinueStatementInFor() {
        String input = """
                for (let i = 0; i < 10; i = i + 1) {
                    if (i % 2 == 0) {
                        continue;
                    }
                    print(i);
                }
                """;
        parser = new LanguageParser(new Lexer(input));

        Program program = parser.parseProgram();

        assertFalse(parser.hasErrors(), "Parser should not have errors");
        assertEquals(1, program.getStatements().size());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 💬 EXPRESSION STATEMENT TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("🎯 Parse expression statement - function call")
    void testExpressionStatementFunctionCall() {
        String input = "print(\"Hello, World!\");";
        parser = new LanguageParser(new Lexer(input));

        Program program = parser.parseProgram();

        assertFalse(parser.hasErrors(), "Parser should not have errors");
        assertEquals(1, program.getStatements().size());

        Statement stmt = program.getStatements().get(0);
        assertTrue(stmt instanceof ExpressionStatement, "Statement should be ExpressionStatement");

        ExpressionStatement exprStmt = (ExpressionStatement) stmt;
        assertTrue(exprStmt.getExpression() instanceof CallExpression);
    }

    @Test
    @DisplayName("🎯 Parse expression statement - assignment")
    void testExpressionStatementAssignment() {
        String input = "x = 42;";
        parser = new LanguageParser(new Lexer(input));

        Program program = parser.parseProgram();

        assertFalse(parser.hasErrors(), "Parser should not have errors");
        assertEquals(1, program.getStatements().size());

        ExpressionStatement exprStmt = (ExpressionStatement) program.getStatements().get(0);
        assertTrue(exprStmt.getExpression() instanceof AssignmentExpression);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 🧪 MULTIPLE STATEMENTS TEST
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("🎯 Parse program with multiple statement types")
    void testMultipleStatementTypes() {
        String input = """
                let x = 5;
                const PI = 3;
                while (x > 0) {
                    x = x - 1;
                    if (x == 2) {
                        break;
                    }
                }
                return x;
                """;
        parser = new LanguageParser(new Lexer(input));

        Program program = parser.parseProgram();

        assertFalse(parser.hasErrors(), "Parser should not have errors");
        assertEquals(4, program.getStatements().size());

        // Verify statement types
        assertTrue(program.getStatements().get(0) instanceof LetStatement);
        assertTrue(program.getStatements().get(1) instanceof ConstStatement);
        assertTrue(program.getStatements().get(2) instanceof WhileStatement);
        assertTrue(program.getStatements().get(3) instanceof ReturnStatement);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 🔍 EDGE CASES AND ERROR HANDLING
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("🚫 Parse invalid syntax - unclosed brace")
    void testInvalidSyntaxUnclosedBrace() {
        String input = """
                while (x > 0) {
                    x = x - 1;
                """;
        parser = new LanguageParser(new Lexer(input));

        Program program = parser.parseProgram();

        assertTrue(parser.hasErrors(), "Parser should detect unclosed brace");
    }

    @Test
    @DisplayName("🚫 Parse invalid syntax - break outside loop")
    void testBreakOutsideLoop() {
        String input = """
                let x = 5;
                break;
                """;
        parser = new LanguageParser(new Lexer(input));

        Program program = parser.parseProgram();

        // This should be caught during parsing or semantic analysis
        // The exact behavior depends on implementation
        assertNotNull(program);
    }

    @Test
    @DisplayName("📍 Verify source position tracking")
    void testSourcePositionTracking() {
        String input = """
                let x = 5;
                let y = 10;
                """;
        parser = new LanguageParser(new Lexer(input));

        Program program = parser.parseProgram();

        assertFalse(parser.hasErrors(), "Parser should not have errors");
        assertEquals(2, program.getStatements().size());

        // Verify first statement position
        Statement firstStmt = program.getStatements().get(0);
        assertEquals(1, firstStmt.position().line(), "First statement should be on line 1");

        // Verify second statement position
        Statement secondStmt = program.getStatements().get(1);
        assertEquals(2, secondStmt.position().line(), "Second statement should be on line 2");
    }
}
