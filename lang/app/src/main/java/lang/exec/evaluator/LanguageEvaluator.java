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
import lang.exec.debug.*;
import lang.token.TokenPosition;

import lang.exec.objects.*;

public class LanguageEvaluator implements EvaluationContext {

    private final Map<Class<? extends Node>, NodeEvaluator<? extends Node>> evaluators;
    private final LoopContext loopContext;
    private final Optional<CallStack> callStack;

    public LanguageEvaluator() {
        this(false);
    }

    public LanguageEvaluator(boolean enableStackTraces) {
        this.evaluators = new HashMap<>();
        this.loopContext = new LoopContext();
        this.callStack = enableStackTraces ? Optional.of(new CallStack(1000)) : Optional.empty();

        registerStatementEvaluators();
        registerExpressionEvaluators();
        registerLiteralEvaluators();

    }

    private void registerStatementEvaluators() {
        registerEvaluator(LetStatement.class, new LetStatementEvaluator());
        registerEvaluator(ConstStatement.class, new ConstStatementEvaluator());
        registerEvaluator(ReturnStatement.class, new ReturnStatementEvaluator());
        registerEvaluator(BlockStatement.class, new BlockStatementEvaluator());
        registerEvaluator(WhileStatement.class, new WhileStatementEvaluator(loopContext));
        registerEvaluator(ForStatement.class, new ForStatementEvaluator(loopContext));
        registerEvaluator(BreakStatement.class, new BreakStatementParser());
        registerEvaluator(ContinueStatement.class, new ContinueStatementParser());
        registerEvaluator(ClassStatement.class, new ClassStatementEvaluator());
    }

    private void registerExpressionEvaluators() {
        registerEvaluator(CallExpression.class, new CallExpressionEvaluator());
        registerEvaluator(IfExpression.class, new IfExpressionEvaluator());
        registerEvaluator(ExpressionStatement.class, new ExpressionStatementEvaluator());
        registerEvaluator(PrefixExpression.class, new PrefixExpressionEvaluator());
        registerEvaluator(InfixExpression.class, new InfixExpressionEvaluator());
        registerEvaluator(Identifier.class, new IndentifierEvaluator());
        registerEvaluator(AssignmentExpression.class, new AssignmentExpressionEvaluator());
        registerEvaluator(IndexExpression.class, new IndexExpressionEvaluator());
        registerEvaluator(NewExpression.class, new NewExpressionEvaluator());
        registerEvaluator(PropertyExpression.class, new PropertyExpressionEvaluator());
        registerEvaluator(SuperExpression.class, new SuperExpressionEvaluator());
        registerEvaluator(ThisExpression.class, new ThisExpressionEvaluator());
    }

    private void registerLiteralEvaluators() {
        registerEvaluator(StringLiteral.class, new StringLiteralEvaluator());
        registerEvaluator(IntegerLiteral.class, new IntegerLiteralEvaluator());
        registerEvaluator(BooleanExpression.class, new BooleanLiteralEvaluator());
        registerEvaluator(ArrayLiteral.class, new ArrayLiteralEvaluator());
        registerEvaluator(HashLiteral.class, new HashLiteralEvaluator());
        registerEvaluator(FunctionLiteral.class, new FunctionLiteralEvaluator());
        registerEvaluator(NullExpression.class, new NullLiteralEvaluator());
        registerEvaluator(FStringLiteral.class, new FStringLiteralEvaluator());
        registerEvaluator(FloatLiteral.class, new FloatLiteralEvaluator());
    }

    /**
     * 📝 Helper method to register evaluators
     */
    private <T extends Node> void registerEvaluator(Class<T> nodeType, NodeEvaluator<T> evaluator) {
        evaluators.put(nodeType, evaluator);
    }

    /**
     * 🏗️ Creates new scopes for blocks, functions, loops etc.
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

        for (Node expr : expressions) {
            BaseObject result = evaluate(expr, env);
            if (ObjectValidator.isError(result)) {
                return Arrays.asList(result);
            }
            results.add(result);
        }

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

    public void enterFunction(String functionName, TokenPosition position, StackFrame.FrameType frameType) {
        if (!callStack.isPresent())
            return;
        StackFrame frame = new StackFrame(functionName, position, frameType);
        callStack.ifPresent(stack -> stack.push(frame));
    }

    public void exitFunction() {
        if (!callStack.isPresent())
            return;
        callStack.ifPresent(stack -> stack.pop());
    }

    public Optional<CallStack> getCallStack() {
        return callStack;
    }

    public ErrorObject createError(String message, TokenPosition position) {
        if (callStack.isPresent()) {
            return ErrorObject.withStackTrace(message, callStack.get());
        } else {
            return new ErrorObject(message, position, null, null);
        }
    }
}
