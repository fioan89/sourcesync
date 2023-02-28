package org.wavescale.sourcesync.notifications

import com.intellij.ide.BrowserUtil
import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import org.wavescale.sourcesync.SourcesyncBundle

private val notificationGroup = NotificationGroupManager.getInstance().getNotificationGroup("Sourcesync")

private const val PROPERTY_IGNORE_SOURCESYNC_FTP_DEPRECATION = "ignore.sourcesync.ftp.deprecation"

class Notifier {
    companion object {
        @JvmStatic
        fun notifyError(project: Project, simpleMessage: String, detailedMessage: String) {
            notificationGroup
                .createNotification(simpleMessage, detailedMessage, NotificationType.ERROR)
                .notify(project)
        }

        @JvmStatic
        fun notifyDeprecation(project: Project, message: String, url: String) {

            val ignored = PropertiesComponent.getInstance().isValueSet(PROPERTY_IGNORE_SOURCESYNC_FTP_DEPRECATION)
            if (ignored) return

            val notification = notificationGroup.createNotification(
                message,
                NotificationType.WARNING
            )

            notification.apply {
                addAction(NotificationAction.createSimpleExpiring(SourcesyncBundle.message("dont.show.again.action")) {
                    PropertiesComponent.getInstance().setValue(PROPERTY_IGNORE_SOURCESYNC_FTP_DEPRECATION, "true")
                })
                addAction(NotificationAction.createSimple(SourcesyncBundle.message("go.to.github.issues")) {
                    BrowserUtil.browse(url)
                })

                setDisplayId(SourcesyncBundle.message("notification.group.sourcesync.ftp.deprecation"))
                isImportant = true
                isSuggestionType = true
            }.notify(project)
        }

        @JvmStatic
        fun notifyInfo(project: Project, simpleMessage: String) {
            notificationGroup.createNotification(simpleMessage, NotificationType.INFORMATION).notify(project)
        }
    }
}