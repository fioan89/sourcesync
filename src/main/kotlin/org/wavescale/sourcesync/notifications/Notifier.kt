package org.wavescale.sourcesync.notifications

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

private val notificationGroup = NotificationGroupManager.getInstance().getNotificationGroup("Sourcesync")

class Notifier {
    companion object {
        @JvmStatic
        fun notifyError(project: Project, simpleMessage: String, detailedMessage: String) {
            notificationGroup
                .createNotification(simpleMessage, detailedMessage, NotificationType.ERROR)
                .notify(project)
        }

        @JvmStatic
        fun notifyInfo(project: Project, simpleMessage: String) {
            notificationGroup.createNotification(simpleMessage, NotificationType.INFORMATION).notify(project)
        }
    }
}