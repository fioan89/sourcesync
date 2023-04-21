package org.wavescale.sourcesync.action

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.ComboBoxAction
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAwareAction
import org.wavescale.sourcesync.SourceSyncIcons
import org.wavescale.sourcesync.SourcesyncBundle
import org.wavescale.sourcesync.factory.ConfigConnectionFactory
import org.wavescale.sourcesync.factory.ConnectionConfig
import org.wavescale.sourcesync.services.SyncStatusService
import org.wavescale.sourcesync.ui.ConnectionConfigurationDialog
import java.awt.Graphics
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.SwingUtilities

class OldUIConnectionConfigurationSelector : ComboBoxAction() {
    private val syncStatusService = service<SyncStatusService>()

    override fun createPopupActionGroup(button: JComponent, dataContext: DataContext): DefaultActionGroup {
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

    override fun update(e: AnActionEvent) {
        val projectName = e.getData(CommonDataKeys.PROJECT)?.name
        val associationFor = ConnectionConfig.getInstance().getAssociationFor(projectName)
        if (!associationFor.isNullOrBlank() && ConfigConnectionFactory.getInstance()
                .getConnectionConfiguration(associationFor) == null
        ) {
            ConnectionConfig.getInstance().apply {
                removeAssociationFor(projectName)
                saveModuleAssociatedConn()
                e.presentation.apply {
                    isEnabled = true
                    text = SourcesyncBundle.message("sourcesyncAddConfigurations")
                    icon = null
                }
            }
        }
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
                icon = SourceSyncIcons.SOURCESYNC
            }
        }
    }


    inner class ConnectionConfigurationsComboBox(presentation: Presentation) : ComboBoxButton(presentation) {
        init {

            addMouseListener(object : MouseAdapter() {
                override fun mousePressed(e: MouseEvent) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        e.consume()
                        if (e.isShiftDown) {
                            onShiftClick()
                        }
                    }
                }
            })
        }

        override fun paint(g: Graphics) {
            super.paint(g)
            presentation.icon = if (syncStatusService.isAnySyncJobRunning()) {
                SourceSyncIcons.SOURCESYNC_RUNNING
            } else {
                SourceSyncIcons.SOURCESYNC
            }
        }

        // TODO remove the listener and override doShiftClick when is no longer experimental
        fun onShiftClick() {
            val context = DataManager.getInstance().getDataContext(this)
            val project = CommonDataKeys.PROJECT.getData(context)
            if (project != null) {
                ConnectionConfigurationDialog(project).show()
            }
        }
    }

    internal class SourceSyncConfigAction(private val configuration: String) : DumbAwareAction() {

        init {
            val presentation = templatePresentation
            presentation.setText(configuration, false)
            presentation.icon = SourceSyncIcons.SOURCESYNC
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