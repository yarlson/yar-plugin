# Yar IntelliJ Plugin

## What

A JetBrains IntelliJ Platform plugin that provides language support for the Yar programming language. Targets IntelliJ IDEA Community Edition 2024.3+ and registers the `Yar` language with `.yar` file extension.

## Architecture

Single-module Gradle plugin using the IntelliJ Platform Gradle Plugin (v2.13.1). The parser and lexer are generated from Grammar-Kit (BNF) and JFlex grammars at build time into `src/main/gen/`. Hand-written Kotlin code in `src/main/kotlin/` implements all IDE integration features on top of the generated PSI tree.

Key architectural layers:

- **Grammar and PSI** -- BNF grammar (`Yar.bnf`) and JFlex lexer (`Yar.flex`) define the language syntax. Generated Java code provides the PSI element hierarchy under `dev.yarlson.yar.psi`.
- **Lexer/Parser** -- `YarLexerAdapter` wraps the generated JFlex lexer. `YarParserDefinition` wires the generated parser into the platform.
- **Highlighting** -- Token-level syntax highlighting via `YarSyntaxHighlighter`. Semantic highlighting via `YarAnnotator` (types, functions, parameters, fields, enum cases, pub modifier, call targets). `YarColorSettingsPage` exposes configurable color settings, including the newer structured concurrency keywords and channel syntax.
- **Editor features** -- Brace matching, line/block commenting, code folding, quote auto-closing, spellchecking in comments/strings, TODO indexing, and live templates.
- **Navigation** -- Go to symbol (`YarGoToSymbolContributor`), find usages (`YarFindUsagesProvider`), PSI-based reference resolution (`YarReference`) with local, file, and cross-package scope.
- **Code intelligence** -- Keyword, builtin, stdlib package, and local symbol completion. Documentation provider for hover info. Rename refactoring via `YarNamedElement` / `PsiNameIdentifierOwner`.
- **Formatting** -- `YarFormattingModelBuilder` with spacing rules for operators, keywords, braces, and delimiters. `YarBlock` handles indentation within braces.
- **Structure view** -- Tree-based structure view showing top-level and nested declarations.
- **External tooling** -- `YarExternalAnnotator` runs the `yar check` CLI tool and maps its output to in-editor error annotations.
- **Run configurations** -- `YarRunConfigurationType` with a run configuration editor for executing Yar programs. Gutter run icons via `YarRunLineMarkerProvider`. Context-based run config producer.

## Core Flow

1. User opens a `.yar` file.
2. The platform invokes `YarLexerAdapter` and `YarParser` to build a PSI tree.
3. `YarSyntaxHighlighter` applies token-level colors; `YarAnnotator` adds semantic highlighting.
4. `YarExternalAnnotator` invokes `yar check` in the background and maps errors to editor annotations.
5. Navigation, completion, references, and refactoring operate on the PSI tree.
6. Run configurations execute `yar run` (or other commands) via `YarCommandLineState`.

## System State

- No persistent services or application-level state.
- Run configuration options (`packagePath`, `yarPath`, `command`) are persisted via standard `RunConfigurationBase` options mechanism.
- No custom settings panel or credential storage.

## Capabilities

- Syntax and semantic highlighting with configurable colors
- Error recovery in parser
- Brace matching, code folding, line/block commenting
- Auto-closing quotes
- Spellchecking in comments and strings
- TODO/FIXME indexing
- Live templates
- Structure view
- Go to definition, find usages, go to symbol
- Cross-package go-to-definition for qualified names, struct literals, and dot-accessed symbols
- Rename refactoring (symbol rename across file)
- Keyword, builtin type/function, and local symbol completion
- Stdlib package name completion for imports
- Concurrency syntax support for `taskgroup`, `spawn`, `chan[T]`, and channel builtins
- Documentation on hover
- Code formatting (spacing and indentation)
- External `yar check` integration with in-editor error display
- Run configurations with gutter run icons
- Context-based run configuration producer

## Tech Stack

- Kotlin 2.1.10 (JVM target 21, JDK 25 toolchain)
- IntelliJ Platform SDK (Community 2024.3.1.1, sinceBuild 243)
- IntelliJ Platform Gradle Plugin 2.13.1
- Grammar-Kit 2023.3.0.3 (BNF parser generator)
- JFlex (lexer generator, via Grammar-Kit)
- JUnit 4 (test framework) + IntelliJ Platform test framework
