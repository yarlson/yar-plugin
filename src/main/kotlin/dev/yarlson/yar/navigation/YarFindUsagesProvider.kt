package dev.yarlson.yar.navigation

import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet
import dev.yarlson.yar.lexer.YarLexerAdapter
import dev.yarlson.yar.psi.*

class YarFindUsagesProvider : FindUsagesProvider {

    override fun getWordsScanner(): WordsScanner {
        return DefaultWordsScanner(
            YarLexerAdapter(),
            TokenSet.create(YarTypes.IDENTIFIER),
            YarTokenSets.COMMENTS,
            YarTokenSets.STRINGS,
        )
    }

    override fun canFindUsagesFor(psiElement: PsiElement): Boolean =
        psiElement is YarNamedElement

    override fun getHelpId(psiElement: PsiElement): String? = null

    override fun getType(element: PsiElement): String = when (element) {
        is YarFunctionDecl -> "function"
        is YarStructDecl -> "struct"
        is YarInterfaceDecl -> "interface"
        is YarEnumDecl -> "enum"
        is YarEnumCase -> "enum case"
        is YarStructField -> "field"
        is YarInterfaceMethod -> "method"
        is YarParam -> "parameter"
        is YarShortDeclStmt -> "variable"
        is YarVarDeclStmt -> "variable"
        else -> "element"
    }

    override fun getDescriptiveName(element: PsiElement): String =
        (element as? YarNamedElement)?.name ?: ""

    override fun getNodeText(element: PsiElement, useFullName: Boolean): String =
        (element as? YarNamedElement)?.name ?: ""
}
