package lang.ast.base;

import lang.token.TokenPosition;

/**
 * Represents a node in the Abstract Syntax Tree (AST).
 * Nodes are the building blocks of the AST, representing expressions,
 * statements,
 * and other language constructs.
 */
public interface Node {
    /**
     * Returns the literal value of the token associated with this node.
     */
    String tokenLiteral();

    /**
     * Returns a string representation of the node.
     */
    String toString();

    /**
     * Returns the position of this node in the source code.
     */
    TokenPosition position();
}
