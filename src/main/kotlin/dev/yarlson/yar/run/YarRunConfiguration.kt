package dev.yarlson.yar.run

import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project

class YarRunConfiguration(
    project: Project,
    factory: ConfigurationFactory,
    name: String,
) : RunConfigurationBase<YarRunConfigurationOptions>(project, factory, name) {

    override fun getOptions(): YarRunConfigurationOptions =
        super.getOptions() as YarRunConfigurationOptions

    var packagePath: String
        get() = options.packagePath
        set(value) { options.packagePath = value }

    var yarPath: String
        get() = options.yarPath
        set(value) { options.yarPath = value }

    var command: String
        get() = options.command
        set(value) { options.command = value }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> =
        YarRunConfigurationEditor(project)

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState =
        YarCommandLineState(this, environment)

    override fun checkConfiguration() {
        if (packagePath.isBlank()) {
            throw RuntimeConfigurationError("Package path is not specified")
        }
    }
}
