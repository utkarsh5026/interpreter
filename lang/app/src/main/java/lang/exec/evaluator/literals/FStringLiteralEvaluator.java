package lang.exec.evaluator.literals;

import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.base.BaseObject;

import lang.exec.objects.Environment;
import lang.exec.objects.StringObject;
import lang.exec.objects.ErrorObject;

import lang.exec.validator.ObjectValidator;
import lang.exec.evaluator.base.EvaluationContext;
import lang.ast.literals.FStringLiteral;
import lang.ast.base.Expression;

import java.util.List;

/**
 * 🎯 FStringLiteralEvaluator - F-String Interpolation Specialist 🎯
 * 
 * Evaluates f-string literals by interpolating expressions into static text.
 * 
 * From first principles, f-string evaluation works like this:
 * 1. Evaluate each embedded expression to get its value
 * 2. Convert each value to its string representation
 * 3. Interleave static text with expression results
 * 4. Concatenate everything into the final string
 * 
 * Example evaluation process:
 * - f"Hello {name}!" with name="Alice"
 * - Static parts: ["Hello ", "!"]
 * - Expressions: [name] → ["Alice"]
 * - Result: "Hello " + "Alice" + "!" = "Hello Alice!"
 * 
 * Error handling:
 * - If any expression evaluation fails, return the error
 * - If any value can't be converted to string, return error
 * - Otherwise, return the interpolated string
 */
public class FStringLiteralEvaluator implements NodeEvaluator<FStringLiteral> {

    @Override
    public BaseObject evaluate(FStringLiteral node, Environment env, EvaluationContext context) {
        List<String> staticParts = node.getActualStrings();
        List<Expression> expressions = node.getExpressions();

        System.out.println("Static parts: " + staticParts);
        System.out.println("Expressions: " + expressions);

        // If no expressions, just return the static text
        if (expressions.isEmpty()) {
            return new StringObject(staticParts.get(0));
        }

        // Evaluate all expressions first
        String[] expressionValues = new String[expressions.size()];
        for (int i = 0; i < expressions.size(); i++) {
            BaseObject result = context.evaluate(expressions.get(i), env);

            // Check for evaluation errors
            if (ObjectValidator.isError(result)) {
                return new ErrorObject(String.format(
                        "Error evaluating expression in f-string: %s",
                        ObjectValidator.asError(result).getMessage()));
            }

            // Convert result to string
            expressionValues[i] = convertToString(result);
        }

        // Interpolate static parts with expression values
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < expressions.size(); i++) {
            result.append(staticParts.get(i)); // Static part before expression
            result.append(expressionValues[i]); // Expression value
        }

        // Add the final static part
        result.append(staticParts.get(staticParts.size() - 1));

        return new StringObject(result.toString());
    }

    /**
     * 🔄 Converts any BaseObject to its string representation
     * 
     * This method handles the string conversion rules for f-string interpolation.
     * Different types have different string representations:
     * 
     * - Strings: use their value directly
     * - Numbers: convert to string representation
     * - Booleans: "true" or "false"
     * - Null: "null"
     * - Arrays/Objects: use their inspect() method
     * - Functions: show function signature
     * 
     * @param obj The object to convert to string
     * @return String representation of the object
     */
    private String convertToString(BaseObject obj) {
        if (obj == null) {
            return "null";
        }

        // Handle different object types with appropriate string conversion
        switch (obj.type()) {
            case STRING:
                // Strings use their value directly (no quotes in f-strings)
                return ObjectValidator.asString(obj).getValue();

            case INTEGER:
                // Numbers convert to their numeric representation
                return String.valueOf(ObjectValidator.asInteger(obj).getValue());

            case BOOLEAN:
                // Booleans become "true" or "false"
                return ObjectValidator.asBoolean(obj).getValue() ? "true" : "false";

            case NULL:
                // Null becomes "null"
                return "null";

            case ARRAY:
                // Arrays show their structure: [1, 2, 3]
                return obj.inspect();

            case HASH:
                // Objects show their structure: {key: value}
                return obj.inspect();

            case FUNCTION:
                // Functions show their signature
                return obj.inspect();

            case BUILTIN:
                // Built-in functions show their name
                return String.format("<builtin function: %s>",
                        ObjectValidator.asBuiltin(obj).getName());

            default:
                // For any other types, use their inspect method
                return obj.inspect();
        }
    }
}