package dev.yarlson.yar.structure

import com.intellij.ide.structureView.StructureViewModel.ElementInfoProvider
import com.intellij.ide.structureView.StructureViewModelBase
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.Sorter
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import dev.yarlson.yar.psi.*

class YarStructureViewModel(psiFile: PsiFile, editor: Editor?) :
    StructureViewModelBase(psiFile, editor, YarStructureViewElement(psiFile)),
    ElementInfoProvider {

    override fun getSorters(): Array<Sorter> = arrayOf(Sorter.ALPHA_SORTER)

    override fun getSuitableClasses(): Array<Class<*>> = arrayOf(
        YarFunctionDecl::class.java,
        YarStructDecl::class.java,
        YarInterfaceDecl::class.java,
        YarEnumDecl::class.java,
        YarStructField::class.java,
        YarInterfaceMethod::class.java,
        YarEnumCase::class.java,
    )

    override fun isAlwaysShowsPlus(element: StructureViewTreeElement): Boolean = false

    override fun isAlwaysLeaf(element: StructureViewTreeElement): Boolean {
        val value = element.value
        return value is YarStructField || value is YarInterfaceMethod || value is YarEnumCase
    }
}
