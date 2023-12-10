package org.wavescale.sourcesync.notifications

import com.intellij.ide.BrowserUtil
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import org.wavescale.sourcesync.SourcesyncBundle

private const val SOURCESYNC_GROUP_ID = "Sourcesync"

class Notifier {
    companion object {
        @JvmStatic
        fun notifyError(project: Project, simpleMessage: String, detailedMessage: String) {
            if (NotificationGroupManager.getInstance().isGroupRegistered(SOURCESYNC_GROUP_ID).not()) {
                return
            }
            val notificationGroup = NotificationGroupManager.getInstance().getNotificationGroup(SOURCESYNC_GROUP_ID)
            notificationGroup
                .createNotification(simpleMessage, detailedMessage, NotificationType.ERROR)
                .notify(project)
        }

        @JvmStatic
        fun notifyToProDueToHighNumberOfUploads(project: Project) {
            if (NotificationGroupManager.getInstance().isGroupRegistered(SOURCESYNC_GROUP_ID).not()) {
                return
            }
            val notificationGroup = NotificationGroupManager.getInstance().getNotificationGroup(SOURCESYNC_GROUP_ID)
            val notification = notificationGroup.createNotification(
                SourcesyncBundle.message("upgrade.nudge.pro.tip.title"),
                SourcesyncBundle.message("upgrade.nudge.pro.tip.message"),
                NotificationType.INFORMATION
            )

            notification.apply {
                addAction(NotificationAction.createSimple(SourcesyncBundle.message("upgrade.to.pro.version")) {
                    BrowserUtil.browse("https://plugins.jetbrains.com/plugin/22318-source-synchronizer-pro")
                })
                addAction(NotificationAction.createSimple(SourcesyncBundle.message("buy.me.a.coffee")) {
                    BrowserUtil.browse("https://www.buymeacoffee.com/fioan89")
                })
                setDisplayId(SourcesyncBundle.message("notification.group.sourcesync.donation"))
                isImportant = true
                isSuggestionType = true
            }.notify(project)
        }

        @JvmStatic
        fun notifyUpgradeToProDueToFolderUpload(project: Project) {
            if (NotificationGroupManager.getInstance().isGroupRegistered(SOURCESYNC_GROUP_ID).not()) {
                return
            }
            val notificationGroup = NotificationGroupManager.getInstance().getNotificationGroup(SOURCESYNC_GROUP_ID)
            val notification = notificationGroup.createNotification(
                SourcesyncBundle.message("upgrade.nudge.directory.upload.title"),
                SourcesyncBundle.message("upgrade.nudge.directory.upload.message"),
                NotificationType.INFORMATION
            )

            notification.apply {
                addAction(NotificationAction.createSimple(SourcesyncBundle.message("upgrade.to.pro.version")) {
                    BrowserUtil.browse("https://plugins.jetbrains.com/plugin/22318-source-synchronizer-pro")
                })
                addAction(NotificationAction.createSimple(SourcesyncBundle.message("buy.me.a.coffee")) {
                    BrowserUtil.browse("https://www.buymeacoffee.com/fioan89")
                })

                setDisplayId(SourcesyncBundle.message("notification.group.sourcesync.donation"))
                isImportant = true
                isSuggestionType = true
            }.notify(project)
        }


        @JvmStatic
        fun notifyInfo(project: Project, simpleMessage: String) {
            if (NotificationGroupManager.getInstance().isGroupRegistered(SOURCESYNC_GROUP_ID).not()) {
                return
            }
            val notificationGroup = NotificationGroupManager.getInstance().getNotificationGroup(SOURCESYNC_GROUP_ID)
            notificationGroup.createNotification(simpleMessage, NotificationType.INFORMATION).notify(project)
        }
    }
}