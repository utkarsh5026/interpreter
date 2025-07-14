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
 * ⚖️ Comprehensive Operator Precedence Tests ⚖️
 * 
 * This test suite rigorously validates operator precedence from first
 * principles:
 * 
 * Mathematical Order of Operations (from highest to lowest precedence):
 * 1. 🔗 Function calls and array indexing: f(), a[i]
 * 2. 🔄 Prefix operators: !, -, +
 * 3. ✖️ Multiplicative: *, /, %
 * 4. ➕ Additive: +, -
 * 5. 🔢 Relational: <, >, <=, >=
 * 6. 🟰 Equality: ==, !=
 * 7. 🔗 Logical AND: &&
 * 8. 🔗 Logical OR: ||
 * 9. 📝 Assignment: =
 * 
 * Testing Philosophy:
 * - Each test verifies a specific precedence rule
 * - Tests are structured to validate AST structure, not just parsing success
 * - Edge cases and complex combinations are thoroughly tested
 * - Mathematical correctness is verified through AST inspection
 */
@DisplayName("Parser Operator Precedence Tests")
public class ParserOperatorPrecedenceTests {

    private LanguageParser parser;

    /**
     * 🔍 Helper method to parse an expression and return the AST
     */
    private Expression parseExpression(String input) {
        parser = new LanguageParser(new Lexer(input + ";"));
        Program program = parser.parseProgram();
        assertFalse(parser.hasErrors(), "Expression should parse without errors: " + input);
        assertEquals(1, program.getStatements().size());

        ExpressionStatement exprStmt = (ExpressionStatement) program.getStatements().get(0);
        return exprStmt.getExpression();
    }

    /**
     * 🔍 Helper method to verify infix expression structure
     */
    private void verifyInfixStructure(Expression expr, String expectedOperator,
            Class<?> expectedLeftType, Class<?> expectedRightType) {
        assertTrue(expr instanceof InfixExpression,
                "Expression should be InfixExpression, got: " + expr.getClass().getSimpleName());

        InfixExpression infixExpr = (InfixExpression) expr;
        assertEquals(expectedOperator, infixExpr.getOperator(), "Operator mismatch");
        assertTrue(expectedLeftType.isInstance(infixExpr.getLeft()),
                "Left operand type mismatch. Expected: " + expectedLeftType.getSimpleName() +
                        ", got: " + infixExpr.getLeft().getClass().getSimpleName());
        assertTrue(expectedRightType.isInstance(infixExpr.getRight()),
                "Right operand type mismatch. Expected: " + expectedRightType.getSimpleName() +
                        ", got: " + infixExpr.getRight().getClass().getSimpleName());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ✖️ MULTIPLICATIVE OPERATORS vs ➕ ADDITIVE OPERATORS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("⚖️ Multiplication has higher precedence than addition: 2 + 3 * 4")
    void testMultiplicationBeforeAddition() {
        Expression expr = parseExpression("2 + 3 * 4");

        // Should parse as: 2 + (3 * 4), not (2 + 3) * 4
        verifyInfixStructure(expr, "+", IntegerLiteral.class, InfixExpression.class);

        InfixExpression addExpr = (InfixExpression) expr;
        IntegerLiteral leftInt = (IntegerLiteral) addExpr.getLeft();
        assertEquals(2, leftInt.getValue());

        InfixExpression multExpr = (InfixExpression) addExpr.getRight();
        assertEquals("*", multExpr.getOperator());

        IntegerLiteral multLeft = (IntegerLiteral) multExpr.getLeft();
        IntegerLiteral multRight = (IntegerLiteral) multExpr.getRight();
        assertEquals(3, multLeft.getValue());
        assertEquals(4, multRight.getValue());
    }

    @Test
    @DisplayName("⚖️ Division has higher precedence than subtraction: 10 - 8 / 2")
    void testDivisionBeforeSubtraction() {
        Expression expr = parseExpression("10 - 8 / 2");

        // Should parse as: 10 - (8 / 2), not (10 - 8) / 2
        verifyInfixStructure(expr, "-", IntegerLiteral.class, InfixExpression.class);

        InfixExpression subExpr = (InfixExpression) expr;
        IntegerLiteral leftInt = (IntegerLiteral) subExpr.getLeft();
        assertEquals(10, leftInt.getValue());

        InfixExpression divExpr = (InfixExpression) subExpr.getRight();
        assertEquals("/", divExpr.getOperator());
    }

    @Test
    @DisplayName("⚖️ Modulus has higher precedence than addition: 7 + 5 % 3")
    void testModulusBeforeAddition() {
        Expression expr = parseExpression("7 + 5 % 3");

        // Should parse as: 7 + (5 % 3), not (7 + 5) % 3
        verifyInfixStructure(expr, "+", IntegerLiteral.class, InfixExpression.class);

        InfixExpression addExpr = (InfixExpression) expr;
        InfixExpression modExpr = (InfixExpression) addExpr.getRight();
        assertEquals("%", modExpr.getOperator());
    }

    @Test
    @DisplayName("⚖️ Complex arithmetic: 2 + 3 * 4 - 5 / 2")
    void testComplexArithmetic() {
        Expression expr = parseExpression("2 + 3 * 4 - 5 / 2");

        // Should parse as: ((2 + (3 * 4)) - (5 / 2))
        verifyInfixStructure(expr, "-", InfixExpression.class, InfixExpression.class);

        InfixExpression subExpr = (InfixExpression) expr;

        // Left side: 2 + (3 * 4)
        InfixExpression leftSide = (InfixExpression) subExpr.getLeft();
        assertEquals("+", leftSide.getOperator());
        assertTrue(leftSide.getRight() instanceof InfixExpression);
        InfixExpression multExpr = (InfixExpression) leftSide.getRight();
        assertEquals("*", multExpr.getOperator());

        // Right side: 5 / 2
        InfixExpression rightSide = (InfixExpression) subExpr.getRight();
        assertEquals("/", rightSide.getOperator());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 🔢 RELATIONAL OPERATORS vs ➕ ARITHMETIC OPERATORS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("⚖️ Addition has higher precedence than comparison: 2 + 3 > 4")
    void testAdditionBeforeComparison() {
        Expression expr = parseExpression("2 + 3 > 4");

        // Should parse as: (2 + 3) > 4, not 2 + (3 > 4)
        verifyInfixStructure(expr, ">", InfixExpression.class, IntegerLiteral.class);

        InfixExpression compExpr = (InfixExpression) expr;
        InfixExpression addExpr = (InfixExpression) compExpr.getLeft();
        assertEquals("+", addExpr.getOperator());

        IntegerLiteral rightInt = (IntegerLiteral) compExpr.getRight();
        assertEquals(4, rightInt.getValue());
    }

    @Test
    @DisplayName("⚖️ Multiplication has higher precedence than comparison: 3 * 4 < 15")
    void testMultiplicationBeforeComparison() {
        Expression expr = parseExpression("3 * 4 < 15");

        // Should parse as: (3 * 4) < 15, not 3 * (4 < 15)
        verifyInfixStructure(expr, "<", InfixExpression.class, IntegerLiteral.class);

        InfixExpression compExpr = (InfixExpression) expr;
        InfixExpression multExpr = (InfixExpression) compExpr.getLeft();
        assertEquals("*", multExpr.getOperator());
    }

    @Test
    @DisplayName("⚖️ Complex comparison: 2 + 3 * 4 >= 10 + 4")
    void testComplexComparison() {
        Expression expr = parseExpression("2 + 3 * 4 >= 10 + 4");

        // Should parse as: (2 + (3 * 4)) >= (10 + 4)
        verifyInfixStructure(expr, ">=", InfixExpression.class, InfixExpression.class);

        InfixExpression compExpr = (InfixExpression) expr;

        // Left side: 2 + (3 * 4)
        InfixExpression leftSide = (InfixExpression) compExpr.getLeft();
        assertEquals("+", leftSide.getOperator());
        assertTrue(leftSide.getRight() instanceof InfixExpression);

        // Right side: 10 + 4
        InfixExpression rightSide = (InfixExpression) compExpr.getRight();
        assertEquals("+", rightSide.getOperator());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 🟰 EQUALITY vs 🔢 RELATIONAL OPERATORS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("⚖️ Relational has higher precedence than equality: 2 < 3 == true")
    void testRelationalBeforeEquality() {
        Expression expr = parseExpression("2 < 3 == true");

        // Should parse as: (2 < 3) == true, not 2 < (3 == true)
        verifyInfixStructure(expr, "==", InfixExpression.class, BooleanExpression.class);

        InfixExpression eqExpr = (InfixExpression) expr;
        InfixExpression compExpr = (InfixExpression) eqExpr.getLeft();
        assertEquals("<", compExpr.getOperator());

        BooleanExpression rightBool = (BooleanExpression) eqExpr.getRight();
        assertTrue(rightBool.getValue());
    }

    @Test
    @DisplayName("⚖️ Greater than has higher precedence than not equal: 5 > 3 != false")
    void testGreaterThanBeforeNotEqual() {
        Expression expr = parseExpression("5 > 3 != false");

        // Should parse as: (5 > 3) != false, not 5 > (3 != false)
        verifyInfixStructure(expr, "!=", InfixExpression.class, BooleanExpression.class);

        InfixExpression neqExpr = (InfixExpression) expr;
        InfixExpression gtExpr = (InfixExpression) neqExpr.getLeft();
        assertEquals(">", gtExpr.getOperator());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 🔗 LOGICAL OPERATORS PRECEDENCE
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("⚖️ Equality has higher precedence than logical AND: 1 == 1 && 2 == 2")
    void testEqualityBeforeLogicalAnd() {
        Expression expr = parseExpression("1 == 1 && 2 == 2");

        // Should parse as: (1 == 1) && (2 == 2), not 1 == (1 && 2) == 2
        verifyInfixStructure(expr, "&&", InfixExpression.class, InfixExpression.class);

        InfixExpression andExpr = (InfixExpression) expr;

        InfixExpression leftEq = (InfixExpression) andExpr.getLeft();
        assertEquals("==", leftEq.getOperator());

        InfixExpression rightEq = (InfixExpression) andExpr.getRight();
        assertEquals("==", rightEq.getOperator());
    }

    @Test
    @DisplayName("⚖️ Logical AND has higher precedence than logical OR: true || false && true")
    void testLogicalAndBeforeLogicalOr() {
        Expression expr = parseExpression("true || false && true");

        // Should parse as: true || (false && true), not (true || false) && true
        verifyInfixStructure(expr, "||", BooleanExpression.class, InfixExpression.class);

        InfixExpression orExpr = (InfixExpression) expr;
        BooleanExpression leftBool = (BooleanExpression) orExpr.getLeft();
        assertTrue(leftBool.getValue());

        InfixExpression andExpr = (InfixExpression) orExpr.getRight();
        assertEquals("&&", andExpr.getOperator());
    }

    @Test
    @DisplayName("⚖️ Complex logical expression: 2 > 1 && 3 < 4 || 5 == 5")
    void testComplexLogicalExpression() {
        Expression expr = parseExpression("2 > 1 && 3 < 4 || 5 == 5");

        // Should parse as: ((2 > 1) && (3 < 4)) || (5 == 5)
        verifyInfixStructure(expr, "||", InfixExpression.class, InfixExpression.class);

        InfixExpression orExpr = (InfixExpression) expr;

        // Left side: (2 > 1) && (3 < 4)
        InfixExpression leftAnd = (InfixExpression) orExpr.getLeft();
        assertEquals("&&", leftAnd.getOperator());

        // Right side: 5 == 5
        InfixExpression rightEq = (InfixExpression) orExpr.getRight();
        assertEquals("==", rightEq.getOperator());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 🔄 PREFIX OPERATORS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("⚖️ Prefix operators have high precedence: !true && false")
    void testPrefixBeforeLogical() {
        Expression expr = parseExpression("!true && false");

        // Should parse as: (!true) && false, not !(true && false)
        verifyInfixStructure(expr, "&&", PrefixExpression.class, BooleanExpression.class);

        InfixExpression andExpr = (InfixExpression) expr;
        PrefixExpression notExpr = (PrefixExpression) andExpr.getLeft();
        assertEquals("!", notExpr.getOperator());

        BooleanExpression rightBool = (BooleanExpression) andExpr.getRight();
        assertFalse(rightBool.getValue());
    }

    @Test
    @DisplayName("⚖️ Prefix minus has higher precedence than multiplication: -2 * 3")
    void testPrefixMinusBeforeMultiplication() {
        Expression expr = parseExpression("-2 * 3");

        // Should parse as: (-2) * 3, not -(2 * 3)
        verifyInfixStructure(expr, "*", PrefixExpression.class, IntegerLiteral.class);

        InfixExpression multExpr = (InfixExpression) expr;
        PrefixExpression negExpr = (PrefixExpression) multExpr.getLeft();
        assertEquals("-", negExpr.getOperator());

        IntegerLiteral rightInt = (IntegerLiteral) multExpr.getRight();
        assertEquals(3, rightInt.getValue());
    }

    @Test
    @DisplayName("⚖️ Multiple prefix operators: !-5 > 0")
    void testMultiplePrefixOperators() {
        Expression expr = parseExpression("!-5 > 0");

        // Should parse as: ((!(-5)) > 0)
        verifyInfixStructure(expr, ">", PrefixExpression.class, IntegerLiteral.class);

        InfixExpression gtExpr = (InfixExpression) expr;
        PrefixExpression notExpr = (PrefixExpression) gtExpr.getLeft();
        assertEquals("!", notExpr.getOperator());

        PrefixExpression negExpr = (PrefixExpression) notExpr.getRight();
        assertEquals("-", negExpr.getOperator());

        IntegerLiteral innerInt = (IntegerLiteral) negExpr.getRight();
        assertEquals(5, innerInt.getValue());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 📞 FUNCTION CALLS AND INDEXING
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("⚖️ Function calls have highest precedence: getValue() + 5")
    void testFunctionCallBeforeAddition() {
        Expression expr = parseExpression("getValue() + 5");

        // Should parse as: (getValue()) + 5, not getValue(() + 5)
        verifyInfixStructure(expr, "+", CallExpression.class, IntegerLiteral.class);

        InfixExpression addExpr = (InfixExpression) expr;
        CallExpression callExpr = (CallExpression) addExpr.getLeft();
        assertTrue(callExpr.getFunction() instanceof Identifier);

        Identifier funcName = (Identifier) callExpr.getFunction();
        assertEquals("getValue", funcName.getValue());
    }

    @Test
    @DisplayName("⚖️ Array indexing has highest precedence: arr[0] * 2")
    void testArrayIndexingBeforeMultiplication() {
        Expression expr = parseExpression("arr[0] * 2");

        // Should parse as: (arr[0]) * 2, not arr[(0 * 2)]
        verifyInfixStructure(expr, "*", IndexExpression.class, IntegerLiteral.class);

        InfixExpression multExpr = (InfixExpression) expr;
        IndexExpression indexExpr = (IndexExpression) multExpr.getLeft();
        assertTrue(indexExpr.getLeft() instanceof Identifier);
        assertTrue(indexExpr.getIndex() instanceof IntegerLiteral);

        Identifier arrayName = (Identifier) indexExpr.getLeft();
        assertEquals("arr", arrayName.getValue());
    }

    @Test
    @DisplayName("⚖️ Chained function calls and indexing: getArray()[getValue()]")
    void testChainedCallsAndIndexing() {
        Expression expr = parseExpression("getArray()[getValue()]");

        // Should parse as: (getArray())[getValue()]
        assertTrue(expr instanceof IndexExpression);
        IndexExpression indexExpr = (IndexExpression) expr;

        assertTrue(indexExpr.getLeft() instanceof CallExpression);
        assertTrue(indexExpr.getIndex() instanceof CallExpression);

        CallExpression arrayCall = (CallExpression) indexExpr.getLeft();
        Identifier arrayFuncName = (Identifier) arrayCall.getFunction();
        assertEquals("getArray", arrayFuncName.getValue());

        CallExpression indexCall = (CallExpression) indexExpr.getIndex();
        Identifier indexFuncName = (Identifier) indexCall.getFunction();
        assertEquals("getValue", indexFuncName.getValue());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 📝 ASSIGNMENT PRECEDENCE
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("⚖️ Assignment has lowest precedence: x = y + z * 2")
    void testAssignmentLowestPrecedence() {
        Expression expr = parseExpression("x = y + z * 2");

        // Should parse as: x = (y + (z * 2)), not (x = y) + (z * 2)
        assertTrue(expr instanceof AssignmentExpression);
        AssignmentExpression assignExpr = (AssignmentExpression) expr;

        assertEquals("x", assignExpr.getName().getValue());
        assertTrue(assignExpr.getValue() instanceof InfixExpression);

        InfixExpression valueExpr = (InfixExpression) assignExpr.getValue();
        assertEquals("+", valueExpr.getOperator());
        assertTrue(valueExpr.getRight() instanceof InfixExpression);

        InfixExpression multExpr = (InfixExpression) valueExpr.getRight();
        assertEquals("*", multExpr.getOperator());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 🧮 COMPLEX PRECEDENCE COMBINATIONS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("⚖️ Extremely complex precedence: !func(x + 1)[y * 2] > z - w / 3 && a == b")
    void testExtremelyComplexPrecedence() {
        Expression expr = parseExpression("!func(x + 1)[y * 2] > z - w / 3 && a == b");

        // Expected structure: (!(func((x + 1))[(y * 2)]) > (z - (w / 3))) && (a == b)
        verifyInfixStructure(expr, "&&", InfixExpression.class, InfixExpression.class);

        InfixExpression andExpr = (InfixExpression) expr;

        // Left side: !(func((x + 1))[(y * 2)]) > (z - (w / 3))
        InfixExpression leftGt = (InfixExpression) andExpr.getLeft();
        assertEquals(">", leftGt.getOperator());

        // Left side of >: !(func((x + 1))[(y * 2)])
        assertTrue(leftGt.getLeft() instanceof PrefixExpression);
        PrefixExpression notExpr = (PrefixExpression) leftGt.getLeft();
        assertEquals("!", notExpr.getOperator());

        // Inside the !: func((x + 1))[(y * 2)]
        assertTrue(notExpr.getRight() instanceof IndexExpression);
        IndexExpression indexExpr = (IndexExpression) notExpr.getRight();

        // Array expression: func((x + 1))
        assertTrue(indexExpr.getLeft() instanceof CallExpression);
        CallExpression callExpr = (CallExpression) indexExpr.getLeft();

        // Function argument: (x + 1)
        assertEquals(1, callExpr.getArguments().size());
        assertTrue(callExpr.getArguments().get(0) instanceof InfixExpression);
        InfixExpression argExpr = (InfixExpression) callExpr.getArguments().get(0);
        assertEquals("+", argExpr.getOperator());

        // Index expression: (y * 2)
        assertTrue(indexExpr.getIndex() instanceof InfixExpression);
        InfixExpression indexMultExpr = (InfixExpression) indexExpr.getIndex();
        assertEquals("*", indexMultExpr.getOperator());

        // Right side of >: (z - (w / 3))
        assertTrue(leftGt.getRight() instanceof InfixExpression);
        InfixExpression rightSubExpr = (InfixExpression) leftGt.getRight();
        assertEquals("-", rightSubExpr.getOperator());
        assertTrue(rightSubExpr.getRight() instanceof InfixExpression);

        InfixExpression divExpr = (InfixExpression) rightSubExpr.getRight();
        assertEquals("/", divExpr.getOperator());

        // Right side of &&: (a == b)
        InfixExpression rightEq = (InfixExpression) andExpr.getRight();
        assertEquals("==", rightEq.getOperator());
    }

    @Test
    @DisplayName("⚖️ Assignment with complex expression: result = a + b * c == d && e || f")
    void testAssignmentWithComplexExpression() {
        Expression expr = parseExpression("result = a + b * c == d && e || f");

        // Should parse as: result = (((a + (b * c)) == d) && e) || f
        assertTrue(expr instanceof AssignmentExpression);
        AssignmentExpression assignExpr = (AssignmentExpression) expr;

        assertEquals("result", assignExpr.getName().getValue());

        // Value should be a complex logical OR expression
        assertTrue(assignExpr.getValue() instanceof InfixExpression);
        InfixExpression orExpr = (InfixExpression) assignExpr.getValue();
        assertEquals("||", orExpr.getOperator());

        // Left side of OR: ((a + (b * c)) == d) && e
        assertTrue(orExpr.getLeft() instanceof InfixExpression);
        InfixExpression andExpr = (InfixExpression) orExpr.getLeft();
        assertEquals("&&", andExpr.getOperator());

        // Left side of AND: (a + (b * c)) == d
        assertTrue(andExpr.getLeft() instanceof InfixExpression);
        InfixExpression eqExpr = (InfixExpression) andExpr.getLeft();
        assertEquals("==", eqExpr.getOperator());

        // Left side of ==: a + (b * c)
        assertTrue(eqExpr.getLeft() instanceof InfixExpression);
        InfixExpression addExpr = (InfixExpression) eqExpr.getLeft();
        assertEquals("+", addExpr.getOperator());

        // Right side of +: (b * c)
        assertTrue(addExpr.getRight() instanceof InfixExpression);
        InfixExpression multExpr = (InfixExpression) addExpr.getRight();
        assertEquals("*", multExpr.getOperator());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 🎭 EDGE CASES AND BOUNDARY CONDITIONS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("⚖️ Same precedence operators (left associativity): 10 - 5 - 2")
    void testLeftAssociativitySamePrecedence() {
        Expression expr = parseExpression("10 - 5 - 2");

        // Should parse as: (10 - 5) - 2, not 10 - (5 - 2)
        verifyInfixStructure(expr, "-", InfixExpression.class, IntegerLiteral.class);

        InfixExpression outerSub = (InfixExpression) expr;
        InfixExpression innerSub = (InfixExpression) outerSub.getLeft();
        assertEquals("-", innerSub.getOperator());

        IntegerLiteral rightInt = (IntegerLiteral) outerSub.getRight();
        assertEquals(2, rightInt.getValue());
    }

    @Test
    @DisplayName("⚖️ Division left associativity: 24 / 4 / 2")
    void testDivisionLeftAssociativity() {
        Expression expr = parseExpression("24 / 4 / 2");

        // Should parse as: (24 / 4) / 2, not 24 / (4 / 2)
        verifyInfixStructure(expr, "/", InfixExpression.class, IntegerLiteral.class);

        InfixExpression outerDiv = (InfixExpression) expr;
        InfixExpression innerDiv = (InfixExpression) outerDiv.getLeft();
        assertEquals("/", innerDiv.getOperator());

        IntegerLiteral rightInt = (IntegerLiteral) outerDiv.getRight();
        assertEquals(2, rightInt.getValue());
    }

    @Test
    @DisplayName("⚖️ Mixed associativity with parentheses: (2 + 3) * (4 - 1)")
    void testParenthesesOverridePrecedence() {
        Expression expr = parseExpression("(2 + 3) * (4 - 1)");

        // Parentheses should override normal precedence
        verifyInfixStructure(expr, "*", InfixExpression.class, InfixExpression.class);

        InfixExpression multExpr = (InfixExpression) expr;

        InfixExpression leftAdd = (InfixExpression) multExpr.getLeft();
        assertEquals("+", leftAdd.getOperator());

        InfixExpression rightSub = (InfixExpression) multExpr.getRight();
        assertEquals("-", rightSub.getOperator());
    }
}