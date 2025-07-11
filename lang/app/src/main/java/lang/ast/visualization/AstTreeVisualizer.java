package lang.ast.visualization;

import lang.token.TokenPosition;
import java.util.*;
import lang.ast.statements.*;
import lang.ast.expressions.*;
import lang.ast.literals.*;
import lang.ast.base.*;

import lang.ast.visitor.BaseAstVisitor;

/**
 * Beautiful AST Tree Visualizer
 * 
 * Creates hierarchical, visually appealing representations of AST trees
 * with proper indentation, tree lines, colors, and detailed node information.
 */
public class AstTreeVisualizer extends BaseAstVisitor<TreeNode> {

    /**
     * Visualization styles and configurations
     */
    public enum Style {
        UNICODE_TREE, // └── ├── │
        ASCII_TREE, // +-- |-- |
        INDENTED, // Simple indentation
        COMPACT, // Minimal formatting
        DETAILED // Maximum information
    }

    public enum ColorScheme {
        NONE, // No colors
        TERMINAL, // ANSI terminal colors
        HTML, // HTML with CSS classes
        MARKDOWN // Markdown formatting
    }

    private final TreeRenderer renderer;

    public AstTreeVisualizer() {
        this(new VisualizationConfig());
    }

    public AstTreeVisualizer(VisualizationConfig config) {
        this.renderer = new TreeRenderer(config);
    }

    /**
     * Main method to visualize an AST
     */
    public String visualize(Node ast) {
        TreeNode treeNode = ast.accept(this);
        return renderer.render(treeNode);
    }

    /**
     * Visualize with custom configuration
     */
    public String visualize(Node ast, VisualizationConfig config) {
        AstTreeVisualizer visualizer = new AstTreeVisualizer(config);
        return visualizer.visualize(ast);
    }

    // =========================================================================
    // PROGRAM AND STATEMENTS
    // =========================================================================

    @Override
    public TreeNode visitProgram(Program program) {
        TreeNode node = createNode("Program", "ROOT", program.position());
        node.addMetadata("statements", program.getStatements().size());

        for (int i = 0; i < program.getStatements().size(); i++) {
            Statement stmt = program.getStatements().get(i);
            TreeNode child = stmt.accept(this);
            child.setIndex(i);
            node.addChild(child);
        }

        return node;
    }

    @Override
    public TreeNode visitLetStatement(LetStatement letStatement) {
        TreeNode node = createNode("LetStatement", "DECLARATION", letStatement.position());
        node.addMetadata("variable", letStatement.getName().getValue());

        TreeNode nameNode = letStatement.getName().accept(this);
        nameNode.setLabel("name");
        node.addChild(nameNode);

        TreeNode valueNode = letStatement.getValue().accept(this);
        valueNode.setLabel("value");
        node.addChild(valueNode);

        return node;
    }

    @Override
    public TreeNode visitConstStatement(ConstStatement constStatement) {
        TreeNode node = createNode("ConstStatement", "DECLARATION", constStatement.position());
        node.addMetadata("variable", constStatement.getName().getValue());
        node.addMetadata("immutable", true);

        TreeNode nameNode = constStatement.getName().accept(this);
        nameNode.setLabel("name");
        node.addChild(nameNode);

        TreeNode valueNode = constStatement.getValue().accept(this);
        valueNode.setLabel("value");
        node.addChild(valueNode);

        return node;
    }

    @Override
    public TreeNode visitReturnStatement(ReturnStatement returnStatement) {
        TreeNode node = createNode("ReturnStatement", "CONTROL", returnStatement.position());

        TreeNode valueNode = returnStatement.getReturnValue().accept(this);
        valueNode.setLabel("return_value");
        node.addChild(valueNode);

        return node;
    }

    @Override
    public TreeNode visitExpressionStatement(ExpressionStatement expressionStatement) {
        TreeNode node = createNode("ExpressionStatement", "STATEMENT", expressionStatement.position());

        TreeNode exprNode = expressionStatement.getExpression().accept(this);
        exprNode.setLabel("expression");
        node.addChild(exprNode);

        return node;
    }

    @Override
    public TreeNode visitBlockStatement(BlockStatement blockStatement) {
        TreeNode node = createNode("BlockStatement", "BLOCK", blockStatement.position());
        node.addMetadata("statement_count", blockStatement.getStatements().size());

        for (int i = 0; i < blockStatement.getStatements().size(); i++) {
            Statement stmt = blockStatement.getStatements().get(i);
            TreeNode child = stmt.accept(this);
            child.setIndex(i);
            node.addChild(child);
        }

        return node;
    }

    @Override
    public TreeNode visitWhileStatement(WhileStatement whileStatement) {
        TreeNode node = createNode("WhileStatement", "LOOP", whileStatement.position());

        TreeNode conditionNode = whileStatement.getCondition().accept(this);
        conditionNode.setLabel("condition");
        node.addChild(conditionNode);

        TreeNode bodyNode = whileStatement.getBody().accept(this);
        bodyNode.setLabel("body");
        node.addChild(bodyNode);

        return node;
    }

    @Override
    public TreeNode visitForStatement(ForStatement forStatement) {
        TreeNode node = createNode("ForStatement", "LOOP", forStatement.position());

        TreeNode initNode = forStatement.getInitializer().accept(this);
        initNode.setLabel("initializer");
        node.addChild(initNode);

        TreeNode conditionNode = forStatement.getCondition().accept(this);
        conditionNode.setLabel("condition");
        node.addChild(conditionNode);

        TreeNode incrementNode = forStatement.getIncrement().accept(this);
        incrementNode.setLabel("increment");
        node.addChild(incrementNode);

        TreeNode bodyNode = forStatement.getBody().accept(this);
        bodyNode.setLabel("body");
        node.addChild(bodyNode);

        return node;
    }

    @Override
    public TreeNode visitBreakStatement(BreakStatement breakStatement) {
        TreeNode node = createNode("BreakStatement", "CONTROL", breakStatement.position());
        node.addMetadata("flow_control", "break");
        return node;
    }

    @Override
    public TreeNode visitContinueStatement(ContinueStatement continueStatement) {
        TreeNode node = createNode("ContinueStatement", "CONTROL", continueStatement.position());
        node.addMetadata("flow_control", "continue");
        return node;
    }

    // =========================================================================
    // EXPRESSIONS
    // =========================================================================

    @Override
    public TreeNode visitIdentifier(Identifier identifier) {
        TreeNode node = createNode("Identifier", "VARIABLE", identifier.position());
        node.addMetadata("name", identifier.getValue());
        node.setValue(identifier.getValue());
        return node;
    }

    @Override
    public TreeNode visitInfixExpression(InfixExpression infixExpression) {
        TreeNode node = createNode("InfixExpression", "OPERATOR", infixExpression.position());
        node.addMetadata("operator", infixExpression.getOperator());
        node.setValue(infixExpression.getOperator());

        TreeNode leftNode = infixExpression.getLeft().accept(this);
        leftNode.setLabel("left");
        node.addChild(leftNode);

        TreeNode rightNode = infixExpression.getRight().accept(this);
        rightNode.setLabel("right");
        node.addChild(rightNode);

        return node;
    }

    @Override
    public TreeNode visitPrefixExpression(PrefixExpression prefixExpression) {
        TreeNode node = createNode("PrefixExpression", "OPERATOR", prefixExpression.position());
        node.addMetadata("operator", prefixExpression.getOperator());
        node.setValue(prefixExpression.getOperator());

        TreeNode operandNode = prefixExpression.getRight().accept(this);
        operandNode.setLabel("operand");
        node.addChild(operandNode);

        return node;
    }

    @Override
    public TreeNode visitBooleanExpression(BooleanExpression booleanExpression) {
        TreeNode node = createNode("BooleanExpression", "LITERAL", booleanExpression.position());
        node.addMetadata("value", booleanExpression.getValue());
        node.setValue(String.valueOf(booleanExpression.getValue()));
        return node;
    }

    @Override
    public TreeNode visitIfExpression(IfExpression ifExpression) {
        TreeNode node = createNode("IfExpression", "CONDITIONAL", ifExpression.position());
        node.addMetadata("branches", ifExpression.getConditions().size());
        node.addMetadata("has_else", ifExpression.getAlternative() != null);

        // Add if/elif branches
        for (int i = 0; i < ifExpression.getConditions().size(); i++) {
            String branchType = i == 0 ? "if" : "elif";

            TreeNode branchNode = createNode(branchType + "_branch", "BRANCH", null);

            TreeNode conditionNode = ifExpression.getConditions().get(i).accept(this);
            conditionNode.setLabel("condition");
            branchNode.addChild(conditionNode);

            TreeNode consequenceNode = ifExpression.getConsequences().get(i).accept(this);
            consequenceNode.setLabel("consequence");
            branchNode.addChild(consequenceNode);

            node.addChild(branchNode);
        }

        // Add else branch if present
        if (ifExpression.getAlternative() != null) {
            TreeNode elseNode = ifExpression.getAlternative().accept(this);
            elseNode.setLabel("else");
            node.addChild(elseNode);
        }

        return node;
    }

    @Override
    public TreeNode visitCallExpression(CallExpression callExpression) {
        TreeNode node = createNode("CallExpression", "CALL", callExpression.position());
        node.addMetadata("argument_count", callExpression.getArguments().size());

        TreeNode functionNode = callExpression.getFunction().accept(this);
        functionNode.setLabel("function");
        node.addChild(functionNode);

        if (!callExpression.getArguments().isEmpty()) {
            TreeNode argsNode = createNode("arguments", "ARGS", null);

            for (int i = 0; i < callExpression.getArguments().size(); i++) {
                Expression arg = callExpression.getArguments().get(i);
                TreeNode argNode = arg.accept(this);
                argNode.setLabel("arg[" + i + "]");
                argsNode.addChild(argNode);
            }

            node.addChild(argsNode);
        }

        return node;
    }

    @Override
    public TreeNode visitIndexExpression(IndexExpression indexExpression) {
        TreeNode node = createNode("IndexExpression", "ACCESS", indexExpression.position());

        TreeNode leftNode = indexExpression.getLeft().accept(this);
        leftNode.setLabel("object");
        node.addChild(leftNode);

        TreeNode indexNode = indexExpression.getIndex().accept(this);
        indexNode.setLabel("index");
        node.addChild(indexNode);

        return node;
    }

    @Override
    public TreeNode visitAssignmentExpression(AssignmentExpression assignmentExpression) {
        TreeNode node = createNode("AssignmentExpression", "ASSIGNMENT", assignmentExpression.position());
        node.addMetadata("variable", assignmentExpression.getName().getValue());

        TreeNode nameNode = assignmentExpression.getName().accept(this);
        nameNode.setLabel("target");
        node.addChild(nameNode);

        TreeNode valueNode = assignmentExpression.getValue().accept(this);
        valueNode.setLabel("value");
        node.addChild(valueNode);

        return node;
    }

    // =========================================================================
    // LITERALS
    // =========================================================================

    @Override
    public TreeNode visitIntegerLiteral(IntegerLiteral integerLiteral) {
        TreeNode node = createNode("IntegerLiteral", "LITERAL", integerLiteral.position());
        node.addMetadata("value", integerLiteral.getValue());
        node.setValue(String.valueOf(integerLiteral.getValue()));
        return node;
    }

    @Override
    public TreeNode visitStringLiteral(StringLiteral stringLiteral) {
        TreeNode node = createNode("StringLiteral", "LITERAL", stringLiteral.position());
        node.addMetadata("value", stringLiteral.getValue());
        node.addMetadata("length", stringLiteral.getValue().length());
        node.setValue("\"" + stringLiteral.getValue() + "\"");
        return node;
    }

    @Override
    public TreeNode visitArrayLiteral(ArrayLiteral arrayLiteral) {
        TreeNode node = createNode("ArrayLiteral", "COLLECTION", arrayLiteral.position());
        node.addMetadata("element_count", arrayLiteral.getElements().size());

        for (int i = 0; i < arrayLiteral.getElements().size(); i++) {
            Expression element = arrayLiteral.getElements().get(i);
            TreeNode elementNode = element.accept(this);
            elementNode.setLabel("[" + i + "]");
            node.addChild(elementNode);
        }

        return node;
    }

    @Override
    public TreeNode visitHashLiteral(HashLiteral hashLiteral) {
        TreeNode node = createNode("HashLiteral", "COLLECTION", hashLiteral.position());
        node.addMetadata("pair_count", hashLiteral.getPairs().size());

        int index = 0;
        for (Map.Entry<String, Expression> pair : hashLiteral.getPairs().entrySet()) {
            TreeNode pairNode = createNode("key_value_pair", "PAIR", null);
            pairNode.setLabel("pair[" + index + "]");

            // Add key as metadata and create simple node
            TreeNode keyNode = createNode("key", "KEY", null);
            keyNode.setValue(pair.getKey());
            pairNode.addChild(keyNode);

            // Add value
            TreeNode valueNode = pair.getValue().accept(this);
            valueNode.setLabel("value");
            pairNode.addChild(valueNode);

            node.addChild(pairNode);
            index++;
        }

        return node;
    }

    @Override
    public TreeNode visitFunctionLiteral(FunctionLiteral functionLiteral) {
        TreeNode node = createNode("FunctionLiteral", "FUNCTION", functionLiteral.position());
        node.addMetadata("parameter_count", functionLiteral.getParameters().size());

        if (!functionLiteral.getParameters().isEmpty()) {
            TreeNode paramsNode = createNode("parameters", "PARAMS", null);

            for (int i = 0; i < functionLiteral.getParameters().size(); i++) {
                Identifier param = functionLiteral.getParameters().get(i);
                TreeNode paramNode = param.accept(this);
                paramNode.setLabel("param[" + i + "]");
                paramsNode.addChild(paramNode);
            }

            node.addChild(paramsNode);
        }

        TreeNode bodyNode = functionLiteral.getBody().accept(this);
        bodyNode.setLabel("body");
        node.addChild(bodyNode);

        return node;
    }

    @Override
    public TreeNode visitFStringLiteral(FStringLiteral fStringLiteral) {
        TreeNode node = createNode("FStringLiteral", "LITERAL", fStringLiteral.position());
        node.addMetadata("template", fStringLiteral.getValue());
        node.setValue("f\"" + fStringLiteral.getValue() + "\"");
        return node;
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    private TreeNode createNode(String type, String category, TokenPosition position) {
        TreeNode node = new TreeNode(type, category);
        if (position != null) {
            node.addMetadata("line", position.line());
            node.addMetadata("column", position.column());
        }
        return node;
    }

    @Override
    protected TreeNode defaultResult() {
        return createNode("Unknown", "UNKNOWN", null);
    }
}