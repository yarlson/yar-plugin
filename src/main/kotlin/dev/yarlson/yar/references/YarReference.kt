package dev.yarlson.yar.references

import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import dev.yarlson.yar.psi.*

class YarReference(element: PsiElement, rangeInElement: TextRange) :
    PsiReferenceBase<PsiElement>(element, rangeInElement, true) {

    override fun resolve(): PsiElement? {
        val name = element.text
        return resolveInLocalScope(name)
            ?: resolveInFileScope(name)
    }

    override fun getVariants(): Array<Any> = emptyArray()

    private fun resolveInLocalScope(name: String): PsiElement? {
        // Walk up through enclosing blocks and functions to find local declarations
        var scope: PsiElement? = element.parent
        while (scope != null) {
            when (scope) {
                is YarBlock -> {
                    // Look for short declarations and var declarations before the reference
                    for (child in scope.children) {
                        if (child.textOffset >= element.textOffset) break
                        when (child) {
                            is YarStatement -> {
                                val decl = findDeclInStatement(child, name)
                                if (decl != null) return decl
                            }
                        }
                    }
                }
                is YarFunctionDecl -> {
                    // Check parameters
                    val params = scope.paramList?.paramList ?: emptyList()
                    for (param in params) {
                        if (param.name == name) return param
                    }
                    // Check receiver
                    val receiver = scope.receiver
                    if (receiver != null) {
                        val receiverIdent = receiver.node.findChildByType(YarTypes.IDENTIFIER)?.psi
                        if (receiverIdent?.text == name) return receiver
                    }
                }
                is YarForStmt -> {
                    // Check for-loop init
                    val forClause = scope.forClause
                    if (forClause != null) {
                        val forInit = forClause.forInit
                        if (forInit != null) {
                            val decl = findDeclInForInit(forInit, name)
                            if (decl != null) return decl
                        }
                    }
                }
                is YarHandleSuffix -> {
                    // Check error binding: or |err| { ... }
                    val errIdent = scope.node.findChildByType(YarTypes.IDENTIFIER)?.psi
                    if (errIdent?.text == name) return errIdent
                }
                is YarMatchArm -> {
                    // Check match arm binding: case Type.Case(v)
                    val identifiers = scope.node.getChildren(null)
                        .filter { it.elementType == YarTypes.IDENTIFIER }
                    // The binding variable is the last IDENTIFIER inside parens
                    if (identifiers.size >= 2) {
                        val binding = identifiers.last()
                        if (binding.psi.text == name) return binding.psi
                    }
                }
                is YarFunctionLiteralExpr -> {
                    // Check closure parameters
                    val params = scope.paramList?.paramList ?: emptyList()
                    for (param in params) {
                        if (param.name == name) return param
                    }
                }
            }
            scope = scope.parent
        }
        return null
    }

    private fun resolveInFileScope(name: String): PsiElement? {
        val file = element.containingFile as? YarFile ?: return null
        // Search top-level declarations in the file
        for (child in file.children) {
            when (child) {
                is YarFunctionDecl -> if (child.name == name) return child
                is YarStructDecl -> if (child.name == name) return child
                is YarInterfaceDecl -> if (child.name == name) return child
                is YarEnumDecl -> if (child.name == name) return child
            }
        }
        return null
    }

    private fun findDeclInStatement(stmt: PsiElement, name: String): PsiElement? {
        // Statement wrapper may contain the actual decl as child
        val inner = stmt.firstChild ?: stmt
        return when (inner) {
            is YarShortDeclStmt -> if (inner.name == name) inner else null
            is YarVarDeclStmt -> if (inner.name == name) inner else null
            else -> {
                // Check direct children for nested decls
                PsiTreeUtil.findChildOfType(stmt, YarShortDeclStmt::class.java)
                    ?.takeIf { it.name == name }
                    ?: PsiTreeUtil.findChildOfType(stmt, YarVarDeclStmt::class.java)
                        ?.takeIf { it.name == name }
            }
        }
    }

    private fun findDeclInForInit(forInit: YarForInit, name: String): PsiElement? {
        val shortDecl = PsiTreeUtil.findChildOfType(forInit, YarShortDeclStmt::class.java)
        if (shortDecl?.name == name) return shortDecl
        val varDecl = PsiTreeUtil.findChildOfType(forInit, YarVarDeclStmt::class.java)
        if (varDecl?.name == name) return varDecl
        return null
    }

    companion object {
        fun create(element: PsiElement): YarReference? {
            if (element.node.elementType != YarTypes.IDENTIFIER) return null
            // Don't create references for declaration names themselves
            val parent = element.parent ?: return null
            if (parent is YarNamedElement && parent.nameIdentifier == element) return null
            // Don't create references for identifiers in package declarations
            if (parent is YarPackageDecl) return null
            // Don't create references for dot-accessed field names
            if (parent is YarDotAccess) return null

            return YarReference(element, TextRange(0, element.textLength))
        }
    }
}
