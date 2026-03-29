package dev.yarlson.yar.run

import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.runners.ExecutionEnvironment
import dev.yarlson.yar.external.YarExternalAnnotator

class YarCommandLineState(
    private val config: YarRunConfiguration,
    environment: ExecutionEnvironment,
) : CommandLineState(environment) {

    override fun startProcess(): ProcessHandler {
        val yarPath = config.yarPath.ifBlank {
            YarExternalAnnotator.findYarExecutable()
                ?: throw IllegalStateException("Cannot find yar executable. Set the path in the run configuration.")
        }

        val commandLine = GeneralCommandLine(yarPath, config.command, config.packagePath)
            .withWorkDirectory(config.packagePath)
            .withCharset(Charsets.UTF_8)

        return ProcessHandlerFactory.getInstance().createColoredProcessHandler(commandLine)
    }
}
