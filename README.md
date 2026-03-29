# Yar IntelliJ Plugin

IntelliJ IDEA language support plugin for the [Yar](https://yarlson.dev) programming language.

## Features

### Syntax & Highlighting

- Lexer-based syntax highlighting for keywords, strings, numbers, comments, operators, delimiters
- Semantic highlighting for type names, function names/calls, parameters, fields, enum cases, error literals, `pub` modifier
- Configurable color scheme (Settings > Editor > Color Scheme > Yar)

### Editor

- Brace matching for `{}`, `[]`, `()`
- Line commenting (`Cmd+/` / `Ctrl+/`)
- Code folding for functions, structs, interfaces, enums, blocks, and import groups
- Code formatting with spacing and indentation rules (`Cmd+Alt+L` / `Ctrl+Alt+L`)

### Navigation

- Structure view with structs (fields), interfaces (methods), enums (cases), and functions
- Go to definition (`Cmd+B` / `Ctrl+B`)
- Find usages (`Alt+F7`)
- Go to symbol (`Cmd+Alt+O` / `Ctrl+Alt+Shift+N`)
- Rename refactoring (`Shift+F6`)

### Code Intelligence

- Code completion for keywords, builtin types/functions, stdlib packages, and local symbols
- Quick documentation for builtins, stdlib packages, and all declarations (`F1` / `Ctrl+Q`)

### External Integration (planned)

- External annotator using `yar check` for real compiler diagnostics
- Run configurations for `yar run` and `yar build`
- Gutter run icons for `fn main()`

## Requirements

- IntelliJ IDEA 2024.3+ (build 243+)
- JDK 21+
- [Yar compiler](https://yarlson.dev) (for run configs and external diagnostics)

## Installation

### From Source

```bash
git clone <repo-url>
cd yar-plugin
export JAVA_HOME=/path/to/jdk21+
./gradlew build
```

Install from `build/distributions/Yar-*.zip` via Settings > Plugins > Install Plugin from Disk.

### Development Sandbox

```bash
./gradlew runIde
```

Opens an IntelliJ instance with the plugin loaded for testing.

## Yar Language

Yar is a compiled programming language with Go-like syntax, LLVM backend, explicit error handling, and no exceptions.

```
package main

import "strings"

pub struct User {
    name str
    age i32
}

enum Status {
    Active
    Inactive { reason str }
}

fn (u User) greet(other str) !str {
    if strings.contains(other, " ") {
        return error.InvalidName
    }
    return u.name + ": hello, " + other
}

pub fn main() i32 {
    user := User{name: "Alice", age: 30}
    msg := user.greet("Bob") or |err| {
        print("error\n")
        return 1
    }
    print(msg)
    return 0
}
```

Key language features: structs, interfaces, enums with payloads, generics, methods with receivers, pointers, slices, maps, closures, `match` expressions, error propagation (`?`) and handling (`or |err| {}`).

## Development

### Build Commands

| Command                                  | Description                                        |
| ---------------------------------------- | -------------------------------------------------- |
| `./gradlew build`                        | Full build (generate + compile + package + verify) |
| `./gradlew generateLexer generateParser` | Regenerate lexer/parser from grammar files         |
| `./gradlew runIde`                       | Launch sandbox IDE with plugin                     |
| `./gradlew test`                         | Run tests                                          |
| `./gradlew verifyPluginStructure`        | Verify plugin structure                            |
| `./gradlew runPluginVerifier`            | Check binary compatibility with target IDEs        |
| `./gradlew clean`                        | Clean build artifacts and generated sources        |

### Project Structure

```
src/main/
├── grammars/
│   ├── Yar.bnf                    # Grammar-Kit BNF grammar
│   └── Yar.flex                   # JFlex lexer definition
├── kotlin/dev/yarlson/yar/
│   ├── YarLanguage.kt             # Language singleton
│   ├── YarFileType.kt             # .yar file type
│   ├── YarIcons.kt                # Plugin icons
│   ├── lexer/                     # FlexAdapter wrapper
│   ├── parser/                    # ParserDefinition
│   ├── psi/                       # PSI types, token sets, named elements, element factory
│   ├── highlighting/              # Syntax highlighter, semantic annotator, color settings
│   ├── editor/                    # Brace matcher, commenter, folding
│   ├── structure/                 # Structure view
│   ├── references/                # Reference resolution
│   ├── navigation/                # Find usages, go-to-symbol
│   ├── completion/                # Code completion
│   ├── documentation/             # Quick documentation
│   └── formatter/                 # Code formatting
├── gen/                           # Generated lexer/parser/PSI (not committed)
└── resources/
    ├── META-INF/plugin.xml        # Plugin descriptor
    └── icons/yar.svg              # File icon
```

### Technology Stack

- **Kotlin** + **Java 21** bytecode target
- **IntelliJ Platform Gradle Plugin** 2.13.1
- **Grammar-Kit** 2023.3.0.3 (BNF parser generation)
- **JFlex** (lexer generation)
- **IntelliJ Platform SDK** 2024.3

---

**Vendor**: [yarlson.dev](https://yarlson.dev)
