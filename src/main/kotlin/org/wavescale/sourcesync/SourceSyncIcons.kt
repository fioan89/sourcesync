package org.wavescale.sourcesync

import com.intellij.openapi.util.IconLoader
import com.intellij.ui.IconManager
import com.intellij.util.ui.JBUI

class SourceSyncIcons {
    companion object {
        val SOURCESYNC = IconLoader.getIcon("sourcesync.svg", javaClass)
        val SOURCESYNC_RUNNING = IconManager.getInstance().withIconBadge(SOURCESYNC, JBUI.CurrentTheme.IconBadge.SUCCESS)
    }

    class ExpUI {
        companion object {
            val SOURCESYNC = IconLoader.getIcon("expui/sourcesync.svg", javaClass)
            val SOURCESYNC_RUNNING = IconManager.getInstance().withIconBadge(SOURCESYNC, JBUI.CurrentTheme.IconBadge.SUCCESS)
        }
    }
}