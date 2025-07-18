package lang.exec.evaluator.literals;

import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.base.BaseObject;
import lang.exec.objects.FloatObject;
import lang.exec.objects.Environment;
import lang.exec.evaluator.base.EvaluationContext;
import lang.ast.literals.FloatLiteral;

/**
 * 🌊 FloatLiteralEvaluator - Float Literal Evaluation Specialist 🌊
 * 
 * Evaluates FloatLiteral AST nodes into FloatObject runtime values.
 */
public class FloatLiteralEvaluator implements NodeEvaluator<FloatLiteral> {

    @Override
    public BaseObject evaluate(FloatLiteral node, Environment env, EvaluationContext context) {
        double value = node.getValue();
        return new FloatObject(value);
    }
}
