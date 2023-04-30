package org.wavescale.sourcesync.action

import com.intellij.ide.ui.laf.darcula.ui.ToolbarComboWidgetUI
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.wm.impl.ExpandableComboAction
import com.intellij.openapi.wm.impl.ToolbarComboWidget
import com.intellij.ui.components.JBList
import com.intellij.ui.popup.PopupFactoryImpl
import org.wavescale.sourcesync.SourceSyncIcons
import org.wavescale.sourcesync.SourcesyncBundle
import org.wavescale.sourcesync.factory.ConfigConnectionFactory
import org.wavescale.sourcesync.factory.ConnectionConfig
import org.wavescale.sourcesync.services.SyncStatusService
import javax.swing.JComponent

class NewUIConnectionConfigurationSelector : ExpandableComboAction() {
    private val syncStatusService = service<SyncStatusService>()
    override fun getActionUpdateThread() = ActionUpdateThread.BGT
    override fun displayTextInToolbar() = true

    override fun createPopup(event: AnActionEvent): JBPopup {
        return SourceSyncConfigurationActionGroupPopup(createActionGroup(), event.dataContext) { Toggleable.setSelected(event.presentation, false) }
    }

    override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
        val comp = super.createCustomComponent(presentation, place)
        (comp.ui as? ToolbarComboWidgetUI)?.setMaxWidth(Int.MAX_VALUE)
        return comp
    }

    override fun updateCustomComponent(component: JComponent, presentation: Presentation) {
        val widget = component as? ToolbarComboWidget ?: return
        (widget.ui as? ToolbarComboWidgetUI)?.setMaxWidth(500)
        if (syncStatusService.isAnySyncJobRunning()) {
            widget.leftIcons = listOfNotNull(SourceSyncIcons.ExpUI.SOURCESYNC_RUNNING)
        } else {
            widget.leftIcons = listOfNotNull(SourceSyncIcons.ExpUI.SOURCESYNC)
        }

        widget.text = presentation.text
        widget.toolTipText = presentation.description
    }

    override fun update(e: AnActionEvent) {
        val projectName = e.project?.name
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
                icon = SourceSyncIcons.ExpUI.SOURCESYNC
            }
        }
    }
}


private fun createActionGroup(): ActionGroup {
    val allActionsGroup = DefaultActionGroup()
    allActionsGroup.add(Separator.create(SourcesyncBundle.message("sourcesyncConfigurations")))
    ConfigConnectionFactory.getInstance().connectionNames.forEach {
        allActionsGroup.add(
            SourceSyncConfigAction(
                it
            )
        )
    }

    allActionsGroup.add(Separator.create())
    allActionsGroup.add(ActionManager.getInstance().getAction("actionSourceSyncMenu"))

    return allActionsGroup
}

class SourceSyncConfigurationActionGroupPopup(
    actionGroup: ActionGroup,
    dataContext: DataContext,
    disposeCallback: (() -> Unit)?
) : PopupFactoryImpl.ActionGroupPopup(
    null,
    actionGroup,
    dataContext,
    false,
    false,
    true,
    false,
    disposeCallback,
    30,
    null,
    null
) {

    init {
        (list as? JBList<*>)?.setExpandableItemsEnabled(false)
    }

    override fun shouldBeShowing(value: Any?) = true
}

class SourceSyncConfigAction(private val configuration: String) : AnAction() {
    init {
        val presentation = templatePresentation
        presentation.setText(configuration, false)
        presentation.icon = SourceSyncIcons.ExpUI.SOURCESYNC
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val projectName = PlatformDataKeys.PROJECT.getData(e.dataContext)!!.name
        associateProjectWithConnection(projectName)
    }

    private fun associateProjectWithConnection(projectName: String) {
        ConnectionConfig.getInstance().associateProjectWithConnection(projectName, configuration)
        ConnectionConfig.getInstance().saveModuleAssociatedConn()
    }
}