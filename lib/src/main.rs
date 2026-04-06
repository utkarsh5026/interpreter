#![allow(dead_code)]
#![allow(unused_imports)]
#![allow(unused_variables)]

mod repl;

mod ast;
mod evaluator;
mod lexer;
mod parser;
mod token;

fn main() {
    repl::start();
}
