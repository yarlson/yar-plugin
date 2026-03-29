package dev.yarlson.yar.editor

import com.intellij.psi.PsiElement
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy
import com.intellij.spellchecker.tokenizer.Tokenizer
import dev.yarlson.yar.psi.YarTypes

class YarSpellcheckingStrategy : SpellcheckingStrategy() {

    override fun getTokenizer(element: PsiElement): Tokenizer<*> {
        val type = element.node.elementType
        return when (type) {
            YarTypes.LINE_COMMENT -> TEXT_TOKENIZER
            YarTypes.STRING_LITERAL -> TEXT_TOKENIZER
            else -> EMPTY_TOKENIZER
        }
    }
}
