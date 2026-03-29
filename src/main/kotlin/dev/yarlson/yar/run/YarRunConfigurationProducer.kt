package dev.yarlson.yar.run

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import dev.yarlson.yar.psi.YarFile
import dev.yarlson.yar.psi.YarFunctionDecl

class YarRunConfigurationProducer : LazyRunConfigurationProducer<YarRunConfiguration>() {

    override fun getConfigurationFactory(): ConfigurationFactory =
        YarRunConfigurationType().configurationFactories[0]

    override fun isConfigurationFromContext(
        configuration: YarRunConfiguration,
        context: ConfigurationContext
    ): Boolean {
        val file = context.psiLocation?.containingFile as? YarFile ?: return false
        val dir = file.virtualFile?.parent?.path ?: return false
        return configuration.packagePath == dir
    }

    override fun setupConfigurationFromContext(
        configuration: YarRunConfiguration,
        context: ConfigurationContext,
        sourceElement: Ref<PsiElement>
    ): Boolean {
        val file = context.psiLocation?.containingFile as? YarFile ?: return false
        if (!hasMainFunction(file)) return false

        val dir = file.virtualFile?.parent?.path ?: return false
        configuration.packagePath = dir
        configuration.name = "yar run ${file.virtualFile.parent.name}"
        return true
    }

    private fun hasMainFunction(file: YarFile): Boolean {
        return file.children.any { child ->
            child is YarFunctionDecl && child.name == "main"
        }
    }
}
