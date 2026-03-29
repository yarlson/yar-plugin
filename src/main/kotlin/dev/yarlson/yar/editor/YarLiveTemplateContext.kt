package dev.yarlson.yar.editor

import com.intellij.codeInsight.template.TemplateActionContext
import com.intellij.codeInsight.template.TemplateContextType
import dev.yarlson.yar.psi.YarFile

class YarLiveTemplateContext : TemplateContextType("YAR", "Yar") {

    override fun isInContext(context: TemplateActionContext): Boolean =
        context.file is YarFile
}
