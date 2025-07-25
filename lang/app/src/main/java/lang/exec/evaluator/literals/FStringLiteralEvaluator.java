package lang.exec.evaluator.literals;

import lang.exec.evaluator.base.EvaluationContext;
import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.objects.base.BaseObject;
import lang.exec.objects.env.Environment;
import lang.exec.objects.literals.StringObject;
import lang.exec.validator.ObjectValidator;
import lang.ast.literals.FStringLiteral;
import lang.ast.base.Expression;

import java.util.List;

public class FStringLiteralEvaluator implements NodeEvaluator<FStringLiteral> {

    @Override
    public BaseObject evaluate(FStringLiteral node, Environment env, EvaluationContext context) {
        List<String> staticParts = node.getActualStrings();
        List<Expression> expressions = node.getExpressions();

        if (expressions.isEmpty()) {
            return new StringObject(staticParts.get(0));
        }

        int epxressionCount = expressions.size();
        String[] expressionValues = new String[epxressionCount];
        for (int i = 0; i < epxressionCount; i++) {
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