package lang.ast.utils;

import lang.ast.base.*;
import lang.ast.statements.*;
import lang.ast.expressions.*;
import lang.ast.literals.*;

/**
 * Utility class for AST node type checking and validation.
 * Provides type-safe methods to check AST node types without casting.
 * 
 * This is similar to the TypeScript AstValidator but uses Java's instanceof
 * operator for runtime type checking.
 */
public final class AstValidator {

    private AstValidator() {
        // Utility class - prevent instantiation
        throw new UnsupportedOperationException("Utility class");
    }

    // Statement type checks

    public static boolean isLetStatement(Node node) {
        return node instanceof LetStatement;
    }

    public static boolean isConstStatement(Node node) {
        return node instanceof ConstStatement;
    }

    public static boolean isReturnStatement(Node node) {
        return node instanceof ReturnStatement;
    }

    public static boolean isBlockStatement(Node node) {
        return node instanceof BlockStatement;
    }

    public static boolean isExpressionStatement(Node node) {
        return node instanceof ExpressionStatement;
    }

    public static boolean isWhileStatement(Node node) {
        return node instanceof WhileStatement;
    }

    public static boolean isBreakStatement(Node node) {
        return node instanceof BreakStatement;
    }

    public static boolean isContinueStatement(Node node) {
        return node instanceof ContinueStatement;
    }

    public static boolean isForStatement(Node node) {
        return node instanceof ForStatement;
    }

    // Expression type checks

    public static boolean isIdentifier(Node node) {
        return node instanceof Identifier;
    }

    public static boolean isPrefixExpression(Node node) {
        return node instanceof PrefixExpression;
    }

    public static boolean isInfixExpression(Node node) {
        return node instanceof InfixExpression;
    }

    public static boolean isBooleanExpression(Node node) {
        return node instanceof BooleanExpression;
    }

    public static boolean isIfExpression(Node node) {
        return node instanceof IfExpression;
    }

    public static boolean isIndexExpression(Node node) {
        return node instanceof IndexExpression;
    }

    public static boolean isCallExpression(Node node) {
        return node instanceof CallExpression;
    }

    public static boolean isAssignmentExpression(Node node) {
        return node instanceof AssignmentExpression;
    }

    // Literal type checks

    public static boolean isIntegerLiteral(Node node) {
        return node instanceof IntegerLiteral;
    }

    public static boolean isStringLiteral(Node node) {
        return node instanceof StringLiteral;
    }

    public static boolean isFunctionLiteral(Node node) {
        return node instanceof FunctionLiteral;
    }

    public static boolean isArrayLiteral(Node node) {
        return node instanceof ArrayLiteral;
    }

    public static boolean isHashLiteral(Node node) {
        return node instanceof HashLiteral;
    }

}