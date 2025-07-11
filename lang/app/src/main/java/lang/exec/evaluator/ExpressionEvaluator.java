package lang.exec.evaluator;

import lang.exec.base.BaseObject;
import lang.exec.objects.BooleanObject;
import lang.exec.objects.ErrorObject;
import lang.exec.objects.IntegerObject;
import lang.exec.objects.StringObject;

import lang.exec.validator.ObjectValidator;

/**
 * 🧮 ExpressionEvaluator - The Mathematical Brain of the Language 🧮
 * 
 * This class handles all expression evaluation operations in the interpreter.
 * Think of it as a super-smart calculator that can handle numbers, text, and
 * logic! 🤖🔢
 * 
 * The evaluator can perform:
 * - 🔢 **Arithmetic**: 2 + 3, 10 - 5, 6 * 7, 15 / 3
 * - 📝 **String operations**: "Hello" + " World", string comparisons
 * - 🧠 **Logic operations**: true && false, !condition, value || fallback
 * - 🔍 **Comparisons**: 5 > 3, "apple" == "banana", 10 <= 20
 * - ➖ **Unary operations**: -42, !true
 * 
 * Each method is designed to:
 * - ✅ Validate input types
 * - 🔄 Perform the operation safely
 * - 🚨 Return errors for invalid operations
 * - 📦 Wrap results in appropriate object types
 * 
 * Like a multilingual translator that understands math, text, and logic! 🌐🧮
 */
public class ExpressionEvaluator {

    /**
     * 🚫 Evaluates logical NOT operator (!)
     * 
     * Flips the truth value of any expression. Like asking "What's the opposite?"
     * This is a private helper method for the main NOT evaluation! 🔄❌
     * 
     * Truth table for NOT:
     * - !true → false ✅➡️❌
     * - !false → true ❌➡️✅
     * - !null → true (null is considered falsy) 🚫➡️✅
     * - !0 → true (zero is falsy) 0️⃣➡️✅
     * - !"" → true (empty string is falsy) 📝➡️✅
     * 
     * @param value The value to logically negate 🎯
     * @return A BooleanObject with the flipped truth value 🔄
     */
    private final static BooleanObject evalLogicalNotOperator(BaseObject value) {
        // 🧠 Special handling for boolean values
        if (ObjectValidator.isBoolean(value)) {
            return new BooleanObject(!ObjectValidator.asBoolean(value).getValue());
        }

        // 🚫 Null values are considered falsy, so !null = true
        if (ObjectValidator.isNull(value)) {
            return new BooleanObject(true);
        }

        // 🎯 For all other types, use the general truthiness check
        return new BooleanObject(!value.isTruthy());
    }

    /**
     * ➖ Evaluates negation operator (-)
     * 
     * Makes positive numbers negative and vice versa. Like flipping a number's
     * sign!
     * This is a private helper method for the main negation evaluation! 🔄📊
     * 
     * Examples:
     * - -5 → -5 ➖
     * - -(-3) → 3 ➖➖➡️➕
     * - -0 → 0 ➖0️⃣➡️0️⃣
     * - -1000 → -1000 ➖📊
     * 
     * Only works with integers - you can't negate strings or booleans!
     * 
     * @param value The value to negate (must be an integer) 🔢
     * @return An IntegerObject with flipped sign, or ErrorObject if invalid type 🔄
     */
    private final static BaseObject evalNegationOperator(BaseObject value) {
        // ✅ Only integers can be negated
        if (ObjectValidator.isInteger(value)) {
            return new IntegerObject(-ObjectValidator.asInteger(value).getValue());
        }

        // 🚨 Return helpful error message for invalid types
        return new ErrorObject(String
                .format("unknown operator: -%s, You can only use - operator with INTEGER like -5, -10, -100, -1000, etc.",
                        value.type()));
    }

    /**
     * 🔗 Evaluates logical AND expression (&&)
     * 
     * Returns true only if BOTH values are truthy. Like asking "Are both conditions
     * met?"
     * Uses short-circuit evaluation - if the first value is false, doesn't even
     * check the second! ⚡
     * 
     * Truth table for AND:
     * - true && true → true ✅✅➡️✅
     * - true && false → false ✅❌➡️❌
     * - false && true → false ❌✅➡️❌
     * - false && false → false ❌❌➡️❌
     * 
     * Examples:
     * - (5 > 3) && (10 < 20) → true && true → true ✅
     * - (2 > 5) && (1 == 1) → false && true → false ❌
     * - user.isLoggedIn() && user.hasPermission() → both must be true 🔐
     * 
     * @param left  The first condition to check 🎯
     * @param right The second condition to check (only evaluated if left is true)
     *              🎯
     * @return A BooleanObject representing the AND result 🔗
     */
    public final static BooleanObject evalAndExpression(BaseObject left, BaseObject right) {
        // ⚡ Short-circuit: if left is false, result is false (don't check right)
        if (left.isTruthy()) {
            return new BooleanObject(right.isTruthy());
        }
        return new BooleanObject(false);
    }

    /**
     * 🔗 Evaluates logical OR expression (||)
     * 
     * Returns true if EITHER value is truthy. Like asking "Is at least one
     * condition met?"
     * Uses short-circuit evaluation - if the first value is true, doesn't check the
     * second! ⚡
     * 
     * Truth table for OR:
     * - true || true → true ✅✅➡️✅
     * - true || false → true ✅❌➡️✅
     * - false || true → true ❌✅➡️✅
     * - false || false → false ❌❌➡️❌
     * 
     * Examples:
     * - (5 > 10) || (3 < 8) → false || true → true ✅
     * - user.isAdmin() || user.isOwner() → either role grants access 🔑
     * - input.isEmpty() || input.isNull() → check for any invalid input 🚫
     * 
     * @param left  The first condition to check 🎯
     * @param right The second condition to check (only evaluated if left is false)
     *              🎯
     * @return A BooleanObject representing the OR result 🔗
     */
    public final static BooleanObject evalOrExpression(BaseObject left, BaseObject right) {
        // ⚡ Short-circuit: if left is true, result is true (don't check right)
        if (left.isTruthy()) {
            return new BooleanObject(true);
        }
        return new BooleanObject(right.isTruthy());
    }

    /**
     * 🚫 Evaluates logical NOT expression (!)
     * 
     * Public interface for logical negation. Flips the truth value of any
     * expression!
     * Like asking "What's the opposite of this?" 🔄❓
     * 
     * This is simpler than the private version - just uses truthiness directly.
     * 
     * Examples:
     * - !true → false 🚫✅
     * - !false → true 🚫❌
     * - !0 → true (zero is falsy) 🚫0️⃣
     * - !"hello" → false (non-empty string is truthy) 🚫📝
     * 
     * @param value The value to logically negate 🎯
     * @return A BooleanObject with the flipped truth value 🔄
     */
    public final static BooleanObject evalNotExpression(BaseObject value) {
        return new BooleanObject(!value.isTruthy());
    }

    /**
     * 📝 Evaluates string infix expressions
     * 
     * Handles operations between two strings like concatenation and comparison.
     * Think of it as a text manipulation specialist! 📝✨
     * 
     * Supported operations:
     * - ➕ **Concatenation**: "Hello" + " World" → "Hello World"
     * - 🟰 **Equality**: "apple" == "apple" → true
     * - 🚫 **Inequality**: "cat" != "dog" → true
     * - 📊 **Comparison**: "apple" < "banana" → true (alphabetical order)
     * 
     * String comparison uses lexicographical (dictionary) order:
     * - "a" < "b" → true 📚
     * - "apple" < "banana" → true 🍎🍌
     * - "zebra" > "apple" → true 🦓🍎
     * 
     * @param operator The operation to perform (+, ==, !=, <, >, <=, >=) 🔧
     * @param left     The first string operand 📝
     * @param right    The second string operand 📝
     * @return StringObject for concatenation, BooleanObject for comparisons, or
     *         ErrorObject 📦
     */
    public final static BaseObject evalStringInfixExpression(String operator, BaseObject left, BaseObject right) {
        // ✅ Validate that both operands are strings
        if (!ObjectValidator.isString(left) || !ObjectValidator.isString(right)) {
            return new ErrorObject("type mismatch: STRING " + operator + " " + left.type() + " " + right.type());
        }

        String leftString = ObjectValidator.asString(left).getValue();
        String rightString = ObjectValidator.asString(right).getValue();

        switch (operator) {
            case "+":
                return new StringObject(String.join("", leftString, rightString));

            case "==":
                return new BooleanObject(leftString.equals(rightString));

            case "!=":
                return new BooleanObject(!leftString.equals(rightString));

            case ">":
                return new BooleanObject(leftString.compareTo(rightString) > 0);

            case "<":
                return new BooleanObject(leftString.compareTo(rightString) < 0);

            case "<=":
                return new BooleanObject(leftString.compareTo(rightString) <= 0);

            case ">=":
                return new BooleanObject(leftString.compareTo(rightString) >= 0);

            default:
                return new ErrorObject("unknown operator: " + operator + " " + left.type() + " " + right.type());
        }
    }

    /**
     * 🔢 Evaluates integer infix expressions
     * 
     * Handles mathematical operations between two integers.
     * Think of it as a powerful calculator for whole numbers! 🧮🔢
     * 
     * Supported operations:
     * - ➕ **Addition**: 5 + 3 → 8
     * - ➖ **Subtraction**: 10 - 4 → 6
     * - ✖️ **Multiplication**: 6 * 7 → 42
     * - ➗ **Division**: 15 / 3 → 5
     * - 📊 **Modulus**: 17 % 5 → 2 (remainder)
     * - 🟰 **Equality**: 5 == 5 → true
     * - 🚫 **Inequality**: 3 != 7 → true
     * - 📈 **Comparisons**: 5 < 10 → true, 8 >= 3 → true
     * 
     * All operations follow standard mathematical rules:
     * - Division by zero may cause issues (implementation-dependent) ⚠️
     * - Modulus gives the remainder after division 📊
     * - Comparisons return boolean results 🔍
     * 
     * @param operator The mathematical operation (+, -, *, /, %, ==, !=, <, >,
     *                 <=, >=) 🔧
     * @param left     The first integer operand 🔢
     * @param right    The second integer operand 🔢
     * @return IntegerObject for arithmetic, BooleanObject for comparisons, or
     *         ErrorObject 📦
     */
    public final static BaseObject evalIntegerInfixExpression(String operator, BaseObject left, BaseObject right) {
        // ✅ Validate that both operands are integers
        if (!ObjectValidator.isInteger(left) || !ObjectValidator.isInteger(right)) {
            return new ErrorObject("type mismatch: INTEGER " + operator + " " + left.type() + " " + right.type());
        }

        long leftInteger = ObjectValidator.asInteger(left).getValue();
        long rightInteger = ObjectValidator.asInteger(right).getValue();

        switch (operator) {
            case "+": // ➕ Addition
                return new IntegerObject(leftInteger + rightInteger);

            case "-": // ➖ Subtraction
                return new IntegerObject(leftInteger - rightInteger);

            case "*": // ✖️ Multiplication
                return new IntegerObject(leftInteger * rightInteger);

            case "/": // ➗ Division
                return new IntegerObject(leftInteger / rightInteger);

            case "%": // 📊 Modulus (remainder)
                return new IntegerObject(leftInteger % rightInteger);

            case "==": // 🟰 Equality comparison
                return new BooleanObject(leftInteger == rightInteger);

            case "!=": // 🚫 Inequality comparison
                return new BooleanObject(leftInteger != rightInteger);

            case "<": // 📈 Less than
                return new BooleanObject(leftInteger < rightInteger);

            case "<=": // 📈 Less than or equal
                return new BooleanObject(leftInteger <= rightInteger);

            case ">": // 📈 Greater than
                return new BooleanObject(leftInteger > rightInteger);

            case ">=": // 📈 Greater than or equal
                return new BooleanObject(leftInteger >= rightInteger);

            default: // 🚨 Unknown operator
                return new ErrorObject("unknown operator: " + operator + " " + left.type() + " " + right.type());
        }
    }

    /**
     * 🧠 Evaluates boolean infix expressions
     * 
     * Handles logical operations between two boolean values.
     * Think of it as a logic specialist that understands true/false relationships!
     * 🧠⚡
     * 
     * Supported operations:
     * - 🟰 **Equality**: true == true → true, true == false → false
     * - 🚫 **Inequality**: true != false → true, false != false → false
     * - 🔗 **Logical AND**: true && true → true, true && false → false
     * - 🔗 **Logical OR**: true || false → true, false || false → false
     * 
     * Boolean logic follows standard rules:
     * - AND requires both values to be true ✅✅
     * - OR requires at least one value to be true ✅❌
     * - Equality checks if both have the same truth value 🟰
     * - Inequality checks if they have different truth values 🚫
     * 
     * Examples:
     * - isLoggedIn == true → check login status 🔐
     * - hasPermission && isActive → both conditions must be met 🔗
     * - isAdmin || isOwner → either role grants access 🔑
     * 
     * @param operator The logical operation (==, !=, &&, ||) 🔧
     * @param left     The first boolean operand 🧠
     * @param right    The second boolean operand 🧠
     * @return BooleanObject with the logical result, or ErrorObject for invalid
     *         operations 📦
     */
    public final static BaseObject evalBooleanInfixExpression(String operator, BaseObject left, BaseObject right) {
        // ✅ Validate that both operands are booleans
        if (!ObjectValidator.isBoolean(left) || !ObjectValidator.isBoolean(right)) {
            return new ErrorObject("type mismatch: BOOLEAN " + operator + " " + left.type() + " " + right.type());
        }

        boolean leftBoolean = ObjectValidator.asBoolean(left).getValue();
        boolean rightBoolean = ObjectValidator.asBoolean(right).getValue();

        switch (operator) {
            case "==": // 🟰 Boolean equality
                return new BooleanObject(leftBoolean == rightBoolean);

            case "!=": // 🚫 Boolean inequality
                return new BooleanObject(leftBoolean != rightBoolean);

            case "&&": // 🔗 Logical AND
                return new BooleanObject(leftBoolean && rightBoolean);

            case "||": // 🔗 Logical OR
                return new BooleanObject(leftBoolean || rightBoolean);

            default: // 🚨 Unknown operator
                return new ErrorObject("unknown operator: " + operator + " " + left.type() + " " + right.type());
        }
    }

    /**
     * 🎯 Evaluates infix expressions (main dispatcher)
     * 
     * The main entry point for evaluating binary operations between two values.
     * Acts like a smart dispatcher that routes operations to the right specialist!
     * 🚦🎯
     * 
     * This method:
     * 1. 🔍 Examines the types of both operands
     * 2. 🎯 Routes to the appropriate specialized evaluator
     * 3. 📦 Returns the result or an error
     * 
     * Supported type combinations:
     * - 📝 String + String → String operations (concat, compare)
     * - 🔢 Integer + Integer → Mathematical operations
     * - 🧠 Boolean + Boolean → Logical operations
     * - 🚫 Mixed types → Error (can't add string to number!)
     * 
     * Examples of valid operations:
     * - "Hello" + " World" → "Hello World" 📝
     * - 5 + 3 → 8 🔢
     * - true && false → false 🧠
     * - "apple" < "banana" → true 📝
     * 
     * Examples of invalid operations:
     * - "hello" + 5 → Error (can't mix string and number) 🚫
     * - true * false → Error (can't multiply booleans) 🚫
     * 
     * @param operator The operation to perform (+, -, *, ==, &&, etc.) 🔧
     * @param left     The first operand 🎯
     * @param right    The second operand 🎯
     * @return The result object or an ErrorObject for invalid operations 📦
     */
    public final static BaseObject evalInfixExpression(String operator, BaseObject left, BaseObject right) {
        // 📝 Route string operations to string specialist
        if (ObjectValidator.isString(left) && ObjectValidator.isString(right))
            return evalStringInfixExpression(operator, left, right);

        // 🔢 Route integer operations to math specialist
        if (ObjectValidator.isInteger(left) && ObjectValidator.isInteger(right))
            return evalIntegerInfixExpression(operator, left, right);

        // 🧠 Route boolean operations to logic specialist
        if (ObjectValidator.isBoolean(left) && ObjectValidator.isBoolean(right))
            return evalBooleanInfixExpression(operator, left, right);

        // 🚨 Mixed or unsupported types
        return new ErrorObject("unknown operator: " + operator + " " + left.type() + " " + right.type());
    }

    /**
     * 🎯 Evaluates prefix expressions (unary operations)
     * 
     * Handles operations that apply to a single value (unary operators).
     * Think of it as operations that modify or test one thing at a time! 🔧🎯
     * 
     * Supported prefix operations:
     * - 🚫 **Logical NOT** (!): Flips truth value
     * - !true → false
     * - !false → true
     * - !0 → true (zero is falsy)
     * - !"hello" → false (non-empty string is truthy)
     * 
     * - ➖ **Negation** (-): Flips number sign
     * - -5 → -5
     * - -(-3) → 3
     * - -0 → 0
     * 
     * Examples:
     * - !user.isLoggedIn() → check if user is NOT logged in 🚫
     * - -temperature → get opposite temperature ➖
     * - !list.isEmpty() → check if list has items 🚫
     * 
     * Error cases:
     * - -"hello" → Error (can't negate strings) 🚫
     * - &value → Error (unsupported operator) 🚫
     * 
     * @param operator The unary operator (! or -) 🔧
     * @param right    The value to apply the operator to 🎯
     * @return The result object or an ErrorObject for invalid operations 📦
     */
    public final static BaseObject evalPrefixExpression(String operator, BaseObject right) {
        // 🚫 Handle logical NOT operator
        if (operator.equals("!")) {
            return evalLogicalNotOperator(right);
        }

        // ➖ Handle negation operator
        if (operator.equals("-")) {
            return evalNegationOperator(right);
        }

        // 🚨 Unknown prefix operator
        return new ErrorObject(
                String.format("unknown operator: %s%s, You can only use ! or - operator with BOOLEAN or INTEGER",
                        operator, right.type()));
    }
}