package org.wavescale.sourcesync.action

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.Separator
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.actionSystem.Toggleable
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import com.intellij.openapi.actionSystem.impl.ActionButtonWithText
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.ui.popup.ListPopup
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.ui.components.JBList
import com.intellij.ui.popup.PopupFactoryImpl
import com.intellij.ui.popup.WizardPopup
import com.intellij.ui.popup.util.PopupImplUtil
import com.intellij.util.ui.JBDimension
import com.intellij.util.ui.JBInsets
import com.intellij.util.ui.JBUI
import org.wavescale.sourcesync.SourceSyncIcons
import org.wavescale.sourcesync.SourcesyncBundle
import org.wavescale.sourcesync.factory.ConfigConnectionFactory
import org.wavescale.sourcesync.factory.ConnectionConfig
import org.wavescale.sourcesync.services.SyncStatusService
import java.awt.Graphics
import java.awt.Insets
import javax.swing.JComponent
import javax.swing.SwingConstants

internal const val MINIMAL_POPUP_WIDTH = 270

class NewUIConnectionConfigurationSelector : ConnectionTogglePopupAction(), CustomComponentAction, DumbAware {
    private val syncStatusService = service<SyncStatusService>()
    override fun displayTextInToolbar() = true
    override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
        return object : ActionButtonWithText(this, presentation, place, JBUI.size(90, 30)) {
            override fun getMargins(): Insets = JBInsets.create(0, 10)
            override fun iconTextSpace(): Int = JBUI.scale(10)
            override fun shallPaintDownArrow() = true

            override fun paint(g: Graphics) {
                super.paint(g)
                presentation.icon = if (syncStatusService.isAnySyncJobRunning()) {
                    SourceSyncIcons.ExpUI.SOURCESYNC_RUNNING
                } else {
                    SourceSyncIcons.ExpUI.SOURCESYNC
                }
            }
        }.also {
            it.setHorizontalTextAlignment(SwingConstants.LEFT)
        }
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

abstract class ConnectionTogglePopupAction() : ToggleAction() {

    override fun isSelected(e: AnActionEvent): Boolean {
        return Toggleable.isSelected(e.presentation)
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        if (!state) return
        val presentation = e.presentation
        val component = e.inputEvent?.component as? JComponent ?: return
        val actionGroup = getActionGroup(e)
        val disposeCallback = { Toggleable.setSelected(presentation, false) }
        val popup = createPopup(actionGroup, e, disposeCallback)
        PopupImplUtil.setPopupToggleButton(popup, e.inputEvent.component)
        popup.setMinimumSize(JBDimension(MINIMAL_POPUP_WIDTH, 0))
        popup.showUnderneathOf(component)
    }

    private fun createPopup(
        actionGroup: ActionGroup,
        e: AnActionEvent,
        disposeCallback: () -> Unit
    ): ListPopup = SourceSyncConfigurationActionGroupPopup(actionGroup, e.dataContext, disposeCallback)

    private fun getActionGroup(e: AnActionEvent): ActionGroup {
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

    override fun createPopup(parent: WizardPopup?, step: PopupStep<*>?, parentValue: Any?): WizardPopup {
        val popup = super.createPopup(parent, step, parentValue)
        popup.setMinimumSize(JBDimension(MINIMAL_POPUP_WIDTH, 0))
        return popup
    }

    override fun shouldBeShowing(value: Any?) = true
}

class SourceSyncConfigAction(private val configuration: String) : AnAction() {
    init {
        val presentation = templatePresentation
        presentation.setText(configuration, false)
        presentation.icon = SourceSyncIcons.ExpUI.SOURCESYNC
    }

    override fun actionPerformed(e: AnActionEvent) {
        val projectName = PlatformDataKeys.PROJECT.getData(e.dataContext)!!.name
        associateProjectWithConnection(projectName)
    }

    private fun associateProjectWithConnection(projectName: String) {
        ConnectionConfig.getInstance().associateProjectWithConnection(projectName, configuration)
        ConnectionConfig.getInstance().saveModuleAssociatedConn()
    }
}