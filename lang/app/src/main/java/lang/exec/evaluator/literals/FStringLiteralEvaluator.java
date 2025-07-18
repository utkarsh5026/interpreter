package lang.exec.evaluator.literals;

import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.base.BaseObject;

import lang.exec.objects.Environment;
import lang.exec.objects.StringObject;

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
 * Example evaluation process:
 * - f"Hello {name}!" with name="Alice"
 * - Static parts: ["Hello ", "!"]
 * - Expressions: [name] → ["Alice"]
 * - Result: "Hello " + "Alice" + "!" = "Hello Alice!"
 */
public class FStringLiteralEvaluator implements NodeEvaluator<FStringLiteral> {

    @Override
    public BaseObject evaluate(FStringLiteral node, Environment env, EvaluationContext context) {
        List<String> staticParts = node.getActualStrings();
        List<Expression> expressions = node.getExpressions();

        if (expressions.isEmpty()) {
            return new StringObject(staticParts.get(0));
        }

        String[] expressionValues = new String[expressions.size()];
        for (int i = 0; i < expressions.size(); i++) {
            BaseObject result = context.evaluate(expressions.get(i), env);

            if (ObjectValidator.isError(result)) {
                return result;
            }

            expressionValues[i] = convertToString(result);
        }

        return createFinalString(staticParts, expressionValues);
    }

    /**
     * 🔄 Converts any BaseObject to its string representation
     */
    private String convertToString(BaseObject obj) {
        if (obj == null) {
            return "null";
        }

        switch (obj.type()) {
            case STRING:
                return ObjectValidator.asString(obj).getValue();

            case INTEGER:
                return String.valueOf(ObjectValidator.asInteger(obj).getValue());

            case BOOLEAN:
                return ObjectValidator.asBoolean(obj).getValue() ? "true" : "false";

            case NULL:
                return "null";

            case BUILTIN:
                return String.format("<builtin function: %s>",
                        ObjectValidator.asBuiltin(obj).getName());

            default:
                return obj.inspect();
        }
    }

    /**
     * 🔄 Creates the final string by interpolating static parts with expression
     * values
     */
    private StringObject createFinalString(List<String> staticParts, String[] expressionValues) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < expressionValues.length; i++) {
            result.append(staticParts.get(i));
            result.append(expressionValues[i]);
        }

        result.append(staticParts.get(staticParts.size() - 1));

        return new StringObject(result.toString());
    }
}