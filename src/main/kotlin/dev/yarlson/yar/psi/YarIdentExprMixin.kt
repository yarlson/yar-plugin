package dev.yarlson.yar.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReference
import dev.yarlson.yar.references.YarReference

abstract class YarIdentExprMixin(node: ASTNode) : ASTWrapperPsiElement(node) {

    override fun getReference(): PsiReference? {
        return YarReference.create(this)
    }
}
