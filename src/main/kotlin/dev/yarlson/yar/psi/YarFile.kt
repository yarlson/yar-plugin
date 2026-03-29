package dev.yarlson.yar.psi

import dev.yarlson.yar.YarFileType
import dev.yarlson.yar.YarLanguage
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider

class YarFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, YarLanguage) {
    override fun getFileType(): FileType = YarFileType
    override fun toString(): String = "Yar File"
}
