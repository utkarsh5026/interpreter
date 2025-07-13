package lang.exec.evaluator.base;

import lang.ast.base.Node;
import lang.exec.base.BaseObject;
import lang.exec.objects.Environment;

/**
 * 🎯 NodeEvaluator - The Universal Evaluation Contract 🎯
 * 
 * This interface defines the fundamental contract that all evaluators must
 * follow.
 * Think of it as the universal language that all evaluation modules speak!
 * 
 * From first principles:
 * - Every piece of code (AST node) needs to be turned into a runtime value
 * (object)
 * - This transformation happens in a specific context (environment with
 * variables)
 * - The result is always a BaseObject (our universal value container)
 */
@FunctionalInterface
public interface NodeEvaluator<T extends Node> {
    /**
     * Evaluates a specific type of AST node into a runtime object
     * 
     * @param node    The AST node to evaluate
     * @param env     The environment containing variables and scope
     * @param context The main evaluator for delegating sub-evaluations
     * @return The evaluated result as a BaseObject
     */
    BaseObject evaluate(T node, Environment env, EvaluationContext context);
}
