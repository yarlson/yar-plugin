package dev.yarlson.yar.lexer

import com.intellij.lexer.FlexAdapter

class YarLexerAdapter : FlexAdapter(YarLexer(null))
