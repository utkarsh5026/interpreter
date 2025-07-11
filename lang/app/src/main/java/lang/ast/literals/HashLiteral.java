package lang.ast.literals;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.stream.Collectors;

import lang.ast.base.Expression;
import lang.token.Token;
import lang.ast.visitor.AstVisitor;

/**
 * Represents a hash literal: {"key": "value", "num": 42}
 * Uses LinkedHashMap to preserve insertion order.
 */
public class HashLiteral extends Expression {
    private final Map<String, Expression> pairs;

    public HashLiteral(Token token, Map<String, Expression> pairs) {
        super(token);
        this.pairs = new LinkedHashMap<>(pairs);
    }

    public Map<String, Expression> getPairs() {
        return Collections.unmodifiableMap(pairs);
    }

    @Override
    public String toString() {
        String pairsStr = pairs.entrySet().stream()
                .map(entry -> String.format("%s: %s",
                        entry.getKey(),
                        entry.getValue().toString()))
                .collect(Collectors.joining(", "));

        return String.format("{%s}", pairsStr);
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitHashLiteral(this);
    }

}
