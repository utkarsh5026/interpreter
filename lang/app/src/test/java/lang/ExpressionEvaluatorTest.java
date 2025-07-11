package lang;

import lang.exec.evaluator.ExpressionEvaluator;
import lang.exec.base.BaseObject;
import lang.exec.objects.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 🧮 Comprehensive test suite for the ExpressionEvaluator class 🧮
 * 
 * This test suite covers all expression evaluation operations:
 * 1. Logical operations (AND, OR, NOT)
 * 2. Arithmetic operations (+, -, *, /, %)
 * 3. String operations (concatenation, comparison)
 * 4. Comparison operations (==, !=, <, >, <=, >=)
 * 5. Prefix operations (unary -, !)
 * 6. Error handling and type validation
 * 7. Edge cases and boundary conditions
 * 
 * Each test category is organized in nested classes for better structure
 * and follows the same patterns as the existing LexerTest.
 */
class ExpressionEvaluatorTest {

    // Helper objects for testing
    private IntegerObject intZero;
    private IntegerObject intFive;
    private IntegerObject intTen;
    private IntegerObject intNegativeFive;

    private BooleanObject boolTrue;
    private BooleanObject boolFalse;

    private StringObject stringEmpty;
    private StringObject stringHello;
    private StringObject stringWorld;
    private StringObject stringApple;
    private StringObject stringBanana;

    private NullObject nullObject;

    @BeforeEach
    void setUp() {
        // Initialize test objects
        intZero = new IntegerObject(0);
        intFive = new IntegerObject(5);
        intTen = new IntegerObject(10);
        intNegativeFive = new IntegerObject(-5);

        boolTrue = new BooleanObject(true);
        boolFalse = new BooleanObject(false);

        stringEmpty = new StringObject("");
        stringHello = new StringObject("hello");
        stringWorld = new StringObject("world");
        stringApple = new StringObject("apple");
        stringBanana = new StringObject("banana");

        nullObject = NullObject.INSTANCE;
    }

    /**
     * Helper method to assert that result is a BooleanObject with expected value
     */
    private void assertBooleanResult(BaseObject result, boolean expectedValue) {
        assertInstanceOf(BooleanObject.class, result, "Result should be a BooleanObject");
        assertEquals(expectedValue, ((BooleanObject) result).getValue(),
                "Boolean value should match expected");
    }

    /**
     * Helper method to assert that result is an IntegerObject with expected value
     */
    private void assertIntegerResult(BaseObject result, long expectedValue) {
        assertInstanceOf(IntegerObject.class, result, "Result should be an IntegerObject");
        assertEquals(expectedValue, ((IntegerObject) result).getValue(),
                "Integer value should match expected");
    }

    /**
     * Helper method to assert that result is a StringObject with expected value
     */
    private void assertStringResult(BaseObject result, String expectedValue) {
        assertInstanceOf(StringObject.class, result, "Result should be a StringObject");
        assertEquals(expectedValue, ((StringObject) result).getValue(),
                "String value should match expected");
    }

    /**
     * Helper method to assert that result is an ErrorObject with expected message
     */
    private void assertErrorResult(BaseObject result, String expectedMessage) {
        assertInstanceOf(ErrorObject.class, result, "Result should be an ErrorObject");
        assertTrue(((ErrorObject) result).getMessage().contains(expectedMessage),
                "Error message should contain: " + expectedMessage);
    }

    @Nested
    @DisplayName("Logical AND Expression Tests")
    class LogicalAndExpressionTests {

        @Test
        @DisplayName("Should return true when both operands are truthy")
        void testAndExpressionBothTrue() {
            BaseObject result = ExpressionEvaluator.evalAndExpression(boolTrue, boolTrue);
            assertBooleanResult(result, true);

            result = ExpressionEvaluator.evalAndExpression(boolTrue, intFive);
            assertBooleanResult(result, true);

            result = ExpressionEvaluator.evalAndExpression(stringHello, boolTrue);
            assertBooleanResult(result, true);
        }

        @Test
        @DisplayName("Should return false when first operand is falsy (short-circuit)")
        void testAndExpressionFirstFalse() {
            BaseObject result = ExpressionEvaluator.evalAndExpression(boolFalse, boolTrue);
            assertBooleanResult(result, false);

            result = ExpressionEvaluator.evalAndExpression(intZero, intFive);
            assertBooleanResult(result, false);

            result = ExpressionEvaluator.evalAndExpression(stringEmpty, stringHello);
            assertBooleanResult(result, false);

            result = ExpressionEvaluator.evalAndExpression(nullObject, boolTrue);
            assertBooleanResult(result, false);
        }

        @Test
        @DisplayName("Should return false when second operand is falsy")
        void testAndExpressionSecondFalse() {
            BaseObject result = ExpressionEvaluator.evalAndExpression(boolTrue, boolFalse);
            assertBooleanResult(result, false);

            result = ExpressionEvaluator.evalAndExpression(intFive, intZero);
            assertBooleanResult(result, false);

            result = ExpressionEvaluator.evalAndExpression(stringHello, stringEmpty);
            assertBooleanResult(result, false);
        }

        @Test
        @DisplayName("Should return false when both operands are falsy")
        void testAndExpressionBothFalse() {
            BaseObject result = ExpressionEvaluator.evalAndExpression(boolFalse, boolFalse);
            assertBooleanResult(result, false);

            result = ExpressionEvaluator.evalAndExpression(intZero, stringEmpty);
            assertBooleanResult(result, false);

            result = ExpressionEvaluator.evalAndExpression(nullObject, intZero);
            assertBooleanResult(result, false);
        }
    }

    @Nested
    @DisplayName("Logical OR Expression Tests")
    class LogicalOrExpressionTests {

        @Test
        @DisplayName("Should return true when first operand is truthy (short-circuit)")
        void testOrExpressionFirstTrue() {
            BaseObject result = ExpressionEvaluator.evalOrExpression(boolTrue, boolFalse);
            assertBooleanResult(result, true);

            result = ExpressionEvaluator.evalOrExpression(intFive, intZero);
            assertBooleanResult(result, true);

            result = ExpressionEvaluator.evalOrExpression(stringHello, stringEmpty);
            assertBooleanResult(result, true);
        }

        @Test
        @DisplayName("Should return true when second operand is truthy")
        void testOrExpressionSecondTrue() {
            BaseObject result = ExpressionEvaluator.evalOrExpression(boolFalse, boolTrue);
            assertBooleanResult(result, true);

            result = ExpressionEvaluator.evalOrExpression(intZero, intFive);
            assertBooleanResult(result, true);

            result = ExpressionEvaluator.evalOrExpression(stringEmpty, stringHello);
            assertBooleanResult(result, true);
        }

        @Test
        @DisplayName("Should return true when both operands are truthy")
        void testOrExpressionBothTrue() {
            BaseObject result = ExpressionEvaluator.evalOrExpression(boolTrue, boolTrue);
            assertBooleanResult(result, true);

            result = ExpressionEvaluator.evalOrExpression(intFive, intTen);
            assertBooleanResult(result, true);

            result = ExpressionEvaluator.evalOrExpression(stringHello, stringWorld);
            assertBooleanResult(result, true);
        }

        @Test
        @DisplayName("Should return false when both operands are falsy")
        void testOrExpressionBothFalse() {
            BaseObject result = ExpressionEvaluator.evalOrExpression(boolFalse, boolFalse);
            assertBooleanResult(result, false);

            result = ExpressionEvaluator.evalOrExpression(intZero, stringEmpty);
            assertBooleanResult(result, false);

            result = ExpressionEvaluator.evalOrExpression(nullObject, intZero);
            assertBooleanResult(result, false);
        }
    }

    @Nested
    @DisplayName("Logical NOT Expression Tests")
    class LogicalNotExpressionTests {

        @Test
        @DisplayName("Should negate boolean values correctly")
        void testNotExpressionBooleans() {
            BaseObject result = ExpressionEvaluator.evalNotExpression(boolTrue);
            assertBooleanResult(result, false);

            result = ExpressionEvaluator.evalNotExpression(boolFalse);
            assertBooleanResult(result, true);
        }

        @Test
        @DisplayName("Should negate integer values based on truthiness")
        void testNotExpressionIntegers() {
            BaseObject result = ExpressionEvaluator.evalNotExpression(intZero);
            assertBooleanResult(result, true); // 0 is falsy

            result = ExpressionEvaluator.evalNotExpression(intFive);
            assertBooleanResult(result, false); // non-zero is truthy

            result = ExpressionEvaluator.evalNotExpression(intNegativeFive);
            assertBooleanResult(result, false); // non-zero is truthy
        }

        @Test
        @DisplayName("Should negate string values based on emptiness")
        void testNotExpressionStrings() {
            BaseObject result = ExpressionEvaluator.evalNotExpression(stringEmpty);
            assertBooleanResult(result, true); // empty string is falsy

            result = ExpressionEvaluator.evalNotExpression(stringHello);
            assertBooleanResult(result, false); // non-empty string is truthy
        }

        @Test
        @DisplayName("Should negate null values correctly")
        void testNotExpressionNull() {
            BaseObject result = ExpressionEvaluator.evalNotExpression(nullObject);
            assertBooleanResult(result, true); // null is falsy
        }
    }

    @Nested
    @DisplayName("String Infix Expression Tests")
    class StringInfixExpressionTests {

        @Test
        @DisplayName("Should concatenate strings with + operator")
        void testStringConcatenation() {
            BaseObject result = ExpressionEvaluator.evalStringInfixExpression("+", stringHello, stringWorld);
            assertStringResult(result, "helloworld");

            result = ExpressionEvaluator.evalStringInfixExpression("+", stringHello, stringEmpty);
            assertStringResult(result, "hello");

            result = ExpressionEvaluator.evalStringInfixExpression("+", stringEmpty, stringWorld);
            assertStringResult(result, "world");
        }

        @Test
        @DisplayName("Should compare strings for equality")
        void testStringEquality() {
            BaseObject result = ExpressionEvaluator.evalStringInfixExpression("==", stringHello, stringHello);
            assertBooleanResult(result, true);

            result = ExpressionEvaluator.evalStringInfixExpression("==", stringHello, stringWorld);
            assertBooleanResult(result, false);

            result = ExpressionEvaluator.evalStringInfixExpression("==", stringEmpty, stringEmpty);
            assertBooleanResult(result, true);
        }

        @Test
        @DisplayName("Should compare strings for inequality")
        void testStringInequality() {
            BaseObject result = ExpressionEvaluator.evalStringInfixExpression("!=", stringHello, stringWorld);
            assertBooleanResult(result, true);

            result = ExpressionEvaluator.evalStringInfixExpression("!=", stringHello, stringHello);
            assertBooleanResult(result, false);
        }

        @ParameterizedTest
        @CsvSource({
                "apple, banana, <, true",
                "banana, apple, <, false",
                "apple, apple, <, false",
                "apple, banana, >, false",
                "banana, apple, >, true",
                "apple, apple, >, false",
                "apple, banana, <=, true",
                "banana, apple, <=, false",
                "apple, apple, <=, true",
                "apple, banana, >=, false",
                "banana, apple, >=, true",
                "apple, apple, >=, true"
        })
        @DisplayName("Should compare strings lexicographically")
        void testStringComparison(String left, String right, String operator, boolean expected) {
            StringObject leftObj = new StringObject(left);
            StringObject rightObj = new StringObject(right);

            BaseObject result = ExpressionEvaluator.evalStringInfixExpression(operator, leftObj, rightObj);
            assertBooleanResult(result, expected);
        }

        @Test
        @DisplayName("Should return error for invalid string operators")
        void testInvalidStringOperators() {
            BaseObject result = ExpressionEvaluator.evalStringInfixExpression("-", stringHello, stringWorld);
            assertErrorResult(result, "unknown operator");

            result = ExpressionEvaluator.evalStringInfixExpression("*", stringHello, stringWorld);
            assertErrorResult(result, "unknown operator");

            result = ExpressionEvaluator.evalStringInfixExpression("&&", stringHello, stringWorld);
            assertErrorResult(result, "unknown operator");
        }

        @Test
        @DisplayName("Should return error for non-string operands")
        void testStringOperationTypeError() {
            BaseObject result = ExpressionEvaluator.evalStringInfixExpression("+", stringHello, intFive);
            assertErrorResult(result, "type mismatch");

            result = ExpressionEvaluator.evalStringInfixExpression("==", intFive, stringHello);
            assertErrorResult(result, "type mismatch");
        }
    }

    @Nested
    @DisplayName("Integer Infix Expression Tests")
    class IntegerInfixExpressionTests {

        @Test
        @DisplayName("Should perform basic arithmetic operations")
        void testBasicArithmetic() {
            // Addition
            BaseObject result = ExpressionEvaluator.evalIntegerInfixExpression("+", intFive, intTen);
            assertIntegerResult(result, 15);

            // Subtraction
            result = ExpressionEvaluator.evalIntegerInfixExpression("-", intTen, intFive);
            assertIntegerResult(result, 5);

            // Multiplication
            result = ExpressionEvaluator.evalIntegerInfixExpression("*", intFive, intTen);
            assertIntegerResult(result, 50);

            // Division
            result = ExpressionEvaluator.evalIntegerInfixExpression("/", intTen, intFive);
            assertIntegerResult(result, 2);

            // Modulus
            IntegerObject seven = new IntegerObject(7);
            IntegerObject three = new IntegerObject(3);
            result = ExpressionEvaluator.evalIntegerInfixExpression("%", seven, three);
            assertIntegerResult(result, 1);
        }

        @Test
        @DisplayName("Should handle arithmetic with negative numbers")
        void testArithmeticWithNegatives() {
            BaseObject result = ExpressionEvaluator.evalIntegerInfixExpression("+", intFive, intNegativeFive);
            assertIntegerResult(result, 0);

            result = ExpressionEvaluator.evalIntegerInfixExpression("-", intFive, intNegativeFive);
            assertIntegerResult(result, 10);

            result = ExpressionEvaluator.evalIntegerInfixExpression("*", intFive, intNegativeFive);
            assertIntegerResult(result, -25);
        }

        @Test
        @DisplayName("Should perform integer equality comparisons")
        void testIntegerEquality() {
            BaseObject result = ExpressionEvaluator.evalIntegerInfixExpression("==", intFive, intFive);
            assertBooleanResult(result, true);

            result = ExpressionEvaluator.evalIntegerInfixExpression("==", intFive, intTen);
            assertBooleanResult(result, false);

            result = ExpressionEvaluator.evalIntegerInfixExpression("!=", intFive, intTen);
            assertBooleanResult(result, true);

            result = ExpressionEvaluator.evalIntegerInfixExpression("!=", intFive, intFive);
            assertBooleanResult(result, false);
        }

        @ParameterizedTest
        @CsvSource({
                "5, 10, <, true",
                "10, 5, <, false",
                "5, 5, <, false",
                "5, 10, >, false",
                "10, 5, >, true",
                "5, 5, >, false",
                "5, 10, <=, true",
                "10, 5, <=, false",
                "5, 5, <=, true",
                "5, 10, >=, false",
                "10, 5, >=, true",
                "5, 5, >=, true"
        })
        @DisplayName("Should perform integer comparison operations")
        void testIntegerComparisons(long left, long right, String operator, boolean expected) {
            IntegerObject leftObj = new IntegerObject(left);
            IntegerObject rightObj = new IntegerObject(right);

            BaseObject result = ExpressionEvaluator.evalIntegerInfixExpression(operator, leftObj, rightObj);
            assertBooleanResult(result, expected);
        }

        @Test
        @DisplayName("Should return error for invalid integer operators")
        void testInvalidIntegerOperators() {
            BaseObject result = ExpressionEvaluator.evalIntegerInfixExpression("&&", intFive, intTen);
            assertErrorResult(result, "unknown operator");

            result = ExpressionEvaluator.evalIntegerInfixExpression("||", intFive, intTen);
            assertErrorResult(result, "unknown operator");

            result = ExpressionEvaluator.evalIntegerInfixExpression("@", intFive, intTen);
            assertErrorResult(result, "unknown operator");
        }

        @Test
        @DisplayName("Should return error for non-integer operands")
        void testIntegerOperationTypeError() {
            BaseObject result = ExpressionEvaluator.evalIntegerInfixExpression("+", intFive, stringHello);
            assertErrorResult(result, "type mismatch");

            result = ExpressionEvaluator.evalIntegerInfixExpression("*", boolTrue, intFive);
            assertErrorResult(result, "type mismatch");
        }

        @Test
        @DisplayName("Should handle division by zero")
        void testDivisionByZero() {
            // Note: This might throw an ArithmeticException or return a specific value
            // depending on the implementation. Adjust accordingly.
            assertThrows(ArithmeticException.class, () -> {
                ExpressionEvaluator.evalIntegerInfixExpression("/", intFive, intZero);
            });
        }
    }

    @Nested
    @DisplayName("Boolean Infix Expression Tests")
    class BooleanInfixExpressionTests {

        @Test
        @DisplayName("Should perform boolean equality operations")
        void testBooleanEquality() {
            BaseObject result = ExpressionEvaluator.evalBooleanInfixExpression("==", boolTrue, boolTrue);
            assertBooleanResult(result, true);

            result = ExpressionEvaluator.evalBooleanInfixExpression("==", boolFalse, boolFalse);
            assertBooleanResult(result, true);

            result = ExpressionEvaluator.evalBooleanInfixExpression("==", boolTrue, boolFalse);
            assertBooleanResult(result, false);

            result = ExpressionEvaluator.evalBooleanInfixExpression("!=", boolTrue, boolFalse);
            assertBooleanResult(result, true);

            result = ExpressionEvaluator.evalBooleanInfixExpression("!=", boolTrue, boolTrue);
            assertBooleanResult(result, false);
        }

        @Test
        @DisplayName("Should perform boolean logical operations")
        void testBooleanLogicalOperations() {
            // AND operations
            BaseObject result = ExpressionEvaluator.evalBooleanInfixExpression("&&", boolTrue, boolTrue);
            assertBooleanResult(result, true);

            result = ExpressionEvaluator.evalBooleanInfixExpression("&&", boolTrue, boolFalse);
            assertBooleanResult(result, false);

            result = ExpressionEvaluator.evalBooleanInfixExpression("&&", boolFalse, boolTrue);
            assertBooleanResult(result, false);

            result = ExpressionEvaluator.evalBooleanInfixExpression("&&", boolFalse, boolFalse);
            assertBooleanResult(result, false);

            // OR operations
            result = ExpressionEvaluator.evalBooleanInfixExpression("||", boolTrue, boolTrue);
            assertBooleanResult(result, true);

            result = ExpressionEvaluator.evalBooleanInfixExpression("||", boolTrue, boolFalse);
            assertBooleanResult(result, true);

            result = ExpressionEvaluator.evalBooleanInfixExpression("||", boolFalse, boolTrue);
            assertBooleanResult(result, true);

            result = ExpressionEvaluator.evalBooleanInfixExpression("||", boolFalse, boolFalse);
            assertBooleanResult(result, false);
        }

        @Test
        @DisplayName("Should return error for invalid boolean operators")
        void testInvalidBooleanOperators() {
            BaseObject result = ExpressionEvaluator.evalBooleanInfixExpression("+", boolTrue, boolFalse);
            assertErrorResult(result, "unknown operator");

            result = ExpressionEvaluator.evalBooleanInfixExpression("<", boolTrue, boolFalse);
            assertErrorResult(result, "unknown operator");

            result = ExpressionEvaluator.evalBooleanInfixExpression("*", boolTrue, boolFalse);
            assertErrorResult(result, "unknown operator");
        }

        @Test
        @DisplayName("Should return error for non-boolean operands")
        void testBooleanOperationTypeError() {
            BaseObject result = ExpressionEvaluator.evalBooleanInfixExpression("&&", boolTrue, intFive);
            assertErrorResult(result, "type mismatch");

            result = ExpressionEvaluator.evalBooleanInfixExpression("==", stringHello, boolTrue);
            assertErrorResult(result, "type mismatch");
        }
    }

    @Nested
    @DisplayName("General Infix Expression Tests (Main Dispatcher)")
    class GeneralInfixExpressionTests {

        @Test
        @DisplayName("Should dispatch string operations correctly")
        void testStringDispatch() {
            BaseObject result = ExpressionEvaluator.evalInfixExpression("+", stringHello, stringWorld);
            assertStringResult(result, "helloworld");

            result = ExpressionEvaluator.evalInfixExpression("==", stringApple, stringBanana);
            assertBooleanResult(result, false);
        }

        @Test
        @DisplayName("Should dispatch integer operations correctly")
        void testIntegerDispatch() {
            BaseObject result = ExpressionEvaluator.evalInfixExpression("+", intFive, intTen);
            assertIntegerResult(result, 15);

            result = ExpressionEvaluator.evalInfixExpression("<", intFive, intTen);
            assertBooleanResult(result, true);
        }

        @Test
        @DisplayName("Should dispatch boolean operations correctly")
        void testBooleanDispatch() {
            BaseObject result = ExpressionEvaluator.evalInfixExpression("&&", boolTrue, boolFalse);
            assertBooleanResult(result, false);

            result = ExpressionEvaluator.evalInfixExpression("==", boolTrue, boolTrue);
            assertBooleanResult(result, true);
        }

        @Test
        @DisplayName("Should return error for mixed types")
        void testMixedTypeError() {
            BaseObject result = ExpressionEvaluator.evalInfixExpression("+", intFive, stringHello);
            assertErrorResult(result, "unknown operator");

            result = ExpressionEvaluator.evalInfixExpression("==", boolTrue, intFive);
            assertErrorResult(result, "unknown operator");

            result = ExpressionEvaluator.evalInfixExpression("*", stringHello, boolTrue);
            assertErrorResult(result, "unknown operator");
        }

        @Test
        @DisplayName("Should return error for unsupported type combinations")
        void testUnsupportedTypeCombinations() {
            BaseObject result = ExpressionEvaluator.evalInfixExpression("+", nullObject, intFive);
            assertErrorResult(result, "unknown operator");

            result = ExpressionEvaluator.evalInfixExpression("&&", nullObject, boolTrue);
            assertErrorResult(result, "unknown operator");
        }
    }

    @Nested
    @DisplayName("Prefix Expression Tests")
    class PrefixExpressionTests {

        @Test
        @DisplayName("Should evaluate logical NOT prefix expressions")
        void testNotPrefixExpressions() {
            BaseObject result = ExpressionEvaluator.evalPrefixExpression("!", boolTrue);
            assertBooleanResult(result, false);

            result = ExpressionEvaluator.evalPrefixExpression("!", boolFalse);
            assertBooleanResult(result, true);

            result = ExpressionEvaluator.evalPrefixExpression("!", intZero);
            assertBooleanResult(result, true); // 0 is falsy

            result = ExpressionEvaluator.evalPrefixExpression("!", intFive);
            assertBooleanResult(result, false); // non-zero is truthy

            result = ExpressionEvaluator.evalPrefixExpression("!", stringEmpty);
            assertBooleanResult(result, true); // empty string is falsy

            result = ExpressionEvaluator.evalPrefixExpression("!", stringHello);
            assertBooleanResult(result, false); // non-empty string is truthy

            result = ExpressionEvaluator.evalPrefixExpression("!", nullObject);
            assertBooleanResult(result, true); // null is falsy
        }

        @Test
        @DisplayName("Should evaluate negation prefix expressions")
        void testNegationPrefixExpressions() {
            BaseObject result = ExpressionEvaluator.evalPrefixExpression("-", intFive);
            assertIntegerResult(result, -5);

            result = ExpressionEvaluator.evalPrefixExpression("-", intNegativeFive);
            assertIntegerResult(result, 5);

            result = ExpressionEvaluator.evalPrefixExpression("-", intZero);
            assertIntegerResult(result, 0);

            IntegerObject large = new IntegerObject(1000);
            result = ExpressionEvaluator.evalPrefixExpression("-", large);
            assertIntegerResult(result, -1000);
        }

        @Test
        @DisplayName("Should return error for negation of non-integers")
        void testNegationTypeError() {
            BaseObject result = ExpressionEvaluator.evalPrefixExpression("-", boolTrue);
            assertErrorResult(result, "unknown operator");
            assertErrorResult(result, "You can only use - operator with INTEGER");

            result = ExpressionEvaluator.evalPrefixExpression("-", stringHello);
            assertErrorResult(result, "unknown operator");
            assertErrorResult(result, "You can only use - operator with INTEGER");

            result = ExpressionEvaluator.evalPrefixExpression("-", nullObject);
            assertErrorResult(result, "unknown operator");
        }

        @Test
        @DisplayName("Should return error for unsupported prefix operators")
        void testUnsupportedPrefixOperators() {
            BaseObject result = ExpressionEvaluator.evalPrefixExpression("+", intFive);
            assertErrorResult(result, "unknown operator");
            assertErrorResult(result, "You can only use ! or - operator");

            result = ExpressionEvaluator.evalPrefixExpression("*", boolTrue);
            assertErrorResult(result, "unknown operator");

            result = ExpressionEvaluator.evalPrefixExpression("&", intFive);
            assertErrorResult(result, "unknown operator");

            result = ExpressionEvaluator.evalPrefixExpression("~", intFive);
            assertErrorResult(result, "unknown operator");
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Tests")
    class EdgeCasesAndBoundaryTests {

        @Test
        @DisplayName("Should handle very large integers")
        void testLargeIntegers() {
            IntegerObject maxLong = new IntegerObject(Long.MAX_VALUE);
            IntegerObject one = new IntegerObject(1);

            BaseObject result = ExpressionEvaluator.evalPrefixExpression("-", maxLong);
            assertIntegerResult(result, -Long.MAX_VALUE);

            result = ExpressionEvaluator.evalIntegerInfixExpression("==", maxLong, maxLong);
            assertBooleanResult(result, true);
        }

        @Test
        @DisplayName("Should handle very long strings")
        void testLongStrings() {
            String longStr = "a".repeat(1000);
            StringObject longString = new StringObject(longStr);
            StringObject anotherLongString = new StringObject(longStr);

            BaseObject result = ExpressionEvaluator.evalStringInfixExpression("==", longString, anotherLongString);
            assertBooleanResult(result, true);

            result = ExpressionEvaluator.evalStringInfixExpression("+", longString, stringHello);
            assertStringResult(result, longStr + "hello");
        }

        @Test
        @DisplayName("Should handle special string characters")
        void testSpecialStringCharacters() {
            StringObject specialString = new StringObject("test\n\t\"'\\");
            StringObject anotherSpecial = new StringObject("test\n\t\"'\\");

            BaseObject result = ExpressionEvaluator.evalStringInfixExpression("==", specialString, anotherSpecial);
            assertBooleanResult(result, true);
        }

        @Test
        @DisplayName("Should handle null object consistently")
        void testNullObjectHandling() {
            BaseObject result = ExpressionEvaluator.evalNotExpression(nullObject);
            assertBooleanResult(result, true);

            result = ExpressionEvaluator.evalAndExpression(nullObject, boolTrue);
            assertBooleanResult(result, false);

            result = ExpressionEvaluator.evalOrExpression(nullObject, boolTrue);
            assertBooleanResult(result, true);
        }

        @Test
        @DisplayName("Should handle chain of operations correctly")
        void testOperationChaining() {
            // Test chaining: ((5 + 3) == 8) && (10 > 5)
            BaseObject addition = ExpressionEvaluator.evalIntegerInfixExpression("+", intFive, new IntegerObject(3));
            BaseObject equality = ExpressionEvaluator.evalIntegerInfixExpression("==", addition, new IntegerObject(8));
            BaseObject comparison = ExpressionEvaluator.evalIntegerInfixExpression(">", intTen, intFive);
            BaseObject result = ExpressionEvaluator.evalAndExpression(equality, comparison);

            assertBooleanResult(result, true);
        }
    }
}