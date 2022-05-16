package org.wavescale.sourcesync.action

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.ex.ComboBoxAction
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.ui.IconManager
import org.wavescale.sourcesync.SourcesyncBundle
import org.wavescale.sourcesync.factory.ConfigConnectionFactory
import org.wavescale.sourcesync.factory.ConnectionConfig
import org.wavescale.sourcesync.ui.ConnectionConfigurationDialog
import java.awt.Component
import java.awt.event.MouseEvent
import javax.swing.JComponent

class ConnectionConfigurationsComboBoxAction : ComboBoxAction() {
    override fun createPopupActionGroup(button: JComponent?): DefaultActionGroup {
        val allActionsGroup = DefaultActionGroup()
        allActionsGroup.add(getEditConnectionConfigurationsAction())
        allActionsGroup.addSeparator(SourcesyncBundle.message("sourcesyncConfigurations"))

        ConfigConnectionFactory.getInstance().connectionNames.forEach { allActionsGroup.add(SourceSyncConfigAction(it)) }

        return allActionsGroup
    }

    override fun createComboBoxButton(presentation: Presentation): ComboBoxButton {
        return ConnectionConfigurationsComboBox(presentation)
    }

    private fun getEditConnectionConfigurationsAction(): AnAction {
        return ActionManager.getInstance().getAction("actionSourceSyncMenu")
    }

    private fun performWhenButton(src: Component, place: String) {
        val manager = ActionManager.getInstance()
        manager.tryToExecute(
            manager.getAction("actionSourceSyncMenu"),
            MouseEvent(src, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, 0, 0, 0, false, 0),
            src, place, true
        )
    }

    override fun update(e: AnActionEvent) {
        val projectName = e.getData(CommonDataKeys.PROJECT)?.name
        val associationFor = ConnectionConfig.getInstance().getAssociationFor(projectName)
        if (associationFor.isNullOrBlank()) {
            e.presentation.apply {
                isEnabled = true
                text = SourcesyncBundle.message("sourcesyncAddConfigurations")
                icon = null
            }
        } else {
            e.presentation.apply {
                isEnabled = true
                text = ConnectionConfig.getInstance().getAssociationFor(projectName)
                icon = IconManager.getInstance().getIcon("sourcesync.svg", ConnectionConfigurationsComboBox::class.java)
            }
        }
    }


    inner class ConnectionConfigurationsComboBox(presentation: Presentation) : ComboBoxButton(presentation) {
        override fun doShiftClick() {
            val context = DataManager.getInstance().getDataContext(this)
            val project = CommonDataKeys.PROJECT.getData(context)
            if (project != null) {
                ConnectionConfigurationDialog(project).show()
            }
            super.doShiftClick()
        }
    }

    internal class SourceSyncConfigAction(private val configuration: String) : DumbAwareAction() {

        init {
            val presentation = templatePresentation
            presentation.setText(configuration, false)
            presentation.icon = IconManager.getInstance().getIcon("sourcesync.svg", SourceSyncConfigAction::class.java)
        }

        override fun actionPerformed(e: AnActionEvent) {
            val presentation = templatePresentation
            val projectName = PlatformDataKeys.PROJECT.getData(e.dataContext)!!.name
            presentation.setText(configuration, false)
            associateProjectWithConnection(projectName)
        }

        private fun associateProjectWithConnection(projectName: String) {
            ConnectionConfig.getInstance().associateProjectWithConnection(projectName, configuration)
            ConnectionConfig.getInstance().saveModuleAssociatedConn()
        }
    }
}