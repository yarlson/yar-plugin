package dev.yarlson.yar.editor

import com.intellij.lexer.Lexer
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.search.IndexPatternBuilder
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import dev.yarlson.yar.lexer.YarLexerAdapter
import dev.yarlson.yar.psi.YarFile
import dev.yarlson.yar.psi.YarTypes

class YarTodoIndexer : IndexPatternBuilder {

    override fun getIndexingLexer(file: PsiFile): Lexer? =
        if (file is YarFile) YarLexerAdapter() else null

    override fun getCommentTokenSet(file: PsiFile): TokenSet? =
        if (file is YarFile) TokenSet.create(YarTypes.LINE_COMMENT) else null

    override fun getCommentStartDelta(tokenType: IElementType): Int =
        if (tokenType == YarTypes.LINE_COMMENT) 2 else 0

    override fun getCommentEndDelta(tokenType: IElementType): Int = 0
}
