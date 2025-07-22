package lang.lexer.core;

import java.util.Stack;

/**
 * 📖 CharacterStream - Input Reading and Position Management 📖
 * 
 * From first principles, any lexer needs to:
 * 1. Read characters sequentially from input
 * 2. Track current position (for error reporting)
 * 3. Support lookahead (peek operations)
 * 4. Handle backtracking when needed
 * 
 * This class encapsulates ALL character-level operations, separating
 * low-level input handling from high-level tokenization logic.
 */
public class CharacterStream {

    private static final char EOF = '\0';

    private final String input;
    private final String[] lines;

    private int currentPosition;
    private int nextPosition;
    private char currentCharacter;
    private LineColumn lineColumn;
    private final Stack<LineColumn> positionHistory;

    /**
     * 📍 Position tracking record
     */
    public record LineColumn(int line, int column) {
    }

    public CharacterStream(String input) {
        this.input = input;
        this.lines = input.split("\\r\\n|\\r|\\n");
        this.lineColumn = new LineColumn(1, 0);
        this.positionHistory = new Stack<>();
        this.currentPosition = 0;
        this.nextPosition = 0;
        this.currentCharacter = EOF;
        advance();
    }

    /**
     * 📖 Gets the current character under the "reading cursor"
     */
    public char currentCharacter() {
        return currentCharacter;
    }

    /**
     * 👀 Peeks at the next character without advancing
     */
    public char peekCharacter() {
        return isAtEnd() ? EOF : input.charAt(nextPosition);
    }

    /**
     * 👀 Peeks ahead by multiple positions
     */
    public char peekCharacter(int offset) {
        int targetPos = nextPosition + offset - 1;
        return (targetPos >= input.length()) ? EOF : input.charAt(targetPos);
    }

    /**
     * ➡️ Advances to the next character
     */
    public void advance() {
        currentPosition = nextPosition;
        nextPosition++;

        // Save current position for backtracking
        positionHistory.push(lineColumn);

        // Update line/column tracking
        if (currentPosition < input.length()) {
            currentCharacter = input.charAt(currentPosition);
            updateLineColumn();
        } else {
            currentCharacter = EOF;
        }
    }

    /**
     * 🚫 Checks if we've reached end of input
     */
    public boolean isAtEnd() {
        return nextPosition >= input.length();
    }

    /**
     * 📍 Gets current line and column
     */
    public LineColumn getPosition() {
        return lineColumn;
    }

    /**
     * 🔄 Resets stream to beginning
     */
    public void reset() {
        currentPosition = 0;
        nextPosition = 0;
        lineColumn = new LineColumn(1, 0);
        positionHistory.clear();
        advance();
    }

    public String[] getLines() {
        return lines;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    private void updateLineColumn() {
        int line = lineColumn.line();
        int column = lineColumn.column();

        if (currentCharacter == '\n' || (currentCharacter == '\r' && peekCharacter() != '\n')) {
            lineColumn = new LineColumn(line + 1, 0);
        } else {
            lineColumn = new LineColumn(line, column + 1);
        }
    }

    /**
     * ⬅️ Backtracks by specified number of steps
     */
    public void backtrack(int steps) {
        for (int i = 0; i < steps; i++) {
            if (positionHistory.isEmpty())
                break;

            currentPosition--;
            nextPosition--;
            lineColumn = positionHistory.pop();
        }

        currentCharacter = (currentPosition >= 0 && currentPosition < input.length())
                ? input.charAt(currentPosition)
                : EOF;
    }

    /**
     * 📏 Gets substring from start position to current position
     */
    public String getSubstring(int startPos) {
        return input.substring(startPos, currentPosition);
    }
}
