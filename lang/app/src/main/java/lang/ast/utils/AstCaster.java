package lang.ast.utils;

import lang.ast.base.*;
import lang.ast.statements.*;
import lang.ast.expressions.*;
import lang.ast.literals.*;

public class AstCaster {

    public static LetStatement asLetStatement(Node node) {
        if (!AstValidator.isLetStatement(node)) {
            throw new IllegalArgumentException("Node is not a LetStatement");
        }
        return (LetStatement) node;
    }

    public static AssignmentExpression asAssignmentExpression(Node node) {
        if (!AstValidator.isAssignmentExpression(node)) {
            throw new IllegalArgumentException("Node is not an AssignmentExpression");
        }
        return (AssignmentExpression) node;
    }

    public static IndexExpression asIndexExpression(Node node) {
        if (!AstValidator.isIndexExpression(node)) {
            throw new IllegalArgumentException("Node is not an IndexExpression");
        }
        return (IndexExpression) node;
    }

    public static ConstStatement asConstStatement(Node node) {
        if (!AstValidator.isConstStatement(node)) {
            throw new IllegalArgumentException("Node is not a ConstStatement");
        }
        return (ConstStatement) node;
    }

    public static ReturnStatement asReturnStatement(Node node) {
        if (!AstValidator.isReturnStatement(node)) {
            throw new IllegalArgumentException("Node is not a ReturnStatement");
        }
        return (ReturnStatement) node;
    }

    public static BlockStatement asBlockStatement(Node node) {
        if (!AstValidator.isBlockStatement(node)) {
            throw new IllegalArgumentException("Node is not a BlockStatement");
        }
        return (BlockStatement) node;
    }

    public static ExpressionStatement asExpressionStatement(Node node) {
        if (!AstValidator.isExpressionStatement(node)) {
            throw new IllegalArgumentException("Node is not an ExpressionStatement");
        }
        return (ExpressionStatement) node;
    }

    public static WhileStatement asWhileStatement(Node node) {
        if (!AstValidator.isWhileStatement(node)) {
            throw new IllegalArgumentException("Node is not a WhileStatement");
        }
        return (WhileStatement) node;
    }

    public static ForStatement asForStatement(Node node) {
        if (!AstValidator.isForStatement(node)) {
            throw new IllegalArgumentException("Node is not a ForStatement");
        }
        return (ForStatement) node;
    }

    public static Identifier asIdentifier(Node node) {
        if (!AstValidator.isIdentifier(node)) {
            throw new IllegalArgumentException("Node is not an Identifier");
        }
        return (Identifier) node;
    }

    public static InfixExpression asInfixExpression(Node node) {
        if (!AstValidator.isInfixExpression(node)) {
            throw new IllegalArgumentException("Node is not an InfixExpression");
        }
        return (InfixExpression) node;
    }

    public static CallExpression asCallExpression(Node node) {
        if (!AstValidator.isCallExpression(node)) {
            throw new IllegalArgumentException("Node is not a CallExpression");
        }
        return (CallExpression) node;
    }

    public static IntegerLiteral asIntegerLiteral(Node node) {
        if (!AstValidator.isIntegerLiteral(node)) {
            throw new IllegalArgumentException("Node is not an IntegerLiteral");
        }
        return (IntegerLiteral) node;
    }

    public static StringLiteral asStringLiteral(Node node) {
        if (!AstValidator.isStringLiteral(node)) {
            throw new IllegalArgumentException("Node is not a StringLiteral");
        }
        return (StringLiteral) node;
    }

    public static FunctionLiteral asFunctionLiteral(Node node) {
        if (!AstValidator.isFunctionLiteral(node)) {
            throw new IllegalArgumentException("Node is not a FunctionLiteral");
        }
        return (FunctionLiteral) node;
    }

    public static ArrayLiteral asArrayLiteral(Node node) {
        if (!AstValidator.isArrayLiteral(node)) {
            throw new IllegalArgumentException("Node is not an ArrayLiteral");
        }
        return (ArrayLiteral) node;
    }

    public static HashLiteral asHashLiteral(Node node) {
        if (!AstValidator.isHashLiteral(node)) {
            throw new IllegalArgumentException("Node is not a HashLiteral");
        }
        return (HashLiteral) node;
    }

    public static BooleanExpression asBooleanExpression(Node node) {
        if (!AstValidator.isBooleanExpression(node)) {
            throw new IllegalArgumentException("Node is not a BooleanExpression");
        }
        return (BooleanExpression) node;
    }

}
