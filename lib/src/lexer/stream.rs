//! Character-level streaming primitives for the Mutant Lang lexer.
//!
//! This module provides [`CharacterStream`], a cursor over a Unicode character
//! sequence, and a small set of sentinel constants that the lexer uses to
//! identify structurally significant characters without scattering magic
//! literals throughout the parsing logic.
//!
//! The stream works at the `char` level (not bytes), so multi-byte Unicode
//! codepoints are handled transparently. It records `(line, column)` position
//! history so that the lexer can attach accurate [`TokenPosition`](crate::token::TokenPosition)
//! information to every token it emits, and can backtrack when a speculative
//! parse fails.

/// Sentinel character returned by [`CharacterStream::current_char`] after the
/// input is exhausted.
///
/// Using the null character (`'\0'`) as EOF avoids wrapping every read in an
/// `Option` while still giving the lexer a distinct, unmatchable value that
/// cannot appear in valid source text.
pub const EOF: char = '\0';

/// The character that begins a single-line comment (`#`).
///
/// Everything from this character to the end of the current line is ignored by
/// the lexer.
pub const COMMENT_START: char = '#';

/// The double-quote delimiter used for ordinary string literals (`"`).
pub const DOUBLE_QUOTE: char = '"';

/// The single-quote delimiter used for character or alternative string literals (`'`).
pub const SINGLE_QUOTE: char = '\'';

/// The prefix character that introduces an f-string literal (`f`).
///
/// The lexer checks for this character immediately before a [`DOUBLE_QUOTE`] or
/// [`SINGLE_QUOTE`] to decide whether to enter f-string tokenisation mode.
pub const FSTRING_PREFIX: char = 'f';

/// The dot character (`.`), used as a member-access operator and decimal
/// separator in numeric literals.
pub const DOT: char = '.';

/// A forward-only, backtrack-capable cursor over a Unicode character sequence.
///
/// `CharacterStream` owns the entire input as a `Vec<char>` and exposes a
/// two-position window — `current_pos` (the character being inspected) and
/// `next_pos` (the lookahead) — together with a full `(line, col)` history
/// that makes single or multi-step backtracking cheap.
///
/// # Position tracking
///
/// Lines are 1-indexed; columns are 1-indexed within a line. After a `'\n'`
/// (or a `'\r\n'` pair) the column resets to `0` so that the *next* advance
/// lands at column `1` of the new line. The stream stores one
/// `(line, col)` entry per position in `pos_history`, so backtracking always
/// restores an exact snapshot rather than recomputing it.
///
/// # Panics
///
/// [`peek_char`](CharacterStream::peek_char) panics if called when
/// `next_pos >= chars.len()` (i.e. when the stream is at or past EOF). Prefer
/// [`peek`](CharacterStream::peek) (returns `Option<char>`) unless you have
/// already verified that more input is available.
pub struct CharacterStream {
    /// The full input decomposed into Unicode scalar values.
    chars: Vec<char>,

    /// Index of the character currently under inspection.
    current_pos: usize,

    /// Index of the next character to be consumed by [`advance`](CharacterStream::advance).
    next_pos: usize,

    /// The character at `current_pos`, or [`EOF`] when the input is exhausted.
    current_char: char,

    /// `(line, column)` of `current_char`. Lines and columns are 1-indexed;
    /// column resets to `0` immediately after a newline so the next character
    /// opens at `(line + 1, 1)`.
    line_col: (usize, usize),

    /// Snapshot of `(line, col)` for every stream position visited so far.
    ///
    /// Entry `i` holds the position that was current after `i` advances.
    /// [`backtrack`](CharacterStream::backtrack) uses this to restore position
    /// metadata without recomputing it from scratch.
    pos_history: Vec<(usize, usize)>, // (line, col) for each position
}

impl CharacterStream {
    /// Construct a new `CharacterStream` from any value that can be converted
    /// into a [`String`], then advance to the first character.
    ///
    /// After construction, [`current_char`](CharacterStream::current_char)
    /// holds the first character of the input, or [`EOF`] if the input is
    /// empty.
    ///
    /// # Examples
    ///
    /// ```rust
    /// # use mutant_lang::lexer::stream::{CharacterStream, EOF};
    /// let mut stream = CharacterStream::new("hello");
    /// assert_eq!(stream.current_char(), 'h');
    /// ```
    pub(crate) fn new(input: impl Into<String>) -> Self {
        let input = input.into();
        let chars = input.chars().collect();

        let mut stream = Self {
            chars,
            current_pos: 0,
            next_pos: 0,
            current_char: EOF,
            line_col: (1, 0),
            pos_history: vec![(1, 0)],
        };

        stream.advance();
        stream
    }

    /// Consume the current character and move the cursor forward by one position.
    ///
    /// After this call:
    /// - [`current_char`](CharacterStream::current_char) holds the character
    ///   that was previously at `next_pos`, or [`EOF`] if the end of input has
    ///   been reached.
    /// - [`token_pos`](CharacterStream::token_pos) reflects the updated
    ///   `(line, col)`.
    ///
    /// Calling `advance` when the stream is already at [`EOF`] is safe — the
    /// current character remains [`EOF`] and position tracking continues
    /// without panicking.
    ///
    /// Windows-style `\r\n` line endings are handled: a `\r` followed by `\n`
    /// increments the line counter only once (on the `\r`), matching the
    /// behaviour users expect when editing on Windows.
    pub(crate) fn advance(&mut self) {
        self.current_pos = self.next_pos;
        self.next_pos += 1;

        let (line, col) = self.line_col;

        if self.current_pos < self.chars.len() {
            self.current_char = self.chars[self.current_pos];

            if self.current_char == '\n' || (self.current_char == '\r' && self.peek() == Some('\n'))
            {
                self.line_col = (line + 1, 0);
            } else {
                self.line_col = (line, col + 1);
            }
        } else {
            self.current_char = EOF;
        }

        self.pos_history.push(self.line_col);
    }

    /// Return the character at `next_pos` without advancing the cursor.
    ///
    /// Returns `None` when the stream has no further input (i.e. the current
    /// character is the last one, or the input is empty).
    fn peek(&self) -> Option<char> {
        if self.next_pos < self.chars.len() {
            Some(self.chars[self.next_pos])
        } else {
            None
        }
    }

    /// Move the cursor backward by up to `steps` positions, restoring the
    /// character and `(line, col)` state that existed at that earlier point.
    ///
    /// Backtracking is bounded: if `steps` exceeds `current_pos` (i.e. you
    /// ask to go before the start of the input), the stream silently clamps to
    /// position `0` and the call is effectively a no-op when already at the
    /// start. This prevents underflow without requiring the caller to
    /// bounds-check first.
    ///
    /// `pos_history` is truncated to the new position so that subsequent
    /// [`advance`](CharacterStream::advance) calls rebuild history correctly.
    ///
    /// # Examples
    ///
    /// ```rust
    /// # use mutant_lang::lexer::stream::CharacterStream;
    /// let mut stream = CharacterStream::new("abc");
    /// stream.advance(); // 'b'
    /// stream.advance(); // 'c'
    /// stream.backtrack(2);
    /// assert_eq!(stream.current_char(), 'a');
    /// ```
    pub(crate) fn backtrack(&mut self, steps: usize) {
        let steps = steps.min(self.current_pos);
        if steps == 0 {
            return;
        }

        self.current_pos -= steps;
        self.next_pos = self.current_pos + 1;

        self.current_char = self.chars[self.current_pos];
        self.line_col = self.pos_history[self.next_pos];
        self.pos_history.truncate(self.next_pos + 1);
    }

    /// Return the byte index of the current character within the character
    /// vector.
    ///
    /// This index is suitable for use with [`get_substring`](CharacterStream::get_substring)
    /// to mark the start of a token before consuming its characters.
    pub(crate) const fn current_position(&self) -> usize {
        self.current_pos
    }

    /// Return the character currently under the cursor.
    ///
    /// Returns [`EOF`] when the input is exhausted. Callers can match against
    /// `EOF` to detect the end of input without unwrapping an `Option`.
    pub(crate) const fn current_char(&self) -> char {
        self.current_char
    }

    /// Extract the source text in the half-open range `[start, current_pos)`.
    ///
    /// Intended to be called after consuming a complete token: record
    /// `current_position()` before the first [`advance`](CharacterStream::advance),
    /// then call `get_substring(start)` once the token is fully consumed to
    /// obtain its lexeme.
    ///
    /// # Examples
    ///
    /// ```rust
    /// # use mutant_lang::lexer::stream::CharacterStream;
    /// let mut stream = CharacterStream::new("let");
    /// let start = stream.current_position();
    /// stream.advance();
    /// stream.advance();
    /// stream.advance();
    /// assert_eq!(stream.get_substring(start), "let");
    /// ```
    pub(crate) fn get_substring(&self, start: usize) -> String {
        self.chars[start..self.current_pos].iter().collect()
    }

    /// Return the `(line, column)` position of the current character.
    ///
    /// Both values are 1-indexed. Call this *before* advancing past a token's
    /// first character to capture the token's start position for error
    /// reporting and [`TokenPosition`](crate::token::TokenPosition) metadata.
    pub(crate) const fn token_pos(&self) -> (usize, usize) {
        self.line_col
    }

    /// Return the character at `next_pos` without advancing the cursor.
    ///
    /// # Panics
    ///
    /// Panics if `next_pos >= chars.len()`, i.e. when the stream is at or past
    /// the last character. Use the private [`peek`](CharacterStream::peek)
    /// method (returns `Option<char>`) in contexts where the stream may be
    /// near the end of input.
    pub(crate) fn peek_char(&self) -> char {
        self.chars[self.next_pos]
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn advance_single_char_then_eof() {
        let mut s = CharacterStream::new("a");
        assert_eq!(s.current_char, 'a');
        s.advance();
        assert_eq!(s.current_char, EOF);
    }

    #[test]
    fn advance_walks_all_chars() {
        let mut s = CharacterStream::new("abc");
        assert_eq!(s.current_char, 'a');
        s.advance();
        assert_eq!(s.current_char, 'b');
        s.advance();
        assert_eq!(s.current_char, 'c');
        s.advance();
        assert_eq!(s.current_char, EOF);
    }

    #[test]
    fn advance_past_eof_stays_eof() {
        let mut s = CharacterStream::new("x");
        s.advance(); // EOF
        s.advance(); // still EOF, should not panic
        assert_eq!(s.current_char, EOF);
    }

    #[test]
    fn advance_empty_input_is_eof() {
        let s = CharacterStream::new("");
        assert_eq!(s.current_char, EOF);
    }

    #[test]
    fn advance_multibyte_unicode() {
        // validates Vec<char> indexing — would break with byte-offset indexing
        let mut s = CharacterStream::new("é!");
        assert_eq!(s.current_char, 'é');
        s.advance();
        assert_eq!(s.current_char, '!');
        s.advance();
        assert_eq!(s.current_char, EOF);
    }

    #[test]
    fn peek_returns_next_char() {
        let s = CharacterStream::new("ab");
        assert_eq!(s.peek(), Some('b'));
    }

    #[test]
    fn peek_at_last_char_returns_none() {
        let s = CharacterStream::new("a");
        assert_eq!(s.peek(), None);
    }

    #[test]
    fn peek_does_not_advance() {
        let s = CharacterStream::new("ab");
        assert_eq!(s.peek(), Some('b'));
        assert_eq!(s.peek(), Some('b')); // still same
        assert_eq!(s.current_char, 'a'); // current unchanged
    }

    #[test]
    fn peek_empty_input_returns_none() {
        let s = CharacterStream::new("");
        assert_eq!(s.peek(), None);
    }

    #[test]
    fn line_col_starts_at_1_1() {
        let s = CharacterStream::new("a");
        assert_eq!(s.line_col, (1, 1));
    }

    #[test]
    fn newline_resets_col_and_bumps_line() {
        let mut s = CharacterStream::new("a\nb");
        assert_eq!(s.line_col, (1, 1)); // 'a'
        s.advance(); // '\n'
        assert_eq!(s.line_col, (2, 0));
        s.advance(); // 'b'
        assert_eq!(s.line_col, (2, 1));
    }

    #[test]
    fn col_increments_within_a_line() {
        let mut s = CharacterStream::new("abc");
        assert_eq!(s.line_col, (1, 1));
        s.advance();
        assert_eq!(s.line_col, (1, 2));
        s.advance();
        assert_eq!(s.line_col, (1, 3));
    }

    #[test]
    fn backtrack_goes_back_one_char() {
        let mut s = CharacterStream::new("abc");
        assert_eq!(s.current_char, 'a');
        s.advance();
        assert_eq!(s.current_char, 'b');
        s.backtrack(1);
        assert_eq!(s.current_char, 'a');
    }

    #[test]
    fn backtrack_then_advance_replays() {
        let mut s = CharacterStream::new("abc");
        s.advance(); // 'b'
        s.backtrack(1); // back to 'a'
        assert_eq!(s.current_char, 'a');
        s.advance();
        assert_eq!(s.current_char, 'b'); // same char again
    }

    #[test]
    fn backtrack_at_start_is_noop() {
        let mut s = CharacterStream::new("abc");
        assert_eq!(s.current_char, 'a');
        s.backtrack(1); // nothing to go back to
        assert_eq!(s.current_char, 'a'); // unchanged, no panic
    }

    #[test]
    fn backtrack_restores_line_col() {
        let mut s = CharacterStream::new("a\nb");
        assert_eq!(s.line_col, (1, 1)); // 'a'
        s.advance(); // '\n' — line becomes 2
        assert_eq!(s.line_col, (2, 0));
        s.backtrack(1); // back to 'a'
        assert_eq!(s.line_col, (1, 1));
    }

    #[test]
    fn backtrack_multiple_steps() {
        let mut s = CharacterStream::new("abcd");
        s.advance(); // 'b'
        s.advance(); // 'c'
        s.advance(); // 'd'
        s.backtrack(3);
        assert_eq!(s.current_char, 'a');
    }

    #[test]
    fn backtrack_more_than_available_is_noop() {
        let mut s = CharacterStream::new("ab");
        s.advance(); // 'b'
        s.backtrack(10);
        assert_eq!(s.current_char, 'a');
    }
}
