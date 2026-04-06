<div align="center">

# Mutant Lang

**A fast, expressive scripting language — built from scratch in Rust.**

[![Build Status](https://img.shields.io/github/actions/workflow/status/utkarsh5026/interpreter/ci.yml?branch=main&style=flat-square&logo=github)](https://github.com/utkarsh5026/interpreter/actions)
[![Version](https://img.shields.io/badge/version-0.1.0-blue?style=flat-square)](Cargo.toml)
[![Rust](https://img.shields.io/badge/rust-2024%20edition-orange?style=flat-square&logo=rust)](https://www.rust-lang.org)
[![MSRV](https://img.shields.io/badge/MSRV-1.87-orange?style=flat-square)](lib/rust-toolchain.toml)
[![License: MIT](https://img.shields.io/badge/license-MIT-green?style=flat-square)](LICENSE)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen?style=flat-square)](CONTRIBUTING.md)

<br/>

![Mutant Lang Banner](https://via.placeholder.com/900x200/1a1a2e/e94560?text=MUTANT+LANG+%E2%80%94+Interpreter+in+Rust)

</div>

---

## Table of Contents

- [About The Project](#about-the-project)
- [Key Features](#key-features)
- [Getting Started](#getting-started)
- [Usage / Examples](#usage--examples)
- [Language Reference](#language-reference)
- [Architecture](#architecture)
- [Roadmap](#roadmap)
- [Contributing](#contributing)
- [License](#license)
- [Contact / Support](#contact--support)

---

## About The Project

**Mutant Lang** is a dynamically-typed, interpreted scripting language implemented as a tree-walking interpreter entirely in Rust. It was built from first principles to explore every layer of language implementation — from raw character streams to a full object-oriented runtime — without relying on any parser-generator or VM framework.

The project exists to answer one question: _what does it actually take to go from a `.mutant` source file to a running program?_ Every stage is hand-rolled:

| Stage         | What it does                                                                                                                                        |
| ------------- | --------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Lexer**     | Converts source text into a typed `Token` stream, handling escape sequences, f-strings, nested block comments, and precise `(line, col)` tracking   |
| **Parser**    | Builds an Abstract Syntax Tree using recursive descent for statements and **Pratt (TDOP) parsing** for expressions with correct operator precedence |
| **AST**       | Strongly-typed Rust enums — one variant per node kind; zero heap indirection for leaf nodes                                                         |
| **Evaluator** | Tree-walking interpreter with lexical scoping via chained `Environment` maps; control-flow is propagated through sentinel `Object` variants         |
| **REPL**      | `rustyline`-powered interactive shell with persistent state, colored output, and command history                                                    |

---

## Key Features

- **Full OOP** — `class`, `extends`, `super`, `this`, constructors, and method overriding
- **First-class functions & closures** — functions capture their defining scope
- **F-string interpolation** — `f"Hello {name}, you are {age} years old!"`
- **Rich type system** — integers, floats, booleans, strings, arrays, hash maps, and `null`
- **Compound assignment operators** — `+=`, `-=`, `*=`, `/=`, `%=`
- **Bitwise operators** — `&`, `|`, `^`, `~`, `<<`, `>>`
- **Flexible control flow** — `if` / `elif` / `else`, `while`, `for`, `break`, `continue`, `return`
- **Nested comment styles** — `# single-line` and `/* nested /* block */ */`
- **Colored REPL output** — integers in yellow, strings in green, booleans in cyan
- **Precise error messages** — every token carries its `(line, col)` source position

---

## Getting Started

### Prerequisites

| Tool                                | Minimum version     |
| ----------------------------------- | ------------------- |
| [Rust toolchain](https://rustup.rs) | 1.87 (2024 edition) |
| Cargo                               | ships with Rust     |

### Installation

```bash
# 1. Clone the repository
git clone https://github.com/utkarsh5026/interpreter.git
cd interpreter/lib

# 2. Build a release binary
cargo build --release

# 3. The binary is now at:
./target/release/mutant-lang
```

### Running the REPL

```bash
cargo run --release
```

```
  ███╗   ███╗██╗   ██╗████████╗ █████╗ ███╗   ██╗████████╗
  ████╗ ████║██║   ██║╚══██╔══╝██╔══██╗████╗  ██║╚══██╔══╝
  ██╔████╔██║██║   ██║   ██║   ███████║██╔██╗ ██║   ██║
  ██║╚██╔╝██║██║   ██║   ██║   ██╔══██║██║╚██╗██║   ██║
  ██║ ╚═╝ ██║╚██████╔╝   ██║   ██║  ██║██║ ╚████║   ██║
  ╚═╝     ╚═╝ ╚═════╝    ╚═╝   ╚═╝  ╚═╝╚═╝  ╚═══╝   ╚═╝
                    L A N G   v 0 . 1
  Type Ctrl-C or Ctrl-D to exit.

>>
```

### Running the test suite

```bash
cargo test
```

---

## Usage / Examples

### Hello, World

```javascript
print("Hello, World!");
```

### Variables & Constants

```javascript
let counter = 0;
counter += 1; // mutable — OK

const PI = 3.14159;
// PI = 3;          // compile-time error: cannot reassign constant
```

### Functions & Closures

```javascript
fn makeAdder(x) {
    return fn(y) { return x + y; };
}

let add5 = makeAdder(5);
print(add5(3));   // 8
```

### F-String Interpolation

```javascript
let name = "Alice";
let score = 42;
print(f"Player {name} scored {score * 2} points!");
// Player Alice scored 84 points!
```

### Classes & Inheritance

```javascript
class Animal {
    constructor(name) {
        this.name = name;
    }
    speak() { return f"{this.name} makes a sound"; }
}

class Dog extends Animal {
    speak() { return f"{this.name} barks!"; }
}

let d = new Dog("Rex");
print(d.speak());  // Rex barks!
```

### Fibonacci

```javascript
fn fib(n) {
    if (n <= 1) { return n; }
    return fib(n - 1) + fib(n - 2);
}

for (let i = 0; i < 10; i += 1) {
    print(f"fib({i}) = {fib(i)}");
}
```

For more examples see the existing README language reference above, or explore [lib/src/evaluator/](lib/src/evaluator/) and [lib/src/parser/](lib/src/parser/).

---

## Language Reference

### Data Types

| Type     | Example                     |
| -------- | --------------------------- |
| Integer  | `42`, `-7`                  |
| Float    | `3.14`, `.5`                |
| Boolean  | `true`, `false`             |
| String   | `"hello"`, `'world'`        |
| F-String | `f"value is {x + 1}"`       |
| Array    | `[1, "two", true]`          |
| Hash     | `{"key": "value", 1: true}` |
| Null     | `null`                      |

### Operators

| Category   | Operators                                   |
| ---------- | ------------------------------------------- |
| Arithmetic | `+` `-` `*` `/` `%` `//` (integer division) |
| Comparison | `==` `!=` `<` `>` `<=` `>=`                 |
| Logical    | `&&` `\|\|` `!`                             |
| Bitwise    | `&` `\|` `^` `~` `<<` `>>`                  |
| Assignment | `=` `+=` `-=` `*=` `/=` `%=`                |

### Built-in Functions

```javascript
len(arr); // length of array or string
first(arr); // first element
last(arr); // last element
rest(arr); // all elements except first
keys(hash); // array of hash keys
values(hash); // array of hash values
type(value); // "INTEGER" | "FLOAT" | "STRING" | ...
print(a, b, c); // print to stdout, space-separated
```

### Comments

```javascript
# This is a single-line comment

/*
 * This is a block comment.
 * /* Nesting is supported. */
 * Still in the outer comment.
 */
```

---

## Architecture

```
Source Code
    │
    ▼
┌──────────────────────────────────────────────┐
│  LEXER  (lib/src/lexer/)                     │
│  CharacterStream → pull-style Token iterator │
│  Handles: strings, f-strings, nested comments│
│           escape sequences, (line,col) pos   │
└──────────────────────┬───────────────────────┘
                       │ Token stream
                       ▼
┌──────────────────────────────────────────────┐
│  PARSER  (lib/src/parser/)                   │
│  Recursive descent (statements)              │
│  Pratt / TDOP (expressions & precedence)     │
└──────────────────────┬───────────────────────┘
                       │ AST (lib/src/ast/)
                       ▼
┌──────────────────────────────────────────────┐
│  EVALUATOR  (lib/src/evaluator/)             │
│  Tree-walking interpreter                    │
│  Chained Environment scoping                 │
│  Control-flow via Object sentinels           │
│  (Return, Break, Continue)                   │
└──────────────────────┬───────────────────────┘
                       │ Object
                       ▼
┌──────────────────────────────────────────────┐
│  REPL  (lib/src/repl.rs)                     │
│  rustyline editor + colored output           │
│  Persistent Environment across inputs        │
└──────────────────────────────────────────────┘
```

### Key design decisions

- **`Object` is a single Rust enum**, not a class hierarchy — pattern matching replaces dynamic dispatch everywhere
- **Environments are `Rc<RefCell<…>>` chains** — child scopes hold a reference to their parent without cloning the whole chain
- **Pratt parser** handles all infix/prefix expressions cleanly with explicit precedence levels (see [lib/src/parser/precedence.rs](lib/src/parser/precedence.rs))
- **`thiserror`** drives all structured error types, giving human-readable messages with source positions for free

---

## Roadmap

- [ ] Standard library (math, string utilities, I/O)
- [ ] `import` / module system
- [ ] Bytecode compiler + stack-based VM
- [ ] Type inference / optional static types
- [ ] Garbage collector (replace clone-on-bind with `Rc` or tracing GC)
- [ ] Concurrency primitives (`async`/`await`)
- [ ] Language Server Protocol (LSP) support
- [ ] WASM compilation target

---

## Contributing

Contributions are warmly welcome. Here's the standard flow:

1. **Fork** the repository
2. **Create** a feature branch — `git checkout -b feat/my-feature`
3. **Commit** your changes — `git commit -m "feat: add my feature"`
4. **Push** to your fork — `git push origin feat/my-feature`
5. **Open a Pull Request** against `main`

Please make sure `cargo test` and `cargo clippy -- -D warnings` both pass before opening the PR.

> **Tip:** The `rust-migration` branch tracks ongoing work porting the original Java implementation to Rust. If you're working on interpreter internals, that's a good place to start reading.

---

## License

Distributed under the **MIT License**. See [`LICENSE`](LICENSE) for the full text.

---

## Contact / Support

| Channel       | Link                                                               |
| ------------- | ------------------------------------------------------------------ |
| GitHub Issues | [Open an issue](https://github.com/utkarsh5026/interpreter/issues) |
| X (Twitter)   | [@utkarsh5026](https://x.com/utkarsh5026)                          |
| LinkedIn      | [Utkarsh Priyadarshi](https://linkedin.com/in/utkarsh5026)         |

---

<div align="center">

Built with Rust — because performance and correctness shouldn't be optional.

</div>
