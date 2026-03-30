package dev.yarlson.yar.formatter

import com.intellij.formatting.*
import com.intellij.psi.codeStyle.CodeStyleSettings
import dev.yarlson.yar.YarLanguage
import dev.yarlson.yar.psi.YarTypes

class YarFormattingModelBuilder : FormattingModelBuilder {

    override fun createModel(formattingContext: FormattingContext): FormattingModel {
        val settings = formattingContext.codeStyleSettings
        val spacingBuilder = createSpacingBuilder(settings)
        val block = YarBlock(
            formattingContext.node,
            null,
            Indent.getNoneIndent(),
            spacingBuilder,
        )
        return FormattingModelProvider.createFormattingModelForPsiFile(
            formattingContext.containingFile,
            block,
            settings,
        )
    }

    companion object {
        fun createSpacingBuilder(settings: CodeStyleSettings): SpacingBuilder {
            val common = settings.getCommonSettings(YarLanguage)

            return SpacingBuilder(settings, YarLanguage)
                // After commas
                .after(YarTypes.COMMA).spaceIf(true)
                .before(YarTypes.COMMA).spaceIf(false)

                // After semicolons (in for loops)
                .after(YarTypes.SEMICOLON).spaceIf(true)
                .before(YarTypes.SEMICOLON).spaceIf(false)

                // Around assignment operators
                .around(YarTypes.EQ).spaceIf(true)
                .around(YarTypes.COLON_ASSIGN).spaceIf(true)
                .around(YarTypes.PLUS_EQ).spaceIf(true)
                .around(YarTypes.MINUS_EQ).spaceIf(true)
                .around(YarTypes.STAR_EQ).spaceIf(true)
                .around(YarTypes.SLASH_EQ).spaceIf(true)
                .around(YarTypes.PERCENT_EQ).spaceIf(true)

                // Around binary operators
                .around(YarTypes.PLUS).spaceIf(true)
                .around(YarTypes.MINUS).spaceIf(true)
                .around(YarTypes.STAR).spaceIf(true)
                .around(YarTypes.SLASH).spaceIf(true)
                .around(YarTypes.PERCENT).spaceIf(true)
                .around(YarTypes.EQ_EQ).spaceIf(true)
                .around(YarTypes.BANG_EQ).spaceIf(true)
                .around(YarTypes.LT).spaceIf(true)
                .around(YarTypes.GT).spaceIf(true)
                .around(YarTypes.LT_EQ).spaceIf(true)
                .around(YarTypes.GT_EQ).spaceIf(true)
                .around(YarTypes.AMP_AMP).spaceIf(true)
                .around(YarTypes.PIPE_PIPE).spaceIf(true)

                // Colon in field init and map entry
                .before(YarTypes.COLON).spaceIf(false)
                .after(YarTypes.COLON).spaceIf(true)

                // Braces
                .before(YarTypes.LBRACE).spaceIf(true)
                .after(YarTypes.LBRACE).lineBreakOrForceSpace(true, true)
                .before(YarTypes.RBRACE).lineBreakOrForceSpace(true, true)

                // No space inside parens
                .after(YarTypes.LPAREN).spaceIf(false)
                .before(YarTypes.RPAREN).spaceIf(false)

                // No space inside brackets
                .after(YarTypes.LBRACKET).spaceIf(false)
                .before(YarTypes.RBRACKET).spaceIf(false)

                // After keywords that precede expressions/blocks
                .after(YarTypes.IF_KW).spaceIf(true)
                .after(YarTypes.ELSE_KW).spaceIf(true)
                .after(YarTypes.FOR_KW).spaceIf(true)
                .after(YarTypes.RETURN_KW).spaceIf(true)
                .after(YarTypes.MATCH_KW).spaceIf(true)
                .after(YarTypes.CASE_KW).spaceIf(true)
                .after(YarTypes.VAR_KW).spaceIf(true)
                .after(YarTypes.PACKAGE_KW).spaceIf(true)
                .after(YarTypes.IMPORT_KW).spaceIf(true)
                .after(YarTypes.PUB_KW).spaceIf(true)
                .after(YarTypes.STRUCT_KW).spaceIf(true)
                .after(YarTypes.INTERFACE_KW).spaceIf(true)
                .after(YarTypes.ENUM_KW).spaceIf(true)
                .after(YarTypes.FN_KW).spaceIf(true)
                .after(YarTypes.OR_KW).spaceIf(true)
                .after(YarTypes.MAP_KW).spaceIf(false)

                // No space before/after dot
                .before(YarTypes.DOT).spaceIf(false)
                .after(YarTypes.DOT).spaceIf(false)

                // No space before question mark (propagate)
                .before(YarTypes.QUESTION).spaceIf(false)

                // Pipe in error handler: or |err|
                .after(YarTypes.PIPE).spaceIf(false)
                .before(YarTypes.PIPE).spaceIf(false)
        }
    }
}
