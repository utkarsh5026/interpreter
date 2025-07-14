package lang.parser.expressions.prefix;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import lang.ast.base.Expression;
import lang.ast.expressions.IfExpression;
import lang.ast.statements.BlockStatement;
import lang.parser.core.ParsingContext;
import lang.parser.core.StatementParse;
import lang.parser.interfaces.ExpressionParser;
import lang.parser.interfaces.PrefixExpressionParser;
import lang.parser.parsers.BlockStatementParser;
import lang.parser.precedence.Precedence;
import lang.token.*;

/**
 * 🔀 IfExpressionParser - Conditional Logic Specialist 🔀
 * 
 * Handles if-expressions that provide conditional evaluation based on boolean
 * conditions.
 * If-expressions can have multiple elif branches and an optional else branch.
 * 
 * Examples:
 * - if (x > 0) { "positive" } else { "non-positive" }
 * - if (age >= 18) { "adult" } elif (age >= 13) { "teen" } else { "child" }
 * - if (condition) { doSomething(); result } (complex block)
 * 
 * Grammar:
 * ```
 * if-expression := 'if' '(' expression ')' block-statement
 * ('elif' '(' expression ')' block-statement)*
 * ('else' block-statement)?
 * ```
 * 
 * The parser builds lists of conditions and corresponding consequence blocks,
 * making it easy for the evaluator to process multiple branches.
 */
public class IfExpressionParser implements PrefixExpressionParser {

    private final ExpressionParser expressionParser;
    private final StatementParse statementParser;

    public IfExpressionParser(ExpressionParser expressionParser, StatementParse statementParser) {
        this.expressionParser = expressionParser;
        this.statementParser = statementParser;
    }

    @Override
    public Expression parsePrefix(ParsingContext context) {
        Token ifToken = context.consumeCurrentToken(TokenType.IF, "Expected 'if' keyword");

        List<Expression> conditions = new ArrayList<>();
        List<BlockStatement> consequences = new ArrayList<>();

        parseIfBranch(context, conditions, consequences);

        while (context.getTokenStream().isCurrentToken(TokenType.ELIF)) {
            context.consumeCurrentToken(TokenType.ELIF);
            parseIfBranch(context, conditions, consequences);
        }

        Optional<BlockStatement> alternative = Optional.empty();
        if (context.getTokenStream().isCurrentToken(TokenType.ELSE)) {
            context.consumeCurrentToken(TokenType.ELSE);
            BlockStatementParser blockParser = new BlockStatementParser(statementParser);
            BlockStatement elseBlock = blockParser.parse(context);
            alternative = Optional.of(elseBlock);
        }

        return new IfExpression(ifToken, conditions, consequences, alternative);
    }

    private void parseIfBranch(ParsingContext context,
            List<Expression> conditions,
            List<BlockStatement> consequences) {

        context.consumeCurrentToken(TokenType.LPAREN, "Expected '(' after 'if' keyword");
        Expression condition = expressionParser.parseExpression(context, Precedence.LOWEST);

        context.consumeCurrentToken(TokenType.RPAREN, "Expected ')' after condition");

        BlockStatementParser blockParser = new BlockStatementParser(statementParser);
        BlockStatement consequence = blockParser.parse(context);

        conditions.add(condition);
        consequences.add(consequence);
    }

    @Override
    public Set<TokenType> getHandledTokenTypes() {
        return Set.of(TokenType.IF);
    }
}
