# Plugin Structure

## Package Layout

All hand-written Kotlin source lives under `dev.yarlson.yar` in `src/main/kotlin/`:

- `dev.yarlson.yar` -- core types: `YarLanguage`, `YarFileType`, `YarIcons`
- `dev.yarlson.yar.lexer` -- `YarLexerAdapter` wrapping the generated JFlex lexer
- `dev.yarlson.yar.parser` -- `YarParserDefinition` wiring the generated parser
- `dev.yarlson.yar.psi` -- PSI infrastructure: `YarFile`, `YarElementType`, `YarTokenType`, `YarTokenSets`, `YarNamedElement`, `YarNamedElementImpl`, `YarElementFactory`, and PSI mixins (`YarIdentExprMixin`, `YarDotAccessMixin`, `YarStructLiteralExprMixin`, `YarNamedTypeMixin`) that provide `getReference()` on composite elements
- `dev.yarlson.yar.highlighting` -- `YarSyntaxHighlighter`, `YarSyntaxHighlighterFactory`, `YarAnnotator`, `YarColorSettingsPage`
- `dev.yarlson.yar.editor` -- `YarBraceMatcher`, `YarCommenter`, `YarFoldingBuilder`, `YarQuoteHandler`, `YarSpellcheckingStrategy`, `YarTodoIndexer`, `YarLiveTemplateContext`
- `dev.yarlson.yar.navigation` -- `YarFindUsagesProvider`, `YarGoToSymbolContributor`
- `dev.yarlson.yar.references` -- `YarReference`, `YarReferenceContributor`
- `dev.yarlson.yar.completion` -- `YarCompletionContributor` (keyword, builtin, local symbol providers)
- `dev.yarlson.yar.documentation` -- `YarDocumentationProvider`
- `dev.yarlson.yar.formatter` -- `YarFormattingModelBuilder`, `YarBlock`
- `dev.yarlson.yar.structure` -- `YarStructureViewFactory`, `YarStructureViewModel`, `YarStructureViewElement`
- `dev.yarlson.yar.external` -- `YarExternalAnnotator` (runs `yar check`)
- `dev.yarlson.yar.run` -- run configuration types, factory, editor, options, command line state, producer, and line marker provider

## Generated Code

- `src/main/gen/dev/yarlson/yar/lexer/YarLexer.java` -- generated JFlex lexer
- `src/main/gen/dev/yarlson/yar/parser/YarParser.java` -- generated PEG parser
- `src/main/gen/dev/yarlson/yar/psi/` -- generated PSI interfaces and `impl/` classes
- `src/main/gen/dev/yarlson/yar/psi/YarTypes.java` -- token and element type constants
- `src/main/gen/dev/yarlson/yar/psi/YarVisitor.java` -- generated PSI visitor

## Grammars

- `src/main/grammars/Yar.bnf` -- Grammar-Kit BNF defining Yar syntax and PSI structure
- `src/main/grammars/Yar.flex` -- JFlex lexer specification

## Resources

- `src/main/resources/META-INF/plugin.xml` -- plugin descriptor with all extension registrations
- `src/main/resources/icons/yar.svg` -- file type icon
- `src/main/resources/liveTemplates/Yar.xml` -- live template definitions

## Tests

- `src/test/kotlin/dev/yarlson/yar/parser/YarParserTest.kt` -- parser tests that compare parsed PSI trees against expected `.txt` fixtures
- `src/test/kotlin/dev/yarlson/yar/references/YarReferenceTest.kt` -- reference resolution tests
- `src/test/resources/testData/parser/` -- paired `.yar` / `.txt` test fixtures for parser validation (Closures, Divide, Enums, Generics, Interfaces, Maps, Methods, Pointers, StructsAndLoops)

## Build

- `build.gradle.kts` -- Gradle build script using IntelliJ Platform Gradle Plugin
- `gradle.properties` -- plugin metadata, platform version, and build settings
- `settings.gradle.kts` -- project name (`yar-plugin`)
- Requires JDK 25 toolchain (`jvmToolchain(25)`), JVM target 21
- Lexer and parser generation tasks run before Kotlin/Java compilation
- `gradle clean` deletes `src/main/gen/`
