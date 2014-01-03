package org.wavescale.sourcesync.logger;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;

/**
 * ****************************************************************************
 * Copyright (c) 2005-2013 Faur Ioan-Aurel.                                     *
 * All rights reserved. This program and the accompanying materials             *
 * are made available under the terms of the MIT License                        *
 * which accompanies this distribution, and is available at                     *
 * http://opensource.org/licenses/MIT                                           *
 * *
 * For any issues or questions send an email at: fioan89@gmail.com              *
 * *****************************************************************************
 */
public class EventDataLogger {
    private EventDataLogger() {

    }

    public static void logError(String htmlMessage, Project project) {
        Notification notification = new Notification("SourceSync Notifications", "Sync error", htmlMessage, NotificationType.ERROR);
        Notifications.Bus.notify(notification, project);
    }

    public static void logWarning(String htmlMessage, Project project) {
        Notification notification = new Notification("SourceSync Notifications", "Sync warning", htmlMessage, NotificationType.WARNING);
        Notifications.Bus.notify(notification, project);
    }
}
