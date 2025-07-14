package lang.parser.parsers;

import lang.ast.base.Expression;
import lang.parser.core.ParsingContext;
import lang.parser.core.PrecedenceTable;
import lang.parser.core.StatementParse;

import lang.parser.core.TokenStream;

import lang.ast.expressions.*;
import lang.ast.statements.*;
import lang.ast.base.*;
import lang.ast.literals.*;

import lang.token.*;
import java.util.*;

/**
 * ExpressionParser implements Pratt parsing for expressions.
 * 
 * Pratt parsing elegantly handles operator precedence by associating
 * each token type with:
 * - A prefix parser (for tokens that can start an expression)
 * - An infix parser (for tokens that can appear in the middle)
 * - A precedence level
 * 
 * This allows us to parse complex expressions like: 5 + 3 * 2 - func(x)[0]
 * with correct precedence and associativity.
 */
public class ExpressionParser {

    private final StatementParse statementParser;

    // Function interfaces for prefix and infix parsers
    @FunctionalInterface
    public interface PrefixParser {
        Expression parse(ParsingContext context);
    }

    @FunctionalInterface
    public interface InfixParser {
        Expression parse(ParsingContext context, Expression left);
    }

    // Maps token types to their parsing functions
    private final Map<TokenType, PrefixParser> prefixParsers = new HashMap<>();
    private final Map<TokenType, InfixParser> infixParsers = new HashMap<>();

    public ExpressionParser(StatementParse statementParser) {
        this.statementParser = statementParser;
        setupPrefixParsers();
        setupInfixParsers();
    }

    /**
     * Main expression parsing method using Pratt parsing.
     * 
     * @param context    The parsing context
     * @param precedence The minimum precedence for this parsing level
     * @return The parsed expression or null if parsing failed
     */
    public Expression parseExpression(ParsingContext context, PrecedenceTable.Precedence precedence) {
        TokenStream tokens = context.getTokenStream();

        PrefixParser prefixParser = prefixParsers.get(tokens.getCurrentToken().type());

        if (prefixParser == null) {
            context.getErrors().addPrefixError(
                    tokens.getCurrentToken().type(),
                    tokens.getCurrentToken());
            return null;
        }

        Expression leftExpression = prefixParser.parse(context);
        if (leftExpression == null) {
            return null;
        }

        System.out.println("Left expression: " + leftExpression);
        System.out.println("current token: " + tokens.getCurrentToken());

        // Continue parsing while we have higher precedence operators
        while (!tokens.isPeekToken(TokenType.SEMICOLON) &&
                precedence.getLevel() < context.getPrecedenceTable()
                        .getPrecedence(tokens.getPeekToken().type()).getLevel()) {

            System.out.println("Infix parser: " + tokens.getPeekToken());

            InfixParser infixParser = infixParsers.get(tokens.getPeekToken().type());

            if (infixParser == null) {
                return leftExpression;
            }

            tokens.advance();
            leftExpression = infixParser.parse(context, leftExpression);

            if (leftExpression == null) {
                return null;
            }
        }

        tokens.advanceIfPeek(TokenType.SEMICOLON);
        return leftExpression;
    }

    /**
     * Sets up prefix parsers for tokens that can start expressions.
     */
    private void setupPrefixParsers() {
        // Literals
        prefixParsers.put(TokenType.IDENTIFIER, this::parseIdentifier);
        prefixParsers.put(TokenType.INT, this::parseIntegerLiteral);
        prefixParsers.put(TokenType.STRING, this::parseStringLiteral);
        prefixParsers.put(TokenType.TRUE, this::parseBoolean);
        prefixParsers.put(TokenType.FALSE, this::parseBoolean);

        // Prefix operators
        prefixParsers.put(TokenType.BANG, this::parsePrefixExpression);
        prefixParsers.put(TokenType.MINUS, this::parsePrefixExpression);

        // Grouped expressions
        prefixParsers.put(TokenType.LPAREN, this::parseGroupedExpression);

        // Control flow
        prefixParsers.put(TokenType.IF, this::parseIfExpression);

        // Complex literals
        prefixParsers.put(TokenType.FUNCTION, this::parseFunctionLiteral);
        prefixParsers.put(TokenType.LBRACKET, this::parseArrayLiteral);
        prefixParsers.put(TokenType.LBRACE, this::parseHashLiteral);
    }

    /**
     * Sets up infix parsers for operators that appear between expressions.
     */
    private void setupInfixParsers() {
        // Arithmetic operators
        infixParsers.put(TokenType.PLUS, this::parseInfixExpression);
        infixParsers.put(TokenType.MINUS, this::parseInfixExpression);
        infixParsers.put(TokenType.ASTERISK, this::parseInfixExpression);
        infixParsers.put(TokenType.SLASH, this::parseInfixExpression);
        infixParsers.put(TokenType.MODULUS, this::parseInfixExpression);

        // Comparison operators
        infixParsers.put(TokenType.EQ, this::parseInfixExpression);
        infixParsers.put(TokenType.NOT_EQ, this::parseInfixExpression);
        infixParsers.put(TokenType.LESS_THAN, this::parseInfixExpression);
        infixParsers.put(TokenType.GREATER_THAN, this::parseInfixExpression);
        infixParsers.put(TokenType.LESS_THAN_OR_EQUAL, this::parseInfixExpression);
        infixParsers.put(TokenType.GREATER_THAN_OR_EQUAL, this::parseInfixExpression);

        // Logical operators
        infixParsers.put(TokenType.AND, this::parseInfixExpression);
        infixParsers.put(TokenType.OR, this::parseInfixExpression);

        // Assignment
        infixParsers.put(TokenType.ASSIGN, this::parseAssignmentExpression);

        // Function calls and indexing
        infixParsers.put(TokenType.LPAREN, this::parseCallExpression);
        infixParsers.put(TokenType.LBRACKET, this::parseIndexExpression);
    }

    // Prefix Parser Implementations

    private Expression parseIdentifier(ParsingContext context) {
        Token token = context.getTokenStream().getCurrentToken();
        return new Identifier(token, token.literal());
    }

    private Expression parseIntegerLiteral(ParsingContext context) {
        Token token = context.getTokenStream().getCurrentToken();

        try {
            int value = Integer.parseInt(token.literal());
            return new IntegerLiteral(token, value);
        } catch (NumberFormatException e) {
            context.addError("Could not parse " + token.literal() + " as integer", token);
            return null;
        }
    }

    private Expression parseStringLiteral(ParsingContext context) {
        Token token = context.getTokenStream().getCurrentToken();
        return new StringLiteral(token, token.literal());
    }

    private Expression parseBoolean(ParsingContext context) {
        Token token = context.getTokenStream().getCurrentToken();
        boolean value = token.type() == TokenType.TRUE;
        return new BooleanExpression(token, value);
    }

    private Expression parsePrefixExpression(ParsingContext context) {
        TokenStream tokens = context.getTokenStream();
        Token token = tokens.getCurrentToken();
        String operator = token.literal();

        tokens.advance();

        Expression right = parseExpression(context, PrecedenceTable.Precedence.PREFIX);
        if (right == null) {
            return null;
        }

        return new PrefixExpression(token, operator, right);
    }

    private Expression parseGroupedExpression(ParsingContext context) {
        TokenStream tokens = context.getTokenStream();

        tokens.advance(); // consume '('

        Expression expression = parseExpression(context, PrecedenceTable.Precedence.LOWEST);
        if (expression == null) {
            return null;
        }

        if (!tokens.expect(TokenType.RPAREN)) {
            context.addTokenError(TokenType.RPAREN, tokens.getCurrentToken());
            return null;
        }

        return expression;
    }

    private Expression parseArrayLiteral(ParsingContext context) {
        Token token = context.getTokenStream().getCurrentToken();
        List<Expression> elements = parseExpressionList(context, TokenType.RBRACKET);
        return new ArrayLiteral(token, elements);
    }

    private Expression parseHashLiteral(ParsingContext context) {
        TokenStream tokens = context.getTokenStream();
        Token token = tokens.getCurrentToken();
        Map<String, Expression> pairs = new LinkedHashMap<>();

        while (!tokens.isPeekToken(TokenType.RBRACE)) {
            tokens.advance();

            // Parse key (must be string or integer literal)
            Expression key = parseExpression(context, PrecedenceTable.Precedence.LOWEST);
            if (key == null) {
                return null;
            }

            if (!(key instanceof StringLiteral) && !(key instanceof IntegerLiteral)) {
                context.addError("Hash key must be string or integer literal",
                        tokens.getCurrentToken());
                return null;
            }

            // Expect ':'
            if (!tokens.expect(TokenType.COLON)) {
                context.addTokenError(TokenType.COLON, tokens.getCurrentToken());
                return null;
            }

            tokens.advance();

            // Parse value
            Expression value = parseExpression(context, PrecedenceTable.Precedence.LOWEST);
            if (value == null) {
                return null;
            }

            pairs.put(key.toString(), value);

            // Handle comma or end
            if (!tokens.isPeekToken(TokenType.RBRACE) && !tokens.expect(TokenType.COMMA)) {
                context.addTokenError(TokenType.RBRACE, tokens.getCurrentToken());
                return null;
            }
        }

        if (!tokens.expect(TokenType.RBRACE)) {
            context.addTokenError(TokenType.RBRACE, tokens.getCurrentToken());
            return null;
        }

        return new HashLiteral(token, pairs);
    }

    private Expression parseFunctionLiteral(ParsingContext context) {
        TokenStream tokens = context.getTokenStream();
        Token token = tokens.getCurrentToken();

        if (!tokens.expect(TokenType.LPAREN)) {
            context.addTokenError(TokenType.LPAREN, tokens.getCurrentToken());
            return null;
        }

        List<Identifier> parameters = parseFunctionParameters(context);
        if (parameters == null) {
            return null;
        }

        if (!tokens.expect(TokenType.LBRACE)) {
            context.addTokenError(TokenType.LBRACE, tokens.getCurrentToken());
            return null;
        }

        BlockStatementParser blockParser = new BlockStatementParser(statementParser);
        BlockStatement body = blockParser.parse(context);

        if (body == null) {
            return null;
        }

        return new FunctionLiteral(token, parameters, body);
    }

    private Expression parseIfExpression(ParsingContext context) {
        TokenStream tokens = context.getTokenStream();
        Token token = tokens.getCurrentToken();

        List<Expression> conditions = new ArrayList<>();
        List<BlockStatement> consequences = new ArrayList<>();

        // Parse initial if condition
        if (!tokens.expect(TokenType.LPAREN)) {
            context.addTokenError(TokenType.LPAREN, tokens.getCurrentToken());
            return null;
        }

        tokens.advance();
        Expression condition = parseExpression(context, PrecedenceTable.Precedence.LOWEST);
        if (condition == null)
            return null;

        if (!tokens.expect(TokenType.RPAREN)) {
            context.addTokenError(TokenType.RPAREN, tokens.getCurrentToken());
            return null;
        }

        if (!tokens.expect(TokenType.LBRACE)) {
            context.addTokenError(TokenType.LBRACE, tokens.getCurrentToken());
            return null;
        }

        BlockStatementParser blockParser = new BlockStatementParser(statementParser);
        BlockStatement consequence = blockParser.parse(context);
        if (consequence == null)
            return null;

        conditions.add(condition);
        consequences.add(consequence);

        // Parse elif conditions
        while (tokens.isPeekToken(TokenType.ELIF)) {
            tokens.advance();

            if (!tokens.expect(TokenType.LPAREN)) {
                context.addTokenError(TokenType.LPAREN, tokens.getCurrentToken());
                return null;
            }

            tokens.advance();
            Expression elifCondition = parseExpression(context, PrecedenceTable.Precedence.LOWEST);
            if (elifCondition == null)
                return null;

            if (!tokens.expect(TokenType.RPAREN)) {
                context.addTokenError(TokenType.RPAREN, tokens.getCurrentToken());
                return null;
            }

            if (!tokens.expect(TokenType.LBRACE)) {
                context.addTokenError(TokenType.LBRACE, tokens.getCurrentToken());
                return null;
            }

            BlockStatement elifConsequence = blockParser.parse(context);
            if (elifConsequence == null)
                return null;

            conditions.add(elifCondition);
            consequences.add(elifConsequence);
        }

        // Parse else block
        BlockStatement alternative = null;
        if (tokens.isPeekToken(TokenType.ELSE)) {
            tokens.advance();

            if (!tokens.expect(TokenType.LBRACE)) {
                context.addTokenError(TokenType.LBRACE, tokens.getCurrentToken());
                return null;
            }

            alternative = blockParser.parse(context);
        }

        return new IfExpression(token, conditions, consequences, alternative);
    }

    // Infix Parser Implementations

    private Expression parseInfixExpression(ParsingContext context, Expression left) {
        TokenStream tokens = context.getTokenStream();
        Token token = tokens.getCurrentToken();
        String operator = token.literal();

        PrecedenceTable.Precedence precedence = context.getPrecedenceTable().getPrecedence(token.type());

        tokens.advance();

        Expression right = parseExpression(context, precedence);
        if (right == null) {
            return null;
        }

        return new InfixExpression(token, left, operator, right);
    }

    private Expression parseAssignmentExpression(ParsingContext context, Expression left) {
        if (!(left instanceof Identifier)) {
            context.addError("Invalid assignment target", context.getTokenStream().getCurrentToken());
            return null;
        }

        Token token = context.getTokenStream().getCurrentToken();
        context.getTokenStream().advance();

        Expression value = parseExpression(context, PrecedenceTable.Precedence.LOWEST);
        if (value == null) {
            return null;
        }

        return new AssignmentExpression(token, (Identifier) left, value);
    }

    private Expression parseCallExpression(ParsingContext context, Expression function) {
        Token token = context.getTokenStream().getCurrentToken();
        List<Expression> arguments = parseExpressionList(context, TokenType.RPAREN);
        return new CallExpression(token, function, arguments);
    }

    private Expression parseIndexExpression(ParsingContext context, Expression left) {
        TokenStream tokens = context.getTokenStream();
        Token token = tokens.getCurrentToken();

        tokens.advance();

        Expression index = parseExpression(context, PrecedenceTable.Precedence.LOWEST);
        if (index == null) {
            return null;
        }

        if (!tokens.expect(TokenType.RBRACKET)) {
            context.addTokenError(TokenType.RBRACKET, tokens.getCurrentToken());
            return null;
        }

        return new IndexExpression(token, left, index);
    }

    // Helper Methods

    private List<Expression> parseExpressionList(ParsingContext context, TokenType endToken) {
        TokenStream tokens = context.getTokenStream();
        List<Expression> expressions = new ArrayList<>();

        if (tokens.isPeekToken(endToken)) {
            tokens.advance();
            return expressions;
        }

        tokens.advance();
        Expression expr = parseExpression(context, PrecedenceTable.Precedence.LOWEST);
        if (expr != null) {
            expressions.add(expr);
        }

        while (tokens.isPeekToken(TokenType.COMMA)) {
            tokens.advance(); // consume comma
            tokens.advance(); // move to next expression

            expr = parseExpression(context, PrecedenceTable.Precedence.LOWEST);
            if (expr != null) {
                expressions.add(expr);
            }
        }

        if (!tokens.expect(endToken)) {
            context.addTokenError(endToken, tokens.getCurrentToken());
            return new ArrayList<>();
        }

        return expressions;
    }

    private List<Identifier> parseFunctionParameters(ParsingContext context) {
        TokenStream tokens = context.getTokenStream();
        List<Identifier> identifiers = new ArrayList<>();

        if (tokens.isPeekToken(TokenType.RPAREN)) {
            tokens.advance();
            return identifiers;
        }

        tokens.advance();

        Token nameToken = tokens.getCurrentToken();
        if (nameToken.type() != TokenType.IDENTIFIER) {
            context.addTokenError(TokenType.IDENTIFIER, nameToken);
            return null;
        }

        identifiers.add(new Identifier(nameToken, nameToken.literal()));

        while (tokens.isPeekToken(TokenType.COMMA)) {
            tokens.advance(); // consume comma
            tokens.advance(); // move to next identifier

            nameToken = tokens.getCurrentToken();
            if (nameToken.type() != TokenType.IDENTIFIER) {
                context.addTokenError(TokenType.IDENTIFIER, nameToken);
                return null;
            }

            identifiers.add(new Identifier(nameToken, nameToken.literal()));
        }

        if (!tokens.expect(TokenType.RPAREN)) {
            context.addTokenError(TokenType.RPAREN, tokens.getCurrentToken());
            return null;
        }

        return identifiers;
    }
}