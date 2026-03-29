package dev.yarlson.yar.run

import com.intellij.execution.configurations.RunConfigurationOptions
import com.intellij.openapi.components.StoredProperty

class YarRunConfigurationOptions : RunConfigurationOptions() {

    private val myPackagePath: StoredProperty<String?> =
        string("").provideDelegate(this, "packagePath")

    private val myYarPath: StoredProperty<String?> =
        string("").provideDelegate(this, "yarPath")

    private val myCommand: StoredProperty<String?> =
        string("run").provideDelegate(this, "command")

    var packagePath: String
        get() = myPackagePath.getValue(this) ?: ""
        set(value) { myPackagePath.setValue(this, value) }

    var yarPath: String
        get() = myYarPath.getValue(this) ?: ""
        set(value) { myYarPath.setValue(this, value) }

    var command: String
        get() = myCommand.getValue(this) ?: "run"
        set(value) { myCommand.setValue(this, value) }
}
