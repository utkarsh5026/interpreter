package lang.lexer;

import lang.token.Token;
import lang.token.TokenPosition;
import lang.token.TokenType;
import lang.token.Keywords;

/**
 * 🔍 Lexer - The Text Scanner 🔍
 * 
 * This class breaks down source code text into meaningful tokens (words,
 * symbols, numbers).
 * Think of it as a smart text reader that understands programming languages!
 * 📖✨
 */
public final class Lexer {

    private final String input;
    private int position = 0;
    private int readPosition = 0;
    private char currentChar = ' ';
    private LineColumn lineColumn;

    private static final char EOF = '\0';

    /**
     * 📍 LineColumn - Position Tracker 📍
     * 
     * Keeps track of where we are in the text file (line and column numbers).
     * Like GPS coordinates for your code! 🗺️
     */
    private class LineColumn {
        private int line;
        private int column;

        /**
         * 🏗️ Creates a new position tracker
         * 
         * @param line   The line number (starts at 1) 📏
         * @param column The column number (starts at 0) ➡️
         */
        public LineColumn(int line, int column) {
            this.line = line;
            this.column = column;
        }

        /**
         * 📏 Gets the current line number
         * 
         * @return The line number we're currently reading 🔢
         */
        public int getLine() {
            return this.line;
        }

        /**
         * ➡️ Gets the current column number
         * 
         * @return The column position in the current line 🔢
         */
        public int getColumn() {
            return this.column;
        }

        /**
         * 🎯 Updates our position in the text
         * 
         * @param line   New line number 📏
         * @param column New column number ➡️
         */
        public void set(int line, int column) {
            this.line = line;
            this.column = column;
        }
    }

    /**
     * 🚀 Creates a new Lexer to scan the given text
     * 
     * Sets up the lexer with source code and prepares to start reading.
     * Like opening a book and pointing to the first character! 📚👆
     * 
     * @param input The source code text to analyze 📝
     */
    public Lexer(String input) {
        this.input = input;
        this.lineColumn = new LineColumn(1, 0);
        this.readCurrChar();
    }

    /**
     * 👀 Gets the character we're currently looking at
     * 
     * @return The current character under our "reading cursor" 🔤
     */
    public char getCurrentChar() {
        return this.currentChar;
    }

    /**
     * 🔄 Resets the lexer to start reading from the beginning
     * 
     * Like rewinding a tape player back to the start! ⏪
     * Useful when you want to scan the same text again.
     */
    public void reset() {
        this.position = 0;
        this.readPosition = 0;
        this.currentChar = ' ';
        this.lineColumn = new LineColumn(1, 0);
        this.readCurrChar();
    }

    /**
     * 🎯 Reads and returns the next meaningful token from the text
     * 
     * This is the main method! It identifies what type of "word" or symbol
     * comes next in the code (like numbers, keywords, operators, etc.) 🔍✨
     * 
     * @return A Token object containing the type and value of what was found 🎫
     */
    public Token nextToken() {
        Token token;
        this.skipWhitespace();
        this.skipComments();

        switch (this.currentChar) {
            case '=':
                token = this.handleDoubleLiteral(TokenType.EQ, TokenType.ASSIGN);
                break;

            case '!':
                token = this.handleDoubleLiteral(TokenType.NOT_EQ, TokenType.BANG);
                break;

            case ';':
                token = this.createToken(TokenType.SEMICOLON, this.currentChar);
                break;

            case '(':
                token = this.createToken(TokenType.LPAREN, this.currentChar);
                break;

            case ')':
                token = this.createToken(TokenType.RPAREN, this.currentChar);
                break;

            case ',':
                token = this.createToken(TokenType.COMMA, this.currentChar);
                break;

            case '+':
                token = this.handleDoubleLiteral(TokenType.PLUS_ASSIGN, TokenType.PLUS);
                break;

            case '-':
                token = this.handleDoubleLiteral(
                        TokenType.MINUS_ASSIGN,
                        TokenType.MINUS);
                break;

            case '*':
                token = this.handleDoubleLiteral(
                        TokenType.ASTERISK_ASSIGN,
                        TokenType.ASTERISK);
                break;

            case '%':
                token = this.createToken(TokenType.MODULUS, this.currentChar);
                break;

            case '&':
                token = this.handleDoubleLiteral(
                        TokenType.AND,
                        TokenType.BITWISE_AND,
                        '&');
                break;

            case '|':
                token = this.handleDoubleLiteral(
                        TokenType.OR,
                        TokenType.BITWISE_OR,
                        '|');
                break;

            case '^':
                token = this.createToken(TokenType.BITWISE_XOR, this.currentChar);
                break;

            case '~':
                token = this.createToken(TokenType.BITWISE_NOT, this.currentChar);
                break;

            case '/':
                if (this.peekChar() == '/') {
                    this.skipSingleLineComment();
                    return this.nextToken();
                } else if (this.peekChar() == '*') {
                    this.skipMultiLineComment();
                    return this.nextToken();
                }

                token = this.handleDoubleLiteral(
                        TokenType.SLASH_ASSIGN,
                        TokenType.SLASH);
                break;

            case '<':
                token = this.handleDoubleLiteral(
                        TokenType.LESS_THAN_OR_EQUAL,
                        TokenType.LESS_THAN);
                break;

            case '>':
                token = this.handleDoubleLiteral(
                        TokenType.GREATER_THAN_OR_EQUAL,
                        TokenType.GREATER_THAN);
                break;

            case '{':
                token = this.createToken(TokenType.LBRACE, this.currentChar);
                break;

            case '}':
                token = this.createToken(TokenType.RBRACE, this.currentChar);
                break;

            case '[':
                token = this.createToken(TokenType.LBRACKET, this.currentChar);
                break;

            case ']':
                token = this.createToken(TokenType.RBRACKET, this.currentChar);
                break;

            case ':':
                token = this.createToken(TokenType.COLON, this.currentChar);
                break;

            case '.':
                token = this.createToken(TokenType.DOT, this.currentChar);
                break;

            case '"':
                token = this.createToken(TokenType.STRING, this.readString());
                break;

            case EOF:
                token = this.createToken(TokenType.EOF, "");
                break;

            default:
                token = this.handleIdentifier();
                break;
        }

        this.readCurrChar();
        return token;
    }

    /**
     * 🔤 Checks if a character is a letter or underscore
     * 
     * Determines if the character can start or be part of a variable name.
     * Letters (A-Z, a-z) and underscore (_) are allowed! ✅
     * 
     * @param ch The character to examine 🔍
     * @return True if it's a letter or underscore, false otherwise ✅❌
     */
    private final static boolean isLetter(char ch) {
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_';
    }

    /**
     * 🔢 Checks if a character is a digit (0-9)
     * 
     * Perfect for identifying numbers in the source code! 🎯
     * 
     * @param ch The character to check 🔍
     * @return True if it's a digit, false otherwise ✅❌
     */
    private final static boolean isDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }

    /**
     * 🚫 Checks if we've reached the end of the input text
     * 
     * Like checking if you've finished reading a book! 📖✋
     * 
     * @return True if we're past the end, false if there's more to read ✅❌
     */
    private boolean isOut() {
        return this.position > this.input.length();
    }

    /**
     * ➡️ Moves our reading position forward by one step
     * 
     * Updates position counters and line/column tracking.
     * Like moving your finger to the next character! 👆📖
     */
    private void advance() {
        this.position = this.readPosition;
        this.readPosition++;

        int line = this.lineColumn.getLine();
        int column = this.lineColumn.getColumn();

        if (this.currentChar == '\n')
            this.lineColumn.set(line + 1, 0);
        else
            this.lineColumn.set(line, column + 1);
    }

    /**
     * 📖 Reads the character at our current position
     * 
     * Gets the next character from the input and advances our position.
     * Like reading the next letter in a book! 🔤➡️
     */
    private void readCurrChar() {
        this.currentChar = this.isOut() ? '\0' : this.input.charAt(this.position);
        this.advance();
    }

    /**
     * 🎫 Creates a new Token with position information
     * 
     * Packages up a piece of code (like a keyword or symbol) with its location.
     * Think of it as putting a label on something you found! 🏷️
     * 
     * @param type    What kind of token this is (keyword, operator, etc.) 🏷️
     * @param literal The actual text value 📝
     * @return A complete Token object with position info 🎫
     */
    private Token createToken(TokenType type, String literal) {
        int line = this.lineColumn.getLine();
        int column = this.lineColumn.getColumn();

        return new Token(type, literal, new TokenPosition(line, column));
    }

    /**
     * 🎫 Creates a Token from a single character
     * 
     * Convenience method for single-character tokens like '+', '-', etc.
     * Just converts the character to a string first! 🔤➡️📝
     * 
     * @param type    The token type 🏷️
     * @param literal Single character value 🔤
     * @return A Token object 🎫
     */
    private Token createToken(TokenType type, char literal) {
        return this.createToken(type, String.valueOf(literal));
    }

    /**
     * 👀 Looks at the next character without moving forward
     * 
     * Like peeking at the next page of a book without turning it.
     * Useful for checking two-character operators like "==" or "!=" 🔍👀
     * 
     * @return The next character, or null character if at end 🔤
     */
    private char peekChar() {
        return this.isOut() ? '\0' : this.input.charAt(this.readPosition);
    }

    /**
     * 📜 Reads a complete string literal from the input
     * 
     * Handles quoted strings with escape sequences like "\n", "\t".
     * Reads everything between the quote marks! "like this" 📝✨
     * 
     * @return The string content (without the surrounding quotes) 📜
     * @throws RuntimeException if string is not properly closed 🚫
     */
    private String readString() {
        StringBuilder sb = new StringBuilder();

        while (true) {
            this.readCurrChar();

            if (this.currentChar == '\0')
                throw new RuntimeException("Unterminated string");

            if (this.currentChar == '"')
                break;

            if (this.currentChar == '\\') {
                this.readCurrChar();
                sb.append(this.handleEscapeSequence());
                continue;
            }

            sb.append(this.currentChar);
        }

        return sb.toString();
    }

    /**
     * 🔀 Handles escape sequences in strings
     * 
     * Converts special sequences like "\n" into actual newlines.
     * Like translating secret codes into real characters! 🔓📝
     * 
     * @return The actual character represented by the escape sequence 🔤
     */
    private String handleEscapeSequence() {
        switch (this.currentChar) {
            case 'n':
                return "\n";
            case 't':
                return "\t";
            case 'r':
                return "\r";
            case 'f':
                return "\f";
            case 'b':
                return "\b";
            case '\'':
                return "\'";
            case '"':
                return "\"";
            default:
                return String.valueOf(this.currentChar);
        }
    }

    /**
     * ⚪ Skips over whitespace characters
     * 
     * Jumps past spaces, tabs, newlines - all the "empty" characters
     * that don't matter for code meaning. Like ignoring blank spaces! 🦘⚪
     */
    private void skipWhitespace() {
        while (this.currentChar == ' ' ||
                this.currentChar == '\t' ||
                this.currentChar == '\n' ||
                this.currentChar == '\r' ||
                this.currentChar == '\f') {
            this.readCurrChar();
        }
    }

    /**
     * 🔍 Handles two-character operators (like == or !=)
     * 
     * Checks if the current character plus the next one form a special operator.
     * If not, treats it as a single character. Smart pattern matching! 🎯🔍
     * 
     * @param tokenTypeIfDouble Token type if it's a two-character operator 🎫
     * @param defaultTokenType  Token type if it's just one character 🎫
     * @param peekChar          The character to look for next 👀
     * @return The appropriate token 🎫
     */
    private Token handleDoubleLiteral(TokenType tokenTypeIfDouble, TokenType defaultTokenType, char peekChar) {
        if (this.peekChar() == peekChar) {
            this.readCurrChar();
            return this.createToken(tokenTypeIfDouble, peekChar);
        }

        return this.createToken(defaultTokenType, this.currentChar);
    }

    /**
     * 🔍 Handles assignment operators (like +=, -=, *=)
     * 
     * Checks if an operator is followed by '=' to make it an assignment.
     * Like checking if '+' becomes '+='! ➕➡️➕🟰
     * 
     * @param tokenTypeIfDouble Token type for assignment version 🎫
     * @param defaultTokenType  Token type for basic operator 🎫
     * @return The appropriate token 🎫
     */
    private Token handleDoubleLiteral(TokenType tokenTypeIfDouble, TokenType defaultTokenType) {
        return this.handleDoubleLiteral(tokenTypeIfDouble, defaultTokenType, '=');
    }

    private void skipComments() {
        while (true) {
            this.skipWhitespace();

            if (this.currentChar == '/' && this.peekChar() == '/') {
                this.skipSingleLineComment();
            } else if (this.currentChar == '/' && this.peekChar() == '*') {
                this.skipMultiLineComment();
            } else {
                break;
            }
        }
    }

    /**
     * 💬 Skips a single-line comment (//)
     * 
     * Reads and ignores everything until the end of the line.
     * Like skipping a side note in a book! 📝➡️🗑️
     */
    private void skipSingleLineComment() {
        while (!this.isOut() && this.currentChar != '\n' && this.currentChar != EOF) {
            this.readCurrChar();
        }
    }

    private void skipMultiLineComment() {
        this.readCurrChar(); // skip the '/'
        this.readCurrChar(); // skip the '*'

        int depth = 1;

        while (depth > 0 && this.currentChar != EOF) {
            if (this.currentChar == '/' && this.peekChar() == '*') {
                depth++;
                this.readCurrChar();
                this.readCurrChar();
            } else if (this.currentChar == '*' && this.peekChar() == '/') {
                depth--;
                this.readCurrChar();
                this.readCurrChar();
            } else {
                this.readCurrChar();
            }
        }

    }

    /**
     * 🔤 Reads a complete identifier (variable/function name)
     * 
     * Collects letters, numbers, and underscores that form a name.
     * Like reading a word letter by letter! 🔤📝
     * 
     * @return The complete identifier string 📝
     */
    private String readIdentifier() {
        int position = this.position;

        while (Lexer.isLetter(this.currentChar) || Lexer.isDigit(this.currentChar)) {
            this.readCurrChar();
        }

        String identifier = this.input.substring(position, this.position);
        this.moveBack(1); // move back to the last character
        return identifier;
    }

    /**
     * ⬅️ Moves the reading position backward
     * 
     * Sometimes we need to "un-read" characters we went too far.
     * Like taking a step back! 👣⬅️
     * 
     * @param steps Number of positions to move backward 🔢
     */
    private void moveBack(int steps) {
        this.position -= steps;
        this.readPosition -= steps;
        this.currentChar = this.input.charAt(this.position);
    }

    /**
     * 🔢 Reads a complete number from the input
     * 
     * Collects consecutive digits to form a number.
     * Like counting digit by digit! 1️⃣2️⃣3️⃣
     * 
     * @return The complete number as a string 📝🔢
     */
    private String readNumber() {
        int position = this.position;

        while (Lexer.isDigit(this.currentChar)) {
            this.readCurrChar();
        }

        String number = this.input.substring(position, this.position);
        this.moveBack(1); // move back to the last character
        return number;
    }

    /**
     * 🎯 Identifies and handles identifiers and numbers
     * 
     * Determines if we're looking at a variable name, keyword, or number.
     * The detective work of figuring out what something is! 🕵️‍♂️🔍
     * 
     * @return A token representing what was identified 🎫
     */
    private Token handleIdentifier() {
        if (Lexer.isLetter(this.currentChar)) {
            String identifier = this.readIdentifier();
            TokenType type = Keywords.lookupIdentifier(identifier);
            return this.createToken(type, identifier);
        } else if (Lexer.isDigit(this.currentChar)) {
            String number = this.readNumber();
            return this.createToken(TokenType.INT, number);
        }

        return this.createToken(TokenType.ILLEGAL, this.currentChar);
    }
}
