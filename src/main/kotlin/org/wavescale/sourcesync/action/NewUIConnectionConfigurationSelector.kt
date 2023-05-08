package org.wavescale.sourcesync.action

import com.intellij.ide.ui.laf.darcula.ui.ToolbarComboWidgetUI
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.Separator
import com.intellij.openapi.actionSystem.Toggleable
import com.intellij.openapi.components.service
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.wm.impl.ExpandableComboAction
import com.intellij.openapi.wm.impl.ToolbarComboWidget
import com.intellij.ui.components.JBList
import com.intellij.ui.popup.PopupFactoryImpl
import org.wavescale.sourcesync.SourceSyncIcons
import org.wavescale.sourcesync.SourcesyncBundle
import org.wavescale.sourcesync.factory.ConnectionConfig
import org.wavescale.sourcesync.services.SyncRemoteConfigurationsService
import org.wavescale.sourcesync.services.SyncStatusService
import javax.swing.JComponent

class NewUIConnectionConfigurationSelector : ExpandableComboAction() {
    private val syncStatusService = service<SyncStatusService>()
    private val syncConfigurationsService = ProjectManager.getInstance().openProjects[0].getService(SyncRemoteConfigurationsService::class.java)
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
        val associationFor = syncConfigurationsService.mainConnection()
        if (!associationFor.isNullOrBlank() && syncConfigurationsService.findFirstWithName(associationFor) == null) {
            ConnectionConfig.getInstance().apply {
                syncConfigurationsService.resetMainConnection()
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
                text = syncConfigurationsService.mainConnection()
                icon = SourceSyncIcons.ExpUI.SOURCESYNC
            }
        }
    }

    private fun createActionGroup(): ActionGroup {
        val allActionsGroup = DefaultActionGroup()
        allActionsGroup.add(Separator.create(SourcesyncBundle.message("sourcesyncConfigurations")))
        syncConfigurationsService.allConnectionNames().forEach {
            allActionsGroup.add(SourceSyncConfigAction(syncConfigurationsService, it))
        }

        allActionsGroup.add(Separator.create())
        allActionsGroup.add(ActionManager.getInstance().getAction("actionSourceSyncMenu"))

        return allActionsGroup
    }
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

class SourceSyncConfigAction(private val syncConfigurationsService: SyncRemoteConfigurationsService, private val configuration: String) : AnAction() {
    init {
        val presentation = templatePresentation
        presentation.setText(configuration, false)
        presentation.icon = SourceSyncIcons.ExpUI.SOURCESYNC
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val projectName = PlatformDataKeys.PROJECT.getData(e.dataContext)!!.name
        syncConfigurationsService.setMainConnection(configuration)
    }

}