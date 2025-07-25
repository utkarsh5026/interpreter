package lang.exec.evaluator.base;

import lang.exec.debug.StackFrame;
import java.util.List;

import lang.ast.base.Node;
import lang.exec.objects.base.BaseObject;
import lang.exec.objects.env.Environment;
import lang.exec.objects.error.ErrorObject;
import lang.token.TokenPosition;

/**
 * 🎛️ EvaluationContext - The Master Control Center 🎛️
 * 
 * This interface provides a way for specialized evaluators to delegate back to
 * the main evaluation system. It's like having a phone line to mission control!
 * 
 * Why we need this:
 * - Expression evaluators need to evaluate sub-expressions
 * - Statement evaluators need to evaluate nested statements
 * - Function calls need to evaluate arguments
 * - We avoid circular dependencies by using this interface
 */
public interface EvaluationContext {
    /**
     * Evaluates any AST node by delegating to the appropriate specialized evaluator
     */
    BaseObject evaluate(Node node, Environment env);

    /**
     * Evaluates a list of expressions (useful for function arguments, array
     * elements)
     */
    List<BaseObject> evaluateExpressions(List<? extends Node> expressions, Environment env);

    /**
     * Creates a new scope for block statements, functions, etc.
     */
    Environment newScope(Environment parent, boolean isBlockScope);

    /**
     * Creates a new error object
     */
    ErrorObject createError(String message, TokenPosition position);

    /**
     * Enters a new function
     */
    void enterFunction(String functionName, TokenPosition position, StackFrame.FrameType frameType);

    /**
     * Exits a function
     */
    void exitFunction();
}
