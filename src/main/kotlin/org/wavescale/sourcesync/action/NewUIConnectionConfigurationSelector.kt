package org.wavescale.sourcesync.action

import com.intellij.ide.ui.laf.darcula.ui.ToolbarComboWidgetUiSizes
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.Separator
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.actionSystem.Toggleable
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import com.intellij.openapi.actionSystem.impl.ActionButtonWithText
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.ListPopup
import com.intellij.ui.RetrievableIcon
import com.intellij.ui.components.JBList
import com.intellij.ui.icons.IconReplacer
import com.intellij.ui.popup.PopupFactoryImpl
import com.intellij.util.ui.JBDimension
import com.intellij.util.ui.JBInsets
import com.intellij.util.ui.JBUI
import java.awt.Component
import java.awt.Graphics
import java.awt.Insets
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.SwingConstants
import org.wavescale.sourcesync.SourceSyncIcons
import org.wavescale.sourcesync.SourcesyncBundle
import org.wavescale.sourcesync.factory.ConnectionConfig
import org.wavescale.sourcesync.services.SyncRemoteConfigurationsService
import org.wavescale.sourcesync.services.SyncStatusService

/***
 * Inspired from IntelliJ RedesignedRunConfigurationSelector.
 */
class NewUIConnectionConfigurationSelector : CustomTogglePopupAction(), CustomComponentAction, DumbAware {
    private val syncStatusService = service<SyncStatusService>()
    override fun getActionUpdateThread() = ActionUpdateThread.BGT
    override fun displayTextInToolbar() = true
    override fun getActionGroup(e: AnActionEvent): ActionGroup? {
        val project = e.project ?: return null
        return createActionGroup(project)
    }

    private fun createActionGroup(project: Project): ActionGroup {
        val syncConfigurationsService = project.service<SyncRemoteConfigurationsService>()
        return DefaultActionGroup().apply {
            add(Separator.create(SourcesyncBundle.message("sourcesyncConfigurations")))
            syncConfigurationsService.allConnectionNames().forEach {
                add(SourceSyncConfigAction(syncConfigurationsService, it))
            }

            add(Separator.create())
            add(ActionManager.getInstance().getAction("actionSourceSyncMenu"))
        }
    }

    override fun createPopup(
        actionGroup: ActionGroup,
        e: AnActionEvent,
        disposeCallback: () -> Unit
    ): ListPopup {
        return object : PopupFactoryImpl.ActionGroupPopup(
            null,
            actionGroup,
            e.dataContext,
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
    }

    override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
        return object : ActionButtonWithText(this, presentation, place, {
            JBUI.size(16, JBUI.CurrentTheme.RunWidget.toolbarHeight())
        }) {
            override fun getMargins(): Insets = JBInsets(0, 10, 0, 6)
            override fun iconTextSpace(): Int = ToolbarComboWidgetUiSizes.gapAfterLeftIcons
            override fun shallPaintDownArrow() = true
            override fun getDownArrowIcon(): Icon = PreparedIcon(super.getDownArrowIcon())

            override fun updateUI() {
                super.updateUI()
                updateFont()
            }

            fun updateFont() {
                font = JBUI.CurrentTheme.RunWidget.configurationSelectorFont()
            }

        }.also {
            it.foreground = JBUI.CurrentTheme.RunWidget.FOREGROUND
            it.setHorizontalTextAlignment(SwingConstants.LEFT)
            it.updateFont()
        }
    }

    override fun update(e: AnActionEvent) {
        super.update(e)

        val syncConfigurationsService = e.project?.service<SyncRemoteConfigurationsService>()
        val associationFor = syncConfigurationsService?.mainConnectionName()
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
                text = syncConfigurationsService.mainConnectionName()

                icon = if (syncStatusService.isAnySyncJobRunning()) {
                    SourceSyncIcons.ExpUI.SOURCESYNC_RUNNING
                } else {
                    SourceSyncIcons.ExpUI.SOURCESYNC
                }
            }
        }
    }
}

class SourceSyncConfigAction(private val syncConfigurationsService: SyncRemoteConfigurationsService, private val configuration: String) : AnAction() {
    init {
        val presentation = templatePresentation
        presentation.setText(configuration, false)
        presentation.icon = SourceSyncIcons.ExpUI.SOURCESYNC
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        syncConfigurationsService.setMainConnection(configuration)
    }
}

internal const val MINIMAL_POPUP_WIDTH = 270

abstract class CustomTogglePopupAction() : ToggleAction() {

    override fun isSelected(e: AnActionEvent): Boolean {
        return Toggleable.isSelected(e.presentation)
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        if (!state) return
        val component = e.inputEvent?.component as? JComponent ?: return
        e.project ?: return
        val popup = createPopup(e)
        popup?.showUnderneathOf(component)

    }

    private fun createPopup(e: AnActionEvent): JBPopup? {
        val presentation = e.presentation
        val actionGroup = getActionGroup(e) ?: return null
        val disposeCallback = { Toggleable.setSelected(presentation, false) }
        val popup = createPopup(actionGroup, e, disposeCallback)
        popup.setMinimumSize(JBDimension(MINIMAL_POPUP_WIDTH, 0))
        return popup
    }

    open fun createPopup(
        actionGroup: ActionGroup,
        e: AnActionEvent,
        disposeCallback: () -> Unit
    ) = JBPopupFactory.getInstance().createActionGroupPopup(null, actionGroup, e.dataContext, false, false, false, disposeCallback, 30, null)

    abstract fun getActionGroup(e: AnActionEvent): ActionGroup?
}

private class PreparedIcon(private val width: Int, private val height: Int, private val iconFn: () -> Icon) : RetrievableIcon {
    constructor(icon: Icon) : this(icon.iconWidth, icon.iconHeight, { icon })

    override fun paintIcon(c: Component?, g: Graphics, x: Int, y: Int) {
        iconFn().paintIcon(c, g, x, y)
    }

    override fun getIconWidth(): Int = width

    override fun getIconHeight(): Int = height

    override fun retrieveIcon(): Icon = iconFn()

    override fun replaceBy(replacer: IconReplacer): Icon {
        return PreparedIcon(width, height) { replacer.replaceIcon(iconFn()) }
    }
}