# Yar IntelliJ Plugin

IntelliJ IDEA language support plugin for the **Yar** programming language. Provides IDE integration features including syntax highlighting, parsing, code folding, structure view, and more.

## Features

- **Syntax and semantic highlighting** - Full lexer-based syntax highlighting with semantic annotations
- **Code parsing and error recovery** - Complete parser with graceful error handling
- **Brace matching** - Automatic matching of `()`, `{}`, and `[]`
- **Line commenting** - Toggle line comments with standard keyboard shortcuts
- **Code folding** - Collapse functions, structs, interfaces, enums, and blocks
- **Structure view** - Navigate code structure in the Structure tool window
- **Configurable color scheme** - Customize colors via Settings → Editor → Color Scheme → Yar

## Requirements

- IntelliJ IDEA 2024.3.1.1 or later (build 243+)
- JDK 21 or later

## Installation

### From JetBrains Marketplace

Not documented. Check JetBrains Marketplace for availability.

### From Source

1. Clone the repository
2. Build the plugin:

```bash
./gradlew build
```

3. Install the plugin from `build/distributions/Yar-*.zip` via **Settings → Plugins → ⚙️ → Install Plugin from Disk**

## Usage

Once installed, the plugin automatically recognizes files with the `.yar` extension and provides:

- Syntax highlighting for keywords, identifiers, literals, operators, and comments
- Code navigation via Structure view
- Code folding for collapsible regions
- Brace matching and auto-pairing
- Line commenting with `Ctrl+/` (or `Cmd+/` on macOS)

### Yar Language Overview

Yar is a programming language with the following constructs:

```yar
package main

import "fmt"

pub struct User {
    name string
    age  int
}

pub fn greet(u User) string {
    return "Hello, " + u.name
}

fn main() {
    user := User{name: "Alice", age: 30}
    message := greet(user)
}
```

**Supported language features:**

- Package and import declarations
- Structs, interfaces, and enums
- Functions with receivers and generics
- Variables (`var`, `:=`)
- Control flow (`if`, `else`, `for`, `match`, `break`, `continue`, `return`)
- Types: pointers (`*`), arrays (`[N]T`), slices (`[]T`), maps (`map[K]V`), function types
- Error handling with `!` and `or` expressions
- Operators: arithmetic, comparison, logical, unary

## Development

### Prerequisites

- JDK 21+
- Gradle (wrapper included)

### Build Commands

| Command                                  | Description                                                  |
| ---------------------------------------- | ------------------------------------------------------------ |
| `./gradlew generateLexer generateParser` | Generate lexer and parser from grammar files                 |
| `./gradlew build`                        | Build the plugin                                             |
| `./gradlew compileKotlin compileJava`    | Compile source code (depends on code generation)             |
| `./gradlew test`                         | Run tests (JUnit 4.13.2)                                     |
| `./gradlew clean`                        | Clean build artifacts and generated sources (`src/main/gen`) |
| `./gradlew verifyPlugin`                 | Verify plugin compatibility with IntelliJ Platform           |

### Project Structure

```
src/
├── main/
│   ├── grammars/
│   │   ├── Yar.bnf          # Parser grammar (BNF)
│   │   └── Yar.flex         # Lexer grammar (JFlex)
│   ├── kotlin/dev/yarlson/yar/
│   │   ├── YarLanguage.kt   # Language definition
│   │   ├── YarFileType.kt   # File type registration
│   │   ├── editor/          # Brace matcher, commenter, folding
│   │   ├── highlighting/    # Syntax highlighter, annotator, color settings
│   │   ├── lexer/           # Lexer adapter
│   │   ├── parser/          # Parser definition
│   │   ├── psi/             # PSI elements and utilities
│   │   └── structure/       # Structure view
│   ├── gen/                 # Generated lexer/parser (created by build)
│   └── resources/
│       └── META-INF/
│           └── plugin.xml   # Plugin manifest
└── test/
```

### Technology Stack

- **Kotlin** (JVM target 21)
- **IntelliJ Platform SDK** 2024.3.1.1
- **JetBrains GrammarKit** - Parser/lexer generation from BNF and Flex grammars
- **JUnit 4** - Testing framework

## Troubleshooting

| Symptom              | Solution                                                                    |
| -------------------- | --------------------------------------------------------------------------- |
| No README.md file    | This file addresses this issue                                              |
| No LICENSE file      | Add a LICENSE file specifying the project's license terms                   |
| Empty test directory | Test resources exist but no test implementations found in `src/test/kotlin` |

## Contributing

Not documented. Check the repository for contribution guidelines.

## License

Not specified. Check the repository for license information.

---

**Vendor**: [yarlson.dev](https://yarlson.dev)
**Version**: 0.1.0
