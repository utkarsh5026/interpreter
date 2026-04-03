#![allow(dead_code)]
#![allow(unused_imports)]
#![allow(unused_variables)]

use mutant_lang::repl;

mod lexer;
mod parser;
mod token;

fn main() {
    repl::start();
}
