package org.wavescale.sourcesync.action;

import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.awt.RelativePoint;
import org.wavescale.sourcesync.factory.ModuleConnectionConfig;

import javax.xml.crypto.Data;

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
public class ActionLocalFileToRemote extends AnAction {
    public void actionPerformed(AnActionEvent e) {
        // first check if there's a connection type associated to this module. If not alert the user
        // and get out
        Project currentProject = DataKeys.PROJECT.getData(e.getDataContext());
        String moduleName = currentProject.getName();
        if (ModuleConnectionConfig.getInstance().getAssociationFor(moduleName) == null) {
            showNoConnectionSpecifiedError(e, moduleName);
        }
    }

    private void showNoConnectionSpecifiedError(AnActionEvent e, String moduleName) {
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(DataKeys.PROJECT.getData(e.getDataContext()));
        StringBuilder message = new StringBuilder();
        message.append("There is no connection type associated to <b>").append(moduleName)
                .append("</b> module.\nPlease right click on module name and then select <b>Module Connection Configuration</b> to select connection type!");

        JBPopupFactory.getInstance().createHtmlTextBalloonBuilder(message.toString(), MessageType.ERROR, null).
                setFadeoutTime(7500).createBalloon().show(RelativePoint.getCenterOf(statusBar.getComponent()),
                Balloon.Position.atRight);
        Notification notification = new Notification("SourceSync Notifications", "Sync error", message.toString(), NotificationType.ERROR);
        Notifications.Bus.notify(notification, e.getProject());

    }
}
