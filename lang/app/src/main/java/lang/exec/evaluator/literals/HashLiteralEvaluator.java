package lang.exec.evaluator.literals;

import java.util.Map;
import java.util.HashMap;

import lang.ast.base.Expression;
import lang.exec.validator.ObjectValidator;
import lang.exec.objects.HashObject;
import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.base.BaseObject;

import lang.exec.objects.Environment;
import lang.exec.evaluator.base.EvaluationContext;
import lang.ast.literals.HashLiteral;

public class HashLiteralEvaluator implements NodeEvaluator<HashLiteral> {

    @Override
    public BaseObject evaluate(HashLiteral node, Environment env, EvaluationContext context) {
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
}
