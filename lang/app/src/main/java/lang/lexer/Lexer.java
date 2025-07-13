package lang.lexer;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Stack;

import lang.token.Token;
import lang.token.TokenPosition;
import lang.token.TokenType;
import lang.token.Keywords;
import lang.lexer.debug.DebugEvent;
import lang.lexer.debug.LexerDebugger;

/**
 * 🔍 Lexer - The Text Scanner 🔍
 * 
 * This class breaks down source code text into meaningful tokens (words,
 * symbols, numbers).
 * Think of it as a smart text reader that understands programming languages!
 * 📖✨
 */
public final class Lexer {

    private Stack<LineColumn> positionHistory;
    private final String input;
    private int currentPosition;
    private int nextPosition;
    private char currentCharacter = ' ';
    private LineColumn lineColumn;

    private static final char EOF = '\0';

    private final LexerDebugger debugger;

    /**
     * 📍 LineColumn - Position Tracker 📍
     * 
     * Keeps track of where we are in the text file (line and column numbers).
     * Like GPS coordinates for your code! 🗺️
     */
    private static record LineColumn(int line, int column) {
    }

    public Lexer(String input) {
        this(input, null);
    }

    /**
     * 🚀 Creates a new Lexer with optional debug support
     * 
     * @param input    The source code to tokenize
     * @param debugger Optional debugger (null to disable debugging)
     */
    public Lexer(String input, LexerDebugger debugger) {
        this.input = input;
        this.debugger = debugger;
        this.lineColumn = new LineColumn(1, 0);
        this.positionHistory = new Stack<>();

        if (isDebugging()) {
            this.debugger.onSessionStart(input);
        }

        this.advanceToNextCharacter();
    }

    private boolean isDebugging() {
        return this.debugger != null;
    }

    /**
     * 👀 Gets the character we're currently looking at
     * 
     * @return The current character under our "reading cursor" 🔤
     */
    public char getCurrentChar() {
        return this.currentCharacter;
    }

    /**
     * 🔄 Resets the lexer to start reading from the beginning
     * 
     */
    public void reset() {
        this.currentPosition = 0;
        this.nextPosition = 0;
        this.currentCharacter = EOF;
        this.lineColumn = new LineColumn(1, 0);
        this.advanceToNextCharacter();
        this.positionHistory.clear();

        if (isDebugging()) {
            this.debugger.onSessionReset();
        }

        this.advanceToNextCharacter();
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

        int preTokenPosition = this.currentPosition;
        char triggerChar = this.currentCharacter;

        Token token;
        this.skipWhitespace();
        this.skipComments();

        switch (this.currentCharacter) {
            case '=':
                token = parseTwoCharacterOperator(TokenType.EQ, TokenType.ASSIGN);
                break;

            case '!':
                token = this.parseTwoCharacterOperator(TokenType.NOT_EQ, TokenType.BANG);
                break;

            case ';':
                token = this.createToken(TokenType.SEMICOLON, this.currentCharacter);
                break;

            case '(':
                token = this.createToken(TokenType.LPAREN, this.currentCharacter);
                break;

            case ')':
                token = this.createToken(TokenType.RPAREN, this.currentCharacter);
                break;

            case ',':
                token = this.createToken(TokenType.COMMA, this.currentCharacter);
                break;

            case '+':
                token = this.parseTwoCharacterOperator(TokenType.PLUS_ASSIGN, TokenType.PLUS);
                break;

            case '-':
                token = this.parseTwoCharacterOperator(
                        TokenType.MINUS_ASSIGN,
                        TokenType.MINUS);
                break;

            case '*':
                token = this.parseTwoCharacterOperator(
                        TokenType.ASTERISK_ASSIGN,
                        TokenType.ASTERISK);
                break;

            case '/':
                token = this.parseTwoCharacterOperator(
                        TokenType.SLASH_ASSIGN,
                        TokenType.SLASH);
                break;

            case '%':
                token = this.createToken(TokenType.MODULUS, this.currentCharacter);
                break;

            case '&':
                token = this.parseTwoCharacterOperator(
                        TokenType.AND,
                        TokenType.BITWISE_AND,
                        '&');
                break;

            case '|':
                token = this.parseTwoCharacterOperator(
                        TokenType.OR,
                        TokenType.BITWISE_OR,
                        '|');
                break;

            case '^':
                token = this.createToken(TokenType.BITWISE_XOR, this.currentCharacter);
                break;

            case '~':
                token = this.createToken(TokenType.BITWISE_NOT, this.currentCharacter);
                break;

            case '<':
                token = this.parseTwoCharacterOperator(
                        TokenType.LESS_THAN_OR_EQUAL,
                        TokenType.LESS_THAN);
                break;

            case '>':
                token = this.parseTwoCharacterOperator(
                        TokenType.GREATER_THAN_OR_EQUAL,
                        TokenType.GREATER_THAN);
                break;

            case '{':
                token = this.createToken(TokenType.LBRACE, this.currentCharacter);
                break;

            case '}':
                token = this.createToken(TokenType.RBRACE, this.currentCharacter);
                break;

            case '[':
                token = this.createToken(TokenType.LBRACKET, this.currentCharacter);
                break;

            case ']':
                token = this.createToken(TokenType.RBRACKET, this.currentCharacter);
                break;

            case ':':
                token = this.createToken(TokenType.COLON, this.currentCharacter);
                break;

            case '.':
                token = this.createToken(TokenType.DOT, currentCharacter);
                break;

            case '"':
                token = this.createToken(TokenType.STRING, readString());
                break;

            case EOF:
                token = this.createToken(TokenType.EOF, "");
                break;

            default:
                token = this.parseIdentifierOrNumber();
                break;
        }

        if (isDebugging()) {
            DebugEvent event = new DebugEvent(
                    token,
                    preTokenPosition,
                    triggerChar,
                    currentPosition,
                    System.nanoTime());
            this.debugger.onTokenCreated(event);
        }

        advanceToNextCharacter();
        return token;
    }

    /**
     * 🏁 Ends the tokenization session
     * Call this when finished tokenizing to get debug summaries
     */
    public void endSession() {
        if (isDebugging()) {
            this.debugger.onSessionEnd();
        }
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
    private boolean isAtEnd() {
        return this.nextPosition >= this.input.length();
    }

    /**
     * ➡️ Moves our reading position forward by one step
     * 
     * Updates position counters and line/column tracking.
     * Like moving your finger to the next character! 👆📖
     */
    private void advancePosition() {
        this.currentPosition = this.nextPosition;
        this.nextPosition++;

        int line = this.lineColumn.line();
        int column = this.lineColumn.column();

        this.positionHistory.push(new LineColumn(line, column));

        if (this.currentPosition >= this.input.length())
            return;

        if (this.currentCharacter == '\n' || (this.currentCharacter == '\r' && peekNextCharacter() != '\n'))
            this.lineColumn = new LineColumn(line + 1, 0);
        else
            this.lineColumn = new LineColumn(line, column + 1);
    }

    /**
     * 📖 Reads the character at our current position
     * 
     * Gets the next character from the input and advances our position.
     * Like reading the next letter in a book! 🔤➡️
     */
    private void advanceToNextCharacter() {
        this.currentCharacter = this.isAtEnd() ? EOF : this.input.charAt(this.nextPosition);
        this.advancePosition();
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
        int line = this.lineColumn.line();
        int column = this.lineColumn.column();

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
    private char peekNextCharacter() {
        return this.isAtEnd() ? '\0' : this.input.charAt(this.nextPosition);
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
            this.advanceToNextCharacter();

            if (this.currentCharacter == '\0')
                throw new RuntimeException("Unterminated string");

            if (this.currentCharacter == '"')
                break;

            if (this.currentCharacter == '\\') {
                this.advanceToNextCharacter();
                sb.append(this.handleEscapeSequence());
                continue;
            }

            sb.append(this.currentCharacter);
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
        switch (this.currentCharacter) {
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
                return String.valueOf(this.currentCharacter);
        }
    }

    /**
     * ⚪ Skips over whitespace characters
     * 
     * Jumps past spaces, tabs, newlines - all the "empty" characters
     * that don't matter for code meaning. Like ignoring blank spaces! 🦘⚪
     */
    private void skipWhitespace() {
        Set<Character> whitespaceCharacters = Set.of(' ', '\t', '\n', '\r', '\f');
        while (whitespaceCharacters.contains(this.currentCharacter)) {
            this.advanceToNextCharacter();
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
    private Token parseTwoCharacterOperator(TokenType tokenTypeIfDouble, TokenType defaultTokenType, char peekChar) {

        char currChar = this.currentCharacter;
        if (this.peekNextCharacter() == peekChar) {
            this.advanceToNextCharacter();
            return this.createToken(tokenTypeIfDouble, String.format("%c%c", currChar, peekChar));
        }

        return this.createToken(defaultTokenType, this.currentCharacter);
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
    private Token parseTwoCharacterOperator(TokenType tokenTypeIfDouble, TokenType defaultTokenType) {
        return this.parseTwoCharacterOperator(tokenTypeIfDouble, defaultTokenType, '=');
    }

    private void skipComments() {
        while (true) {
            this.skipWhitespace();

            if (this.currentCharacter == '/' && this.peekNextCharacter() == '/') {
                this.skipSingleLineComment();
            } else if (this.currentCharacter == '/' && this.peekNextCharacter() == '*') {
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
        while (this.currentCharacter != '\n' && this.currentCharacter != EOF) {
            this.advanceToNextCharacter();
        }
    }

    private void skipMultiLineComment() {
        this.advanceToNextCharacter(); // skip the '/'
        this.advanceToNextCharacter(); // skip the '*'

        int depth = 1;

        while (depth > 0 && this.currentCharacter != EOF) {
            if (this.currentCharacter == '/' && this.peekNextCharacter() == '*') {
                depth++;
                this.advanceToNextCharacter();
                this.advanceToNextCharacter();
            } else if (this.currentCharacter == '*' && this.peekNextCharacter() == '/') {
                depth--;
                this.advanceToNextCharacter();
                this.advanceToNextCharacter();
            } else {
                this.advanceToNextCharacter();
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
        int position = this.currentPosition;

        while (Lexer.isLetter(this.currentCharacter) || Lexer.isDigit(this.currentCharacter)) {
            this.advanceToNextCharacter();
        }

        String identifier = this.input.substring(position, this.currentPosition);
        this.backtrack(1);
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
    private void backtrack(int steps) {
        this.currentPosition -= steps;
        this.nextPosition -= steps;
        this.currentCharacter = this.input.charAt(this.currentPosition);

        for (int i = 0; i < steps; i++) {
            LineColumn position = this.positionHistory.pop();
            this.lineColumn = position;
        }
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
        int startPosition = this.currentPosition;

        while (Lexer.isDigit(this.currentCharacter)) {
            this.advanceToNextCharacter();
        }

        String number = this.input.substring(startPosition, this.currentPosition);
        this.backtrack(1);
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
    private Token parseIdentifierOrNumber() {
        if (Lexer.isLetter(this.currentCharacter)) {
            String identifier = this.readIdentifier();
            TokenType type = Keywords.lookupIdentifier(identifier);
            return this.createToken(type, identifier);
        } else if (Lexer.isDigit(this.currentCharacter)) {
            String number = this.readNumber();
            return this.createToken(TokenType.INT, number);
        }

        return this.createToken(TokenType.ILLEGAL, this.currentCharacter);
    }

    public List<Token> getTokenStream() {
        List<Token> tokens = new ArrayList<>();
        while (!this.isAtEnd()) {
            tokens.add(this.nextToken());
        }
        reset();
        endSession();
        return tokens;
    }

}

class EOFException extends RuntimeException {
    public EOFException() {
        super("EOF");
    }
}