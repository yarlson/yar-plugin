# Practices

## Grammar and Code Generation

- The BNF grammar (`src/main/grammars/Yar.bnf`) and JFlex lexer (`src/main/grammars/Yar.flex`) are the single source of truth for language syntax.
- Generated Java code goes to `src/main/gen/` and is excluded from version-controlled source sets (cleaned on `gradle clean`).
- PSI element classes use Grammar-Kit's `mixin` and `implements` attributes to wire in `YarNamedElement` / `YarNamedElementImpl` for declarations that need naming/rename support.

## PSI and References

- Reference resolution follows a three-tier chain: local scope (walking up through blocks, functions, for-loops, match arms, closures, error handlers), file-level top-level declarations, then cross-package resolution via imported packages.
- Cross-package resolution matches the import's package name against `package` declarations in all project `.yar` files, then resolves the symbol within matching files.
- References are provided through two mechanisms: PSI mixins (`YarIdentExprMixin`, `YarDotAccessMixin`, `YarStructLiteralExprMixin`, `YarNamedTypeMixin`) that override `getReference()` on composite elements, and `YarReferenceContributor` that creates references for leaf `IDENTIFIER` tokens.
- References are not created for declaration names themselves, identifiers in package declarations, or field names in struct literal field initializers.
- Dot-accessed identifiers and non-first identifiers in qualified names only get references when they represent cross-package access (the prefix matches an imported package name).
- Top-level declarations (functions, structs, interfaces, enums) serve as resolution targets at both file and package scope.

## Highlighting Strategy

- Token-level highlighting (keywords, literals, comments, operators) is handled by `YarSyntaxHighlighter`.
- Semantic highlighting is layered on top via `YarAnnotator`, which annotates type names, function names, function calls, parameters, fields, enum cases, pub modifiers, and error literals.
- Function call highlighting processes all `CallArgs` children in a `PostfixExpr`, supporting chained call highlighting (e.g., `a.b().c()`).
- Qualified type names (e.g., `pkg.Type`) highlight only the last identifier as a type name.
- All semantic annotations use `HighlightSeverity.INFORMATION` with `TextAttributesKey` mappings to standard IntelliJ defaults.

## Completion Strategy

- Three completion providers run for all positions in Yar files: keywords, builtins (types, functions, stdlib packages), and local symbols.
- Local symbol completion walks the same scope chain as reference resolution (enclosing blocks, function params, receivers, closure params, top-level declarations).

## External Tooling Integration

- The external annotator discovers the `yar` executable via `YAR_PATH` env var, common install paths, or `which yar`.
- It runs `yar check <directory>` with a 10-second timeout and parses `file:line:col: message` output format.
- Errors are matched to the current file by filename suffix comparison.

## Run Configurations

- Run configurations store `packagePath`, `yarPath`, and `command` as persistent options.
- `YarRunLineMarkerProvider` adds gutter run icons on `main` function declarations.
- `YarRunConfigurationProducer` creates run configurations from context (right-click on a file or main function).

## Extension Registration

- All extensions are registered in `plugin.xml` with no programmatic registration.
- The plugin depends only on `com.intellij.modules.platform` and `com.intellij.modules.lang`.
