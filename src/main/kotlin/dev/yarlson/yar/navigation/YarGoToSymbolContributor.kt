package dev.yarlson.yar.navigation

import com.intellij.navigation.ChooseByNameContributorEx
import com.intellij.navigation.NavigationItem
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.Processor
import com.intellij.util.indexing.FindSymbolParameters
import com.intellij.util.indexing.IdFilter
import dev.yarlson.yar.YarFileType
import dev.yarlson.yar.psi.*

class YarGoToSymbolContributor : ChooseByNameContributorEx {

    override fun processNames(processor: Processor<in String>, scope: GlobalSearchScope, filter: IdFilter?) {
        for (file in FileTypeIndex.getFiles(YarFileType, scope)) {
            val psiFile = com.intellij.psi.PsiManager.getInstance(scope.project!!).findFile(file) as? YarFile
                ?: continue
            collectNames(psiFile, processor)
        }
    }

    override fun processElementsWithName(
        name: String,
        processor: Processor<in NavigationItem>,
        parameters: FindSymbolParameters
    ) {
        for (file in FileTypeIndex.getFiles(YarFileType, parameters.searchScope)) {
            val psiFile = com.intellij.psi.PsiManager.getInstance(parameters.project).findFile(file) as? YarFile
                ?: continue
            collectElements(psiFile, name, processor)
        }
    }

    private fun collectNames(file: YarFile, processor: Processor<in String>) {
        for (child in file.children) {
            when (child) {
                is YarNamedElement -> child.name?.let { processor.process(it) }
            }
        }
    }

    private fun collectElements(file: YarFile, name: String, processor: Processor<in NavigationItem>) {
        for (child in file.children) {
            if (child is YarNamedElement && child.name == name && child is NavigationItem) {
                if (!processor.process(child)) return
            }
        }
    }
}
