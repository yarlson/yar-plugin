package dev.yarlson.yar

import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

object YarFileType : LanguageFileType(YarLanguage) {
    override fun getName(): String = "Yar"
    override fun getDescription(): String = "Yar language file"
    override fun getDefaultExtension(): String = "yar"
    override fun getIcon(): Icon = YarIcons.FILE
}
