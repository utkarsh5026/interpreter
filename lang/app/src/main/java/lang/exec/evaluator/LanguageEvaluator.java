package lang.exec.evaluator;

import java.util.*;
import lang.ast.base.*;
import lang.ast.statements.*;
import lang.ast.expressions.*;
import lang.ast.literals.*;
import lang.exec.evaluator.base.*;
import lang.exec.evaluator.expressions.*;
import lang.exec.evaluator.statements.*;
import lang.exec.evaluator.literals.*;
import lang.exec.validator.ObjectValidator;
import lang.exec.debug.*;
import lang.token.TokenPosition;
import lang.exec.objects.base.*;
import lang.exec.objects.env.Environment;
import lang.exec.objects.error.ErrorObject;
import lang.exec.objects.functions.ReturnObject;
import lang.exec.objects.literals.NullObject;

public class LanguageEvaluator implements EvaluationContext {

    private final Map<Class<? extends Node>, NodeEvaluator<? extends Node>> evaluators;
    private final LoopContext loopContext;
    private final Optional<CallStack> callStack;
    private final String[] sourceLines; // 📝 NEW: Store source lines for error context

    public LanguageEvaluator() {
        this(false, null);
    }

    public LanguageEvaluator(boolean enableStackTraces) {
        this(enableStackTraces, null);
    }

    // 🆕 NEW: Constructor that accepts source lines
    public LanguageEvaluator(boolean enableStackTraces, String[] sourceLines) {
        this.evaluators = new HashMap<>();
        this.loopContext = new LoopContext();
        this.callStack = enableStackTraces ? Optional.of(new CallStack(1000)) : Optional.empty();
        this.sourceLines = sourceLines;

        registerStatementEvaluators();
        registerExpressionEvaluators();
        registerLiteralEvaluators();
    }

    // 🆕 NEW: Convenience constructor that accepts source code
    public static LanguageEvaluator withSourceCode(String sourceCode, boolean enableStackTraces) {
        String[] lines = sourceCode.split("\\r\\n|\\r|\\n");
        return new LanguageEvaluator(enableStackTraces, lines);
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

    /**
     * 🔴 Creates an error object with source context and stack trace
     */
    public ErrorObject createError(String message, TokenPosition position) {
        String sourceContext = extractSourceContext(position);

        if (callStack.isPresent()) {
            return ErrorObject.withStackTrace(message, callStack.get(), position, sourceContext);
        } else {
            return new ErrorObject(message, position, null, sourceContext);
        }
    }

    /**
     * 📝 Extracts source context around the error position
     * Get the one line above and one line below the text
     */
    private String extractSourceContext(TokenPosition position) {
        if (sourceLines == null || position == null) {
            return null;
        }

        int line = position.line();
        int column = position.column();

        if (line < 1 || line > sourceLines.length) {
            return null;
        }

        StringBuilder context = new StringBuilder();

        int startLine = Math.max(1, line - 1);
        int endLine = Math.min(sourceLines.length, line + 1);

        // Calculate the maximum width needed for the box
        int maxLineNumWidth = String.valueOf(endLine).length();
        int maxContentWidth = 0;

        // First pass: calculate max content width
        for (int i = startLine; i <= endLine; i++) {
            String lineContent = sourceLines[i - 1];
            int contentWidth = String.format("  %s  %s",
                    String.format("%" + maxLineNumWidth + "d", i), lineContent).length();
            maxContentWidth = Math.max(maxContentWidth, contentWidth);
        }

        // Ensure minimum width for the box
        maxContentWidth = Math.max(maxContentWidth, 50);

        // Top border
        context.append("┌").append("─".repeat(maxContentWidth + 2)).append("┐\n");
        context.append("│").append(String.format(" %-" + (maxContentWidth + 1) + "s", "Source Context")).append("│\n");
        context.append("├").append("─".repeat(maxContentWidth + 2)).append("┤\n");

        for (int i = startLine; i <= endLine; i++) {
            String lineContent = sourceLines[i - 1];
            String lineNumStr = String.format("%" + maxLineNumWidth + "d", i);

            if (i == line) {
                // Error line with arrow
                String content = String.format(" → %s  %s", lineNumStr, lineContent);
                context.append("│").append(String.format(" %-" + (maxContentWidth + 1) + "s", content)).append("│\n");

                // Add pointer line if column is specified
                if (column > 0) {
                    StringBuilder pointer = new StringBuilder();
                    pointer.append("   ").append(" ".repeat(maxLineNumWidth)).append("  ");
                    for (int j = 0; j < column - 1; j++) {
                        pointer.append(" ");
                    }
                    pointer.append("^");
                    context.append("│").append(String.format(" %-" + (maxContentWidth + 1) + "s", pointer.toString()))
                            .append("│\n");
                }
            } else {
                // Regular context line
                String content = String.format("   %s  %s", lineNumStr, lineContent);
                context.append("│").append(String.format(" %-" + (maxContentWidth + 1) + "s", content)).append("│\n");
            }
        }

        // Bottom border
        context.append("└").append("─".repeat(maxContentWidth + 2)).append("┘\n");

        return context.toString();
    }
}
