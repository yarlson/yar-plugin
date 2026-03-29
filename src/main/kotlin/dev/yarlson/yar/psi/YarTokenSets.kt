package dev.yarlson.yar.psi

import com.intellij.psi.tree.TokenSet

object YarTokenSets {
    @JvmField
    val COMMENTS = TokenSet.create(YarTypes.LINE_COMMENT)

    @JvmField
    val WHITE_SPACES = TokenSet.create(com.intellij.psi.TokenType.WHITE_SPACE)

    @JvmField
    val STRINGS = TokenSet.create(YarTypes.STRING_LITERAL)

    @JvmField
    val KEYWORDS = TokenSet.create(
        YarTypes.PACKAGE_KW, YarTypes.IMPORT_KW, YarTypes.FN_KW, YarTypes.PUB_KW,
        YarTypes.VAR_KW, YarTypes.STRUCT_KW, YarTypes.INTERFACE_KW, YarTypes.ENUM_KW,
        YarTypes.OR_KW, YarTypes.IF_KW, YarTypes.ELSE_KW, YarTypes.FOR_KW,
        YarTypes.BREAK_KW, YarTypes.CONTINUE_KW, YarTypes.RETURN_KW,
        YarTypes.MATCH_KW, YarTypes.CASE_KW, YarTypes.TRUE_KW, YarTypes.FALSE_KW,
        YarTypes.NIL_KW, YarTypes.ERROR_KW, YarTypes.MAP_KW, YarTypes.LET_KW,
    )

    @JvmField
    val NUMBERS = TokenSet.create(YarTypes.INTEGER_LITERAL)
}
