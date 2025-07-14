package lang.ast.visitor;

import lang.ast.statements.*;
import lang.ast.expressions.*;
import lang.ast.literals.*;
import lang.ast.base.*;

/**
 * Abstract base class for visitors that provides default implementations.
 * Subclasses only need to override the methods they care about.
 */
public abstract class BaseAstVisitor<T> implements AstVisitor<T> {

    protected T defaultResult() {
        return null;
    }

    @Override
    public T visitProgram(Program program) {
        return defaultResult();
    }

    @Override
    public T visitLetStatement(LetStatement letStatement) {
        return defaultResult();
    }

    @Override
    public T visitConstStatement(ConstStatement constStatement) {
        return defaultResult();
    }

    @Override
    public T visitReturnStatement(ReturnStatement returnStatement) {
        return defaultResult();
    }

    @Override
    public T visitExpressionStatement(ExpressionStatement expressionStatement) {
        return defaultResult();
    }

    @Override
    public T visitBlockStatement(BlockStatement blockStatement) {
        return defaultResult();
    }

    @Override
    public T visitWhileStatement(WhileStatement whileStatement) {
        return defaultResult();
    }

    @Override
    public T visitForStatement(ForStatement forStatement) {
        return defaultResult();
    }

    @Override
    public T visitBreakStatement(BreakStatement breakStatement) {
        return defaultResult();
    }

    @Override
    public T visitContinueStatement(ContinueStatement continueStatement) {
        return defaultResult();
    }

    @Override
    public T visitIdentifier(Identifier identifier) {
        return defaultResult();
    }

    @Override
    public T visitInfixExpression(InfixExpression infixExpression) {
        return defaultResult();
    }

    @Override
    public T visitPrefixExpression(PrefixExpression prefixExpression) {
        return defaultResult();
    }

    @Override
    public T visitBooleanExpression(BooleanExpression booleanExpression) {
        return defaultResult();
    }

    @Override
    public T visitIfExpression(IfExpression ifExpression) {
        return defaultResult();
    }

    @Override
    public T visitCallExpression(CallExpression callExpression) {
        return defaultResult();
    }

    @Override
    public T visitIndexExpression(IndexExpression indexExpression) {
        return defaultResult();
    }

    @Override
    public T visitAssignmentExpression(AssignmentExpression assignmentExpression) {
        return defaultResult();
    }

    @Override
    public T visitIntegerLiteral(IntegerLiteral integerLiteral) {
        return defaultResult();
    }

    @Override
    public T visitStringLiteral(StringLiteral stringLiteral) {
        return defaultResult();
    }

    @Override
    public T visitArrayLiteral(ArrayLiteral arrayLiteral) {
        return defaultResult();
    }

    @Override
    public T visitHashLiteral(HashLiteral hashLiteral) {
        return defaultResult();
    }

    @Override
    public T visitFunctionLiteral(FunctionLiteral functionLiteral) {
        return defaultResult();
    }

    @Override
    public T visitFStringLiteral(FStringLiteral fStringLiteral) {
        return defaultResult();
    }

    @Override
    public T visitNullExpression(NullExpression nullExpression) {
        return defaultResult();
    }
}
