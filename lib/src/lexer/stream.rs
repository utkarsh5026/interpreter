use thiserror::Error;

pub const EOF: char = '\0';
pub const COMMENT_START: char = '#';
pub const DOUBLE_QUOTE: char = '"';
pub const SINGLE_QUOTE: char = '\'';
pub const FSTRING_PREFIX: char = 'f';
pub const DOT: char = '.';

pub(crate) struct CharacterStream {
    input: String,
    lines: Vec<String>,
    chars: Vec<char>,

    current_pos: usize,
    next_pos: usize,

    current_char: char,
    line_col: (usize, usize),
    pos_history: Vec<(usize, usize)>, // (line, col) for each position
}

impl CharacterStream {
    pub(crate) fn new(input: impl Into<String>) -> Self {
        let input = input.into();
        let lines = input.lines().map(ToString::to_string).collect();
        let chars = input.chars().collect();

        let mut stream = Self {
            input,
            lines,
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

    fn peek(&self) -> Option<char> {
        if self.next_pos < self.chars.len() {
            Some(self.chars[self.next_pos])
        } else {
            None
        }
    }

    pub(crate) fn reset(&mut self) {
        self.current_pos = 0;
        self.next_pos = 1;
        self.current_char = EOF;
        self.line_col = (1, 0);
        self.pos_history = vec![(1, 0)];
    }

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

    pub(crate) const fn current_position(&self) -> usize {
        self.current_pos
    }

    pub(crate) fn current_char(&self) -> char {
        self.current_char
    }

    pub(crate) fn get_substring(&self, start: usize) -> String {
        self.chars[start..self.current_pos].iter().collect()
    }

    pub(crate) fn token_pos(&self) -> (usize, usize) {
        return self.line_col;
    }

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
