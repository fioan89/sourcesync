package org.wavescale.sourcesync

import com.intellij.openapi.util.IconLoader
import com.intellij.ui.BadgeIcon
import com.intellij.util.ui.JBUI

class SourceSyncIcons {
    class ExpUI {
        companion object {
            val SOURCESYNC = IconLoader.getIcon("expui/sourcesync.svg", javaClass)
            val SOURCESYNC_RUNNING = BadgeIcon(SOURCESYNC, JBUI.CurrentTheme.RunWidget.RUNNING_BACKGROUND)
        }
    }
}