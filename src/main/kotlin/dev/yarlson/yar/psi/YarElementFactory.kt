package dev.yarlson.yar.psi

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import dev.yarlson.yar.YarFileType

object YarElementFactory {

    fun createIdentifier(project: Project, name: String): PsiElement {
        val file = createFile(project, "package $name\n")
        // The package decl's IDENTIFIER token is the name we want
        val packageDecl = file.firstChild
        return packageDecl.lastChild
    }

    fun createFile(project: Project, text: String): YarFile {
        return PsiFileFactory.getInstance(project)
            .createFileFromText("dummy.yar", YarFileType, text) as YarFile
    }
}
