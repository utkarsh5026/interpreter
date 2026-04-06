//! Interactive Read-Eval-Print Loop (REPL) for the Mutant Lang interpreter.
//!
//! Reads a line, parses it into statements, evaluates each statement against a
//! persistent global environment, and prints any non-null result.  Errors at
//! any stage are reported to stderr and the session continues.
//!
//! The [`Environment`] is created once *outside* the readline loop so that
//! bindings persist across inputs — `let x = 5;` on one line is visible as
//! `x` on the next.

use colored::Colorize;
use rustyline::{DefaultEditor, error::ReadlineError};

use crate::evaluator::{Evaluator, env::Environment, objects::Object};
use crate::lexer::Lexer;
use crate::parser::Parser;

const PROMPT: &str = ">> ";

const BANNER: &str = r"
  ███╗   ███╗██╗   ██╗████████╗ █████╗ ███╗   ██╗████████╗
  ████╗ ████║██║   ██║╚══██╔══╝██╔══██╗████╗  ██║╚══██╔══╝
  ██╔████╔██║██║   ██║   ██║   ███████║██╔██╗ ██║   ██║
  ██║╚██╔╝██║██║   ██║   ██║   ██╔══██║██║╚██╗██║   ██║
  ██║ ╚═╝ ██║╚██████╔╝   ██║   ██║  ██║██║ ╚████║   ██║
  ╚═╝     ╚═╝ ╚═════╝    ╚═╝   ╚═╝  ╚═╝╚═╝  ╚═══╝   ╚═╝
                    L A N G   v 0 . 1
";

/// Starts the Mutant Lang REPL and blocks until the user exits.
///
/// 1. Creates a single global [`Environment`] and [`Evaluator`] that live for
///    the entire session.
/// 2. For each line of input: lexes → parses → evaluates each statement.
/// 3. Prints any result that is not [`Object::Null`] (declarations and void
///    statements produce `Null` and are silently skipped).
/// 4. Parse or eval errors go to stderr; the session is not terminated.
pub fn start() {
    let mut rl = DefaultEditor::new().expect("failed to create editor");
    let evaluator = Evaluator::new();
    let env = Environment::new();

    print!("{BANNER}");
    println!("  Type Ctrl-C or Ctrl-D to exit.\n");

    loop {
        match rl.readline(&PROMPT.cyan().bold().to_string()) {
            Ok(line) => {
                if line.trim().is_empty() {
                    continue;
                }
                let _ = rl.add_history_entry(&line);

                let lexer = Lexer::new(line);
                let mut parser = match Parser::new(lexer) {
                    Ok(p) => p,
                    Err(e) => {
                        eprintln!("{} {e}", "lex error:".red().bold());
                        continue;
                    }
                };

                let (stmts, errors) = parser.parse_program();
                for e in &errors {
                    eprintln!("{} {e}", "parse error:".red().bold());
                }
                if !errors.is_empty() {
                    continue;
                }

                for stmt in &stmts {
                    match evaluator.eval_statement(stmt, &env) {
                        Ok(Object::Null) => {}
                        Ok(result) => {
                            let formatted = match &result {
                                Object::Integer(_) => result.to_string().yellow(),
                                Object::Str(_) => result.to_string().green(),
                                Object::Boolean(_) => result.to_string().cyan(),
                                Object::Array(_) => result.to_string().bright_blue(),
                                _ => result.to_string().white(),
                            };
                            println!("{formatted}");
                        }
                        Err(e) => eprintln!("{} {e}", "eval error:".red().bold()),
                    }
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
