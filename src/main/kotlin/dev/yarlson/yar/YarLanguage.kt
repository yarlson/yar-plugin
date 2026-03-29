package dev.yarlson.yar

import com.intellij.lang.Language

object YarLanguage : Language("Yar") {
    private fun readResolve(): Any = YarLanguage
}
