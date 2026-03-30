package dev.yarlson.yar.highlighting

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import dev.yarlson.yar.lexer.YarLexerAdapter
import dev.yarlson.yar.psi.YarTokenSets
import dev.yarlson.yar.psi.YarTypes

class YarSyntaxHighlighter : SyntaxHighlighterBase() {

    override fun getHighlightingLexer(): Lexer = YarLexerAdapter()

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
        return when {
            tokenType in YarTokenSets.KEYWORDS -> KEYWORD_KEYS
            tokenType == YarTypes.INTEGER_LITERAL -> NUMBER_KEYS
            tokenType == YarTypes.STRING_LITERAL || tokenType == YarTypes.CHAR_LITERAL -> STRING_KEYS
            tokenType == YarTypes.LINE_COMMENT -> COMMENT_KEYS
            tokenType == YarTypes.LPAREN || tokenType == YarTypes.RPAREN -> PAREN_KEYS
            tokenType == YarTypes.LBRACE || tokenType == YarTypes.RBRACE -> BRACE_KEYS
            tokenType == YarTypes.LBRACKET || tokenType == YarTypes.RBRACKET -> BRACKET_KEYS
            tokenType == YarTypes.COMMA -> COMMA_KEYS
            tokenType == YarTypes.DOT -> DOT_KEYS
            tokenType == YarTypes.SEMICOLON -> SEMICOLON_KEYS
            isOperator(tokenType) -> OPERATOR_KEYS
            tokenType == TokenType.BAD_CHARACTER -> BAD_CHAR_KEYS
            else -> EMPTY_KEYS
        }
    }

    private fun isOperator(tokenType: IElementType): Boolean = tokenType in OPERATOR_TOKENS

    companion object {
        val KEYWORD = createTextAttributesKey("YAR_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
        val NUMBER = createTextAttributesKey("YAR_NUMBER", DefaultLanguageHighlighterColors.NUMBER)
        val STRING = createTextAttributesKey("YAR_STRING", DefaultLanguageHighlighterColors.STRING)
        val COMMENT = createTextAttributesKey("YAR_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
        val OPERATOR = createTextAttributesKey("YAR_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN)
        val PARENTHESES = createTextAttributesKey("YAR_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES)
        val BRACES = createTextAttributesKey("YAR_BRACES", DefaultLanguageHighlighterColors.BRACES)
        val BRACKETS = createTextAttributesKey("YAR_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS)
        val COMMA = createTextAttributesKey("YAR_COMMA", DefaultLanguageHighlighterColors.COMMA)
        val DOT = createTextAttributesKey("YAR_DOT", DefaultLanguageHighlighterColors.DOT)
        val SEMICOLON = createTextAttributesKey("YAR_SEMICOLON", DefaultLanguageHighlighterColors.SEMICOLON)
        val BAD_CHARACTER = createTextAttributesKey("YAR_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER)

        private val KEYWORD_KEYS = arrayOf(KEYWORD)
        private val NUMBER_KEYS = arrayOf(NUMBER)
        private val STRING_KEYS = arrayOf(STRING)
        private val COMMENT_KEYS = arrayOf(COMMENT)
        private val OPERATOR_KEYS = arrayOf(OPERATOR)
        private val PAREN_KEYS = arrayOf(PARENTHESES)
        private val BRACE_KEYS = arrayOf(BRACES)
        private val BRACKET_KEYS = arrayOf(BRACKETS)
        private val COMMA_KEYS = arrayOf(COMMA)
        private val DOT_KEYS = arrayOf(DOT)
        private val SEMICOLON_KEYS = arrayOf(SEMICOLON)
        private val BAD_CHAR_KEYS = arrayOf(BAD_CHARACTER)
        private val EMPTY_KEYS = emptyArray<TextAttributesKey>()

        private val OPERATOR_TOKENS = setOf(
            YarTypes.EQ, YarTypes.COLON, YarTypes.COLON_ASSIGN,
            YarTypes.PLUS_EQ, YarTypes.MINUS_EQ, YarTypes.STAR_EQ, YarTypes.SLASH_EQ, YarTypes.PERCENT_EQ,
            YarTypes.BANG, YarTypes.QUESTION,
            YarTypes.AMP, YarTypes.PIPE,
            YarTypes.PLUS, YarTypes.MINUS, YarTypes.STAR, YarTypes.SLASH, YarTypes.PERCENT,
            YarTypes.LT, YarTypes.GT, YarTypes.LT_EQ, YarTypes.GT_EQ,
            YarTypes.EQ_EQ, YarTypes.BANG_EQ,
            YarTypes.AMP_AMP, YarTypes.PIPE_PIPE,
        )
    }
}
