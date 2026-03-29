package dev.yarlson.yar.run

import com.intellij.execution.lineMarker.ExecutorAction
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement
import dev.yarlson.yar.psi.YarFunctionDecl
import dev.yarlson.yar.psi.YarTypes

class YarRunLineMarkerProvider : RunLineMarkerContributor() {

    override fun getInfo(element: PsiElement): Info? {
        // Only trigger on the IDENTIFIER leaf token inside a function declaration
        if (element.node.elementType != YarTypes.IDENTIFIER) return null
        val parent = element.parent as? YarFunctionDecl ?: return null
        if (parent.nameIdentifier != element) return null
        if (parent.name != "main") return null

        return Info(
            AllIcons.RunConfigurations.TestState.Run,
            ExecutorAction.getActions(0),
        ) { "Run '${parent.name}'" }
    }
}
