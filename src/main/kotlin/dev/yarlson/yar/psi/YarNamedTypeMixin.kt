package dev.yarlson.yar.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReference
import dev.yarlson.yar.references.YarReference

abstract class YarNamedTypeMixin(node: ASTNode) : ASTWrapperPsiElement(node) {

    override fun getReference(): PsiReference? {
        val qualifiedName = node.findChildByType(YarTypes.QUALIFIED_NAME) ?: return null
        val identifiers = qualifiedName.getChildren(null)
            .filter { it.elementType == YarTypes.IDENTIFIER }

        if (identifiers.size >= 2) {
            // Qualified type: pkg.Type -- reference points to "Type" for cross-package resolution
            val file = containingFile as? YarFile ?: return null
            val pkgName = identifiers.first().text
            if (!YarReference.hasImport(file, pkgName)) return null
            val typeIdent = identifiers.last()
            val offset = typeIdent.startOffset - node.startOffset
            return YarReference(this, TextRange(offset, offset + typeIdent.textLength))
        }

        // Unqualified type: Type -- reference points to the single identifier
        val ident = identifiers.firstOrNull() ?: return null
        val offset = ident.startOffset - node.startOffset
        return YarReference(this, TextRange(offset, offset + ident.textLength))
    }
}
