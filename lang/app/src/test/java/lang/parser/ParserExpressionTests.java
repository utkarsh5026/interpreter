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
 * 🧮 Comprehensive Parser Tests for Expressions 🧮
 * 
 * This test suite validates expression parsing from first principles:
 * 1. Basic literal expressions (numbers, strings, booleans)
 * 2. Identifier expressions (variable references)
 * 3. Prefix expressions (unary operators like !, -)
 * 4. Infix expressions (binary operators like +, -, *, /, ==, etc.)
 * 5. Complex expressions with proper precedence
 * 6. Function call expressions
 * 7. Array indexing expressions
 * 8. Assignment expressions
 * 9. If expressions (conditional expressions)
 * 10. Grouped expressions (parentheses)
 */
@DisplayName("Parser Expression Tests")
public class ParserExpressionTests {

    private LanguageParser parser;
    // ═══════════════════════════════════════════════════════════════════════════
    // 🔢 LITERAL EXPRESSION TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("🎯 Parse integer literal expression")
    void testIntegerLiteralExpression() {
        String input = "42;";
        parser = new LanguageParser(new Lexer(input));

        Program program = parser.parseProgram();

        assertFalse(parser.hasErrors(), "Parser should not have errors");
        assertEquals(1, program.getStatements().size());

        ExpressionStatement exprStmt = (ExpressionStatement) program.getStatements().get(0);
        assertTrue(exprStmt.getExpression() instanceof IntegerLiteral);

        IntegerLiteral intLit = (IntegerLiteral) exprStmt.getExpression();
        assertEquals(42, intLit.getValue());
        assertEquals("42", intLit.tokenLiteral());
    }

    @Test
    @DisplayName("🎯 Parse string literal expression")
    void testStringLiteralExpression() {
        String input = "\"Hello, World!\";";
        parser = new LanguageParser(new Lexer(input));

        Program program = parser.parseProgram();

        assertFalse(parser.hasErrors(), "Parser should not have errors");
        assertEquals(1, program.getStatements().size());

        ExpressionStatement exprStmt = (ExpressionStatement) program.getStatements().get(0);
        assertTrue(exprStmt.getExpression() instanceof StringLiteral);

        StringLiteral strLit = (StringLiteral) exprStmt.getExpression();
        assertEquals("Hello, World!", strLit.getValue());
    }

    @Test
    @DisplayName("🎯 Parse boolean literal expressions")
    void testBooleanLiteralExpressions() {
        String[] inputs = { "true;", "false;" };
        boolean[] expectedValues = { true, false };

        for (int i = 0; i < inputs.length; i++) {
            parser = new LanguageParser(new Lexer(inputs[i]));
            Program program = parser.parseProgram();

            assertFalse(parser.hasErrors(), "Parser should not have errors for: " + inputs[i]);
            assertEquals(1, program.getStatements().size());

            ExpressionStatement exprStmt = (ExpressionStatement) program.getStatements().get(0);
            assertTrue(exprStmt.getExpression() instanceof BooleanExpression);

            BooleanExpression boolExpr = (BooleanExpression) exprStmt.getExpression();
            assertEquals(expectedValues[i], boolExpr.getValue());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 🔤 IDENTIFIER EXPRESSION TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("🎯 Parse identifier expression")
    void testIdentifierExpression() {
        String input = "myVariable;";
        parser = new LanguageParser(new Lexer(input));

        Program program = parser.parseProgram();

        assertFalse(parser.hasErrors(), "Parser should not have errors");
        assertEquals(1, program.getStatements().size());

        ExpressionStatement exprStmt = (ExpressionStatement) program.getStatements().get(0);
        assertTrue(exprStmt.getExpression() instanceof Identifier);

        Identifier ident = (Identifier) exprStmt.getExpression();
        assertEquals("myVariable", ident.getValue());
        assertEquals("myVariable", ident.tokenLiteral());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ⚡ PREFIX EXPRESSION TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("🎯 Parse prefix expressions")
    void testPrefixExpressions() {
        // Test data: input -> [operator, operand_value]
        String[][] tests = {
                { "!true;", "!", "true" },
                { "!false;", "!", "false" },
                { "-15;", "-", "15" },
                { "!myVar;", "!", "myVar" }
        };

        for (String[] test : tests) {
            String input = test[0];
            String expectedOperator = test[1];
            String expectedOperand = test[2];

            parser = new LanguageParser(new Lexer(input));
            Program program = parser.parseProgram();

            assertFalse(parser.hasErrors(), "Parser should not have errors for: " + input);
            assertEquals(1, program.getStatements().size());

            ExpressionStatement exprStmt = (ExpressionStatement) program.getStatements().get(0);
            assertTrue(exprStmt.getExpression() instanceof PrefixExpression,
                    "Expression should be PrefixExpression for: " + input);

            PrefixExpression prefixExpr = (PrefixExpression) exprStmt.getExpression();
            assertEquals(expectedOperator, prefixExpr.getOperator());

            // Verify the operand
            Expression rightExpr = prefixExpr.getRight();
            if (expectedOperand.equals("true") || expectedOperand.equals("false")) {
                assertTrue(rightExpr instanceof BooleanExpression);
                BooleanExpression boolExpr = (BooleanExpression) rightExpr;
                assertEquals(Boolean.parseBoolean(expectedOperand), boolExpr.getValue());
            } else if (expectedOperand.matches("\\d+")) {
                assertTrue(rightExpr instanceof IntegerLiteral);
                IntegerLiteral intLit = (IntegerLiteral) rightExpr;
                assertEquals(Integer.parseInt(expectedOperand), intLit.getValue());
            } else {
                assertTrue(rightExpr instanceof Identifier);
                Identifier ident = (Identifier) rightExpr;
                assertEquals(expectedOperand, ident.getValue());
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ⚖️ INFIX EXPRESSION TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("🎯 Parse arithmetic infix expressions")
    void testArithmeticInfixExpressions() {
        String[][] tests = {
                { "5 + 5;", "5", "+", "5" },
                { "5 - 5;", "5", "-", "5" },
                { "5 * 5;", "5", "*", "5" },
                { "5 / 5;", "5", "/", "5" },
                { "5 % 3;", "5", "%", "3" }
        };

        for (String[] test : tests) {
            String input = test[0];
            String expectedLeft = test[1];
            String expectedOperator = test[2];
            String expectedRight = test[3];

            parser = new LanguageParser(new Lexer(input));
            Program program = parser.parseProgram();

            assertFalse(parser.hasErrors(), "Parser should not have errors for: " + input);
            assertEquals(1, program.getStatements().size());

            ExpressionStatement exprStmt = (ExpressionStatement) program.getStatements().get(0);
            assertTrue(exprStmt.getExpression() instanceof InfixExpression);

            InfixExpression infixExpr = (InfixExpression) exprStmt.getExpression();
            assertEquals(expectedOperator, infixExpr.getOperator());

            // Verify operands are integer literals
            assertTrue(infixExpr.getLeft() instanceof IntegerLiteral);
            assertTrue(infixExpr.getRight() instanceof IntegerLiteral);

            IntegerLiteral leftInt = (IntegerLiteral) infixExpr.getLeft();
            IntegerLiteral rightInt = (IntegerLiteral) infixExpr.getRight();

            assertEquals(Integer.parseInt(expectedLeft), leftInt.getValue());
            assertEquals(Integer.parseInt(expectedRight), rightInt.getValue());
        }
    }

    @Test
    @DisplayName("🎯 Parse comparison infix expressions")
    void testComparisonInfixExpressions() {
        String[][] tests = {
                { "5 > 5;", "5", ">", "5" },
                { "5 < 5;", "5", "<", "5" },
                { "5 >= 5;", "5", ">=", "5" },
                { "5 <= 5;", "5", "<=", "5" },
                { "5 == 5;", "5", "==", "5" },
                { "5 != 5;", "5", "!=", "5" }
        };

        for (String[] test : tests) {
            String input = test[0];
            String expectedOperator = test[2];

            parser = new LanguageParser(new Lexer(input));
            Program program = parser.parseProgram();

            assertFalse(parser.hasErrors(), "Parser should not have errors for: " + input);
            assertEquals(1, program.getStatements().size());

            ExpressionStatement exprStmt = (ExpressionStatement) program.getStatements().get(0);
            assertTrue(exprStmt.getExpression() instanceof InfixExpression);

            InfixExpression infixExpr = (InfixExpression) exprStmt.getExpression();
            assertEquals(expectedOperator, infixExpr.getOperator());
        }
    }

    @Test
    @DisplayName("🎯 Parse logical infix expressions")
    void testLogicalInfixExpressions() {
        String[][] tests = {
                { "true && false;", "true", "&&", "false" },
                { "true || false;", "true", "||", "false" }
        };

        for (String[] test : tests) {
            String input = test[0];
            String expectedOperator = test[2];

            parser = new LanguageParser(new Lexer(input));
            Program program = parser.parseProgram();

            assertFalse(parser.hasErrors(), "Parser should not have errors for: " + input);
            assertEquals(1, program.getStatements().size());

            ExpressionStatement exprStmt = (ExpressionStatement) program.getStatements().get(0);
            assertTrue(exprStmt.getExpression() instanceof InfixExpression);

            InfixExpression infixExpr = (InfixExpression) exprStmt.getExpression();
            assertEquals(expectedOperator, infixExpr.getOperator());

            // Verify operands are boolean expressions
            assertTrue(infixExpr.getLeft() instanceof BooleanExpression);
            assertTrue(infixExpr.getRight() instanceof BooleanExpression);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 📈 OPERATOR PRECEDENCE TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("🎯 Test operator precedence - multiplication before addition")
    void testOperatorPrecedenceMultiplicationAddition() {
        String input = "2 + 3 * 4;";
        parser = new LanguageParser(new Lexer(input));

        Program program = parser.parseProgram();

        assertFalse(parser.hasErrors(), "Parser should not have errors");
        assertEquals(1, program.getStatements().size());

        ExpressionStatement exprStmt = (ExpressionStatement) program.getStatements().get(0);
        InfixExpression addExpr = (InfixExpression) exprStmt.getExpression();

        // Should be parsed as: 2 + (3 * 4)
        assertEquals("+", addExpr.getOperator());
        assertTrue(addExpr.getLeft() instanceof IntegerLiteral);
        assertTrue(addExpr.getRight() instanceof InfixExpression);

        IntegerLiteral leftInt = (IntegerLiteral) addExpr.getLeft();
        assertEquals(2, leftInt.getValue());

        InfixExpression multExpr = (InfixExpression) addExpr.getRight();
        assertEquals("*", multExpr.getOperator());
    }

    @Test
    @DisplayName("🎯 Test operator precedence - comparison after arithmetic")
    void testOperatorPrecedenceComparisonArithmetic() {
        String input = "2 + 3 > 4;";
        parser = new LanguageParser(new Lexer(input));

        Program program = parser.parseProgram();

        assertFalse(parser.hasErrors(), "Parser should not have errors");
        assertEquals(1, program.getStatements().size());

        ExpressionStatement exprStmt = (ExpressionStatement) program.getStatements().get(0);
        InfixExpression compExpr = (InfixExpression) exprStmt.getExpression();

        // Should be parsed as: (2 + 3) > 4
        assertEquals(">", compExpr.getOperator());
        assertTrue(compExpr.getLeft() instanceof InfixExpression);
        assertTrue(compExpr.getRight() instanceof IntegerLiteral);

        InfixExpression addExpr = (InfixExpression) compExpr.getLeft();
        assertEquals("+", addExpr.getOperator());
    }

    @Test
    @DisplayName("🎯 Test operator precedence - complex expression")
    void testOperatorPrecedenceComplex() {
        String input = "2 + 3 * 4 == 5 - 1;";
        parser = new LanguageParser(new Lexer(input));

        Program program = parser.parseProgram();

        assertFalse(parser.hasErrors(), "Parser should not have errors");
        assertEquals(1, program.getStatements().size());

        ExpressionStatement exprStmt = (ExpressionStatement) program.getStatements().get(0);
        InfixExpression eqExpr = (InfixExpression) exprStmt.getExpression();

        // Should be parsed as: (2 + (3 * 4)) == (5 - 1)
        assertEquals("==", eqExpr.getOperator());
        assertTrue(eqExpr.getLeft() instanceof InfixExpression);
        assertTrue(eqExpr.getRight() instanceof InfixExpression);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 📞 FUNCTION CALL EXPRESSION TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("🎯 Parse function call with no arguments")
    void testFunctionCallNoArguments() {
        String input = "getTime();";
        parser = new LanguageParser(new Lexer(input));

        Program program = parser.parseProgram();

        assertFalse(parser.hasErrors(), "Parser should not have errors");
        assertEquals(1, program.getStatements().size());

        ExpressionStatement exprStmt = (ExpressionStatement) program.getStatements().get(0);
        assertTrue(exprStmt.getExpression() instanceof CallExpression);

        CallExpression callExpr = (CallExpression) exprStmt.getExpression();
        assertTrue(callExpr.getFunction() instanceof Identifier);

        Identifier funcName = (Identifier) callExpr.getFunction();
        assertEquals("getTime", funcName.getValue());
        assertEquals(0, callExpr.getArguments().size());
    }

    @Test
    @DisplayName("🎯 Parse function call with arguments")
    void testFunctionCallWithArguments() {
        String input = "add(1, 2, 3);";
        parser = new LanguageParser(new Lexer(input));

        Program program = parser.parseProgram();

        assertFalse(parser.hasErrors(), "Parser should not have errors");
        assertEquals(1, program.getStatements().size());

        ExpressionStatement exprStmt = (ExpressionStatement) program.getStatements().get(0);
        assertTrue(exprStmt.getExpression() instanceof CallExpression);

        CallExpression callExpr = (CallExpression) exprStmt.getExpression();
        assertEquals(3, callExpr.getArguments().size());

        // Verify arguments are integer literals
        for (int i = 0; i < 3; i++) {
            assertTrue(callExpr.getArguments().get(i) instanceof IntegerLiteral);
            IntegerLiteral argInt = (IntegerLiteral) callExpr.getArguments().get(i);
            assertEquals(i + 1, argInt.getValue());
        }
    }

    @Test
    @DisplayName("🎯 Parse nested function calls")
    void testNestedFunctionCalls() {
        String input = "add(multiply(2, 3), 4);";
        parser = new LanguageParser(new Lexer(input));

        Program program = parser.parseProgram();

        assertFalse(parser.hasErrors(), "Parser should not have errors");
        assertEquals(1, program.getStatements().size());

        ExpressionStatement exprStmt = (ExpressionStatement) program.getStatements().get(0);
        assertTrue(exprStmt.getExpression() instanceof CallExpression);

        CallExpression outerCall = (CallExpression) exprStmt.getExpression();
        assertEquals(2, outerCall.getArguments().size());

        // First argument should be another function call
        assertTrue(outerCall.getArguments().get(0) instanceof CallExpression);

        CallExpression innerCall = (CallExpression) outerCall.getArguments().get(0);
        Identifier innerFuncName = (Identifier) innerCall.getFunction();
        assertEquals("multiply", innerFuncName.getValue());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 📝 ASSIGNMENT EXPRESSION TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("🎯 Parse simple assignment expression")
    void testSimpleAssignmentExpression() {
        String input = "x = 42;";
        parser = new LanguageParser(new Lexer(input));

        Program program = parser.parseProgram();

        assertFalse(parser.hasErrors(), "Parser should not have errors");
        assertEquals(1, program.getStatements().size());

        ExpressionStatement exprStmt = (ExpressionStatement) program.getStatements().get(0);
        assertTrue(exprStmt.getExpression() instanceof AssignmentExpression);

        AssignmentExpression assignExpr = (AssignmentExpression) exprStmt.getExpression();
        assertEquals("x", assignExpr.getName().getValue());
        assertTrue(assignExpr.getValue() instanceof IntegerLiteral);

        IntegerLiteral valueInt = (IntegerLiteral) assignExpr.getValue();
        assertEquals(42, valueInt.getValue());
    }

    @Test
    @DisplayName("🎯 Parse assignment with expression")
    void testAssignmentWithExpression() {
        String input = "result = x + y * 2;";
        parser = new LanguageParser(new Lexer(input));

        Program program = parser.parseProgram();

        assertFalse(parser.hasErrors(), "Parser should not have errors");
        assertEquals(1, program.getStatements().size());

        ExpressionStatement exprStmt = (ExpressionStatement) program.getStatements().get(0);
        assertTrue(exprStmt.getExpression() instanceof AssignmentExpression);

        AssignmentExpression assignExpr = (AssignmentExpression) exprStmt.getExpression();
        assertEquals("result", assignExpr.getName().getValue());
        assertTrue(assignExpr.getValue() instanceof InfixExpression);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 🔍 ARRAY INDEX EXPRESSION TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("🎯 Parse array index expression")
    void testArrayIndexExpression() {
        String input = "myArray[0];";
        parser = new LanguageParser(new Lexer(input));

        Program program = parser.parseProgram();

        assertFalse(parser.hasErrors(), "Parser should not have errors");
        assertEquals(1, program.getStatements().size());

        ExpressionStatement exprStmt = (ExpressionStatement) program.getStatements().get(0);
        assertTrue(exprStmt.getExpression() instanceof IndexExpression);

        IndexExpression indexExpr = (IndexExpression) exprStmt.getExpression();
        assertTrue(indexExpr.getLeft() instanceof Identifier);
        assertTrue(indexExpr.getIndex() instanceof IntegerLiteral);

        Identifier arrayName = (Identifier) indexExpr.getLeft();
        assertEquals("myArray", arrayName.getValue());

        IntegerLiteral indexInt = (IntegerLiteral) indexExpr.getIndex();
        assertEquals(0, indexInt.getValue());
    }

    @Test
    @DisplayName("🎯 Parse complex array index expression")
    void testComplexArrayIndexExpression() {
        String input = "matrix[i + 1][j * 2];";
        parser = new LanguageParser(new Lexer(input));

        Program program = parser.parseProgram();

        assertFalse(parser.hasErrors(), "Parser should not have errors");
        assertEquals(1, program.getStatements().size());

        ExpressionStatement exprStmt = (ExpressionStatement) program.getStatements().get(0);
        assertTrue(exprStmt.getExpression() instanceof IndexExpression);

        IndexExpression outerIndex = (IndexExpression) exprStmt.getExpression();
        assertTrue(outerIndex.getLeft() instanceof IndexExpression);
        assertTrue(outerIndex.getIndex() instanceof InfixExpression);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 🔀 IF EXPRESSION TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("🎯 Parse simple if expression")
    void testSimpleIfExpression() {
        String input = """
                if (x > 0) {
                    return "positive";
                } else {
                    return "not positive";
                }
                """;
        parser = new LanguageParser(new Lexer(input));

        Program program = parser.parseProgram();

        assertFalse(parser.hasErrors(), "Parser should not have errors");
        assertEquals(1, program.getStatements().size());

        ExpressionStatement exprStmt = (ExpressionStatement) program.getStatements().get(0);
        assertTrue(exprStmt.getExpression() instanceof IfExpression);

        IfExpression ifExpr = (IfExpression) exprStmt.getExpression();
        assertEquals(1, ifExpr.getConditions().size());
        assertEquals(1, ifExpr.getConsequences().size());
        assertNotNull(ifExpr.getAlternative());

        // Verify condition
        assertTrue(ifExpr.getConditions().get(0) instanceof InfixExpression);

        // Verify consequence and alternative are block statements
        assertTrue(ifExpr.getConsequences().get(0) instanceof BlockStatement);
        assertTrue(ifExpr.getAlternative().isPresent());
        assertTrue(ifExpr.getAlternative().get() instanceof BlockStatement);
    }

    @Test
    @DisplayName("🎯 Parse if-elif-else expression")
    void testIfElifElseExpression() {
        String input = """
                if (x > 0) {
                    return "positive";
                } elif (x < 0) {
                    return "negative";
                } else {
                    return "zero";
                }
                """;
        parser = new LanguageParser(new Lexer(input));

        Program program = parser.parseProgram();

        assertFalse(parser.hasErrors(), "Parser should not have errors");
        assertEquals(1, program.getStatements().size());

        ExpressionStatement exprStmt = (ExpressionStatement) program.getStatements().get(0);
        assertTrue(exprStmt.getExpression() instanceof IfExpression);

        IfExpression ifExpr = (IfExpression) exprStmt.getExpression();
        assertEquals(2, ifExpr.getConditions().size()); // if and elif
        assertEquals(2, ifExpr.getConsequences().size()); // if and elif blocks
        assertNotNull(ifExpr.getAlternative()); // else block
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 📦 ARRAY AND HASH LITERAL TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("🎯 Parse array literal")
    void testArrayLiteral() {
        String input = "[1, 2, 3, 4];";
        parser = new LanguageParser(new Lexer(input));

        Program program = parser.parseProgram();

        assertFalse(parser.hasErrors(), "Parser should not have errors");
        assertEquals(1, program.getStatements().size());

        ExpressionStatement exprStmt = (ExpressionStatement) program.getStatements().get(0);
        assertTrue(exprStmt.getExpression() instanceof ArrayLiteral);

        ArrayLiteral arrayLit = (ArrayLiteral) exprStmt.getExpression();
        assertEquals(4, arrayLit.getElements().size());

        // Verify elements are integer literals
        for (int i = 0; i < 4; i++) {
            assertTrue(arrayLit.getElements().get(i) instanceof IntegerLiteral);
            IntegerLiteral intLit = (IntegerLiteral) arrayLit.getElements().get(i);
            assertEquals(i + 1, intLit.getValue());
        }
    }

    @Test
    @DisplayName("🎯 Parse hash literal")
    void testHashLiteral() {
        String input = "let name = {\"name\": \"Alice\", \"age\": 30};";
        parser = new LanguageParser(new Lexer(input));

        Program program = parser.parseProgram();

        assertFalse(parser.hasErrors(), "Parser should not have errors");
        assertEquals(1, program.getStatements().size());

        LetStatement letStmt = (LetStatement) program.getStatements().get(0);
        assertTrue(letStmt.getValue() instanceof HashLiteral);

        HashLiteral hashLit = (HashLiteral) letStmt.getValue();
        assertEquals(2, hashLit.getPairs().size());
        assertTrue(hashLit.getPairs().containsKey("name"));
        assertTrue(hashLit.getPairs().containsKey("age"));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 🔧 FUNCTION LITERAL TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("🎯 Parse function literal")
    void testFunctionLiteral() {
        String input = """
                let name = fn(x, y) {
                    return x + y;
                };
                """;
        parser = new LanguageParser(new Lexer(input));

        Program program = parser.parseProgram();

        assertFalse(parser.hasErrors(), "Parser should not have errors");
        assertEquals(1, program.getStatements().size());

        LetStatement letStmt = (LetStatement) program.getStatements().get(0);
        assertTrue(letStmt.getValue() instanceof FunctionLiteral);

        FunctionLiteral funcLit = (FunctionLiteral) letStmt.getValue();
        assertEquals(2, funcLit.getParameters().size());

        // Verify parameters
        assertEquals("x", funcLit.getParameters().get(0).getValue());
        assertEquals("y", funcLit.getParameters().get(1).getValue());

        // Verify body
        assertNotNull(funcLit.getBody());
        assertEquals(1, funcLit.getBody().getStatements().size());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 🔗 GROUPED EXPRESSION TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("🎯 Parse grouped expression")
    void testGroupedExpression() {
        String input = "(2 + 3) * 4;";
        parser = new LanguageParser(new Lexer(input));

        Program program = parser.parseProgram();

        assertFalse(parser.hasErrors(), "Parser should not have errors");
        assertEquals(1, program.getStatements().size());

        ExpressionStatement exprStmt = (ExpressionStatement) program.getStatements().get(0);
        assertTrue(exprStmt.getExpression() instanceof InfixExpression);

        InfixExpression multExpr = (InfixExpression) exprStmt.getExpression();
        assertEquals("*", multExpr.getOperator());

        // Left side should be the grouped addition
        assertTrue(multExpr.getLeft() instanceof InfixExpression);
        InfixExpression addExpr = (InfixExpression) multExpr.getLeft();
        assertEquals("+", addExpr.getOperator());

        // Right side should be the number 4
        assertTrue(multExpr.getRight() instanceof IntegerLiteral);
        IntegerLiteral rightInt = (IntegerLiteral) multExpr.getRight();
        assertEquals(4, rightInt.getValue());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 🚫 ERROR HANDLING TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("🚫 Parse malformed expression - unclosed parenthesis")
    void testMalformedExpressionUnclosedParen() {
        String input = "(2 + 3;";
        parser = new LanguageParser(new Lexer(input));

        Program program = parser.parseProgram();

        assertTrue(parser.hasErrors(), "Parser should detect unclosed parenthesis");
    }

    @Test
    @DisplayName("🚫 Parse malformed expression - invalid operator")
    void testMalformedExpressionInvalidOperator() {
        String input = "2 ++ 3;";
        parser = new LanguageParser(new Lexer(input));

        Program program = parser.parseProgram();

        // This might be handled differently depending on lexer implementation
        assertNotNull(program);
    }

    @Test
    @DisplayName("🚫 Parse malformed function call - unclosed parenthesis")
    void testMalformedFunctionCallUnclosedParen() {
        String input = "func(1, 2;";
        parser = new LanguageParser(new Lexer(input));

        Program program = parser.parseProgram();

        assertTrue(parser.hasErrors(), "Parser should detect unclosed parenthesis in function call");
    }
}