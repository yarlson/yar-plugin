package dev.yarlson.yar.highlighting

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import dev.yarlson.yar.psi.*

class YarAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element) {
            is YarStructDecl -> highlightDeclName(element.identifier, TYPE_NAME, holder)
            is YarInterfaceDecl -> highlightDeclName(element.identifier, TYPE_NAME, holder)
            is YarEnumDecl -> highlightDeclName(element.identifier, TYPE_NAME, holder)
            is YarFunctionDecl -> highlightFunctionDecl(element, holder)
            is YarEnumCase -> highlightDeclName(element.identifier, ENUM_CASE, holder)
            is YarParam -> highlightDeclName(element.identifier, PARAMETER, holder)
            is YarStructField -> highlightDeclName(element.identifier, FIELD, holder)
            is YarInterfaceMethod -> highlightDeclName(element.identifier, FUNCTION_NAME, holder)
            is YarReceiver -> highlightDeclName(element.identifier, PARAMETER, holder)
            is YarErrorLiteralExpr -> highlightErrorLiteral(element, holder)
            is YarPubModifier -> highlight(element, PUB_MODIFIER, holder)
            is YarNamedType -> highlightNamedType(element, holder)
            is YarDotAccess -> highlightDeclName(element.identifier, FIELD, holder)
            is YarCallArgs -> highlightCallTarget(element, holder)
        }
    }

    private fun highlightFunctionDecl(decl: YarFunctionDecl, holder: AnnotationHolder) {
        highlightDeclName(decl.identifier, FUNCTION_NAME, holder)
    }

    private fun highlightErrorLiteral(element: YarErrorLiteralExpr, holder: AnnotationHolder) {
        highlight(element, ERROR_LITERAL, holder)
    }

    private fun highlightNamedType(element: YarNamedType, holder: AnnotationHolder) {
        val qualifiedName = element.qualifiedName
        // Find all IDENTIFIER tokens under the qualified name
        val identifiers = qualifiedName.node.getChildren(null)
            .filter { it.elementType == YarTypes.IDENTIFIER }
        // Highlight the last identifier as type name (e.g., in "pkg.Type", highlight "Type")
        val typeName = identifiers.lastOrNull() ?: return
        highlight(typeName.psi, TYPE_NAME, holder)
    }

    private fun highlightCallTarget(callArgs: YarCallArgs, holder: AnnotationHolder) {
        // Walk up to postfixExpr, then find what precedes the call args
        val postfix = callArgs.parent as? YarPostfixExpr ?: return
        val children = postfix.children
        val callIndex = children.indexOf(callArgs)
        if (callIndex <= 0) return

        val target = children[callIndex - 1]
        when (target) {
            is YarDotAccess -> {
                val ident = target.identifier ?: return
                highlight(ident, FUNCTION_CALL, holder)
            }
            is YarPrimaryExpr -> {
                val identExpr = target.identExpr ?: return
                highlight(identExpr, FUNCTION_CALL, holder)
            }
        }
    }

    private fun highlightDeclName(ident: PsiElement?, key: TextAttributesKey, holder: AnnotationHolder) {
        if (ident == null) return
        highlight(ident, key, holder)
    }

    private fun highlight(element: PsiElement, key: TextAttributesKey, holder: AnnotationHolder) {
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(element)
            .textAttributes(key)
            .create()
    }

    companion object {
        val TYPE_NAME = createTextAttributesKey("YAR_TYPE_NAME", DefaultLanguageHighlighterColors.CLASS_NAME)
        val FUNCTION_NAME = createTextAttributesKey("YAR_FUNCTION_NAME", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION)
        val FUNCTION_CALL = createTextAttributesKey("YAR_FUNCTION_CALL", DefaultLanguageHighlighterColors.FUNCTION_CALL)
        val PARAMETER = createTextAttributesKey("YAR_PARAMETER", DefaultLanguageHighlighterColors.PARAMETER)
        val FIELD = createTextAttributesKey("YAR_FIELD", DefaultLanguageHighlighterColors.INSTANCE_FIELD)
        val ENUM_CASE = createTextAttributesKey("YAR_ENUM_CASE", DefaultLanguageHighlighterColors.STATIC_FIELD)
        val ERROR_LITERAL = createTextAttributesKey("YAR_ERROR_LITERAL", DefaultLanguageHighlighterColors.CONSTANT)
        val PUB_MODIFIER = createTextAttributesKey("YAR_PUB_MODIFIER", DefaultLanguageHighlighterColors.KEYWORD)
    }
}
