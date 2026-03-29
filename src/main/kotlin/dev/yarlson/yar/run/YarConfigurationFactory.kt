package dev.yarlson.yar.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.project.Project

class YarConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {

    override fun getId(): String = "YarConfigurationFactory"

    override fun createTemplateConfiguration(project: Project): RunConfiguration =
        YarRunConfiguration(project, this, "Yar")

    override fun getOptionsClass(): Class<out BaseState> = YarRunConfigurationOptions::class.java
}
