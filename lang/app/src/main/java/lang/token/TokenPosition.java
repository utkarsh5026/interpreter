package lang.token;

public record TokenPosition(int line, int column) {
    public TokenPosition {
        if (line < 1) {
            throw new IllegalArgumentException("Line number must be >= 1, got: " + line);
        }
        if (column < 0) {
            throw new IllegalArgumentException("Column number must be >= 0, got: " + column);
        }
    }

    @Override
    public String toString() {
        return String.format("line %d, column %d", line, column);
    }
}
