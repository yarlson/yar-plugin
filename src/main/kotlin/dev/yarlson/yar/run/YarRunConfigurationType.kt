package dev.yarlson.yar.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import dev.yarlson.yar.YarIcons
import javax.swing.Icon

class YarRunConfigurationType : ConfigurationType {

    override fun getDisplayName(): String = "Yar"

    override fun getConfigurationTypeDescription(): String = "Yar application run configuration"

    override fun getIcon(): Icon = YarIcons.FILE

    override fun getId(): String = "YarRunConfiguration"

    override fun getConfigurationFactories(): Array<ConfigurationFactory> =
        arrayOf(YarConfigurationFactory(this))
}
