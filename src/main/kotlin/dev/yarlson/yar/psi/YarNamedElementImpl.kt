package dev.yarlson.yar.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

abstract class YarNamedElementImpl(node: ASTNode) : ASTWrapperPsiElement(node), YarNamedElement {

    override fun getNameIdentifier(): PsiElement? =
        node.findChildByType(YarTypes.IDENTIFIER)?.psi

    override fun getName(): String? = nameIdentifier?.text

    override fun setName(name: String): PsiElement {
        val identNode = node.findChildByType(YarTypes.IDENTIFIER) ?: return this
        val newIdent = YarElementFactory.createIdentifier(project, name)
        node.replaceChild(identNode, newIdent.node)
        return this
    }

    override fun getTextOffset(): Int = nameIdentifier?.textOffset ?: super.getTextOffset()
}
