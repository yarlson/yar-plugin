# Terminology

- **Yar** -- the programming language this plugin supports
- **PSI** -- Program Structure Interface; IntelliJ's tree representation of parsed source code
- **YarNamedElement** -- PSI interface for Yar elements that have a name and support rename (extends `PsiNameIdentifierOwner`)
- **Grammar-Kit** -- JetBrains tool that generates a PEG parser and PSI classes from a BNF grammar file
- **JFlex** -- lexer generator used via Grammar-Kit to produce the token-level lexer
- **External annotator** -- IntelliJ extension point that runs an external process (here `yar check`) and maps its output to editor annotations
- **Run line marker** -- gutter icon that appears next to runnable `main` functions, providing one-click execution
- **Errorable type** -- Yar type prefixed with `!`, representing a value that may be an error
- **Propagate operator** -- the `?` postfix operator in Yar that propagates errors to the caller
- **Handle expression** -- `or |err| { ... }` suffix for handling errorable values
- **Short declaration** -- `:=` syntax in Yar for declaring and initializing a variable
- **pub modifier** -- visibility modifier marking a declaration as public
- **Receiver** -- method receiver parameter syntax `(name Type)` preceding a function name, similar to Go
- **Qualified name** -- dot-separated identifier path (e.g., `pkg.Type`)
