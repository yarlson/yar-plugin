package dev.yarlson.yar.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import dev.yarlson.yar.YarIcons
import dev.yarlson.yar.YarLanguage
import dev.yarlson.yar.psi.*

class YarCompletionContributor : CompletionContributor() {

    init {
        val yarPattern = PlatformPatterns.psiElement().withLanguage(YarLanguage)

        extend(CompletionType.BASIC, yarPattern, KeywordCompletionProvider())
        extend(CompletionType.BASIC, yarPattern, BuiltinCompletionProvider())
        extend(CompletionType.BASIC, yarPattern, LocalSymbolCompletionProvider())
    }

    private class KeywordCompletionProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet
        ) {
            for (kw in KEYWORDS) {
                result.addElement(
                    LookupElementBuilder.create(kw)
                        .bold()
                        .withTypeText("keyword")
                )
            }
        }

        companion object {
            private val KEYWORDS = listOf(
                "package", "import", "fn", "pub", "var", "struct", "interface", "enum",
                "if", "else", "for", "break", "continue", "return", "match", "case",
                "taskgroup", "spawn", "true", "false", "nil", "error", "map", "chan", "or",
            )
        }
    }

    private class BuiltinCompletionProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet
        ) {
            // Builtin types
            for ((name, desc) in BUILTIN_TYPES) {
                result.addElement(
                    LookupElementBuilder.create(name)
                        .withTypeText(desc)
                        .withIcon(YarIcons.FILE)
                        .withBoldness(true)
                )
            }

            // Builtin functions
            for ((name, sig) in BUILTIN_FUNCTIONS) {
                result.addElement(
                    LookupElementBuilder.create(name)
                        .withTailText(sig, true)
                        .withTypeText("builtin")
                        .withIcon(YarIcons.FILE)
                        .withInsertHandler { ctx, _ ->
                            ctx.document.insertString(ctx.tailOffset, "()")
                            ctx.editor.caretModel.moveToOffset(ctx.tailOffset - 1)
                        }
                )
            }

            // Stdlib package names (for import completion)
            for ((pkg, desc) in STDLIB_PACKAGES) {
                result.addElement(
                    LookupElementBuilder.create(pkg)
                        .withTypeText(desc)
                        .withIcon(YarIcons.FILE)
                )
            }
        }

        companion object {
            private val BUILTIN_TYPES = listOf(
                "bool" to "boolean type",
                "i32" to "32-bit integer",
                "i64" to "64-bit integer",
                "str" to "string type",
                "void" to "empty return type",
                "noreturn" to "never returns",
                "error" to "error type",
            )

            private val BUILTIN_FUNCTIONS = listOf(
                "print" to "(str) void",
                "panic" to "(str) noreturn",
                "len" to "(collection) i32",
                "append" to "([]T, T) []T",
                "has" to "(map[K]V, K) bool",
                "delete" to "(map[K]V, K) void",
                "keys" to "(map[K]V) []K",
                "to_str" to "(i32 | i64 | bool | str | error) str",
                "sb_new" to "() i64",
                "sb_write" to "(i64, str) void",
                "sb_string" to "(i64) str",
                "chan_new" to "[T](i32) chan[T]",
                "chan_send" to "(chan[T], T) !void",
                "chan_recv" to "(chan[T]) !T",
                "chan_close" to "(chan[T]) void",
            )

            private val STDLIB_PACKAGES = listOf(
                "strings" to "string manipulation",
                "utf8" to "UTF-8 decoding",
                "conv" to "type conversions",
                "sort" to "sorting",
                "path" to "path manipulation",
                "fs" to "filesystem operations",
                "process" to "process execution",
                "env" to "environment variables",
                "stdio" to "stderr output",
                "net" to "TCP networking",
                "testing" to "test framework",
            )
        }
    }

    private class LocalSymbolCompletionProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet
        ) {
            val position = parameters.position
            val file = position.containingFile as? YarFile ?: return

            // Collect top-level declarations from the file
            for (child in file.children) {
                when (child) {
                    is YarFunctionDecl -> child.name?.let { name ->
                        result.addElement(
                            LookupElementBuilder.create(name)
                                .withTypeText("function")
                                .withIcon(YarIcons.FILE)
                                .withTailText(buildParamHint(child), true)
                        )
                    }
                    is YarStructDecl -> child.name?.let { name ->
                        result.addElement(
                            LookupElementBuilder.create(name)
                                .withTypeText("struct")
                                .withIcon(YarIcons.FILE)
                        )
                    }
                    is YarInterfaceDecl -> child.name?.let { name ->
                        result.addElement(
                            LookupElementBuilder.create(name)
                                .withTypeText("interface")
                                .withIcon(YarIcons.FILE)
                        )
                    }
                    is YarEnumDecl -> child.name?.let { name ->
                        result.addElement(
                            LookupElementBuilder.create(name)
                                .withTypeText("enum")
                                .withIcon(YarIcons.FILE)
                        )
                    }
                }
            }

            // Collect local declarations from enclosing scopes
            collectLocalDeclarations(position, result)
        }

        private fun collectLocalDeclarations(position: PsiElement, result: CompletionResultSet) {
            var scope: PsiElement? = position.parent
            while (scope != null) {
                when (scope) {
                    is YarBlock -> {
                        for (child in scope.children) {
                            if (child.textOffset >= position.textOffset) break
                            addDeclFromStatement(child, result)
                        }
                    }
                    is YarFunctionDecl -> {
                        scope.paramList?.paramList?.forEach { param ->
                            param.name?.let { name ->
                                result.addElement(
                                    LookupElementBuilder.create(name)
                                        .withTypeText(param.typeExpr?.text ?: "")
                                        .withIcon(YarIcons.FILE)
                                )
                            }
                        }
                        scope.receiver?.let { recv ->
                            val recvName = recv.node.findChildByType(YarTypes.IDENTIFIER)?.text
                            recvName?.let { name ->
                                result.addElement(
                                    LookupElementBuilder.create(name)
                                        .withTypeText(recv.typeExpr?.text ?: "receiver")
                                        .withIcon(YarIcons.FILE)
                                )
                            }
                        }
                    }
                    is YarFunctionLiteralExpr -> {
                        scope.paramList?.paramList?.forEach { param ->
                            param.name?.let { name ->
                                result.addElement(
                                    LookupElementBuilder.create(name)
                                        .withTypeText(param.typeExpr?.text ?: "")
                                        .withIcon(YarIcons.FILE)
                                )
                            }
                        }
                    }
                }
                scope = scope.parent
            }
        }

        private fun addDeclFromStatement(element: PsiElement, result: CompletionResultSet) {
            val shortDecl = PsiTreeUtil.findChildOfType(element, YarShortDeclStmt::class.java)
            shortDecl?.name?.let { name ->
                result.addElement(
                    LookupElementBuilder.create(name)
                        .withTypeText("variable")
                        .withIcon(YarIcons.FILE)
                )
            }
            val varDecl = PsiTreeUtil.findChildOfType(element, YarVarDeclStmt::class.java)
            varDecl?.name?.let { name ->
                result.addElement(
                    LookupElementBuilder.create(name)
                        .withTypeText(varDecl.typeExpr?.text ?: "variable")
                        .withIcon(YarIcons.FILE)
                )
            }
        }

        private fun buildParamHint(decl: YarFunctionDecl): String {
            val params = decl.paramList?.text ?: ""
            return "($params)"
        }
    }
}
