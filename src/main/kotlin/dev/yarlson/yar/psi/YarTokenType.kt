package dev.yarlson.yar.psi

import dev.yarlson.yar.YarLanguage
import com.intellij.psi.tree.IElementType

class YarTokenType(debugName: String) : IElementType(debugName, YarLanguage) {
    override fun toString(): String = "YarTokenType.${super.toString()}"
}
