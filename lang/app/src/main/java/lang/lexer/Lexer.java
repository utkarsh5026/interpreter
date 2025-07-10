package lang.lexer;

import lang.token.Token;
import lang.token.TokenPosition;
import lang.token.TokenType;
import lang.token.Keywords;

public final class Lexer {

    private final String input;
    private int position = 0;
    private int readPosition = 0;
    private char currentChar = ' ';
    private LineColumn lineColumn;

    private static final char EOF = '\0';

    private class LineColumn {
        private int line;
        private int column;

        public LineColumn(int line, int column) {
            this.line = line;
            this.column = column;
        }

        public int getLine() {
            return this.line;
        }

        public int getColumn() {
            return this.column;
        }

        public void set(int line, int column) {
            this.line = line;
            this.column = column;
        }
    }

    public Lexer(String input) {
        this.input = input;
        this.lineColumn = new LineColumn(1, 0);
        this.readCurrChar();
    }

    /**
     * Gets the current character.
     * 
     * @returns The current character.
     */
    public char getCurrentChar() {
        return this.currentChar;
    }

    public void reset() {
        this.position = 0;
        this.readPosition = 0;
        this.currentChar = ' ';
        this.lineColumn = new LineColumn(1, 0);
        this.readCurrChar();
    }

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
     * Checks if a character is a letter
     * or an underscore.
     * 
     * @param ch The character to check.
     * @returns True if the character is a letter, false otherwise.
     */
    private final static boolean isLetter(char ch) {
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_';
    }

    /**
     * Checks if a character is a digit (0-9)
     * 
     * @param ch The character to check.
     * @returns True if the character is a digit, false otherwise.
     */
    private final static boolean isDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }

    /**
     * Checks if the current position is out of bounds.
     * 
     * @returns True if the current position is out of bounds, false otherwise.
     */
    private boolean isOut() {
        return this.position > this.input.length();
    }

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

    private void readCurrChar() {
        this.currentChar = this.isOut() ? '\0' : this.input.charAt(this.position);
        this.advance();
    }

    private Token createToken(TokenType type, String literal) {
        int line = this.lineColumn.getLine();
        int column = this.lineColumn.getColumn();

        return new Token(type, literal, new TokenPosition(line, column));
    }

    private Token createToken(TokenType type, char literal) {
        return this.createToken(type, String.valueOf(literal));
    }

    private char peekChar() {
        return this.isOut() ? '\0' : this.input.charAt(this.readPosition);
    }

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

    private void skipWhitespace() {
        while (this.currentChar == ' ' ||
                this.currentChar == '\t' ||
                this.currentChar == '\n' ||
                this.currentChar == '\r' ||
                this.currentChar == '\f') {
            this.readCurrChar();
        }
    }

    private Token handleDoubleLiteral(TokenType tokenTypeIfDouble, TokenType defaultTokenType, char peekChar) {
        if (this.peekChar() == peekChar) {
            this.readCurrChar();
            return this.createToken(tokenTypeIfDouble, peekChar);
        }

        return this.createToken(defaultTokenType, this.currentChar);
    }

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

    private String readIdentifier() {
        int position = this.position;

        while (Lexer.isLetter(this.currentChar) || Lexer.isDigit(this.currentChar)) {
            this.readCurrChar();
        }

        String identifier = this.input.substring(position, this.position);
        this.moveBack(1); // move back to the last character
        return identifier;
    }

    private void moveBack(int steps) {
        this.position -= steps;
        this.readPosition -= steps;
        this.currentChar = this.input.charAt(this.position);
    }

    private String readNumber() {
        int position = this.position;

        while (Lexer.isDigit(this.currentChar)) {
            this.readCurrChar();
        }

        String number = this.input.substring(position, this.position);
        this.moveBack(1); // move back to the last character
        return number;
    }

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
