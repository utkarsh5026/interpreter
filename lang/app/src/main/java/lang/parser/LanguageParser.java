package lang.parser;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

import lang.parser.core.*;
import lang.parser.error.ParseError;
import lang.parser.error.ParserException;
import lang.parser.interfaces.TypedStatementParser;
import lang.parser.interfaces.ExpressionParser;
import lang.parser.precedence.Precedence;
import lang.parser.registry.*;
import lang.token.Token;
import lang.lexer.Lexer;
import lang.ast.statements.Program;

import lang.ast.base.Statement;
import lang.ast.base.Expression;
import lang.token.TokenType;

/**
 * Main Parser class that coordinates the entire parsing process.
 * 
 * This is the main entry point for parsing. It:
 * 1. Takes a Lexer as input
 * 2. Creates a parsing context
 * 3. Uses the statement parser registry to parse the program
 * 4. Handles errors and recovery
 */
public class LanguageParser {
    private final ParsingContext context;
    private final StatementParserRegistry statementRegistry;
    private final ExpressionParser expressionParser;

    public LanguageParser(Lexer lexer) {
        this.context = new ParsingContext(lexer);
        this.statementRegistry = new StatementParserRegistry();
        this.expressionParser = statementRegistry.getExpressionParser();
    }

    /**
     * Parses the entire program.
     * 
     * @return A Program AST node containing all parsed statements
     */
    public Program parseProgram() {
        List<Statement> statements = new ArrayList<>();

        while (!context.getTokenStream().isAtEnd()) {
            try {
                Statement statement = parseStatement();
                if (statement != null) {
                    statements.add(statement);
                }
            } catch (ParserException e) {
                Optional<Token> token = e.getToken();
                if (token.isPresent()) {
                    context.addError(e.getMessage(), token.get());
                }
                synchronize();
            }
        }

        return new Program(statements);
    }

    /**
     * Parses a single statement.
     */
    private Statement parseStatement() {
        return statementRegistry.parseStatement(context);
    }

    /**
     * Error recovery: skip tokens until we find a safe point to resume parsing.
     */
    private void synchronize() {
        System.out.println("Synchronizing...");
        TokenStream tokens = context.getTokenStream();
        tokens.advance();

        while (!tokens.isAtEnd()) {
            // If we see a semicolon, we're probably at the end of a statement
            if (tokens.isCurrentToken(TokenType.SEMICOLON)) {
                tokens.advance();
                return;
            }

            // If we see the start of a new statement, we can resume
            switch (tokens.getPeekToken().type()) {
                case CLASS:
                case FUNCTION:
                case LET:
                case CONST:
                case FOR:
                case IF:
                case WHILE:
                case RETURN:
                    return;
                default:
                    tokens.advance();
            }
        }
    }

    /**
     * Returns all parsing errors encountered.
     */
    public List<ParseError> getErrors() {
        return context.getErrors().getErrors();
    }

    /**
     * Checks if any parsing errors occurred.
     */
    public boolean hasErrors() {
        return context.getErrors().hasErrors();
    }

    /**
     * Prints all parsing errors to stderr.
     */
    public void printErrors() {
        context.getErrors().printErrors();
    }

    /**
     * Gets the parsing context (useful for testing or advanced usage).
     */
    public ParsingContext getContext() {
        return context;
    }

    /**
     * Adds a custom statement parser.
     */
    public void addStatementParser(TypedStatementParser<? extends Statement> parser) {
        statementRegistry.addParser(parser);
    }

    /**
     * Removes a statement parser.
     */
    public void removeStatementParser(Class<? extends TypedStatementParser<? extends Statement>> parserType) {
        statementRegistry.removeParser(parserType);
    }

    /**
     * Parses a single expression (useful for REPL or testing).
     */
    public Expression parseExpression() {
        return expressionParser.parseExpression(context,
                Precedence.LOWEST);
    }

    /**
     * Parses expressions from a string (convenience method).
     */
    public static Expression parseExpressionFromString(String input) {
        Lexer lexer = new Lexer(input);
        LanguageParser parser = new LanguageParser(lexer);
        return parser.parseExpression();
    }

    /**
     * Parses a program from a string (convenience method).
     */
    public static Program parseProgramFromString(String input) {
        Lexer lexer = new Lexer(input);
        LanguageParser parser = new LanguageParser(lexer);
        return parser.parseProgram();
    }
}
