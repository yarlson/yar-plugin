package dev.yarlson.yar.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import dev.yarlson.yar.YarLanguage
import dev.yarlson.yar.lexer.YarLexerAdapter
import dev.yarlson.yar.psi.YarFile
import dev.yarlson.yar.psi.YarTokenSets
import dev.yarlson.yar.psi.YarTypes

class YarParserDefinition : ParserDefinition {

    override fun createLexer(project: Project): Lexer = YarLexerAdapter()

    override fun createParser(project: Project): PsiParser = YarParser()

    override fun getFileNodeType(): IFileElementType = FILE

    override fun getCommentTokens(): TokenSet = YarTokenSets.COMMENTS

    override fun getWhitespaceTokens(): TokenSet = YarTokenSets.WHITE_SPACES

    override fun getStringLiteralElements(): TokenSet = YarTokenSets.STRINGS

    override fun createElement(node: ASTNode): PsiElement = YarTypes.Factory.createElement(node)

    override fun createFile(viewProvider: FileViewProvider): PsiFile = YarFile(viewProvider)

    companion object {
        val FILE = IFileElementType(YarLanguage)
    }
}
