package dev.yarlson.yar.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReference
import com.intellij.psi.util.PsiTreeUtil
import dev.yarlson.yar.references.YarReference

abstract class YarDotAccessMixin(node: ASTNode) : ASTWrapperPsiElement(node) {

    override fun getReference(): PsiReference? {
        if (!isCrossPackageAccess()) return null
        val ident = node.findChildByType(YarTypes.IDENTIFIER)?.psi ?: return null
        val offset = ident.startOffsetInParent
        return YarReference(this, TextRange(offset, offset + ident.textLength))
    }

    private fun isCrossPackageAccess(): Boolean {
        val postfix = parent as? YarPostfixExpr ?: return false
        val children = postfix.children
        val dotIndex = children.indexOf(this)
        if (dotIndex <= 0) return false
        val prefix = children[dotIndex - 1]
        val prefixName = when (prefix) {
            is YarIdentExpr -> prefix.text
            is YarPrimaryExpr -> PsiTreeUtil.findChildOfType(prefix, YarIdentExpr::class.java)?.text
            else -> null
        } ?: return false
        val file = containingFile as? YarFile ?: return false
        return YarReference.hasImport(file, prefixName)
    }
}
