package dev.yarlson.yar.references

import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import dev.yarlson.yar.YarFileType
import dev.yarlson.yar.psi.*

class YarReference(element: PsiElement, rangeInElement: TextRange) :
    PsiReferenceBase<PsiElement>(element, rangeInElement, true) {

    override fun resolve(): PsiElement? {
        val name = rangeInElement.substring(element.text)
        return resolveInLocalScope(name)
            ?: resolveInFileScope(name)
            ?: resolveInImportedPackage(name)
    }

    override fun getVariants(): Array<Any> = emptyArray()

    private fun resolveInLocalScope(name: String): PsiElement? {
        var scope: PsiElement? = element.parent
        while (scope != null) {
            when (scope) {
                is YarBlock -> {
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
                    val params = scope.paramList?.paramList ?: emptyList()
                    for (param in params) {
                        if (param.name == name) return param
                    }
                    val receiver = scope.receiver
                    if (receiver != null) {
                        val receiverIdent = receiver.node.findChildByType(YarTypes.IDENTIFIER)?.psi
                        if (receiverIdent?.text == name) return receiver
                    }
                }
                is YarForStmt -> {
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
                    val errIdent = scope.node.findChildByType(YarTypes.IDENTIFIER)?.psi
                    if (errIdent?.text == name) return errIdent
                }
                is YarMatchArm -> {
                    val identifiers = scope.node.getChildren(null)
                        .filter { it.elementType == YarTypes.IDENTIFIER }
                    if (identifiers.size >= 2) {
                        val binding = identifiers.last()
                        if (binding.psi.text == name) return binding.psi
                    }
                }
                is YarFunctionLiteralExpr -> {
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
        return findDeclInFile(file, name)
    }

    /**
     * Resolve a symbol in an imported package.
     *
     * Handles two patterns:
     * - Dot access after package identifier: `pkg.symbol` where element is the leaf IDENTIFIER
     * - Qualified name in struct literal: `pkg.Type{...}` where element is `structLiteralExpr`
     */
    private fun resolveInImportedPackage(name: String): PsiElement? {
        val file = element.containingFile as? YarFile ?: return null
        val packageName = findPackagePrefix(file) ?: return null
        return findDeclInPackage(file, packageName, name)
    }

    /**
     * Determine the package prefix for cross-package resolution based on context.
     */
    private fun findPackagePrefix(file: YarFile): String? {
        // Case 1: YarDotAccess element itself (from mixin) -- e.g., ".greet" in "lib.greet()"
        if (element is YarDotAccess) {
            val postfix = element.parent as? YarPostfixExpr ?: return null
            val children = postfix.children
            val dotIndex = children.indexOf(element)
            if (dotIndex <= 0) return null
            val prefix = children[dotIndex - 1]
            val prefixName = extractSimpleIdentName(prefix) ?: return null
            if (hasImport(file, prefixName)) return prefixName
            return null
        }

        // Case 2: leaf IDENTIFIER inside YarDotAccess (from reference contributor)
        val parent = element.parent
        if (parent is YarDotAccess) {
            val postfix = parent.parent as? YarPostfixExpr ?: return null
            val children = postfix.children
            val dotIndex = children.indexOf(parent)
            if (dotIndex <= 0) return null
            val prefix = children[dotIndex - 1]
            val prefixName = extractSimpleIdentName(prefix) ?: return null
            if (hasImport(file, prefixName)) return prefixName
            return null
        }

        // Case 3: composite element with qualifiedName child (structLiteralExpr, namedType)
        val qualifiedNameNode = when (element) {
            is YarStructLiteralExpr -> element.node.findChildByType(YarTypes.QUALIFIED_NAME)
            is YarNamedType -> element.node.findChildByType(YarTypes.QUALIFIED_NAME)
            else -> null
        }
        if (qualifiedNameNode != null) {
            val identifiers = qualifiedNameNode.getChildren(null)
                .filter { it.elementType == YarTypes.IDENTIFIER }
            if (identifiers.size >= 2) {
                val pkgName = identifiers.first().text
                if (hasImport(file, pkgName)) return pkgName
            }
            return null
        }

        // Case 4: leaf IDENTIFIER inside YarQualifiedName (from reference contributor)
        if (parent is YarQualifiedName) {
            val firstIdent = parent.node.findChildByType(YarTypes.IDENTIFIER)?.text ?: return null
            if (firstIdent != element.text && hasImport(file, firstIdent)) return firstIdent
            return null
        }

        return null
    }

    private fun extractSimpleIdentName(element: PsiElement): String? {
        if (element is YarIdentExpr) return element.text
        val primary = element as? YarPrimaryExpr ?: return null
        val identExpr = PsiTreeUtil.findChildOfType(primary, YarIdentExpr::class.java)
        return identExpr?.text
    }

    private fun findDeclInStatement(stmt: PsiElement, name: String): PsiElement? {
        val inner = stmt.firstChild ?: stmt
        return when (inner) {
            is YarShortDeclStmt -> if (inner.name == name) inner else null
            is YarVarDeclStmt -> if (inner.name == name) inner else null
            else -> {
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
            // Accept identExpr composite element (from mixin)
            if (element is YarIdentExpr) {
                return YarReference(element, TextRange(0, element.textLength))
            }

            // Accept structLiteralExpr composite element (from mixin)
            if (element is YarStructLiteralExpr) {
                return createForStructLiteral(element)
            }

            // Accept leaf IDENTIFIER tokens (from reference contributor)
            if (element.node.elementType != YarTypes.IDENTIFIER) return null
            val parent = element.parent ?: return null
            // Don't create references for declaration names themselves
            if (parent is YarNamedElement && parent.nameIdentifier == element) return null
            // Don't create references for identifiers in package declarations
            if (parent is YarPackageDecl) return null
            // Don't create references for field names in struct literal field inits
            if (parent is YarFieldInit && parent.firstChild == element) return null

            // Dot-accessed names: only allow for cross-package access (pkg.Symbol)
            if (parent is YarDotAccess) {
                return if (isCrossPackageDotAccess(element)) {
                    YarReference(element, TextRange(0, element.textLength))
                } else {
                    null
                }
            }

            // Non-first identifiers in qualified names: only for cross-package access
            if (parent is YarQualifiedName && element.prevSibling != null) {
                return if (isCrossPackageQualifiedAccess(element)) {
                    YarReference(element, TextRange(0, element.textLength))
                } else {
                    null
                }
            }

            return YarReference(element, TextRange(0, element.textLength))
        }

        private fun createForStructLiteral(element: YarStructLiteralExpr): YarReference? {
            val qualifiedName = element.qualifiedName
            val identifiers = qualifiedName.node.getChildren(null)
                .filter { it.elementType == YarTypes.IDENTIFIER }

            if (identifiers.size >= 2) {
                // Qualified: pkg.Type{...} -- reference points to "Type" (second identifier)
                val typeIdent = identifiers.last()
                val offset = typeIdent.startOffset - element.node.startOffset
                return YarReference(element, TextRange(offset, offset + typeIdent.textLength))
            }

            // Unqualified: Type{...} -- reference points to "Type" (first identifier)
            val firstIdent = identifiers.firstOrNull() ?: return null
            val offset = firstIdent.startOffset - element.node.startOffset
            return YarReference(element, TextRange(offset, offset + firstIdent.textLength))
        }

        /**
         * Check if an IDENTIFIER in a dot access is a cross-package symbol reference.
         * E.g., "greet" in "lib.greet()" where "lib" is an imported package.
         */
        private fun isCrossPackageDotAccess(ident: PsiElement): Boolean {
            val dotAccess = ident.parent as? YarDotAccess ?: return false
            val postfix = dotAccess.parent as? YarPostfixExpr ?: return false
            val children = postfix.children
            val dotIndex = children.indexOf(dotAccess)
            if (dotIndex <= 0) return false
            val prefix = children[dotIndex - 1]
            val prefixName = when (prefix) {
                is YarIdentExpr -> prefix.text
                is YarPrimaryExpr -> PsiTreeUtil.findChildOfType(prefix, YarIdentExpr::class.java)?.text
                else -> null
            } ?: return false
            val file = ident.containingFile as? YarFile ?: return false
            return hasImport(file, prefixName)
        }

        /**
         * Check if a non-first IDENTIFIER in a qualified name is a cross-package reference.
         * E.g., "User" in "lib.User{...}" where "lib" is an imported package.
         */
        private fun isCrossPackageQualifiedAccess(ident: PsiElement): Boolean {
            val qualifiedName = ident.parent as? YarQualifiedName ?: return false
            val firstIdent = qualifiedName.node.findChildByType(YarTypes.IDENTIFIER)?.text ?: return false
            if (firstIdent == ident.text) return false
            val file = ident.containingFile as? YarFile ?: return false
            return hasImport(file, firstIdent)
        }

        fun hasImport(file: YarFile, packageName: String): Boolean {
            for (child in file.children) {
                if (child is YarImportDecl) {
                    val importPath = child.node.findChildByType(YarTypes.STRING_LITERAL)?.text
                        ?.removeSurrounding("\"") ?: continue
                    val pkgName = importPath.substringAfterLast('/')
                    if (pkgName == packageName) return true
                }
            }
            return false
        }

        fun findDeclInFile(file: YarFile, name: String): PsiElement? {
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

        fun findDeclInPackage(contextFile: YarFile, packageName: String, symbolName: String): PsiElement? {
            val project = contextFile.project
            val psiManager = PsiManager.getInstance(project)
            val scope = GlobalSearchScope.projectScope(project)
            for (vf in FileTypeIndex.getFiles(YarFileType, scope)) {
                if (vf == contextFile.virtualFile) continue
                val psiFile = psiManager.findFile(vf) as? YarFile ?: continue
                val pkgDecl = PsiTreeUtil.findChildOfType(psiFile, YarPackageDecl::class.java)
                val pkgName = pkgDecl?.node?.findChildByType(YarTypes.IDENTIFIER)?.text ?: continue
                if (pkgName != packageName) continue
                val decl = findDeclInFile(psiFile, symbolName)
                if (decl != null) return decl
            }
            return null
        }
    }
}
