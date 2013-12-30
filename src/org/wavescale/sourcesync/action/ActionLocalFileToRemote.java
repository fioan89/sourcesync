package org.wavescale.sourcesync.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.wavescale.sourcesync.config.SCPConfiguration;
import org.wavescale.sourcesync.factory.ConfigConnectionFactory;
import org.wavescale.sourcesync.factory.ModuleConnectionConfig;
import org.wavescale.sourcesync.logger.BalloonLogger;
import org.wavescale.sourcesync.logger.EventDataLogger;
import org.wavescale.sourcesync.synchronizer.SCPFileSynchronizer;

import java.io.File;

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
        String associationName = ModuleConnectionConfig.getInstance().getAssociationFor(moduleName);
        if (associationName == null) {
            showNoConnectionSpecifiedError(e, moduleName);
            return;
        }
        VirtualFile virtualFile = DataKeys.VIRTUAL_FILE.getData(e.getDataContext());
        SCPConfiguration connectionConfiguration = (SCPConfiguration) ConfigConnectionFactory.getInstance().getConnectionConfiguration(associationName);
        SCPFileSynchronizer scpFileSynchronizer = new SCPFileSynchronizer(connectionConfiguration, e.getProject());
        scpFileSynchronizer.connect();
        File relativeFile = new File(virtualFile.getPath().replaceFirst(currentProject.getBasePath(), ""));
        scpFileSynchronizer.syncFile(relativeFile.getPath(), relativeFile.getParent());
        scpFileSynchronizer.disconnect();
    }

    private void showNoConnectionSpecifiedError(AnActionEvent e, String moduleName) {
        StringBuilder message = new StringBuilder();
        message.append("There is no connection type associated to <b>").append(moduleName)
                .append("</b> module.\nPlease right click on module name and then select <b>Module Connection Configuration</b> to select connection type!");
        BalloonLogger.logBalloonError(message.toString(), e.getProject());
        EventDataLogger.logError(message.toString(), e.getProject());


    }
}
