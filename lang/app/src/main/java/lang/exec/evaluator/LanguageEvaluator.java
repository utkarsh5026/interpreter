package lang.exec.evaluator;

import java.util.*;
import lang.ast.base.*;
import lang.ast.statements.*;
import lang.ast.expressions.*;
import lang.ast.literals.*;
import lang.exec.evaluator.base.EvaluationContext;
import lang.exec.evaluator.base.*;
import lang.exec.evaluator.expressions.*;
import lang.exec.evaluator.statements.*;
import lang.exec.evaluator.literals.*;
import lang.exec.validator.ObjectValidator;
import lang.exec.base.*;

import lang.exec.objects.*;

public class LanguageEvaluator implements EvaluationContext {

    private final Map<Class<? extends Node>, NodeEvaluator<? extends Node>> evaluators;

    public LanguageEvaluator() {
        this.evaluators = new HashMap<>();

        registerStatementEvaluators();
        registerExpressionEvaluators();
        registerLiteralEvaluators();
    }

    private void registerStatementEvaluators() {
        registerEvaluator(LetStatement.class, new LetStatementEvaluator());
        registerEvaluator(ConstStatement.class, new ConstStatementEvaluator());
        registerEvaluator(ReturnStatement.class, new ReturnStatementEvaluator());
        registerEvaluator(BlockStatement.class, new BlockStatementEvaluator());
    }

    private void registerExpressionEvaluators() {
        registerEvaluator(CallExpression.class, new CallExpressionEvaluator());
        registerEvaluator(IfExpression.class, new IfExpressionEvaluator());
        registerEvaluator(ExpressionStatement.class, new ExpressionStatementEvaluator());
        registerEvaluator(PrefixExpression.class, new PrefixExpressionEvaluator());
        registerEvaluator(InfixExpression.class, new InfixExpressionEvaluator());
        registerEvaluator(Identifier.class, new IndentifierEvaluator());
        registerEvaluator(AssignmentExpression.class, new AssignmentExpressionEvaluator());
    }

    private void registerLiteralEvaluators() {
        registerEvaluator(StringLiteral.class, new StringLiteralEvaluator());
        registerEvaluator(IntegerLiteral.class, new IntegerLiteralEvaluator());
        registerEvaluator(BooleanExpression.class, new BooleanLiteralEvaluator());
        registerEvaluator(ArrayLiteral.class, new ArrayLiteralEvaluator());
        registerEvaluator(HashLiteral.class, new HashLiteralEvaluator());
        registerEvaluator(FunctionLiteral.class, new FunctionLiteralEvaluator());
    }

    /**
     * 📝 Helper method to register evaluators
     */
    private <T extends Node> void registerEvaluator(Class<T> nodeType, NodeEvaluator<T> evaluator) {
        evaluators.put(nodeType, evaluator);
    }

    /**
     * 🏗️ Creates new scopes for blocks, functions, etc.
     */
    @Override
    public Environment newScope(Environment parent, boolean isBlockScope) {
        return new Environment(parent, isBlockScope);
    }

    /**
     * 📋 Evaluates a list of expressions (for function arguments, array elements,
     * etc.)
     */
    @Override
    public List<BaseObject> evaluateExpressions(List<? extends Node> expressions, Environment env) {
        List<BaseObject> results = new ArrayList<>();

        expressions.stream()
                .map(expr -> evaluate(expr, env))
                .takeWhile(result -> !ObjectValidator.isError(result))
                .forEach(results::add);

        return results;
    }

    /**
     * 🎯 Main evaluation method - delegates to appropriate specialist
     */
    @Override
    @SuppressWarnings("unchecked")
    public BaseObject evaluate(Node node, Environment env) {
        if (node == null) {
            return new ErrorObject("Cannot evaluate null node");
        }

        NodeEvaluator<Node> evaluator = (NodeEvaluator<Node>) evaluators.get(node.getClass());

        if (evaluator != null) {
            return evaluator.evaluate(node, env, this);
        }

        return new ErrorObject("No evaluator found for node type: " + node.getClass().getSimpleName());
    }

    /**
     * 🚀 Public entry point for evaluating programs
     */
    public BaseObject evaluateProgram(Program program, Environment env) {
        BaseObject result = NullObject.INSTANCE;

        for (Statement stmt : program.getStatements()) {
            result = evaluate(stmt, env);

            if (ObjectValidator.isReturnValue(result)) {
                return ((ReturnObject) result).getValue();
            }
            if (ObjectValidator.isError(result)) {
                return result;
            }
        }

        return result;
    }
}
