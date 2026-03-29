package dev.yarlson.yar.run

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField

class YarRunConfigurationEditor(private val project: Project) : SettingsEditor<YarRunConfiguration>() {

    private val packagePathField = TextFieldWithBrowseButton().apply {
        addBrowseFolderListener(
            "Select Package Directory",
            "Select the directory containing the Yar package to run",
            project,
            FileChooserDescriptorFactory.createSingleFolderDescriptor(),
        )
    }

    private val yarPathField = TextFieldWithBrowseButton().apply {
        addBrowseFolderListener(
            "Select Yar Executable",
            "Select the yar compiler executable",
            project,
            FileChooserDescriptorFactory.createSingleFileDescriptor(),
        )
    }

    private val commandField = JTextField("run")

    private val panel: JPanel = FormBuilder.createFormBuilder()
        .addLabeledComponent("Package path:", packagePathField)
        .addLabeledComponent("Yar executable:", yarPathField)
        .addLabeledComponent("Command (run/build):", commandField)
        .panel

    override fun resetEditorFrom(config: YarRunConfiguration) {
        packagePathField.text = config.packagePath
        yarPathField.text = config.yarPath
        commandField.text = config.command
    }

    override fun applyEditorTo(config: YarRunConfiguration) {
        config.packagePath = packagePathField.text
        config.yarPath = yarPathField.text
        config.command = commandField.text
    }

    override fun createEditor(): JComponent = panel
}
