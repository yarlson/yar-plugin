package dev.yarlson.yar.editor

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler
import dev.yarlson.yar.psi.YarTypes

class YarQuoteHandler : SimpleTokenSetQuoteHandler(YarTypes.STRING_LITERAL, YarTypes.CHAR_LITERAL)
