package lang.builtins;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.junit.jupiter.api.Assertions.*;
import lang.exec.builtins.BuiltinFunctions;

import java.util.*;
import java.util.stream.IntStream;

import lang.exec.objects.*;
import lang.exec.objects.base.BaseObject;
import lang.exec.objects.base.ObjectType;
import lang.exec.objects.error.ErrorObject;
import lang.exec.objects.functions.BuiltinObject;
import lang.exec.objects.literals.BooleanObject;
import lang.exec.objects.literals.IntegerObject;
import lang.exec.objects.literals.NullObject;
import lang.exec.objects.literals.StringObject;
import lang.exec.objects.structures.ArrayObject;
import lang.exec.objects.structures.HashObject;

/**
 * 🧪 Comprehensive Test Suite for Builtin Functions 🧪
 * 
 * This test suite rigorously tests all builtin functions from first principles.
 * Each test category covers:
 * - ✅ Normal usage cases
 * - 🔍 Edge cases and boundary conditions
 * - 🚨 Error cases and invalid inputs
 * - 🎯 Type validation and conversions
 * - 📊 Performance considerations for large inputs
 * 
 * The tests are organized by functional categories to match the builtin
 * structure.
 */
@DisplayName("Builtin Functions Test Suite")
class BuiltinFunctionsTest {

    private Map<String, BuiltinObject> builtins;

    @BeforeEach
    void setUp() {
        // Initialize the builtin functions before each test
        builtins = BuiltinFunctions.BUILTINS;
        assertNotNull(builtins, "Builtins map should not be null");
        assertFalse(builtins.isEmpty(), "Builtins map should not be empty");
    }

    // ============================================================================
    // HELPER METHODS FOR TEST SETUP
    // ============================================================================

    /**
     * Helper method to call a builtin function and get the result
     */
    private BaseObject callBuiltin(String functionName, BaseObject... args) {
        BuiltinObject function = builtins.get(functionName);
        assertNotNull(function, "Function " + functionName + " should exist");
        return function.getFunction().apply(args);
    }

    /**
     * Helper to create an IntegerObject
     */
    private IntegerObject integer(long value) {
        return new IntegerObject(value);
    }

    /**
     * Helper to create a StringObject
     */
    private StringObject string(String value) {
        return new StringObject(value);
    }

    /**
     * Helper to create a BooleanObject
     */
    private BooleanObject bool(boolean value) {
        return new BooleanObject(value);
    }

    /**
     * Helper to create an ArrayObject
     */
    private ArrayObject array(BaseObject... elements) {
        return new ArrayObject(Arrays.asList(elements));
    }

    /**
     * Helper to create a HashObject
     */
    private HashObject hash(String... keyValuePairs) {
        if (keyValuePairs.length % 2 != 0) {
            throw new IllegalArgumentException("Key-value pairs must be even");
        }

        Map<String, BaseObject> pairs = new LinkedHashMap<>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            pairs.put(keyValuePairs[i], string(keyValuePairs[i + 1]));
        }
        return new HashObject(pairs);
    }

    /**
     * Helper to assert that a result is an error with a specific message
     */
    private void assertError(BaseObject result, String expectedMessage) {
        assertEquals(ObjectType.ERROR, result.type(), "Result should be an error");
        ErrorObject error = (ErrorObject) result;
        assertTrue(error.getMessage().contains(expectedMessage),
                "Error message should contain: " + expectedMessage + ", but was: " + error.getMessage());
    }

    /**
     * Helper to assert that a result is an integer with a specific value
     */
    private void assertInteger(BaseObject result, long expectedValue) {
        assertEquals(ObjectType.INTEGER, result.type(), "Result should be an integer");
        IntegerObject intObj = (IntegerObject) result;
        assertEquals(expectedValue, intObj.getValue(), "Integer value should match");
    }

    /**
     * Helper to assert that a result is a string with a specific value
     */
    private void assertString(BaseObject result, String expectedValue) {
        assertEquals(ObjectType.STRING, result.type(), "Result should be a string");
        StringObject strObj = (StringObject) result;
        assertEquals(expectedValue, strObj.getValue(), "String value should match");
    }

    /**
     * Helper to assert that a result is a boolean with a specific value
     */
    private void assertBoolean(BaseObject result, boolean expectedValue) {
        assertEquals(ObjectType.BOOLEAN, result.type(), "Result should be a boolean");
        BooleanObject boolObj = (BooleanObject) result;
        assertEquals(expectedValue, boolObj.getValue(), "Boolean value should match");
    }

    // ============================================================================
    // 1. CORE DATA OPERATIONS TESTS
    // ============================================================================

    @Nested
    @DisplayName("Core Data Operations")
    class CoreDataOperationsTest {

        @Nested
        @DisplayName("len() function")
        class LenFunctionTest {

            @Test
            @DisplayName("should return length of arrays")
            void testArrayLength() {
                // Test empty array
                assertInteger(callBuiltin("len", array()), 0);

                // Test arrays of various sizes
                assertInteger(callBuiltin("len", array(integer(1))), 1);
                assertInteger(callBuiltin("len", array(integer(1), integer(2), integer(3))), 3);

                // Test large array
                BaseObject[] largeArray = IntStream.range(0, 1000)
                        .mapToObj(i -> integer(i))
                        .toArray(BaseObject[]::new);
                assertInteger(callBuiltin("len", array(largeArray)), 1000);
            }

            @Test
            @DisplayName("should return length of strings")
            void testStringLength() {
                // Test empty string
                assertInteger(callBuiltin("len", string("")), 0);

                // Test various string lengths
                assertInteger(callBuiltin("len", string("a")), 1);
                assertInteger(callBuiltin("len", string("hello")), 5);
                assertInteger(callBuiltin("len", string("Hello, World!")), 13);

                // Test string with special characters
                assertInteger(callBuiltin("len", string("café")), 4);
                assertInteger(callBuiltin("len", string("line1\nline2")), 11);

                // Test very long string
                String longString = "a".repeat(10000);
                assertInteger(callBuiltin("len", string(longString)), 10000);
            }

            @Test
            @DisplayName("should return length of hash objects")
            void testHashLength() {
                // Test empty hash
                assertInteger(callBuiltin("len", hash()), 0);

                // Test hashes of various sizes
                assertInteger(callBuiltin("len", hash("key1", "value1")), 1);
                assertInteger(callBuiltin("len", hash("key1", "value1", "key2", "value2")), 2);

                // Test hash with many entries
                Map<String, BaseObject> largePairs = new LinkedHashMap<>();
                for (int i = 0; i < 100; i++) {
                    largePairs.put("key" + i, string("value" + i));
                }
                HashObject largeHash = new HashObject(largePairs);
                assertInteger(callBuiltin("len", largeHash), 100);
            }

            @Test
            @DisplayName("should handle error cases")
            void testLenErrors() {
                // Wrong number of arguments
                assertError(callBuiltin("len"), "wrong number of arguments");
                assertError(callBuiltin("len", integer(1), integer(2)), "wrong number of arguments");

                // Unsupported types
                assertError(callBuiltin("len", integer(42)), "not supported");
                assertError(callBuiltin("len", bool(true)), "not supported");
                assertError(callBuiltin("len", NullObject.INSTANCE), "not supported");
            }
        }

        @Nested
        @DisplayName("type() function")
        class TypeFunctionTest {

            @Test
            @DisplayName("should return correct type names")
            void testTypeIdentification() {
                // Test all basic types
                assertString(callBuiltin("type", integer(42)), "INTEGER");
                assertString(callBuiltin("type", string("hello")), "STRING");
                assertString(callBuiltin("type", bool(true)), "BOOLEAN");
                assertString(callBuiltin("type", NullObject.INSTANCE), "NULL");
                assertString(callBuiltin("type", array()), "ARRAY");
                assertString(callBuiltin("type", hash()), "HASH");

                // Test with various values
                assertString(callBuiltin("type", integer(0)), "INTEGER");
                assertString(callBuiltin("type", integer(-1)), "INTEGER");
                assertString(callBuiltin("type", string("")), "STRING");
                assertString(callBuiltin("type", bool(false)), "BOOLEAN");
            }

            @Test
            @DisplayName("should handle error cases")
            void testTypeErrors() {
                // Wrong number of arguments
                assertError(callBuiltin("type"), "wrong number of arguments");
                assertError(callBuiltin("type", integer(1), integer(2)), "wrong number of arguments");
            }
        }

        @Nested
        @DisplayName("str() function")
        class StrFunctionTest {

            @Test
            @DisplayName("should convert values to strings")
            void testStringConversion() {
                // Test integer conversion
                assertString(callBuiltin("str", integer(42)), "42");
                assertString(callBuiltin("str", integer(0)), "0");
                assertString(callBuiltin("str", integer(-123)), "-123");

                // Test boolean conversion
                assertString(callBuiltin("str", bool(true)), "true");
                assertString(callBuiltin("str", bool(false)), "false");

                // Test null conversion
                assertString(callBuiltin("str", NullObject.INSTANCE), "null");

                // Test string conversion (identity)
                assertString(callBuiltin("str", string("hello")), "hello");
                assertString(callBuiltin("str", string("")), "");
            }

            @Test
            @DisplayName("should handle error cases")
            void testStrErrors() {
                // Wrong number of arguments
                assertError(callBuiltin("str"), "wrong number of arguments");
                assertError(callBuiltin("str", integer(1), integer(2)), "wrong number of arguments");
            }
        }

        @Nested
        @DisplayName("int() function")
        class IntFunctionTest {

            @Test
            @DisplayName("should convert strings to integers")
            void testStringToInt() {
                // Test valid integer strings
                assertInteger(callBuiltin("int", string("42")), 42);
                assertInteger(callBuiltin("int", string("0")), 0);
                assertInteger(callBuiltin("int", string("-123")), -123);
                assertInteger(callBuiltin("int", string("999999")), 999999);

                // Test leading/trailing whitespace (should fail)
                assertError(callBuiltin("int", string(" 42 ")), "cannot convert");
                assertError(callBuiltin("int", string("42x")), "cannot convert");
                assertError(callBuiltin("int", string("abc")), "cannot convert");
                assertError(callBuiltin("int", string("")), "cannot convert");
            }

            @Test
            @DisplayName("should handle integer identity")
            void testIntegerIdentity() {
                // Integer should return itself
                IntegerObject original = integer(42);
                BaseObject result = callBuiltin("int", original);
                assertSame(original, result, "Integer conversion should return the same object");
            }

            @Test
            @DisplayName("should handle error cases")
            void testIntErrors() {
                // Wrong number of arguments
                assertError(callBuiltin("int"), "wrong number of arguments");
                assertError(callBuiltin("int", integer(1), integer(2)), "wrong number of arguments");

                // Unsupported types
                assertError(callBuiltin("int", bool(true)), "not supported");
                assertError(callBuiltin("int", array()), "not supported");
            }
        }

        @Nested
        @DisplayName("bool() function")
        class BoolFunctionTest {

            @Test
            @DisplayName("should convert values using truthiness rules")
            void testBooleanConversion() {
                // Test integer truthiness
                assertBoolean(callBuiltin("bool", integer(0)), false);
                assertBoolean(callBuiltin("bool", integer(1)), true);
                assertBoolean(callBuiltin("bool", integer(-1)), true);
                assertBoolean(callBuiltin("bool", integer(42)), true);

                // Test string truthiness
                assertBoolean(callBuiltin("bool", string("")), false);
                assertBoolean(callBuiltin("bool", string("hello")), true);
                assertBoolean(callBuiltin("bool", string("0")), true); // Non-empty string is truthy

                // Test boolean identity
                assertBoolean(callBuiltin("bool", bool(true)), true);
                assertBoolean(callBuiltin("bool", bool(false)), false);

                // Test null
                assertBoolean(callBuiltin("bool", NullObject.INSTANCE), false);

                // Test array truthiness
                assertBoolean(callBuiltin("bool", array()), false);
                assertBoolean(callBuiltin("bool", array(integer(1))), true);

                // Test hash truthiness
                assertBoolean(callBuiltin("bool", hash()), false);
                assertBoolean(callBuiltin("bool", hash("key", "value")), true);
            }

            @Test
            @DisplayName("should handle error cases")
            void testBoolErrors() {
                // Wrong number of arguments
                assertError(callBuiltin("bool"), "wrong number of arguments");
                assertError(callBuiltin("bool", integer(1), integer(2)), "wrong number of arguments");
            }
        }
    }

    // ============================================================================
    // 2. ARRAY OPERATIONS TESTS
    // ============================================================================

    @Nested
    @DisplayName("Array Operations")
    class ArrayOperationsTest {

        @Nested
        @DisplayName("first() function")
        class FirstFunctionTest {

            @Test
            @DisplayName("should return first element of arrays")
            void testFirstElement() {
                // Test single element
                assertInteger(callBuiltin("first", array(integer(42))), 42);

                // Test multiple elements
                assertInteger(callBuiltin("first", array(integer(1), integer(2), integer(3))), 1);

                // Test mixed types
                BaseObject result = callBuiltin("first", array(string("hello"), integer(42)));
                assertString(result, "hello");
            }

            @Test
            @DisplayName("should return null for empty arrays")
            void testEmptyArray() {
                BaseObject result = callBuiltin("first", array());
                assertEquals(ObjectType.NULL, result.type());
            }

            @Test
            @DisplayName("should handle error cases")
            void testFirstErrors() {
                assertError(callBuiltin("first"), "wrong number of arguments");
                assertError(callBuiltin("first", integer(1), integer(2)), "wrong number of arguments");
                assertError(callBuiltin("first", integer(42)), "must be ARRAY");
                assertError(callBuiltin("first", string("hello")), "must be ARRAY");
            }
        }

        @Nested
        @DisplayName("last() function")
        class LastFunctionTest {

            @Test
            @DisplayName("should return last element of arrays")
            void testLastElement() {
                // Test single element
                assertInteger(callBuiltin("last", array(integer(42))), 42);

                // Test multiple elements
                assertInteger(callBuiltin("last", array(integer(1), integer(2), integer(3))), 3);

                // Test mixed types
                BaseObject result = callBuiltin("last", array(string("hello"), integer(42)));
                assertInteger(result, 42);
            }

            @Test
            @DisplayName("should return null for empty arrays")
            void testEmptyArray() {
                BaseObject result = callBuiltin("last", array());
                assertEquals(ObjectType.NULL, result.type());
            }

            @Test
            @DisplayName("should handle error cases")
            void testLastErrors() {
                assertError(callBuiltin("last"), "wrong number of arguments");
                assertError(callBuiltin("last", integer(1), integer(2)), "wrong number of arguments");
                assertError(callBuiltin("last", integer(42)), "must be ARRAY");
            }
        }

        @Nested
        @DisplayName("rest() function")
        class RestFunctionTest {

            @Test
            @DisplayName("should return array without first element")
            void testRestElements() {
                // Test multiple elements
                BaseObject result = callBuiltin("rest", array(integer(1), integer(2), integer(3)));
                assertEquals(ObjectType.ARRAY, result.type());
                ArrayObject restArray = (ArrayObject) result;
                assertEquals(2, restArray.getElements().size());
                assertInteger(restArray.getElements().get(0), 2);
                assertInteger(restArray.getElements().get(1), 3);

                // Test single element
                result = callBuiltin("rest", array(integer(42)));
                assertEquals(ObjectType.ARRAY, result.type());
                restArray = (ArrayObject) result;
                assertTrue(restArray.getElements().isEmpty());
            }

            @Test
            @DisplayName("should return null for empty arrays")
            void testEmptyArray() {
                BaseObject result = callBuiltin("rest", array());
                assertEquals(ObjectType.NULL, result.type());
            }

            @Test
            @DisplayName("should handle error cases")
            void testRestErrors() {
                assertError(callBuiltin("rest"), "wrong number of arguments");
                assertError(callBuiltin("rest", integer(42)), "must be ARRAY");
            }
        }

        @Nested
        @DisplayName("push() function")
        class PushFunctionTest {

            @Test
            @DisplayName("should add element to end of array")
            void testPushElement() {
                // Test pushing to empty array
                BaseObject result = callBuiltin("push", array(), integer(42));
                assertEquals(ObjectType.ARRAY, result.type());
                ArrayObject newArray = (ArrayObject) result;
                assertEquals(1, newArray.getElements().size());
                assertInteger(newArray.getElements().get(0), 42);

                // Test pushing to non-empty array
                result = callBuiltin("push", array(integer(1), integer(2)), integer(3));
                newArray = (ArrayObject) result;
                assertEquals(3, newArray.getElements().size());
                assertInteger(newArray.getElements().get(2), 3);

                // Test pushing different types
                result = callBuiltin("push", array(integer(1)), string("hello"));
                newArray = (ArrayObject) result;
                assertEquals(2, newArray.getElements().size());
                assertString(newArray.getElements().get(1), "hello");
            }

            @Test
            @DisplayName("should not modify original array")
            void testImmutability() {
                ArrayObject original = array(integer(1), integer(2));
                BaseObject result = callBuiltin("push", original, integer(3));

                // Original should be unchanged
                assertEquals(2, original.getElements().size());

                // Result should have new element
                ArrayObject newArray = (ArrayObject) result;
                assertEquals(3, newArray.getElements().size());
            }

            @Test
            @DisplayName("should handle error cases")
            void testPushErrors() {
                assertError(callBuiltin("push"), "wrong number of arguments");
                assertError(callBuiltin("push", array()), "wrong number of arguments");
                assertError(callBuiltin("push", array(), integer(1), integer(2)), "wrong number of arguments");
                assertError(callBuiltin("push", integer(42), integer(1)), "must be ARRAY");
            }
        }

        @Nested
        @DisplayName("pop() function")
        class PopFunctionTest {

            @Test
            @DisplayName("should remove last element from array")
            void testPopElement() {
                // Test popping from multi-element array
                BaseObject result = callBuiltin("pop", array(integer(1), integer(2), integer(3)));
                assertEquals(ObjectType.ARRAY, result.type());
                ArrayObject newArray = (ArrayObject) result;
                assertEquals(2, newArray.getElements().size());
                assertInteger(newArray.getElements().get(0), 1);
                assertInteger(newArray.getElements().get(1), 2);

                // Test popping from single-element array
                result = callBuiltin("pop", array(integer(42)));
                newArray = (ArrayObject) result;
                assertTrue(newArray.getElements().isEmpty());
            }

            @Test
            @DisplayName("should handle empty array")
            void testPopEmpty() {
                assertError(callBuiltin("pop", array()), "cannot pop from empty array");
            }

            @Test
            @DisplayName("should handle error cases")
            void testPopErrors() {
                assertError(callBuiltin("pop"), "wrong number of arguments");
                assertError(callBuiltin("pop", integer(42)), "must be ARRAY");
            }
        }

        @Nested
        @DisplayName("slice() function")
        class SliceFunctionTest {

            @Test
            @DisplayName("should slice arrays with start and end")
            void testSliceWithStartEnd() {
                ArrayObject testArray = array(integer(0), integer(1), integer(2), integer(3), integer(4));

                // Test normal slice
                BaseObject result = callBuiltin("slice", testArray, integer(1), integer(3));
                assertEquals(ObjectType.ARRAY, result.type());
                ArrayObject sliced = (ArrayObject) result;
                assertEquals(2, sliced.getElements().size());
                assertInteger(sliced.getElements().get(0), 1);
                assertInteger(sliced.getElements().get(1), 2);

                // Test slice to end
                result = callBuiltin("slice", testArray, integer(2));
                sliced = (ArrayObject) result;
                assertEquals(3, sliced.getElements().size());
                assertInteger(sliced.getElements().get(0), 2);
                assertInteger(sliced.getElements().get(2), 4);
            }

            @Test
            @DisplayName("should handle negative indices")
            void testSliceNegativeIndices() {
                ArrayObject testArray = array(integer(0), integer(1), integer(2), integer(3), integer(4));

                // Test negative start
                BaseObject result = callBuiltin("slice", testArray, integer(-2));
                ArrayObject sliced = (ArrayObject) result;
                assertEquals(2, sliced.getElements().size());
                assertInteger(sliced.getElements().get(0), 3);
                assertInteger(sliced.getElements().get(1), 4);

                // Test negative end
                result = callBuiltin("slice", testArray, integer(1), integer(-1));
                sliced = (ArrayObject) result;
                assertEquals(3, sliced.getElements().size());
            }

            @Test
            @DisplayName("should handle boundary conditions")
            void testSliceBoundary() {
                ArrayObject testArray = array(integer(0), integer(1), integer(2));

                // Test out of bounds
                BaseObject result = callBuiltin("slice", testArray, integer(10), integer(20));
                ArrayObject sliced = (ArrayObject) result;
                assertTrue(sliced.getElements().isEmpty());

                // Test empty slice
                result = callBuiltin("slice", testArray, integer(1), integer(1));
                sliced = (ArrayObject) result;
                assertTrue(sliced.getElements().isEmpty());
            }

            @Test
            @DisplayName("should handle error cases")
            void testSliceErrors() {
                assertError(callBuiltin("slice"), "wrong number of arguments");
                assertError(callBuiltin("slice", array()), "wrong number of arguments");
                assertError(callBuiltin("slice", integer(42), integer(0)), "must be ARRAY");
                assertError(callBuiltin("slice", array(), string("0")), "must be INTEGER");
            }
        }

        @Nested
        @DisplayName("concat() function")
        class ConcatFunctionTest {

            @Test
            @DisplayName("should concatenate two arrays")
            void testConcatenation() {
                // Test basic concatenation
                BaseObject result = callBuiltin("concat",
                        array(integer(1), integer(2)),
                        array(integer(3), integer(4)));
                assertEquals(ObjectType.ARRAY, result.type());
                ArrayObject concatenated = (ArrayObject) result;
                assertEquals(4, concatenated.getElements().size());
                assertInteger(concatenated.getElements().get(0), 1);
                assertInteger(concatenated.getElements().get(3), 4);

                // Test concatenating with empty arrays
                result = callBuiltin("concat", array(), array(integer(1)));
                concatenated = (ArrayObject) result;
                assertEquals(1, concatenated.getElements().size());

                result = callBuiltin("concat", array(integer(1)), array());
                concatenated = (ArrayObject) result;
                assertEquals(1, concatenated.getElements().size());

                // Test concatenating two empty arrays
                result = callBuiltin("concat", array(), array());
                concatenated = (ArrayObject) result;
                assertTrue(concatenated.getElements().isEmpty());
            }

            @Test
            @DisplayName("should handle error cases")
            void testConcatErrors() {
                assertError(callBuiltin("concat"), "wrong number of arguments");
                assertError(callBuiltin("concat", array()), "wrong number of arguments");
                assertError(callBuiltin("concat", integer(1), array()), "must be ARRAY");
                assertError(callBuiltin("concat", array(), integer(1)), "must be ARRAY");
            }
        }

        @Nested
        @DisplayName("reverse() function")
        class ReverseFunctionTest {

            @Test
            @DisplayName("should reverse array elements")
            void testReverse() {
                // Test normal reversal
                BaseObject result = callBuiltin("reverse", array(integer(1), integer(2), integer(3)));
                assertEquals(ObjectType.ARRAY, result.type());
                ArrayObject reversed = (ArrayObject) result;
                assertEquals(3, reversed.getElements().size());
                assertInteger(reversed.getElements().get(0), 3);
                assertInteger(reversed.getElements().get(1), 2);
                assertInteger(reversed.getElements().get(2), 1);

                // Test single element
                result = callBuiltin("reverse", array(integer(42)));
                reversed = (ArrayObject) result;
                assertEquals(1, reversed.getElements().size());
                assertInteger(reversed.getElements().get(0), 42);

                // Test empty array
                result = callBuiltin("reverse", array());
                reversed = (ArrayObject) result;
                assertTrue(reversed.getElements().isEmpty());
            }

            @Test
            @DisplayName("should not modify original array")
            void testReverseImmutability() {
                ArrayObject original = array(integer(1), integer(2), integer(3));
                BaseObject result = callBuiltin("reverse", original);

                // Original should be unchanged
                assertInteger(original.getElements().get(0), 1);
                assertInteger(original.getElements().get(2), 3);

                // Result should be reversed
                ArrayObject reversed = (ArrayObject) result;
                assertInteger(reversed.getElements().get(0), 3);
                assertInteger(reversed.getElements().get(2), 1);
            }

            @Test
            @DisplayName("should handle error cases")
            void testReverseErrors() {
                assertError(callBuiltin("reverse"), "wrong number of arguments");
                assertError(callBuiltin("reverse", integer(42)), "must be ARRAY");
            }
        }

        @Nested
        @DisplayName("join() function")
        class JoinFunctionTest {

            @Test
            @DisplayName("should join array elements with separator")
            void testJoinWithSeparator() {
                // Test with custom separator
                BaseObject result = callBuiltin("join",
                        array(string("a"), string("b"), string("c")),
                        string("-"));
                assertString(result, "a-b-c");

                // Test with space separator
                result = callBuiltin("join",
                        array(string("hello"), string("world")),
                        string(" "));
                assertString(result, "hello world");

                // Test with empty separator
                result = callBuiltin("join",
                        array(string("a"), string("b"), string("c")),
                        string(""));
                assertString(result, "abc");
            }

            @Test
            @DisplayName("should use default comma separator")
            void testJoinDefault() {
                BaseObject result = callBuiltin("join",
                        array(string("a"), string("b"), string("c")));
                assertString(result, "a,b,c");
            }

            @Test
            @DisplayName("should handle mixed types and edge cases")
            void testJoinEdgeCases() {
                // Test mixed types (should use inspect() method)
                BaseObject result = callBuiltin("join",
                        array(integer(1), string("hello"), bool(true)),
                        string(","));
                assertString(result, "1,hello,true");

                // Test empty array
                result = callBuiltin("join", array(), string(","));
                assertString(result, "");

                // Test single element
                result = callBuiltin("join", array(string("solo")), string(","));
                assertString(result, "solo");
            }

            @Test
            @DisplayName("should handle error cases")
            void testJoinErrors() {
                assertError(callBuiltin("join"), "wrong number of arguments");
                assertError(callBuiltin("join", integer(42)), "must be ARRAY");
                assertError(callBuiltin("join", array(), integer(42)), "must be STRING");
            }
        }
    }

    // ============================================================================
    // 3. STRING OPERATIONS TESTS
    // ============================================================================

    @Nested
    @DisplayName("String Operations")
    class StringOperationsTest {

        @Nested
        @DisplayName("split() function")
        class SplitFunctionTest {

            @Test
            @DisplayName("should split strings by delimiter")
            void testSplitBasic() {
                // Test comma splitting
                BaseObject result = callBuiltin("split", string("a,b,c"), string(","));
                assertEquals(ObjectType.ARRAY, result.type());
                ArrayObject split = (ArrayObject) result;
                assertEquals(3, split.getElements().size());
                assertString(split.getElements().get(0), "a");
                assertString(split.getElements().get(1), "b");
                assertString(split.getElements().get(2), "c");

                // Test space splitting
                result = callBuiltin("split", string("hello world"), string(" "));
                split = (ArrayObject) result;
                assertEquals(2, split.getElements().size());
                assertString(split.getElements().get(0), "hello");
                assertString(split.getElements().get(1), "world");
            }

            @Test
            @DisplayName("should handle edge cases")
            void testSplitEdgeCases() {
                // Test empty string
                BaseObject result = callBuiltin("split", string(""), string(","));
                ArrayObject split = (ArrayObject) result;
                assertEquals(1, split.getElements().size());
                assertString(split.getElements().get(0), "");

                // Test no delimiter found
                result = callBuiltin("split", string("hello"), string(","));
                split = (ArrayObject) result;
                assertEquals(1, split.getElements().size());
                assertString(split.getElements().get(0), "hello");

                // Test consecutive delimiters
                result = callBuiltin("split", string("a,,b"), string(","));
                split = (ArrayObject) result;
                assertEquals(3, split.getElements().size());
                assertString(split.getElements().get(1), "");
            }

            @Test
            @DisplayName("should handle error cases")
            void testSplitErrors() {
                assertError(callBuiltin("split"), "wrong number of arguments");
                assertError(callBuiltin("split", string("hello")), "wrong number of arguments");
                assertError(callBuiltin("split", integer(42), string(",")), "must be STRING");
                assertError(callBuiltin("split", string("hello"), integer(42)), "must be STRING");
            }
        }

        @Nested
        @DisplayName("replace() function")
        class ReplaceFunctionTest {

            @Test
            @DisplayName("should replace all occurrences")
            void testReplaceAll() {
                // Test basic replacement
                BaseObject result = callBuiltin("replace", string("hello world"), string("o"), string("0"));
                assertString(result, "hell0 w0rld");

                // Test replacing with longer string
                result = callBuiltin("replace", string("cat"), string("at"), string("atch"));
                assertString(result, "catch");

                // Test replacing with empty string (deletion)
                result = callBuiltin("replace", string("hello"), string("l"), string(""));
                assertString(result, "heo");
            }

            @Test
            @DisplayName("should handle edge cases")
            void testReplaceEdgeCases() {
                // Test no matches
                BaseObject result = callBuiltin("replace", string("hello"), string("x"), string("y"));
                assertString(result, "hello");

                // Test empty search string (should not replace)
                result = callBuiltin("replace", string("hello"), string(""), string("x"));
                assertString(result, "xhxexlxlxox");

                // Test replacing entire string
                result = callBuiltin("replace", string("hello"), string("hello"), string("world"));
                assertString(result, "world");
            }

            @Test
            @DisplayName("should handle error cases")
            void testReplaceErrors() {
                assertError(callBuiltin("replace"), "wrong number of arguments");
                assertError(callBuiltin("replace", string("hello"), string("o")), "wrong number of arguments");
                assertError(callBuiltin("replace", integer(42), string("o"), string("0")), "must be STRING");
                assertError(callBuiltin("replace", string("hello"), integer(42), string("0")), "must be STRING");
                assertError(callBuiltin("replace", string("hello"), string("o"), integer(42)), "must be STRING");
            }
        }

        @Nested
        @DisplayName("trim() function")
        class TrimFunctionTest {

            @Test
            @DisplayName("should remove leading and trailing whitespace")
            void testTrimWhitespace() {
                // Test basic trimming
                assertString(callBuiltin("trim", string("  hello  ")), "hello");
                assertString(callBuiltin("trim", string("\t\nhello\r\n\t")), "hello");

                // Test no whitespace
                assertString(callBuiltin("trim", string("hello")), "hello");

                // Test only whitespace
                assertString(callBuiltin("trim", string("   ")), "");

                // Test empty string
                assertString(callBuiltin("trim", string("")), "");

                // Test internal whitespace (should not be removed)
                assertString(callBuiltin("trim", string("  hello world  ")), "hello world");
            }

            @Test
            @DisplayName("should handle error cases")
            void testTrimErrors() {
                assertError(callBuiltin("trim"), "wrong number of arguments");
                assertError(callBuiltin("trim", string("hello"), string("world")), "wrong number of arguments");
                assertError(callBuiltin("trim", integer(42)), "must be STRING");
            }
        }

        @Nested
        @DisplayName("upper() and lower() functions")
        class CaseFunctionTest {

            @Test
            @DisplayName("should convert to uppercase")
            void testUpperCase() {
                assertString(callBuiltin("upper", string("hello")), "HELLO");
                assertString(callBuiltin("upper", string("Hello World")), "HELLO WORLD");
                assertString(callBuiltin("upper", string("123abc")), "123ABC");
                assertString(callBuiltin("upper", string("")), "");
                assertString(callBuiltin("upper", string("ALREADY UPPER")), "ALREADY UPPER");
            }

            @Test
            @DisplayName("should convert to lowercase")
            void testLowerCase() {
                assertString(callBuiltin("lower", string("HELLO")), "hello");
                assertString(callBuiltin("lower", string("Hello World")), "hello world");
                assertString(callBuiltin("lower", string("123ABC")), "123abc");
                assertString(callBuiltin("lower", string("")), "");
                assertString(callBuiltin("lower", string("already lower")), "already lower");
            }

            @Test
            @DisplayName("should handle error cases")
            void testCaseErrors() {
                assertError(callBuiltin("upper"), "wrong number of arguments");
                assertError(callBuiltin("upper", integer(42)), "must be STRING");
                assertError(callBuiltin("lower"), "wrong number of arguments");
                assertError(callBuiltin("lower", integer(42)), "must be STRING");
            }
        }

        @Nested
        @DisplayName("substr() function")
        class SubstrFunctionTest {

            @Test
            @DisplayName("should extract substrings")
            void testSubstringExtraction() {
                String testStr = "hello world";

                // Test with start and length
                assertString(callBuiltin("substr", string(testStr), integer(0), integer(5)), "hello");
                assertString(callBuiltin("substr", string(testStr), integer(6), integer(5)), "world");

                // Test with start only (to end)
                assertString(callBuiltin("substr", string(testStr), integer(6)), "world");

                // Test single character
                assertString(callBuiltin("substr", string(testStr), integer(0), integer(1)), "h");
            }

            @Test
            @DisplayName("should handle negative indices")
            void testSubstringNegative() {
                String testStr = "hello";

                // Test negative start (from end)
                assertString(callBuiltin("substr", string(testStr), integer(-2)), "lo");
                assertString(callBuiltin("substr", string(testStr), integer(-3), integer(2)), "ll");
            }

            @Test
            @DisplayName("should handle boundary conditions")
            void testSubstringBoundary() {
                String testStr = "hello";

                // Test out of bounds
                assertString(callBuiltin("substr", string(testStr), integer(10)), "");
                assertString(callBuiltin("substr", string(testStr), integer(0), integer(100)), "hello");

                // Test zero length
                assertString(callBuiltin("substr", string(testStr), integer(2), integer(0)), "");

                // Test empty string
                assertString(callBuiltin("substr", string(""), integer(0)), "");
            }

            @Test
            @DisplayName("should handle error cases")
            void testSubstrErrors() {
                assertError(callBuiltin("substr"), "wrong number of arguments");
                assertError(callBuiltin("substr", string("hello")), "wrong number of arguments");
                assertError(callBuiltin("substr", integer(42), integer(0)), "must be STRING");
                assertError(callBuiltin("substr", string("hello"), string("0")), "must be INTEGER");
                assertError(callBuiltin("substr", string("hello"), integer(0), string("5")), "must be INTEGER");
            }
        }

        @Nested
        @DisplayName("indexOf() and contains() functions")
        class SearchFunctionTest {

            @Test
            @DisplayName("should find substring indices")
            void testIndexOf() {
                // Test basic search
                assertInteger(callBuiltin("indexOf", string("hello world"), string("world")), 6);
                assertInteger(callBuiltin("indexOf", string("hello world"), string("hello")), 0);

                // Test not found
                assertInteger(callBuiltin("indexOf", string("hello world"), string("xyz")), -1);

                // Test empty substring
                assertInteger(callBuiltin("indexOf", string("hello"), string("")), 0);

                // Test same string
                assertInteger(callBuiltin("indexOf", string("hello"), string("hello")), 0);

                // Test case sensitivity
                assertInteger(callBuiltin("indexOf", string("Hello"), string("hello")), -1);
            }

            @Test
            @DisplayName("should check if string contains substring")
            void testContains() {
                // Test basic contains
                assertBoolean(callBuiltin("contains", string("hello world"), string("world")), true);
                assertBoolean(callBuiltin("contains", string("hello world"), string("hello")), true);

                // Test not contains
                assertBoolean(callBuiltin("contains", string("hello world"), string("xyz")), false);

                // Test empty substring
                assertBoolean(callBuiltin("contains", string("hello"), string("")), true);

                // Test case sensitivity
                assertBoolean(callBuiltin("contains", string("Hello"), string("hello")), false);
            }

            @Test
            @DisplayName("should handle error cases")
            void testSearchErrors() {
                assertError(callBuiltin("indexOf"), "wrong number of arguments");
                assertError(callBuiltin("indexOf", string("hello")), "wrong number of arguments");
                assertError(callBuiltin("indexOf", integer(42), string("test")), "must be STRING");
                assertError(callBuiltin("indexOf", string("hello"), integer(42)), "must be STRING");

                assertError(callBuiltin("contains"), "wrong number of arguments");
                assertError(callBuiltin("contains", integer(42), string("test")), "must be STRING");
                assertError(callBuiltin("contains", string("hello"), integer(42)), "must be STRING");
            }
        }
    }

    // ============================================================================
    // 4. MATHEMATICAL OPERATIONS TESTS
    // ============================================================================

    @Nested
    @DisplayName("Mathematical Operations")
    class MathematicalOperationsTest {

        @Nested
        @DisplayName("abs() function")
        class AbsFunctionTest {

            @Test
            @DisplayName("should return absolute values")
            void testAbsoluteValue() {
                assertInteger(callBuiltin("abs", integer(42)), 42);
                assertInteger(callBuiltin("abs", integer(-42)), 42);
                assertInteger(callBuiltin("abs", integer(0)), 0);
                assertInteger(callBuiltin("abs", integer(-1)), 1);
                assertInteger(callBuiltin("abs", integer(1)), 1);

                // Test large numbers
                assertInteger(callBuiltin("abs", integer(-999999)), 999999);
                assertInteger(callBuiltin("abs", integer(Long.MIN_VALUE)), Long.MIN_VALUE); // Overflow case
            }

            @Test
            @DisplayName("should handle error cases")
            void testAbsErrors() {
                assertError(callBuiltin("abs"), "wrong number of arguments");
                assertError(callBuiltin("abs", integer(1), integer(2)), "wrong number of arguments");
                assertError(callBuiltin("abs", string("42")), "must be INTEGER");
            }
        }

        @Nested
        @DisplayName("max() and min() functions")
        class MinMaxFunctionTest {

            @Test
            @DisplayName("should find maximum values")
            void testMaximum() {
                // Test two arguments
                assertInteger(callBuiltin("max", integer(5), integer(3)), 5);
                assertInteger(callBuiltin("max", integer(-5), integer(-3)), -3);

                // Test multiple arguments
                assertInteger(callBuiltin("max", integer(1), integer(5), integer(3), integer(2)), 5);
                assertInteger(callBuiltin("max", integer(-10), integer(-5), integer(-20)), -5);

                // Test single argument
                assertInteger(callBuiltin("max", integer(42)), 42);

                // Test with zeros
                assertInteger(callBuiltin("max", integer(0), integer(-1), integer(1)), 1);
            }

            @Test
            @DisplayName("should find minimum values")
            void testMinimum() {
                // Test two arguments
                assertInteger(callBuiltin("min", integer(5), integer(3)), 3);
                assertInteger(callBuiltin("min", integer(-5), integer(-3)), -5);

                // Test multiple arguments
                assertInteger(callBuiltin("min", integer(1), integer(5), integer(3), integer(2)), 1);
                assertInteger(callBuiltin("min", integer(-10), integer(-5), integer(-20)), -20);

                // Test single argument
                assertInteger(callBuiltin("min", integer(42)), 42);

                // Test with zeros
                assertInteger(callBuiltin("min", integer(0), integer(-1), integer(1)), -1);
            }

            @Test
            @DisplayName("should handle error cases")
            void testMinMaxErrors() {
                assertError(callBuiltin("max"), "expected at least 1 argument");
                assertError(callBuiltin("min"), "expected at least 1 argument");
                assertError(callBuiltin("max", string("5")), "must be INTEGER");
                assertError(callBuiltin("min", integer(1), string("2")), "must be INTEGER");
            }
        }

        @Nested
        @DisplayName("Mathematical functions")
        class MathFunctionTest {

            @Test
            @DisplayName("should calculate powers")
            void testPower() {
                assertInteger(callBuiltin("pow", integer(2), integer(3)), 8);
                assertInteger(callBuiltin("pow", integer(5), integer(2)), 25);
                assertInteger(callBuiltin("pow", integer(10), integer(0)), 1);
                assertInteger(callBuiltin("pow", integer(1), integer(100)), 1);
                assertInteger(callBuiltin("pow", integer(0), integer(5)), 0);

                // Test edge case
                assertError(callBuiltin("pow", integer(2), integer(-1)), "negative exponents not supported");
            }

            @Test
            @DisplayName("should calculate square roots")
            void testSquareRoot() {
                assertInteger(callBuiltin("sqrt", integer(4)), 2);
                assertInteger(callBuiltin("sqrt", integer(9)), 3);
                assertInteger(callBuiltin("sqrt", integer(16)), 4);
                assertInteger(callBuiltin("sqrt", integer(0)), 0);
                assertInteger(callBuiltin("sqrt", integer(1)), 1);

                // Test non-perfect squares (truncated)
                assertInteger(callBuiltin("sqrt", integer(8)), 2); // Floor of sqrt(8) ≈ 2.83

                // Test negative number
                assertError(callBuiltin("sqrt", integer(-4)), "negative number");
            }

            @Test
            @DisplayName("should handle rounding functions")
            void testRounding() {
                // For integers, these should be identity functions
                assertInteger(callBuiltin("round", integer(42)), 42);
                assertInteger(callBuiltin("floor", integer(42)), 42);
                assertInteger(callBuiltin("ceil", integer(42)), 42);

                assertInteger(callBuiltin("round", integer(-5)), -5);
                assertInteger(callBuiltin("floor", integer(-5)), -5);
                assertInteger(callBuiltin("ceil", integer(-5)), -5);
            }

            @Test
            @DisplayName("should generate random numbers")
            void testRandom() {
                // Test random with max
                BaseObject result = callBuiltin("random", integer(10));
                assertEquals(ObjectType.INTEGER, result.type());
                IntegerObject randomInt = (IntegerObject) result;
                assertTrue(randomInt.getValue() >= 0 && randomInt.getValue() < 10);

                // Test random without arguments (0 or 1)
                result = callBuiltin("random");
                assertEquals(ObjectType.INTEGER, result.type());
                randomInt = (IntegerObject) result;
                assertTrue(randomInt.getValue() == 0 || randomInt.getValue() == 1);

                // Test multiple calls are different (probabilistically)
                Set<Long> results = new HashSet<>();
                for (int i = 0; i < 100; i++) {
                    result = callBuiltin("random", integer(1000));
                    randomInt = (IntegerObject) result;
                    results.add(randomInt.getValue());
                }
                // Should have multiple different values
                assertTrue(results.size() > 10, "Random should generate varied results");
            }

            @Test
            @DisplayName("should handle math function errors")
            void testMathErrors() {
                assertError(callBuiltin("pow"), "wrong number of arguments");
                assertError(callBuiltin("pow", integer(2)), "wrong number of arguments");
                assertError(callBuiltin("sqrt"), "wrong number of arguments");
                assertError(callBuiltin("random", integer(0)), "must be positive");
                assertError(callBuiltin("random", integer(-5)), "must be positive");
            }
        }
    }

    // ============================================================================
    // 5. UTILITY FUNCTIONS TESTS
    // ============================================================================

    @Nested
    @DisplayName("Utility Functions")
    class UtilityFunctionsTest {

        @Nested
        @DisplayName("range() function")
        class RangeFunctionTest {

            @Test
            @DisplayName("should generate ranges with single argument")
            void testRangeSingle() {
                // Test range(5)
                BaseObject result = callBuiltin("range", integer(5));
                assertEquals(ObjectType.ARRAY, result.type());
                ArrayObject range = (ArrayObject) result;
                assertEquals(5, range.getElements().size());
                for (int i = 0; i < 5; i++) {
                    assertInteger(range.getElements().get(i), i);
                }

                // Test range(0)
                result = callBuiltin("range", integer(0));
                range = (ArrayObject) result;
                assertTrue(range.getElements().isEmpty());
            }

            @Test
            @DisplayName("should generate ranges with start and end")
            void testRangeStartEnd() {
                // Test range(2, 5)
                BaseObject result = callBuiltin("range", integer(2), integer(5));
                ArrayObject range = (ArrayObject) result;
                assertEquals(3, range.getElements().size());
                assertInteger(range.getElements().get(0), 2);
                assertInteger(range.getElements().get(1), 3);
                assertInteger(range.getElements().get(2), 4);

                // Test range(5, 2) - should be empty
                result = callBuiltin("range", integer(5), integer(2));
                range = (ArrayObject) result;
                assertTrue(range.getElements().isEmpty());
            }

            @Test
            @DisplayName("should generate ranges with step")
            void testRangeWithStep() {
                // Test range(0, 10, 2)
                BaseObject result = callBuiltin("range", integer(0), integer(10), integer(2));
                ArrayObject range = (ArrayObject) result;
                assertEquals(5, range.getElements().size());
                assertInteger(range.getElements().get(0), 0);
                assertInteger(range.getElements().get(1), 2);
                assertInteger(range.getElements().get(4), 8);

                // Test negative step
                result = callBuiltin("range", integer(5), integer(0), integer(-1));
                range = (ArrayObject) result;
                assertEquals(5, range.getElements().size());
                assertInteger(range.getElements().get(0), 5);
                assertInteger(range.getElements().get(4), 1);
            }

            @Test
            @DisplayName("should handle error cases")
            void testRangeErrors() {
                assertError(callBuiltin("range"), "wrong number of arguments");
                assertError(callBuiltin("range", integer(1), integer(2), integer(3), integer(4)),
                        "wrong number of arguments");
                assertError(callBuiltin("range", string("5")), "must be INTEGER");
                assertError(callBuiltin("range", integer(1), integer(5), integer(0)), "cannot be zero");
            }
        }

        @Nested
        @DisplayName("keys() and values() functions")
        class HashUtilityTest {

            @Test
            @DisplayName("should extract hash keys")
            void testKeys() {
                // Test hash with multiple keys
                HashObject testHash = hash("name", "Alice", "age", "30", "city", "NYC");
                BaseObject result = callBuiltin("keys", testHash);
                assertEquals(ObjectType.ARRAY, result.type());
                ArrayObject keys = (ArrayObject) result;
                assertEquals(3, keys.getElements().size());

                // Keys should be strings
                Set<String> keySet = new HashSet<>();
                for (BaseObject key : keys.getElements()) {
                    assertEquals(ObjectType.STRING, key.type());
                    keySet.add(((StringObject) key).getValue());
                }
                assertTrue(keySet.contains("name"));
                assertTrue(keySet.contains("age"));
                assertTrue(keySet.contains("city"));

                // Test empty hash
                result = callBuiltin("keys", hash());
                keys = (ArrayObject) result;
                assertTrue(keys.getElements().isEmpty());
            }

            @Test
            @DisplayName("should extract hash values")
            void testValues() {
                // Test hash with multiple values
                HashObject testHash = hash("a", "1", "b", "2", "c", "3");
                BaseObject result = callBuiltin("values", testHash);
                assertEquals(ObjectType.ARRAY, result.type());
                ArrayObject values = (ArrayObject) result;
                assertEquals(3, values.getElements().size());

                // Values should be strings (as created by helper)
                Set<String> valueSet = new HashSet<>();
                for (BaseObject value : values.getElements()) {
                    assertEquals(ObjectType.STRING, value.type());
                    valueSet.add(((StringObject) value).getValue());
                }
                assertTrue(valueSet.contains("1"));
                assertTrue(valueSet.contains("2"));
                assertTrue(valueSet.contains("3"));

                // Test empty hash
                result = callBuiltin("values", hash());
                values = (ArrayObject) result;
                assertTrue(values.getElements().isEmpty());
            }

            @Test
            @DisplayName("should handle error cases")
            void testHashUtilityErrors() {
                assertError(callBuiltin("keys"), "wrong number of arguments");
                assertError(callBuiltin("keys", array()), "must be HASH");
                assertError(callBuiltin("values"), "wrong number of arguments");
                assertError(callBuiltin("values", string("test")), "must be HASH");
            }
        }
    }

    // ============================================================================
    // 6. ERROR HANDLING TESTS
    // ============================================================================

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandlingTest {

        @Nested
        @DisplayName("error() function")
        class ErrorFunctionTest {

            @Test
            @DisplayName("should create error objects")
            void testErrorCreation() {
                BaseObject result = callBuiltin("error", string("Something went wrong"));
                assertEquals(ObjectType.ERROR, result.type());
                ErrorObject error = (ErrorObject) result;
                assertEquals("Something went wrong", error.getMessage());

                // Test empty error message
                result = callBuiltin("error", string(""));
                assertEquals(ObjectType.ERROR, result.type());
                error = (ErrorObject) result;
                assertEquals("", error.getMessage());
            }

            @Test
            @DisplayName("should handle error cases")
            void testErrorErrors() {
                assertError(callBuiltin("error"), "wrong number of arguments");
                assertError(callBuiltin("error", string("msg1"), string("msg2")), "wrong number of arguments");
                assertError(callBuiltin("error", integer(42)), "must be STRING");
            }
        }

        @Nested
        @DisplayName("assert() function")
        class AssertFunctionTest {

            @Test
            @DisplayName("should pass on truthy conditions")
            void testAssertPass() {
                // Test with true
                BaseObject result = callBuiltin("assert", bool(true));
                assertEquals(ObjectType.NULL, result.type());

                // Test with truthy values
                result = callBuiltin("assert", integer(1));
                assertEquals(ObjectType.NULL, result.type());

                result = callBuiltin("assert", string("hello"));
                assertEquals(ObjectType.NULL, result.type());

                // Test with custom message (should still pass)
                result = callBuiltin("assert", bool(true), string("Custom message"));
                assertEquals(ObjectType.NULL, result.type());
            }

            @Test
            @DisplayName("should fail on falsy conditions")
            void testAssertFail() {
                // Test with false
                BaseObject result = callBuiltin("assert", bool(false));
                assertEquals(ObjectType.ERROR, result.type());
                ErrorObject error = (ErrorObject) result;
                assertEquals("Assertion failed", error.getMessage());

                // Test with falsy values
                result = callBuiltin("assert", integer(0));
                assertEquals(ObjectType.ERROR, result.type());

                result = callBuiltin("assert", string(""));
                assertEquals(ObjectType.ERROR, result.type());

                result = callBuiltin("assert", NullObject.INSTANCE);
                assertEquals(ObjectType.ERROR, result.type());

                // Test with custom message
                result = callBuiltin("assert", bool(false), string("Custom failure message"));
                error = (ErrorObject) result;
                assertEquals("Custom failure message", error.getMessage());
            }

            @Test
            @DisplayName("should handle error cases")
            void testAssertErrors() {
                assertError(callBuiltin("assert"), "wrong number of arguments");
                assertError(callBuiltin("assert", bool(true), string("msg"), integer(42)), "wrong number of arguments");
            }
        }
    }

    // ============================================================================
    // 7. INTEGRATION AND STRESS TESTS
    // ============================================================================

    @Nested
    @DisplayName("Integration and Stress Tests")
    class IntegrationTest {

        @Test
        @DisplayName("should handle large data sets efficiently")
        void testLargeDataSets() {
            // Create large array
            BaseObject[] largeElements = IntStream.range(0, 10000)
                    .mapToObj(i -> integer(i))
                    .toArray(BaseObject[]::new);
            ArrayObject largeArray = array(largeElements);

            // Test len on large array
            assertInteger(callBuiltin("len", largeArray), 10000);

            // Test first and last
            assertInteger(callBuiltin("first", largeArray), 0);
            assertInteger(callBuiltin("last", largeArray), 9999);

            // Test slice on large array
            BaseObject result = callBuiltin("slice", largeArray, integer(100), integer(200));
            ArrayObject sliced = (ArrayObject) result;
            assertEquals(100, sliced.getElements().size());
            assertInteger(sliced.getElements().get(0), 100);
            assertInteger(sliced.getElements().get(99), 199);
        }

        @Test
        @DisplayName("should handle complex nested operations")
        void testNestedOperations() {
            // Create nested data structure
            ArrayObject nestedArray = array(
                    array(integer(1), integer(2)),
                    array(integer(3), integer(4)),
                    array(integer(5), integer(6)));

            // Test operations on nested structure
            assertInteger(callBuiltin("len", nestedArray), 3);

            BaseObject firstSub = callBuiltin("first", nestedArray);
            assertEquals(ObjectType.ARRAY, firstSub.type());
            assertInteger(callBuiltin("len", firstSub), 2);
            assertInteger(callBuiltin("first", firstSub), 1);
        }

        @Test
        @DisplayName("should chain function calls correctly")
        void testFunctionChaining() {
            // Test chaining operations
            ArrayObject testArray = array(integer(3), integer(1), integer(4), integer(1), integer(5));

            // Push then get length
            BaseObject pushed = callBuiltin("push", testArray, integer(9));
            assertInteger(callBuiltin("len", pushed), 6);
            assertInteger(callBuiltin("last", pushed), 9);

            // Multiple transformations
            BaseObject reversed = callBuiltin("reverse", testArray);
            BaseObject firstOfReversed = callBuiltin("first", reversed);
            assertInteger(firstOfReversed, 5);
        }

        @Test
        @DisplayName("should maintain immutability across operations")
        void testImmutability() {
            ArrayObject original = array(integer(1), integer(2), integer(3));

            // Perform various operations
            BaseObject pushed = callBuiltin("push", original, integer(4));
            BaseObject popped = callBuiltin("pop", original);
            BaseObject reversed = callBuiltin("reverse", original);
            BaseObject sliced = callBuiltin("slice", original, integer(1));

            // Original should be unchanged
            assertEquals(3, original.getElements().size());
            assertInteger(original.getElements().get(0), 1);
            assertInteger(original.getElements().get(2), 3);

            // Results should be different
            assertInteger(callBuiltin("len", pushed), 4);
            assertInteger(callBuiltin("len", popped), 2);
            assertInteger(callBuiltin("len", reversed), 3);
            assertInteger(callBuiltin("len", sliced), 2);
        }

        @Test
        @DisplayName("should handle edge cases consistently")
        void testEdgeCaseConsistency() {
            // Test empty inputs across functions
            ArrayObject empty = array();
            assertInteger(callBuiltin("len", empty), 0);
            assertEquals(ObjectType.NULL, callBuiltin("first", empty).type());
            assertEquals(ObjectType.NULL, callBuiltin("last", empty).type());
            assertEquals(ObjectType.NULL, callBuiltin("rest", empty).type());

            // Test single element arrays
            ArrayObject single = array(integer(42));
            assertInteger(callBuiltin("len", single), 1);
            assertInteger(callBuiltin("first", single), 42);
            assertInteger(callBuiltin("last", single), 42);

            BaseObject restOfSingle = callBuiltin("rest", single);
            assertEquals(ObjectType.ARRAY, restOfSingle.type());
            ArrayObject restArray = (ArrayObject) restOfSingle;
            assertTrue(restArray.getElements().isEmpty());
        }
    }

    // ============================================================================
    // 8. PERFORMANCE AND BOUNDARY TESTS
    // ============================================================================

    @Nested
    @DisplayName("Performance and Boundary Tests")
    class PerformanceTest {

        @Test
        @DisplayName("should handle maximum safe integer values")
        void testIntegerBoundaries() {
            // Test with large positive and negative values
            long maxValue = Long.MAX_VALUE;
            long minValue = Long.MIN_VALUE;

            assertInteger(callBuiltin("abs", integer(maxValue)), maxValue);
            assertInteger(callBuiltin("max", integer(maxValue), integer(0)), maxValue);
            assertInteger(callBuiltin("min", integer(minValue), integer(0)), minValue);

            // Test string conversion of large numbers
            assertString(callBuiltin("str", integer(maxValue)), String.valueOf(maxValue));
            assertString(callBuiltin("str", integer(minValue)), String.valueOf(minValue));
        }

        @Test
        @DisplayName("should handle very long strings efficiently")
        void testLongStrings() {
            // Create a very long string
            String longString = "a".repeat(100000);
            StringObject longStringObj = string(longString);

            // Test length
            assertInteger(callBuiltin("len", longStringObj), 100000);

            // Test substring
            BaseObject substr = callBuiltin("substr", longStringObj, integer(0), integer(10));
            assertString(substr, "aaaaaaaaaa");

            // Test case conversion
            BaseObject upper = callBuiltin("upper", longStringObj);
            assertEquals(ObjectType.STRING, upper.type());
            StringObject upperStr = (StringObject) upper;
            assertEquals(100000, upperStr.getValue().length());
            assertTrue(upperStr.getValue().startsWith("AAAAAAAAAA"));
        }

        @Test
        @DisplayName("should handle deep array nesting")
        void testDeepNesting() {
            // Create deeply nested array structure
            BaseObject nested = integer(42);
            for (int i = 0; i < 100; i++) {
                nested = array(nested);
            }

            // Should be able to get length at each level
            BaseObject current = nested;
            for (int i = 0; i < 100; i++) {
                assertInteger(callBuiltin("len", current), 1);
                current = callBuiltin("first", current);
            }

            // Final element should be the integer
            assertInteger(current, 42);
        }

        @Test
        @DisplayName("should handle many hash entries efficiently")
        void testLargeHash() {
            // Create hash with many entries
            Map<String, BaseObject> largePairs = new LinkedHashMap<>();
            for (int i = 0; i < 10000; i++) {
                largePairs.put("key" + i, string("value" + i));
            }
            HashObject largeHash = new HashObject(largePairs);

            // Test operations
            assertInteger(callBuiltin("len", largeHash), 10000);

            BaseObject keys = callBuiltin("keys", largeHash);
            assertInteger(callBuiltin("len", keys), 10000);

            BaseObject values = callBuiltin("values", largeHash);
            assertInteger(callBuiltin("len", values), 10000);
        }
    }
}