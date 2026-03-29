package dev.yarlson.yar.documentation

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.psi.PsiElement
import dev.yarlson.yar.psi.*

class YarDocumentationProvider : AbstractDocumentationProvider() {

    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        if (element == null) return null

        return when (element) {
            is YarFunctionDecl -> generateFunctionDoc(element)
            is YarStructDecl -> generateStructDoc(element)
            is YarInterfaceDecl -> generateInterfaceDoc(element)
            is YarEnumDecl -> generateEnumDoc(element)
            is YarParam -> generateParamDoc(element)
            is YarStructField -> generateFieldDoc(element)
            is YarShortDeclStmt -> generateLocalDoc(element)
            is YarVarDeclStmt -> generateVarDoc(element)
            else -> generateBuiltinDoc(originalElement)
        }
    }

    override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
        return when (element) {
            is YarFunctionDecl -> buildFunctionSignature(element)
            is YarStructDecl -> "struct ${element.name ?: "?"}"
            is YarInterfaceDecl -> "interface ${element.name ?: "?"}"
            is YarEnumDecl -> "enum ${element.name ?: "?"}"
            is YarParam -> "${element.name ?: "?"} ${element.typeExpr?.text ?: ""}"
            is YarStructField -> "${element.name ?: "?"} ${element.typeExpr?.text ?: ""}"
            is YarShortDeclStmt -> "${element.name ?: "?"} := ..."
            is YarVarDeclStmt -> "var ${element.name ?: "?"} ${element.typeExpr?.text ?: ""}"
            else -> null
        }
    }

    private fun generateFunctionDoc(decl: YarFunctionDecl): String {
        val sig = buildFunctionSignature(decl)
        val exported = if (decl.pubModifier != null) "<i>exported</i>" else "<i>package-private</i>"
        return "<pre>$sig</pre><p>$exported</p>"
    }

    private fun generateStructDoc(decl: YarStructDecl): String {
        val name = decl.name ?: "?"
        val typeParams = decl.typeParamList?.text ?: ""
        val fields = decl.structFieldList.joinToString("\n") { f ->
            "    ${f.name ?: "?"} ${f.typeExpr?.text ?: ""}"
        }
        val exported = if (decl.pubModifier != null) "<i>exported</i>" else "<i>package-private</i>"
        return "<pre>struct $name$typeParams {\n$fields\n}</pre><p>$exported</p>"
    }

    private fun generateInterfaceDoc(decl: YarInterfaceDecl): String {
        val name = decl.name ?: "?"
        val methods = decl.interfaceMethodList.joinToString("\n") { m ->
            val params = m.paramList?.text ?: ""
            val ret = m.typeExpr?.text ?: ""
            val err = if (m.errorableMarker != null) "!" else ""
            "    ${m.name ?: "?"}($params) $err$ret"
        }
        return "<pre>interface $name {\n$methods\n}</pre>"
    }

    private fun generateEnumDoc(decl: YarEnumDecl): String {
        val name = decl.name ?: "?"
        val cases = decl.enumCaseList.joinToString("\n") { c ->
            val fields = c.enumCaseFields?.text ?: ""
            "    ${c.name ?: "?"} $fields"
        }
        return "<pre>enum $name {\n$cases\n}</pre>"
    }

    private fun generateParamDoc(param: YarParam): String {
        return "<pre>${param.name ?: "?"} ${param.typeExpr?.text ?: ""}</pre><p><i>parameter</i></p>"
    }

    private fun generateFieldDoc(field: YarStructField): String {
        return "<pre>${field.name ?: "?"} ${field.typeExpr?.text ?: ""}</pre><p><i>field</i></p>"
    }

    private fun generateLocalDoc(decl: YarShortDeclStmt): String {
        return "<pre>${decl.name ?: "?"} := ...</pre><p><i>local variable</i></p>"
    }

    private fun generateVarDoc(decl: YarVarDeclStmt): String {
        return "<pre>var ${decl.name ?: "?"} ${decl.typeExpr?.text ?: ""}</pre><p><i>local variable</i></p>"
    }

    private fun generateBuiltinDoc(element: PsiElement?): String? {
        val name = element?.text ?: return null
        return BUILTIN_DOCS[name]
    }

    private fun buildFunctionSignature(decl: YarFunctionDecl): String {
        val pub = if (decl.pubModifier != null) "pub " else ""
        val receiver = decl.receiver?.let { r ->
            "(${r.node.findChildByType(YarTypes.IDENTIFIER)?.text ?: ""} ${r.typeExpr?.text ?: ""}) "
        } ?: ""
        val name = decl.name ?: "?"
        val typeParams = decl.typeParamList?.text ?: ""
        val params = decl.paramList?.text ?: ""
        val err = if (decl.errorableMarker != null) "!" else ""
        val ret = decl.typeExpr?.text ?: ""
        return "${pub}fn $receiver$name$typeParams($params) $err$ret"
    }

    companion object {
        private val BUILTIN_DOCS = mapOf(
            "print" to "<pre>fn print(msg str) void</pre><p>Prints a string to stdout.</p>",
            "panic" to "<pre>fn panic(msg str) noreturn</pre><p>Terminates the program with an error message.</p>",
            "len" to "<pre>fn len(collection) i32</pre><p>Returns the length of an array, slice, map, or string.</p>",
            "append" to "<pre>fn append(slice []T, elem T) []T</pre><p>Appends an element to a slice and returns the new slice.</p>",
            "has" to "<pre>fn has(m map[K]V, key K) bool</pre><p>Returns true if the map contains the given key.</p>",
            "delete" to "<pre>fn delete(m map[K]V, key K) void</pre><p>Removes the entry with the given key from the map.</p>",
            "keys" to "<pre>fn keys(m map[K]V) []K</pre><p>Returns a slice of all keys in the map.</p>",
            "to_str" to "<pre>fn to_str(value) str</pre><p>Converts i32, i64, bool, str, or error to its string representation.</p>",
            // Stdlib packages
            "strings" to "<pre>package strings</pre><p>String manipulation functions: contains, has_prefix, has_suffix, index, count, repeat, replace, trim_left, trim_right, join, from_byte, parse_i64.</p>",
            "utf8" to "<pre>package utf8</pre><p>UTF-8 decoding and rune classification: decode, width, is_letter, is_digit, is_space.</p>",
            "conv" to "<pre>package conv</pre><p>Type conversions: itoa, itoa64, to_i64, to_i32, byte_to_str.</p>",
            "sort" to "<pre>package sort</pre><p>In-place sorting: strings, i32s, i64s.</p>",
            "path" to "<pre>package path</pre><p>Path manipulation: clean, join, dir, base, ext.</p>",
            "fs" to "<pre>package fs</pre><p>Filesystem operations: read_file, write_file, read_dir, stat, mkdir_all, remove_all, temp_dir.</p>",
            "process" to "<pre>package process</pre><p>Process execution: args, run, run_inherit.</p>",
            "env" to "<pre>package env</pre><p>Environment variable access: lookup.</p>",
            "stdio" to "<pre>package stdio</pre><p>Stderr output: eprint.</p>",
            "testing" to "<pre>package testing</pre><p>Test framework: T, equal, not_equal, is_true, is_false, fail.</p>",
            // Builtin types
            "bool" to "<pre>type bool</pre><p>Boolean type. Values: <code>true</code>, <code>false</code>.</p>",
            "i32" to "<pre>type i32</pre><p>32-bit signed integer.</p>",
            "i64" to "<pre>type i64</pre><p>64-bit signed integer.</p>",
            "str" to "<pre>type str</pre><p>Immutable, heap-backed string. Byte-indexed.</p>",
            "void" to "<pre>type void</pre><p>Empty return type for functions that return nothing.</p>",
            "noreturn" to "<pre>type noreturn</pre><p>Return type for functions that never return (e.g., panic).</p>",
            "error" to "<pre>type error</pre><p>Error type. Constructed as <code>error.Name</code>.</p>",
        )
    }
}
