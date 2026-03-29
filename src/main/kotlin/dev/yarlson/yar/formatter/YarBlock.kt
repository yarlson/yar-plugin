package dev.yarlson.yar.formatter

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.psi.TokenType
import com.intellij.psi.formatter.common.AbstractBlock
import com.intellij.psi.tree.IElementType
import dev.yarlson.yar.psi.YarTypes

class YarBlock(
    node: ASTNode,
    wrap: Wrap?,
    private val indent: Indent,
    private val spacingBuilder: SpacingBuilder,
) : AbstractBlock(node, wrap, null) {

    override fun buildChildren(): List<Block> {
        val blocks = mutableListOf<Block>()
        var child = myNode.firstChildNode
        while (child != null) {
            if (child.elementType != TokenType.WHITE_SPACE && child.textRange.length > 0) {
                val childIndent = computeChildIndent(child)
                blocks.add(YarBlock(child, null, childIndent, spacingBuilder))
            }
            child = child.treeNext
        }
        return blocks
    }

    override fun getIndent(): Indent = indent

    override fun getSpacing(child1: Block?, child2: Block): Spacing? =
        spacingBuilder.getSpacing(this, child1, child2)

    override fun isLeaf(): Boolean = myNode.firstChildNode == null

    override fun getChildAttributes(newChildIndex: Int): ChildAttributes {
        val type = myNode.elementType
        return if (type in INDENTED_PARENTS) {
            ChildAttributes(Indent.getNormalIndent(), null)
        } else {
            ChildAttributes(Indent.getNoneIndent(), null)
        }
    }

    private fun computeChildIndent(child: ASTNode): Indent {
        val parentType = myNode.elementType
        val childType = child.elementType

        // Braces themselves are not indented
        if (childType == YarTypes.LBRACE || childType == YarTypes.RBRACE) {
            return Indent.getNoneIndent()
        }

        // Content inside braced constructs is indented
        if (parentType in INDENTED_PARENTS && childType != YarTypes.LBRACE && childType != YarTypes.RBRACE) {
            return Indent.getNormalIndent()
        }

        return Indent.getNoneIndent()
    }

    companion object {
        private val INDENTED_PARENTS: Set<IElementType> = setOf(
            YarTypes.BLOCK,
            YarTypes.STRUCT_DECL,
            YarTypes.INTERFACE_DECL,
            YarTypes.ENUM_DECL,
            YarTypes.MATCH_STMT,
            YarTypes.ENUM_CASE_FIELDS,
            YarTypes.STRUCT_LITERAL_EXPR,
        )
    }
}
