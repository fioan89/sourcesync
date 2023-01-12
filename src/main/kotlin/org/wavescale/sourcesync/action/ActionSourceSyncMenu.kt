package org.wavescale.sourcesync.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import org.wavescale.sourcesync.ui.ConnectionConfigurationDialog

class ActionSourceSyncMenu : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        ConnectionConfigurationDialog(e.project!!).show()
    }
}