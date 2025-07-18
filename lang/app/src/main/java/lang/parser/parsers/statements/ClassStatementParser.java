package lang.parser.parsers.statements;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lang.ast.statements.BlockStatement;
import lang.ast.base.Identifier;
import lang.ast.literals.FunctionLiteral;
import lang.ast.statements.ClassStatement;
import lang.ast.statements.ClassStatement.MethodDefinition;
import lang.parser.core.*;
import lang.parser.error.ParserException;
import lang.parser.interfaces.TypedStatementParser;
import lang.token.*;

/**
 * 🏛️ ClassStatementParser - Class Definition Parser 🏛️
 * 
 * Parses class definitions with inheritance, constructors, and methods.
 * 
 * From first principles, class parsing involves:
 * 1. Parse 'class' keyword
 * 2. Parse class name (identifier)
 * 3. Optionally parse 'extends' and parent class name
 * 4. Parse class body (constructor and methods)
 * 5. Create ClassStatement AST node
 * 
 * Grammar:
 * ```
 * class-statement := 'class' IDENTIFIER ('extends' IDENTIFIER)? '{' class-body
 * '}'
 * class-body := constructor? method*
 * constructor := 'constructor' '(' parameters ')' block-statement
 * method := IDENTIFIER '(' parameters ')' block-statement
 * ```
 */
public class ClassStatementParser implements TypedStatementParser<ClassStatement> {
    private final StatementParse statementParser;

    public ClassStatementParser(StatementParse statementParser) {
        this.statementParser = statementParser;
    }

    @Override
    public boolean canParse(ParsingContext context) {
        return context.getTokenStream().isCurrentToken(TokenType.CLASS);
    }

    @Override
    public ClassStatement parse(ParsingContext context) throws ParserException {
        Token classToken = context.consumeCurrentToken(TokenType.CLASS);
        Token nameToken = context.consumeCurrentToken(TokenType.IDENTIFIER,
                "Expected class name after the 'class' keyword");
        Identifier className = new Identifier(nameToken, nameToken.literal());

        Optional<Identifier> parentClass = parseParentClass(context);

        context.consumeCurrentToken(TokenType.LBRACE, "Expected '{' to start class body");
        ClassBodyParseResult bodyResult = parseClassBody(context);
        context.consumeCurrentToken(TokenType.RBRACE, "Expected '}' to end class body");

        return new ClassStatement(classToken, className, parentClass,
                bodyResult.constructor, bodyResult.methods);
    }

    /**
     * 🔗 Parses the parent class of a class
     */
    private Optional<Identifier> parseParentClass(ParsingContext context) throws ParserException {
        if (!context.isCurrentToken(TokenType.EXTENDS)) {
            return Optional.empty();
        }

        context.consumeCurrentToken(TokenType.EXTENDS);
        Token parentToken = context.consumeCurrentToken(TokenType.IDENTIFIER,
                "Expected parent class name after 'extends'");
        return Optional.of(new Identifier(parentToken, parentToken.literal()));
    }

    /**
     * 📦 Container for class body parsing results
     */
    private record ClassBodyParseResult(Optional<FunctionLiteral> constructor,
            List<MethodDefinition> methods) {
    }

    /**
     * 📖 Parses the body of a class (constructor and methods)
     */
    private ClassBodyParseResult parseClassBody(ParsingContext context) throws ParserException {
        Optional<FunctionLiteral> constructor = Optional.empty();
        List<MethodDefinition> methods = new ArrayList<>();

        while (!context.isCurrentToken(TokenType.RBRACE) &&
                !context.isAtEnd()) {

            if (context.isCurrentToken(TokenType.IDENTIFIER)) {
                Token nameToken = context.getCurrentToken();
                String name = nameToken.literal();

                if ("constructor".equals(name)) {
                    if (constructor.isPresent()) {
                        throw new ParserException("Class can only have one constructor", nameToken);
                    }
                    constructor = Optional.of(parseConstructor(context));
                    continue;
                }

                MethodDefinition method = parseMethod(context);
                methods.add(method);

            } else {
                throw new ParserException("Expected method or constructor in class body",
                        context.getCurrentToken());
            }
        }

        return new ClassBodyParseResult(constructor, methods);
    }

    /**
     * 🏗️ Parses a constructor definition
     */
    private FunctionLiteral parseConstructor(ParsingContext context) throws ParserException {
        Token constructorToken = context.consumeCurrentToken(TokenType.IDENTIFIER); // constructor identifier
        context.consumeCurrentToken(TokenType.LPAREN, "Expected '(' after 'constructor'");

        List<Identifier> parameters = parseParameters(context);
        context.consumeCurrentToken(TokenType.RPAREN, "Expected ')' after constructor parameters");

        BlockStatementParser blockParser = new BlockStatementParser(statementParser);
        BlockStatement body = blockParser.parse(context);

        return new FunctionLiteral(constructorToken, parameters, body);
    }

    /**
     * 🔧 Parses a method definition
     */
    private MethodDefinition parseMethod(ParsingContext context) throws ParserException {
        Token nameToken = context.consumeCurrentToken(
                TokenType.IDENTIFIER,
                "Expected method name");
        Identifier methodName = new Identifier(nameToken, nameToken.literal());

        context.consumeCurrentToken(TokenType.LPAREN, "Expected '(' after method name");

        List<Identifier> parameters = parseParameters(context);
        context.consumeCurrentToken(TokenType.RPAREN, "Expected ')' after method parameters");

        BlockStatementParser blockParser = new BlockStatementParser(statementParser);
        BlockStatement body = blockParser.parse(context);

        FunctionLiteral function = new FunctionLiteral(nameToken, parameters, body);
        return new MethodDefinition(methodName, function);
    }

    /**
     * 📋 Parses function parameters
     */
    private List<Identifier> parseParameters(ParsingContext context) {
        List<Identifier> parameters = new ArrayList<>();

        if (context.getTokenStream().isCurrentToken(TokenType.RPAREN)) {
            return parameters; // No parameters
        }

        while (!context.getTokenStream().isCurrentToken(TokenType.RPAREN)) {
            Token paramToken = context.consumeCurrentToken(TokenType.IDENTIFIER,
                    "Expected parameter name");
            parameters.add(new Identifier(paramToken, paramToken.literal()));

            if (!context.getTokenStream().isCurrentToken(TokenType.COMMA) &&
                    !context.getTokenStream().isCurrentToken(TokenType.RPAREN)) {
                throw new ParserException("Expected ',' or ')' after parameter",
                        context.getTokenStream().getCurrentToken());
            }

            if (context.getTokenStream().isCurrentToken(TokenType.COMMA)) {
                context.consumeCurrentToken(TokenType.COMMA);
            }
        }

        return parameters;
    }
}
