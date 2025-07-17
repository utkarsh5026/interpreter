package lang.ast.visitor;

import lang.ast.statements.*;
import lang.ast.expressions.*;
import lang.ast.literals.*;
import lang.ast.base.*;

/**
 * Visitor interface for AST nodes.
 * 
 * The Visitor pattern allows us to define operations on AST nodes
 * without modifying the node classes. This is crucial for:
 * 1. Separation of concerns - parsing vs evaluation vs optimization
 * 2. Extensibility - adding new operations without changing AST
 * 3. Type safety - compile-time checking of operations
 * 
 * Each AST node type has a corresponding visit method.
 */
public interface AstVisitor<T> {

    // Program
    T visitProgram(Program program);

    // Statements
    T visitLetStatement(LetStatement letStatement);

    T visitConstStatement(ConstStatement constStatement);

    T visitReturnStatement(ReturnStatement returnStatement);

    T visitExpressionStatement(ExpressionStatement expressionStatement);

    T visitBlockStatement(BlockStatement blockStatement);

    T visitWhileStatement(WhileStatement whileStatement);

    T visitForStatement(ForStatement forStatement);

    T visitBreakStatement(BreakStatement breakStatement);

    T visitContinueStatement(ContinueStatement continueStatement);

    // Expressions
    T visitIdentifier(Identifier identifier);

    T visitInfixExpression(InfixExpression infixExpression);

    T visitPrefixExpression(PrefixExpression prefixExpression);

    T visitBooleanExpression(BooleanExpression booleanExpression);

    T visitIfExpression(IfExpression ifExpression);

    T visitCallExpression(CallExpression callExpression);

    T visitIndexExpression(IndexExpression indexExpression);

    T visitAssignmentExpression(AssignmentExpression assignmentExpression);

    // Literals
    T visitIntegerLiteral(IntegerLiteral integerLiteral);

    T visitStringLiteral(StringLiteral stringLiteral);

    T visitArrayLiteral(ArrayLiteral arrayLiteral);

    T visitHashLiteral(HashLiteral hashLiteral);

    T visitFunctionLiteral(FunctionLiteral functionLiteral);

    T visitFStringLiteral(FStringLiteral fStringLiteral);

    T visitFloatLiteral(FloatLiteral floatLiteral);

    T visitNullExpression(NullExpression nullExpression);
}
