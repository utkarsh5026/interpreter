package lang.lexer.parsers;

import lang.lexer.core.CharacterStream;
import lang.lexer.core.TokenDescription;

/**
 * ⚪ WhitespaceHandler - Skips Non-Meaningful Characters ⚪
 * 
 * From first principles, lexers need to skip:
 * 1. Whitespace characters (spaces, tabs, newlines)
 * 2. Comments (single-line and multi-line)
 * 
 * This component handles all "ignorable" content, keeping the
 * main tokenization loop focused on meaningful tokens.
 */
public class WhitespaceHandler {

    private final CharacterStream stream;

    public WhitespaceHandler(CharacterStream stream) {
        this.stream = stream;
    }

    /**
     * ⚪ Skips all whitespace and comments
     */
    public void skipNonTokens() {
        while (true) {
            skipWhitespace();

            if (stream.currentCharacter() == TokenDescription.COMMENT_START) {
                skipSingleLineComment();
            } else if (stream.currentCharacter() == '/' && stream.peekCharacter() == '*') {
                skipMultiLineComment();
            } else {
                break; // No more whitespace or comments
            }
        }
    }

    /**
     * Skips Basic Whitespace Characters
     * 
     * This handles the simple stuff - spaces, tabs, newlines, etc.
     * It keeps moving forward character by character until it finds
     * something that isn't whitespace.
     */
    private void skipWhitespace() {
        while (TokenDescription.isWhitespace(stream.currentCharacter())) {
            stream.advance();
        }
    }

    private void skipSingleLineComment() {
        while (stream.currentCharacter() != TokenDescription.NEWLINE
                && stream.currentCharacter() != TokenDescription.EOF) {
            stream.advance();
        }
    }

    private void skipMultiLineComment() {
        stream.advance(); // skip '/'
        stream.advance(); // skip '*'

        int depth = 1;

        while (depth > 0 && stream.currentCharacter() != TokenDescription.EOF) {
            if (stream.currentCharacter() == '/' && stream.peekCharacter() == '*') {
                depth++;
                stream.advance();
                stream.advance();
            } else if (stream.currentCharacter() == '*' && stream.peekCharacter() == '/') {
                depth--;
                stream.advance();
                stream.advance();
            } else {
                stream.advance();
            }
        }
    }
}