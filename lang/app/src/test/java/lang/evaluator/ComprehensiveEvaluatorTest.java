package lang.evaluator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.junit.jupiter.api.Assertions.*;

import lang.lexer.Lexer;
import lang.parser.LanguageParser;
import lang.ast.statements.Program;
import lang.exec.evaluator.LanguageEvaluator;
import lang.exec.objects.base.BaseObject;
import lang.exec.objects.env.Environment;
import lang.exec.objects.structures.ArrayObject;
import lang.exec.objects.structures.HashObject;
import lang.exec.validator.ObjectValidator;

/**
 * 🧪 Comprehensive Evaluator Test Suite 🧪
 * 
 * This test suite covers every aspect of the language evaluator from first
 * principles:
 * 1. Basic data types and literals
 * 2. Variable operations (let, const, assignment)
 * 3. Arithmetic and logical operations
 * 4. Control flow structures
 * 5. Functions and scope management
 * 6. Data structures (arrays, hashes)
 * 7. Error handling and edge cases
 * 8. Complex integration scenarios
 * 
 * Each test is designed to verify both correct behavior and proper error
 * handling.
 */
public class ComprehensiveEvaluatorTest {

    private LanguageEvaluator evaluator;
    private Environment globalEnvironment;

    @BeforeEach
    void setUp() {
        evaluator = new LanguageEvaluator();
        globalEnvironment = new Environment();
    }

    /**
     * Helper method to evaluate code and return the result
     */
    private BaseObject evaluateCode(String code) {
        Lexer lexer = new Lexer(code);
        LanguageParser parser = new LanguageParser(lexer);
        Program program = parser.parseProgram();

        if (parser.hasErrors()) {
            parser.printErrors();
            fail("Parser errors occurred");
        }

        return evaluator.evaluateProgram(program, globalEnvironment);
    }

    /**
     * Helper method to assert integer value
     */
    private void assertIntegerValue(BaseObject obj, long expected) {
        assertTrue(ObjectValidator.isInteger(obj), "Expected integer object");
        assertEquals(expected, ObjectValidator.asInteger(obj).getValue());
    }

    /**
     * Helper method to assert string value
     */
    private void assertStringValue(BaseObject obj, String expected) {
        assertTrue(ObjectValidator.isString(obj), "Expected string object");
        assertEquals(expected, ObjectValidator.asString(obj).getValue());
    }

    /**
     * Helper method to assert boolean value
     */
    private void assertBooleanValue(BaseObject obj, boolean expected) {
        assertTrue(ObjectValidator.isBoolean(obj), "Expected boolean object");
        assertEquals(expected, ObjectValidator.asBoolean(obj).getValue());
    }

    /**
     * Helper method to assert error
     */
    private void assertError(BaseObject obj, String expectedMessage) {
        assertTrue(ObjectValidator.isError(obj), "Expected error object, got: " + obj.inspect());
        assertTrue(ObjectValidator.asError(obj).getMessage().contains(expectedMessage),
                "Expected error message to contain: " + expectedMessage +
                        ", but was: " + ObjectValidator.asError(obj).getMessage());
    }

    /**
     * Helper method to assert null value
     */
    private void assertNull(BaseObject obj) {
        assertTrue(ObjectValidator.isNull(obj), "Expected null object, got: " + obj.inspect());
    }

    // ==========================================
    // BASIC LITERAL EVALUATION TESTS
    // ==========================================

    @Nested
    @DisplayName("🔢 Basic Literal Evaluation")
    class BasicLiteralTests {

        @Test
        @DisplayName("Integer literals should evaluate correctly")
        void testIntegerLiterals() {
            assertIntegerValue(evaluateCode("42;"), 42);
            assertIntegerValue(evaluateCode("0;"), 0);
            assertIntegerValue(evaluateCode("999999;"), 999999);
        }

        @Test
        @DisplayName("String literals should evaluate correctly")
        void testStringLiterals() {
            assertStringValue(evaluateCode("\"hello\";"), "hello");
            assertStringValue(evaluateCode("\"\";"), "");
            assertStringValue(evaluateCode("\"hello world\";"), "hello world");
            assertStringValue(evaluateCode("\"with\\nescapes\";"), "with\nescapes");
        }

        @Test
        @DisplayName("Boolean literals should evaluate correctly")
        void testBooleanLiterals() {
            assertBooleanValue(evaluateCode("true;"), true);
            assertBooleanValue(evaluateCode("false;"), false);
        }

        @Test
        @DisplayName("Null literal should evaluate correctly")
        void testNullLiteral() {
            assertNull(evaluateCode("null;"));
        }
    }

    // ==========================================
    // ARITHMETIC OPERATIONS TESTS
    // ==========================================

    @Nested
    @DisplayName("➕ Arithmetic Operations")
    class ArithmeticOperationTests {

        @Test
        @DisplayName("Basic arithmetic operations should work correctly")
        void testBasicArithmetic() {
            assertIntegerValue(evaluateCode("5 + 3;"), 8);
            assertIntegerValue(evaluateCode("10 - 4;"), 6);
            assertIntegerValue(evaluateCode("3 * 7;"), 21);
            assertIntegerValue(evaluateCode("15 / 3;"), 5);
            assertIntegerValue(evaluateCode("17 % 5;"), 2);
        }

        @Test
        @DisplayName("Arithmetic precedence should be respected")
        void testArithmeticPrecedence() {
            assertIntegerValue(evaluateCode("2 + 3 * 4;"), 14); // 2 + (3 * 4)
            assertIntegerValue(evaluateCode("(2 + 3) * 4;"), 20); // (2 + 3) * 4
            assertIntegerValue(evaluateCode("10 - 2 * 3;"), 4); // 10 - (2 * 3)
            assertIntegerValue(evaluateCode("20 / 4 + 2;"), 7); // (20 / 4) + 2
        }

        @Test
        @DisplayName("Negative numbers should work correctly")
        void testNegativeNumbers() {
            assertIntegerValue(evaluateCode("-5;"), -5);
            assertIntegerValue(evaluateCode("-(-10);"), 10);
            assertIntegerValue(evaluateCode("5 + (-3);"), 2);
            assertIntegerValue(evaluateCode("-5 * -3;"), 15);
        }

        @Test
        @DisplayName("Complex arithmetic expressions should work")
        void testComplexArithmetic() {
            assertIntegerValue(evaluateCode("((5 + 2) * 3) - (4 / 2);"), 19);
            assertIntegerValue(evaluateCode("1 + 2 + 3 + 4 + 5;"), 15);
            assertIntegerValue(evaluateCode("100 - 50 - 25 - 12;"), 13);
        }

        @Test
        @DisplayName("Division by zero should produce error")
        void testDivisionByZero() {
            // Note: This depends on your implementation - you might handle this differently
            BaseObject result = evaluateCode("10 / 0;");
            // Either should be an error or handle according to your language semantics
            assertError(result, "division by zero");
        }

        @Test
        @DisplayName("String concatenation should work")
        void testStringConcatenation() {
            assertStringValue(evaluateCode("\"hello\" + \" world\";"), "hello world");
            assertStringValue(evaluateCode("\"\" + \"test\";"), "test");
            assertStringValue(evaluateCode("\"a\" + \"b\" + \"c\";"), "abc");
        }

        @Test
        @DisplayName("Type mismatch in arithmetic should produce error")
        void testArithmeticTypeMismatch() {
            assertError(evaluateCode("5 + \"hello\";"),
                    "Invalid operator '+' for types INTEGER and STRING. This operation is not supported.");
            assertError(evaluateCode("\"hello\" - 5;"),
                    "Invalid operator '-' for types STRING and INTEGER. This operation is not supported.");
            assertError(evaluateCode("true * 5;"),
                    "Invalid operator '*' for types BOOLEAN and INTEGER. This operation is not supported.");
        }
    }

    // ==========================================
    // COMPARISON OPERATIONS TESTS
    // ==========================================

    @Nested
    @DisplayName("⚖️ Comparison Operations")
    class ComparisonOperationTests {

        @Test
        @DisplayName("Integer comparisons should work correctly")
        void testIntegerComparisons() {
            assertBooleanValue(evaluateCode("5 == 5;"), true);
            assertBooleanValue(evaluateCode("5 == 3;"), false);
            assertBooleanValue(evaluateCode("5 != 3;"), true);
            assertBooleanValue(evaluateCode("5 != 5;"), false);

            assertBooleanValue(evaluateCode("5 > 3;"), true);
            assertBooleanValue(evaluateCode("3 > 5;"), false);
            assertBooleanValue(evaluateCode("5 < 3;"), false);
            assertBooleanValue(evaluateCode("3 < 5;"), true);

            assertBooleanValue(evaluateCode("5 >= 5;"), true);
            assertBooleanValue(evaluateCode("5 >= 3;"), true);
            assertBooleanValue(evaluateCode("3 >= 5;"), false);

            assertBooleanValue(evaluateCode("5 <= 5;"), true);
            assertBooleanValue(evaluateCode("3 <= 5;"), true);
            assertBooleanValue(evaluateCode("5 <= 3;"), false);
        }

        @Test
        @DisplayName("String comparisons should work correctly")
        void testStringComparisons() {
            assertBooleanValue(evaluateCode("\"hello\" == \"hello\";"), true);
            assertBooleanValue(evaluateCode("\"hello\" == \"world\";"), false);
            assertBooleanValue(evaluateCode("\"hello\" != \"world\";"), true);

            assertBooleanValue(evaluateCode("\"apple\" < \"banana\";"), true);
            assertBooleanValue(evaluateCode("\"zebra\" > \"apple\";"), true);
        }

        @Test
        @DisplayName("Boolean comparisons should work correctly")
        void testBooleanComparisons() {
            assertBooleanValue(evaluateCode("true == true;"), true);
            assertBooleanValue(evaluateCode("true == false;"), false);
            assertBooleanValue(evaluateCode("false != true;"), true);
        }

        @Test
        @DisplayName("Type mismatch in comparisons should produce error")
        void testComparisonTypeMismatch() {
            assertError(evaluateCode("5 == \"5\";"),
                    "Invalid operator '==' for types INTEGER and STRING. This operation is not supported.");
            assertError(evaluateCode("true > 5;"),
                    "Invalid operator '>' for types BOOLEAN and INTEGER. This operation is not supported.");
        }
    }

    // ==========================================
    // LOGICAL OPERATIONS TESTS
    // ==========================================

    @Nested
    @DisplayName("🔗 Logical Operations")
    class LogicalOperationTests {

        @Test
        @DisplayName("Logical AND should work correctly")
        void testLogicalAnd() {
            assertBooleanValue(evaluateCode("true && true;"), true);
            assertBooleanValue(evaluateCode("true && false;"), false);
            assertBooleanValue(evaluateCode("false && true;"), false);
            assertBooleanValue(evaluateCode("false && false;"), false);
        }

        @Test
        @DisplayName("Logical OR should work correctly")
        void testLogicalOr() {
            assertBooleanValue(evaluateCode("true || true;"), true);
            assertBooleanValue(evaluateCode("true || false;"), true);
            assertBooleanValue(evaluateCode("false || true;"), true);
            assertBooleanValue(evaluateCode("false || false;"), false);
        }

        @Test
        @DisplayName("Logical NOT should work correctly")
        void testLogicalNot() {
            assertBooleanValue(evaluateCode("!true;"), false);
            assertBooleanValue(evaluateCode("!false;"), true);
            assertBooleanValue(evaluateCode("!!true;"), true);
            assertBooleanValue(evaluateCode("!!!false;"), true);
        }

        @Test
        @DisplayName("Complex logical expressions should work")
        void testComplexLogicalExpressions() {
            assertBooleanValue(evaluateCode("true && (false || true);"), true);
            assertBooleanValue(evaluateCode("(true && false) || (true && true);"), true);
            assertBooleanValue(evaluateCode("!(true && false);"), true);
        }

        @Test
        @DisplayName("Logical operations with non-boolean values should work with truthiness")
        void testLogicalWithTruthiness() {
            // Test truthiness of different types
            assertBooleanValue(evaluateCode("!0;"), true); // 0 is falsy
            assertBooleanValue(evaluateCode("!5;"), false); // non-zero is truthy
            assertBooleanValue(evaluateCode("!\"\";"), true); // empty string is falsy
            assertBooleanValue(evaluateCode("!\"hello\";"), false); // non-empty string is truthy
        }
    }

    // ==========================================
    // VARIABLE DECLARATION AND ASSIGNMENT TESTS
    // ==========================================

    @Nested
    @DisplayName("📦 Variable Operations")
    class VariableOperationTests {

        @Test
        @DisplayName("Let statements should work correctly")
        void testLetStatements() {
            assertIntegerValue(evaluateCode("let x = 5; x;"), 5);
            assertStringValue(evaluateCode("let name = \"Alice\"; name;"), "Alice");
            assertBooleanValue(evaluateCode("let flag = true; flag;"), true);
        }

        @Test
        @DisplayName("Const statements should work correctly")
        void testConstStatements() {
            assertIntegerValue(evaluateCode("const x = 10; x;"), 10);
            assertStringValue(evaluateCode("const greeting = \"Hello\"; greeting;"), "Hello");
        }

        @Test
        @DisplayName("Variable assignment should work correctly")
        void testVariableAssignment() {
            assertIntegerValue(evaluateCode("let x = 5; x = 10; x;"), 10);
            assertStringValue(evaluateCode("let msg = \"old\"; msg = \"new\"; msg;"), "new");
        }

        @Test
        @DisplayName("Const reassignment should produce error")
        void testConstReassignment() {
            assertError(evaluateCode("const x = 5; x = 10;"), "cannot assign to constant");
        }

        @Test
        @DisplayName("Undefined variable access should produce error")
        void testUndefinedVariable() {
            assertError(evaluateCode("unknownVariable;"), "identifier not found");
        }

        @Test
        @DisplayName("Variable redeclaration in same scope should produce error")
        void testVariableRedeclaration() {
            assertError(evaluateCode("let x = 5; let x = 10;"), "already declared");
        }

        @Test
        @DisplayName("Complex variable expressions should work")
        void testComplexVariableExpressions() {
            assertIntegerValue(evaluateCode("let a = 5; let b = 3; a + b;"), 8);
            assertIntegerValue(evaluateCode("let x = 10; let y = x * 2; y;"), 20);
            assertBooleanValue(evaluateCode("let flag = false; let result = !flag; result;"), true);
        }
    }

    // ==========================================
    // CONTROL FLOW TESTS
    // ==========================================

    @Nested
    @DisplayName("🔀 Control Flow")
    class ControlFlowTests {

        @Test
        @DisplayName("Simple if expressions should work correctly")
        void testSimpleIfExpressions() {
            assertIntegerValue(evaluateCode("if (true) { 5; }"), 5);
            assertNull(evaluateCode("if (false) { 5; }"));
        }

        @Test
        @DisplayName("If-else expressions should work correctly")
        void testIfElseExpressions() {
            assertIntegerValue(evaluateCode("if (true) { 5; } else { 10; }"), 5);
            assertIntegerValue(evaluateCode("if (false) { 5; } else { 10; }"), 10);
        }

        @Test
        @DisplayName("If-elif-else expressions should work correctly")
        void testIfElifElseExpressions() {
            String code = """
                    let x = 5;
                    if (x < 0) {
                        1;
                    } elif (x == 0) {
                        2;
                    } elif (x > 0) {
                        3;
                    } else {
                        4;
                    }
                    """;
            assertIntegerValue(evaluateCode(code), 3);
        }

        @Test
        @DisplayName("Nested if expressions should work correctly")
        void testNestedIfExpressions() {
            String code = """
                    let x = 5;
                    let y = 10;
                    if (x > 0) {
                        if (y > 0) {
                            x + y;
                        } else {
                            x;
                        }
                    } else {
                        0;
                    }
                    """;
            assertIntegerValue(evaluateCode(code), 15);
        }

        @Test
        @DisplayName("While loops should work correctly")
        void testWhileLoops() {
            String code = """
                    let i = 0;
                    let sum = 0;
                    while (i < 5) {
                        sum = sum + i;
                        i = i + 1;
                    }
                    sum;
                    """;
            assertIntegerValue(evaluateCode(code), 10); // 0+1+2+3+4 = 10
        }

        @Test
        @DisplayName("For loops should work correctly")
        void testForLoops() {
            String code = """
                    let sum = 0;
                    for (let i = 1; i <= 5; i = i + 1) {
                        sum = sum + i;
                    }
                    sum;
                    """;
            assertIntegerValue(evaluateCode(code), 15); // 1+2+3+4+5 = 15
        }

        @Test
        @DisplayName("Break statements should work correctly")
        void testBreakStatements() {
            String code = """
                    let i = 0;
                    while (true) {
                        if (i >= 3) {
                            break;
                        }
                        i = i + 1;
                    }
                    i;
                    """;
            assertIntegerValue(evaluateCode(code), 3);
        }

        @Test
        @DisplayName("Continue statements should work correctly")
        void testContinueStatements() {
            String code = """
                    let sum = 0;
                    for (let i = 1; i <= 5; i = i + 1) {
                        if (i == 3) {
                            continue;
                        }
                        sum = sum + i;
                    }
                    sum;
                    """;
            assertIntegerValue(evaluateCode(code), 12); // 1+2+4+5 = 12 (skips 3)
        }

        @Test
        @DisplayName("Return statements should work correctly")
        void testReturnStatements() {
            String code = """
                    let x = 5;
                    if (x > 0) {
                        return x * 2;
                    }
                    x;
                    """;
            assertIntegerValue(evaluateCode(code), 10);
        }
    }

    // ==========================================
    // FUNCTION TESTS
    // ==========================================

    @Nested
    @DisplayName("🔧 Function Operations")
    class FunctionOperationTests {

        @Test
        @DisplayName("Function calls should work correctly")
        void testFunctionCalls() {
            String code = """
                    let add = fn(a, b) { a + b; };
                    add(3, 4);
                    """;
            assertIntegerValue(evaluateCode(code), 7);
        }

        @Test
        @DisplayName("Functions with no parameters should work")
        void testNoParameterFunctions() {
            String code = """
                    let getValue = fn() { 42; };
                    getValue();
                    """;
            assertIntegerValue(evaluateCode(code), 42);
        }

        @Test
        @DisplayName("Functions with explicit return should work")
        void testFunctionsWithReturn() {
            String code = """
                    let multiply = fn(x, y) {
                        let result = x * y;
                        return result;
                    };
                    multiply(6, 7);
                    """;
            assertIntegerValue(evaluateCode(code), 42);
        }

        @Test
        @DisplayName("Recursive functions should work correctly")
        void testRecursiveFunctions() {
            String code = """
                    let factorial = fn(n) {
                        if (n <= 1) {
                            return 1;
                        } else {
                            return n * factorial(n - 1);
                        }
                    };
                    factorial(5);
                    """;
            assertIntegerValue(evaluateCode(code), 120); // 5! = 120
        }

        @Test
        @DisplayName("Closures should work correctly")
        void testClosures() {
            String code = """
                    let makeAdder = fn(x) {
                        return fn(y) { x + y; };
                    };
                    let addFive = makeAdder(5);
                    addFive(3);
                    """;
            assertIntegerValue(evaluateCode(code), 8);
        }

        @Test
        @DisplayName("Functions should have access to outer scope")
        void testFunctionScope() {
            String code = """
                    let globalVar = 100;
                    let getGlobal = fn() { globalVar; };
                    getGlobal();
                    """;
            assertIntegerValue(evaluateCode(code), 100);
        }

        @Test
        @DisplayName("Function parameter shadowing should work correctly")
        void testParameterShadowing() {
            String code = """
                    let x = 10;
                    let func = fn(x) { x + 1; };
                    func(5);
                    """;
            assertIntegerValue(evaluateCode(code), 6); // Uses parameter x (5), not global x (10)
        }

        @Test
        @DisplayName("Wrong number of arguments should produce error")
        void testWrongArgumentCount() {
            String code = """
                    let add = fn(a, b) { a + b; };
                    add(5);
                    """;
            assertError(evaluateCode(code), "Wrong number of arguments. Expected 2, got 1");
        }
    }

    // ==========================================
    // ARRAY TESTS
    // ==========================================

    @Nested
    @DisplayName("📋 Array Operations")
    class ArrayOperationTests {

        @Test
        @DisplayName("Array literals should be created correctly")
        void testArrayLiterals() {
            BaseObject result = evaluateCode("[1, 2, 3];");
            assertTrue(ObjectValidator.isArray(result), "Expected array object");
            assertEquals(3, ObjectValidator.asArray(result).getElements().size());
        }

        @Test
        @DisplayName("Empty arrays should work correctly")
        void testEmptyArrays() {
            BaseObject result = evaluateCode("[];");
            assertTrue(ObjectValidator.isArray(result), "Expected array object");
            assertEquals(0, ObjectValidator.asArray(result).getElements().size());
        }

        @Test
        @DisplayName("Mixed type arrays should work correctly")
        void testMixedTypeArrays() {
            BaseObject result = evaluateCode("[1, \"hello\", true];");
            assertTrue(ObjectValidator.isArray(result), "Expected array object");
            ArrayObject array = ObjectValidator.asArray(result);
            assertEquals(3, array.getElements().size());

            assertTrue(ObjectValidator.isInteger(array.getElements().get(0)));
            assertTrue(ObjectValidator.isString(array.getElements().get(1)));
            assertTrue(ObjectValidator.isBoolean(array.getElements().get(2)));
        }

        @Test
        @DisplayName("Nested arrays should work correctly")
        void testNestedArrays() {
            BaseObject result = evaluateCode("[[1, 2], [3, 4]];");
            assertTrue(ObjectValidator.isArray(result), "Expected array object");
            ArrayObject outerArray = ObjectValidator.asArray(result);
            assertEquals(2, outerArray.getElements().size());

            assertTrue(ObjectValidator.isArray(outerArray.getElements().get(0)));
            assertTrue(ObjectValidator.isArray(outerArray.getElements().get(1)));
        }

        @Test
        @DisplayName("Array indexing should work correctly")
        void testArrayIndexing() {
            assertIntegerValue(evaluateCode("let a = [1, 2, 3]; a[0];"), 1);
            assertIntegerValue(evaluateCode("let b = [1, 2, 3]; b[1];"), 2);
            assertIntegerValue(evaluateCode("let c = [1, 2, 3]; c[2];"), 3);
            assertError(evaluateCode("let d = [1, 2, 3]; d[10];"), "Index out of bounds: 10 for array of size 3");
        }

        @Test
        @DisplayName("Array indexing with variables should work")
        void testArrayIndexingWithVariables() {
            String code = """
                    let arr = [10, 20, 30];
                    let index = 1;
                    arr[index];
                    """;
            assertIntegerValue(evaluateCode(code), 20);
        }

        @Test
        @DisplayName("Array indexing out of bounds should handle gracefully")
        void testArrayIndexOutOfBounds() {
            String code = """
                    let a = [1, 2, 3];
                    a[10];
                    """;
            assertError(evaluateCode(code), "Index out of bounds: 10 for array of size 3");
        }

        @Test
        @DisplayName("Array expressions as elements should work")
        void testArrayExpressionElements() {
            String code = """
                    let x = 5;
                    [x + 1, x * 2, x - 1];
                    """;
            BaseObject result = evaluateCode(code);
            assertTrue(ObjectValidator.isArray(result));
            ArrayObject array = ObjectValidator.asArray(result);

            assertIntegerValue(array.getElements().get(0), 6);
            assertIntegerValue(array.getElements().get(1), 10);
            assertIntegerValue(array.getElements().get(2), 4);
        }
    }

    // ==========================================
    // HASH/OBJECT TESTS
    // ==========================================

    @Nested
    @DisplayName("🗃️ Hash Operations")
    class HashOperationTests {

        @Test
        @DisplayName("Hash literals should be created correctly")
        void testHashLiterals() {
            BaseObject result = evaluateCode("let a = {\"name\": \"Alice\", \"age\": 30}; a;");
            assertTrue(ObjectValidator.isHash(result), "Expected hash object");
            assertEquals(2, ObjectValidator.asHash(result).getPairs().size());
        }

        @Test
        @DisplayName("Empty hashes should work correctly")
        void testEmptyHashes() {
            BaseObject result = evaluateCode("let a = {}; a;");
            assertTrue(ObjectValidator.isHash(result), "Expected hash object");
            assertEquals(0, ObjectValidator.asHash(result).getPairs().size());
        }

        @Test
        @DisplayName("Hash access should work correctly")
        void testHashAccess() {
            String code = """
                    let person = {"name": "Alice", "age": 30};
                    person["name"];
                    """;
            assertStringValue(evaluateCode(code), "Alice");
        }

        @Test
        @DisplayName("Hash with integer keys should work")
        void testHashIntegerKeys() {
            String code = """
                    let a = {1: \"one\", 2: \"two\"};
                    a;
                    """;
            BaseObject result = evaluateCode(code);
            assertTrue(ObjectValidator.isHash(result), "Expected hash object");
            HashObject hash = ObjectValidator.asHash(result);
            assertTrue(hash.getPairs().containsKey("1"));
            assertTrue(hash.getPairs().containsKey("2"));
        }

        @Test
        @DisplayName("Hash with expression values should work")
        void testHashExpressionValues() {
            String code = """
                    let x = 10;
                    let a = {"doubled": x * 2, "halved": x / 2};
                    a;
                    """;
            BaseObject result = evaluateCode(code);
            assertTrue(ObjectValidator.isHash(result));
            HashObject hash = ObjectValidator.asHash(result);

            assertIntegerValue(hash.getPairs().get("doubled"), 20);
            assertIntegerValue(hash.getPairs().get("halved"), 5);
        }

        @Test
        @DisplayName("Nested hashes should work correctly")
        void testNestedHashes() {
            String code = """
                    let a = {"outer": {"inner": "value"}};
                    a;
                    """;
            BaseObject result = evaluateCode(code);
            assertTrue(ObjectValidator.isHash(result));
        }

        @Test
        @DisplayName("Hash access with non-existent key should handle gracefully")
        void testHashNonExistentKey() {
            String code = """
                    let obj = {"name": "Alice"};
                    obj["nonexistent"];
                    """;
            assertNull(evaluateCode(code));
        }
    }

    // ==========================================
    // SCOPE AND ENVIRONMENT TESTS
    // ==========================================

    @Nested
    @DisplayName("🔒 Scope Management")
    class ScopeManagementTests {

        @Test
        @DisplayName("Block scope should work correctly")
        void testBlockScope() {
            String code = """
                    let x = 1;
                    {
                        let x = 2;
                        x;
                    }
                    """;
            assertIntegerValue(evaluateCode(code), 2);
        }

        @Test
        @DisplayName("Variable shadowing should work correctly")
        void testVariableShadowing() {
            String code = """
                    let x = 1;
                    {
                        let x = 2;
                        {
                            let x = 3;
                            x;
                        }
                    }
                    """;
            assertIntegerValue(evaluateCode(code), 3);
        }

        @Test
        @DisplayName("Outer scope access should work correctly")
        void testOuterScopeAccess() {
            String code = """
                    let outer = 100;
                    {
                        let inner = 200;
                        outer + inner;
                    }
                    """;
            assertIntegerValue(evaluateCode(code), 300);
        }

        @Test
        @DisplayName("Function scope should be isolated")
        void testFunctionScopeIsolation() {
            String code = """
                    let x = 10;
                    let func = fn() {
                        let x = 20;
                        x;
                    };
                    func();
                    """;
            assertIntegerValue(evaluateCode(code), 20);
        }

        @Test
        @DisplayName("Loop scope should work correctly")
        void testLoopScope() {
            String code = """
                    let sum = 0;
                    for (let i = 1; i <= 3; i = i + 1) {
                        let multiplier = 2;
                        sum = sum + (i * multiplier);
                    }
                    sum;
                    """;
            assertIntegerValue(evaluateCode(code), 12); // (1*2) + (2*2) + (3*2) = 12
        }
    }

    // ==========================================
    // ERROR HANDLING TESTS
    // ==========================================

    @Nested
    @DisplayName("🚨 Error Handling")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Type errors should be handled gracefully")
        void testTypeErrors() {
            assertError(evaluateCode("5 + true;"),
                    "Invalid operator '+' for types INTEGER and BOOLEAN. This operation is not supported.");
            assertError(evaluateCode("\"hello\" * 3;"),
                    "Invalid operator '*' for types STRING and INTEGER. This operation is not supported.");
            assertError(evaluateCode("5 && 3;"),
                    "Invalid operator '&&' for types INTEGER and INTEGER. This operation is not supported.");
        }

        @Test
        @DisplayName("Runtime errors should propagate correctly")
        void testRuntimeErrorPropagation() {
            String code = """
                    let func = fn() {
                        unknownVariable;
                    };
                    func();
                    """;
            assertError(evaluateCode(code), "identifier not found");
        }

        @Test
        @DisplayName("Errors in complex expressions should be handled")
        void testErrorsInComplexExpressions() {
            assertError(evaluateCode("5 + (3 * unknownVar);"), "identifier not found");
            assertError(evaluateCode("[1, 2, unknownVar];"), "identifier not found");
            assertError(evaluateCode("let a = {\"key\": unknownVar}; a;"), "identifier not found");
        }
    }

    // ==========================================
    // COMPLEX INTEGRATION TESTS
    // ==========================================

    @Nested
    @DisplayName("🏗️ Complex Integration Scenarios")
    class ComplexIntegrationTests {

        @Test
        @DisplayName("Fibonacci function should work correctly")
        void testFibonacciFunction() {
            String code = """
                    let fib = fn(n) {
                        if (n <= 1) {
                            return n;
                        }
                        return fib(n - 1) + fib(n - 2);
                    };
                    fib(10);
                    """;
            assertIntegerValue(evaluateCode(code), 55);
        }

        @Test
        @DisplayName("Array processing with higher-order functions")
        void testArrayProcessing() {
            String code = """
                    let map = fn(arr, func) {
                        let result = [];
                        for (let i = 0; i < len(arr); i = i + 1) {
                            result = push(result, func(arr[i]));
                        }
                        result;
                    };

                    let double = fn(x) { x * 2; };
                    let numbers = [1, 2, 3, 4, 5];
                    map(numbers, double);
                    """;
            // This test assumes you have built-in functions like len() and push()
            // Adapt according to your built-in functions
        }

        @Test
        @DisplayName("Complex object manipulation")
        void testComplexObjectManipulation() {
            String code = """
                    let person = {
                        "name": "Alice",
                        "age": 30,
                        "address": {
                            "street": "123 Main St",
                            "city": "Anytown"
                        }
                    };
                    person["address"]["city"];
                    """;
            assertStringValue(evaluateCode(code), "Anytown");
        }

        @Test
        @DisplayName("Factory function pattern")
        void testFactoryFunction() {
            String code = """
                    let createPerson = fn(name, age) {
                        let person = {
                            "name": name,
                            "age": age,
                            "greet": fn() {
                                "Hello, I'm " + name;
                            }
                        };

                        return person;
                    };

                    let alice = createPerson("Alice", 30);
                    alice["greet"]();
                    """;
            assertStringValue(evaluateCode(code), "Hello, I'm Alice");
        }

        @Test
        @DisplayName("Counter with closure")
        void testCounterWithClosure() {
            String code = """
                    let makeCounter = fn() {
                        let count = 0;
                        fn() {
                            count = count + 1;
                            count;
                        };
                    };

                    let counter = makeCounter();
                    counter();
                    counter();
                    counter();
                    """;
            assertIntegerValue(evaluateCode(code), 3);
        }

        @Test
        @DisplayName("Complex control flow with early returns")
        void testComplexControlFlow() {
            String code = """
                    let processNumber = fn(n) {
                        if (n < 0) {
                            return "negative";
                        }

                        if (n == 0) {
                            return "zero";
                        }

                        if (n % 2 == 0) {
                            return "even";
                        } else {
                            return "odd";
                        }
                    };

                    [processNumber(-5), processNumber(0), processNumber(4), processNumber(7)];
                    """;
            BaseObject result = evaluateCode(code);
            assertTrue(ObjectValidator.isArray(result));
            ArrayObject array = ObjectValidator.asArray(result);

            assertStringValue(array.getElements().get(0), "negative");
            assertStringValue(array.getElements().get(1), "zero");
            assertStringValue(array.getElements().get(2), "even");
            assertStringValue(array.getElements().get(3), "odd");
        }

        @Test
        @DisplayName("Nested loops with break and continue")
        void testNestedLoopsWithControlFlow() {
            String code = """
                    let result = [];
                    for (let i = 1; i <= 3; i = i + 1) {
                        for (let j = 1; j <= 3; j = j + 1) {
                            if (i == 2 && j == 2) {
                                continue;
                            }
                            if (i == 3 && j == 1) {
                                break;
                            }
                            result = push(result, i * 10 + j);
                        }
                    }
                    result;
                    """;
            // This test assumes you have a built-in push() function
            // Adapt according to your built-in functions
        }

        @Test
        @DisplayName("Variable scope complexity")
        void testVariableScopeComplexity() {
            String code = """
                    let x = 1;
                    let func1 = fn() {
                        let x = 2;
                        let func2 = fn() {
                            let x = 3;
                            let func3 = fn() {
                                x + 10;
                            };
                            func3();
                        };
                        func2();
                    };
                    func1();
                    """;
            assertIntegerValue(evaluateCode(code), 13);
        }
    }

    // ==========================================
    // EDGE CASE TESTS
    // ==========================================

    @Nested
    @DisplayName("🎯 Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Empty program should return null")
        void testEmptyProgram() {
            assertNull(evaluateCode(""));
        }

        @Test
        @DisplayName("Multiple expressions should return last one")
        void testMultipleExpressions() {
            assertIntegerValue(evaluateCode("1; 2; 3;"), 3);
        }

        @Test
        @DisplayName("Very large numbers should work correctly")
        void testLargeNumbers() {
            assertIntegerValue(evaluateCode("999999999;"), 999999999);
        }

        @Test
        @DisplayName("Deeply nested expressions should work")
        void testDeeplyNestedExpressions() {
            assertIntegerValue(evaluateCode("((((5))));"), 5);
            assertIntegerValue(evaluateCode("1 + (2 + (3 + (4 + 5)));"), 15);
        }

        @Test
        @DisplayName("Long identifier names should work")
        void testLongIdentifierNames() {
            String code = """
                    let veryLongVariableNameThatShouldStillWork = 42;
                    veryLongVariableNameThatShouldStillWork;
                    """;
            assertIntegerValue(evaluateCode(code), 42);
        }

        @Test
        @DisplayName("Special characters in strings should work")
        void testSpecialCharactersInStrings() {
            assertStringValue(evaluateCode("\"line1\\nline2\\ttabbed\";"), "line1\nline2\ttabbed");
            assertStringValue(evaluateCode("\"quote: \\\"hello\\\"\";"), "quote: \"hello\"");
        }

        @Test
        @DisplayName("Zero and negative numbers in various contexts")
        void testZeroAndNegativeNumbers() {
            assertIntegerValue(evaluateCode("0 + 0;"), 0);
            assertIntegerValue(evaluateCode("-0;"), 0);
            assertIntegerValue(evaluateCode("5 + (-5);"), 0);
            assertBooleanValue(evaluateCode("-1 < 0;"), true);
        }

        @Test
        @DisplayName("Boolean arithmetic edge cases")
        void testBooleanArithmeticEdgeCases() {
            // These might produce errors or have specific behavior in your language
            BaseObject result1 = evaluateCode("true + false;");
            BaseObject result2 = evaluateCode("!null;");
            // Test according to your language's semantics
        }
    }

    // ==========================================
    // PERFORMANCE AND STRESS TESTS
    // ==========================================

    @Nested
    @DisplayName("⚡ Performance Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Deep recursion should work within limits")
        void testDeepRecursion() {
            String code = """
                    let countdown = fn(n) {
                        if (n <= 0) {
                            return 0;
                        }
                        return countdown(n - 1);
                    };
                    countdown(100);
                    """;
            assertIntegerValue(evaluateCode(code), 0);
        }

        @Test
        @DisplayName("Large arrays should work correctly")
        void testLargeArrays() {
            StringBuilder codeBuilder = new StringBuilder();
            codeBuilder.append("let arr = [");
            for (int i = 0; i < 100; i++) {
                if (i > 0)
                    codeBuilder.append(", ");
                codeBuilder.append(i);
            }
            codeBuilder.append("]; arr[50];");

            assertIntegerValue(evaluateCode(codeBuilder.toString()), 50);
        }

        @Test
        @DisplayName("Complex nested structures should work")
        void testComplexNestedStructures() {
            String code = """
                    let complex = {
                        "level1": {
                            "level2": {
                                "level3": [
                                    {"value": 42},
                                    {"value": 84}
                                ]
                            }
                        }
                    };
                    complex["level1"]["level2"]["level3"][1]["value"];
                    """;
            assertIntegerValue(evaluateCode(code), 84);
        }
    }
}