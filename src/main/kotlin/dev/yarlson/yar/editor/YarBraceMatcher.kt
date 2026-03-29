package dev.yarlson.yar.editor

import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import dev.yarlson.yar.psi.YarTypes

class YarBraceMatcher : PairedBraceMatcher {

    override fun getPairs(): Array<BracePair> = PAIRS

    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean = true

    override fun getCodeConstructStart(file: PsiFile, openingBraceOffset: Int): Int = openingBraceOffset

    companion object {
        private val PAIRS = arrayOf(
            BracePair(YarTypes.LBRACE, YarTypes.RBRACE, true),
            BracePair(YarTypes.LBRACKET, YarTypes.RBRACKET, false),
            BracePair(YarTypes.LPAREN, YarTypes.RPAREN, false),
        )
    }
}
