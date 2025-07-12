package lang.exec.evaluator;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import lang.ast.literals.*;
import lang.ast.expressions.*;
import lang.exec.base.*;
import lang.exec.objects.*;
import lang.ast.base.*;

import lang.ast.utils.AstValidator;
import lang.ast.utils.AstCaster;

import lang.exec.validator.ObjectValidator;

public class LiteralEvaluatorRegistry {

    /**
     * Checks if a node is a literal type that this registry can handle
     */
    public boolean canHandle(Node node) {
        return AstValidator.isIntegerLiteral(node) ||
                AstValidator.isStringLiteral(node) ||
                AstValidator.isBooleanExpression(node) ||
                AstValidator.isArrayLiteral(node) ||
                AstValidator.isHashLiteral(node) ||
                AstValidator.isFunctionLiteral(node);
    }

    /**
     * Evaluates literal nodes directly
     */
    public BaseObject evaluate(Node node, Environment env, EvaluationContext context) {

        if (AstValidator.isIntegerLiteral(node)) {
            IntegerLiteral integerLiteral = AstCaster.asIntegerLiteral(node);
            return new IntegerObject(integerLiteral.getValue());
        }

        if (AstValidator.isStringLiteral(node)) {
            StringLiteral stringLiteral = AstCaster.asStringLiteral(node);
            return new StringObject(stringLiteral.getValue());
        }

        if (AstValidator.isBooleanExpression(node)) {
            BooleanExpression booleanExpression = AstCaster.asBooleanExpression(node);
            return new BooleanObject(booleanExpression.getValue());
        }

        if (node instanceof ArrayLiteral) {
            return evaluateArrayLiteral((ArrayLiteral) node, env, context);
        }

        if (node instanceof HashLiteral) {
            return evaluateHashLiteral((HashLiteral) node, env, context);
        }

        if (node instanceof FunctionLiteral) {
            return evaluateFunctionLiteral((FunctionLiteral) node, env);
        }

        return new ErrorObject("Unknown literal type: " + node.getClass().getSimpleName());
    }

    /**
     * Evaluates array literals: [1, 2, 3, x + y]
     */
    private BaseObject evaluateArrayLiteral(ArrayLiteral node, Environment env, EvaluationContext context) {
        List<BaseObject> elements = context.evaluateExpressions(node.getElements(), env);

        // Check for errors in any element
        for (BaseObject element : elements) {
            if (ObjectValidator.isError(element)) {
                return element;
            }
        }

        return new ArrayObject(elements);
    }

    /**
     * Evaluates hash literals: {"key": value, "count": x + 1}
     */
    private BaseObject evaluateHashLiteral(HashLiteral node, Environment env, EvaluationContext context) {
        Map<String, BaseObject> pairs = new HashMap<>();

        for (Map.Entry<String, Expression> entry : node.getPairs().entrySet()) {
            BaseObject value = context.evaluate(entry.getValue(), env);

            if (ObjectValidator.isError(value)) {
                return value;
            }

            pairs.put(entry.getKey(), value);
        }

        return new HashObject(pairs);
    }

    /**
     * Evaluates function literals: fn(x, y) { return x + y; }
     */
    private BaseObject evaluateFunctionLiteral(FunctionLiteral node, Environment env) {
        return new FunctionObject(env, node.getParameters(), node.getBody());
    }

}