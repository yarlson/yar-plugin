package dev.yarlson.yar.editor

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import dev.yarlson.yar.psi.*

class YarFoldingBuilder : FoldingBuilderEx(), DumbAware {

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val descriptors = mutableListOf<FoldingDescriptor>()

        PsiTreeUtil.processElements(root) { element ->
            when (element) {
                is YarFunctionDecl -> foldBraces(element.block, descriptors)
                is YarStructDecl -> foldBracedBody(element, descriptors)
                is YarInterfaceDecl -> foldBracedBody(element, descriptors)
                is YarEnumDecl -> foldBracedBody(element, descriptors)
                is YarBlock -> {
                    // Only fold blocks that are not already folded as part of a declaration
                    val parent = element.parent
                    if (parent !is YarFunctionDecl) {
                        foldBraces(element, descriptors)
                    }
                }
            }
            true
        }

        // Fold import groups (multiple consecutive imports)
        foldImportGroup(root, descriptors)

        return descriptors.toTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode): String = "{...}"

    override fun isCollapsedByDefault(node: ASTNode): Boolean = false

    private fun foldBraces(block: YarBlock?, descriptors: MutableList<FoldingDescriptor>) {
        if (block == null) return
        val lbrace = block.node.findChildByType(YarTypes.LBRACE) ?: return
        val rbrace = block.node.findChildByType(YarTypes.RBRACE) ?: return
        if (rbrace.startOffset - lbrace.startOffset > 1) {
            descriptors.add(FoldingDescriptor(block.node, TextRange(lbrace.startOffset, rbrace.startOffset + 1)))
        }
    }

    private fun foldBracedBody(element: PsiElement, descriptors: MutableList<FoldingDescriptor>) {
        val lbrace = element.node.findChildByType(YarTypes.LBRACE) ?: return
        val rbrace = element.node.findChildByType(YarTypes.RBRACE) ?: return
        if (rbrace.startOffset - lbrace.startOffset > 1) {
            descriptors.add(FoldingDescriptor(element.node, TextRange(lbrace.startOffset, rbrace.startOffset + 1)))
        }
    }

    private fun foldImportGroup(root: PsiElement, descriptors: MutableList<FoldingDescriptor>) {
        val file = root as? YarFile ?: return
        val imports = PsiTreeUtil.getChildrenOfType(file, YarImportDecl::class.java) ?: return
        if (imports.size < 2) return
        val first = imports.first()
        val last = imports.last()
        descriptors.add(FoldingDescriptor(
            first.node,
            TextRange(first.textRange.startOffset, last.textRange.endOffset),
        ))
    }
}
