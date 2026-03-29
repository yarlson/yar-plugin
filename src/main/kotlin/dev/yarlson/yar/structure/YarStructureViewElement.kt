package dev.yarlson.yar.structure

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import dev.yarlson.yar.YarIcons
import dev.yarlson.yar.psi.*

class YarStructureViewElement(private val element: PsiElement) :
    StructureViewTreeElement, SortableTreeElement {

    override fun getValue(): Any = element

    override fun navigate(requestFocus: Boolean) {
        (element as? NavigatablePsiElement)?.navigate(requestFocus)
    }

    override fun canNavigate(): Boolean = (element as? NavigatablePsiElement)?.canNavigate() == true

    override fun canNavigateToSource(): Boolean = (element as? NavigatablePsiElement)?.canNavigateToSource() == true

    override fun getAlphaSortKey(): String = getPresentation().presentableText ?: ""

    override fun getPresentation(): ItemPresentation = when (element) {
        is YarFile -> PresentationData(element.name, null, YarIcons.FILE, null)
        is YarStructDecl -> PresentationData(
            element.identifier?.text ?: "struct",
            buildTypeParams(element.typeParamList),
            YarIcons.FILE, null
        )
        is YarInterfaceDecl -> PresentationData(
            element.identifier?.text ?: "interface",
            null, YarIcons.FILE, null
        )
        is YarEnumDecl -> PresentationData(
            element.identifier?.text ?: "enum",
            null, YarIcons.FILE, null
        )
        is YarFunctionDecl -> PresentationData(
            buildFunctionPresentation(element),
            null, YarIcons.FILE, null
        )
        is YarStructField -> PresentationData(
            "${element.identifier?.text ?: "?"} ${element.typeExpr?.text ?: ""}",
            null, YarIcons.FILE, null
        )
        is YarInterfaceMethod -> PresentationData(
            "${element.identifier?.text ?: "?"}(${element.paramList?.text ?: ""})",
            null, YarIcons.FILE, null
        )
        is YarEnumCase -> PresentationData(
            element.identifier.text,
            null, YarIcons.FILE, null
        )
        else -> PresentationData(element.text, null, null, null)
    }

    override fun getChildren(): Array<TreeElement> {
        val children = mutableListOf<TreeElement>()
        when (element) {
            is YarFile -> {
                PsiTreeUtil.getChildrenOfType(element, YarStructDecl::class.java)
                    ?.forEach { children.add(YarStructureViewElement(it)) }
                PsiTreeUtil.getChildrenOfType(element, YarInterfaceDecl::class.java)
                    ?.forEach { children.add(YarStructureViewElement(it)) }
                PsiTreeUtil.getChildrenOfType(element, YarEnumDecl::class.java)
                    ?.forEach { children.add(YarStructureViewElement(it)) }
                PsiTreeUtil.getChildrenOfType(element, YarFunctionDecl::class.java)
                    ?.forEach { children.add(YarStructureViewElement(it)) }
            }
            is YarStructDecl -> {
                element.structFieldList.forEach { children.add(YarStructureViewElement(it)) }
            }
            is YarInterfaceDecl -> {
                element.interfaceMethodList.forEach { children.add(YarStructureViewElement(it)) }
            }
            is YarEnumDecl -> {
                element.enumCaseList.forEach { children.add(YarStructureViewElement(it)) }
            }
        }
        return children.toTypedArray()
    }

    private fun buildFunctionPresentation(decl: YarFunctionDecl): String {
        val name = decl.identifier?.text ?: "fn"
        val receiver = decl.receiver?.let { r ->
            val rType = r.typeExpr?.text ?: ""
            "($rType) "
        } ?: ""
        val params = decl.paramList?.text ?: ""
        val ret = decl.typeExpr?.text ?: ""
        val errorable = if (decl.errorableMarker != null) "!" else ""
        return "${receiver}${name}($params) $errorable$ret".trim()
    }

    private fun buildTypeParams(typeParamList: YarTypeParamList?): String? {
        if (typeParamList == null) return null
        return typeParamList.text
    }
}
