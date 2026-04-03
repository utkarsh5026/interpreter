//! Interactive Read-Eval-Print Loop (REPL) for the Mutant Lang interpreter.
//!
//! This module provides a terminal-based REPL that reads source lines from
//! the user, runs them through the [`Lexer`], and pretty-prints the resulting
//! token stream as a formatted table.
//!
//! Line editing (cursor movement, history recall with ↑/↓) is provided by
//! [`rustyline`]. Table rendering is handled by [`comfy_table`] using the
//! `UTF8_FULL` preset, which draws full Unicode box-drawing borders.
//!
//! # Current stage
//!
//! The REPL currently stops at the **lexing** stage — it tokenises each input
//! line and displays the token stream. Parsing and evaluation will be wired in
//! as those pipeline stages are completed.

use comfy_table::{presets::UTF8_FULL, Table};
use rustyline::{error::ReadlineError, DefaultEditor};

use crate::lexer::Lexer;

const PROMPT: &str = ">> ";

/// Starts the Mutant Lang REPL and blocks until the user exits.
///
/// Initialises a [`rustyline`] line editor, then enters an infinite read loop.
/// On each iteration:
///
/// 1. Prints [`PROMPT`] and waits for a line of input.
/// 2. Skips blank lines silently.
/// 3. Adds the line to the in-session history so the user can recall it with ↑.
/// 4. Feeds the line to [`Lexer::tokenize_all`] and renders the resulting
///    tokens as a [`comfy_table`] table with columns **TOKEN TYPE**, **LITERAL**,
///    and **POSITION**.
/// 5. Prints any [`crate::lexer::parsers::LexError`] to `stderr` and continues
///    — a lex error does not terminate the session.
///
/// The loop exits cleanly on `Ctrl-C` ([`ReadlineError::Interrupted`]) or
/// `Ctrl-D` ([`ReadlineError::Eof`]). Any other [`rustyline`] error is printed
/// to `stderr` and also terminates the loop.
///
/// # Panics
///
/// Panics if [`rustyline`] cannot initialise the terminal editor (e.g. the
/// process has no controlling terminal and `rustyline` cannot open one).
pub fn start() {
    let mut rl = DefaultEditor::new().expect("failed to create editor");

    println!("Welcome to Mutant Lang REPL!");
    println!("Press Ctrl-C or Ctrl-D to exit.");
    println!();

    loop {
        match rl.readline(PROMPT) {
            Ok(line) => {
                if line.trim().is_empty() {
                    continue;
                }

                let _ = rl.add_history_entry(&line);
                let lexer = Lexer::new(line);

                match lexer.tokenize_all() {
                    Ok(tokens) => {
                        let mut table = Table::new();
                        table.load_preset(UTF8_FULL);
                        table.set_header(["TOKEN TYPE", "LITERAL", "POSITION"]);

                        for token in tokens {
                            table.add_row([
                                token.kind.to_string(),
                                token.literal.clone(),
                                token.position.to_string(),
                            ]);
                        }

                        println!("{table}");
                        println!();
                    }
                    Err(e) => eprintln!("lex error: {e}"),
                }
            }
            Err(ReadlineError::Interrupted | ReadlineError::Eof) => {
                println!("bye");
                break;
            }
            Err(e) => {
                eprintln!("readline error: {e}");
                break;
            }
        }
    }
}
