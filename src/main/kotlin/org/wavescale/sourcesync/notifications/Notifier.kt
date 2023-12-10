package org.wavescale.sourcesync.notifications

import com.intellij.ide.BrowserUtil
import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import org.wavescale.sourcesync.SourcesyncBundle

private const val SOURCESYNC_GROUP_ID = "Sourcesync"

private const val PROPERTY_IGNORE_SOURCESYNC_DONATION = "ignore.sourcesync.donation.v303"

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
        fun notifyDonation(project: Project) {
            val ignored = PropertiesComponent.getInstance().isValueSet(PROPERTY_IGNORE_SOURCESYNC_DONATION)
            if (ignored) return
            if (NotificationGroupManager.getInstance().isGroupRegistered(SOURCESYNC_GROUP_ID).not()) {
                return
            }
            val notificationGroup = NotificationGroupManager.getInstance().getNotificationGroup(SOURCESYNC_GROUP_ID)
            val notification = notificationGroup.createNotification(
                SourcesyncBundle.message("donation.title"),
                SourcesyncBundle.message("donation.message"),
                NotificationType.INFORMATION
            )

            notification.apply {
                addAction(NotificationAction.createSimple(SourcesyncBundle.message("upgrade.to.pro.version")) {
                    BrowserUtil.browse("https://plugins.jetbrains.com/plugin/22318-source-synchronizer-pro")
                })
                addAction(NotificationAction.createSimple(SourcesyncBundle.message("buy.me.a.coffee")) {
                    BrowserUtil.browse("https://www.buymeacoffee.com/fioan89")
                })
                addAction(NotificationAction.createSimpleExpiring(SourcesyncBundle.message("dont.show.again.action")) {
                    PropertiesComponent.getInstance().setValue(PROPERTY_IGNORE_SOURCESYNC_DONATION, "true")
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