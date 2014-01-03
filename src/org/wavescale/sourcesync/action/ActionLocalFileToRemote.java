package org.wavescale.sourcesync.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.wavescale.sourcesync.api.ConnectionConfiguration;
import org.wavescale.sourcesync.api.ConnectionConstants;
import org.wavescale.sourcesync.api.FileSynchronizer;
import org.wavescale.sourcesync.config.FTPConfiguration;
import org.wavescale.sourcesync.config.FTPSConfiguration;
import org.wavescale.sourcesync.config.SCPConfiguration;
import org.wavescale.sourcesync.config.SFTPConfiguration;
import org.wavescale.sourcesync.factory.ConfigConnectionFactory;
import org.wavescale.sourcesync.factory.ModuleConnectionConfig;
import org.wavescale.sourcesync.logger.BalloonLogger;
import org.wavescale.sourcesync.logger.EventDataLogger;
import org.wavescale.sourcesync.synchronizer.FTPFileSynchronizer;
import org.wavescale.sourcesync.synchronizer.FTPSFileSynchronizer;
import org.wavescale.sourcesync.synchronizer.SCPFileSynchronizer;
import org.wavescale.sourcesync.synchronizer.SFTPFileSynchronizer;

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
    public void actionPerformed(final AnActionEvent e) {
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
        final ConnectionConfiguration connectionConfiguration = ConfigConnectionFactory.getInstance().
                getConnectionConfiguration(associationName);
        final File relativeFile = new File(virtualFile.getPath().replaceFirst(currentProject.getBasePath(), ""));
        ProgressManager.getInstance().run(new Task.Backgroundable(e.getProject(), "Uploading", false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                FileSynchronizer fileSynchronizer = null;
                if (ConnectionConstants.CONN_TYPE_SCP.equals(connectionConfiguration.getConnectionType())) {
                    fileSynchronizer = new SCPFileSynchronizer((SCPConfiguration) connectionConfiguration,
                            e.getProject(), indicator);
                } else if (ConnectionConstants.CONN_TYPE_SFTP.equals(connectionConfiguration.getConnectionType())) {
                    fileSynchronizer = new SFTPFileSynchronizer((SFTPConfiguration) connectionConfiguration,
                            e.getProject(), indicator);
                } else if (ConnectionConstants.CONN_TYPE_FTP.equals(connectionConfiguration.getConnectionType())) {
                    fileSynchronizer = new FTPFileSynchronizer((FTPConfiguration) connectionConfiguration,
                            e.getProject(), indicator);
                } else if (ConnectionConstants.CONN_TYPE_FTPS.equals(connectionConfiguration.getConnectionType())) {
                    fileSynchronizer = new FTPSFileSynchronizer((FTPSConfiguration) connectionConfiguration,
                            e.getProject(), indicator);
                }

                if (fileSynchronizer != null) {
                    fileSynchronizer.connect();
                    // so final destination will look like this:
                    // root_home/ + project_name/ + project_relative_path_to_file/
                    fileSynchronizer.syncFile(relativeFile.getPath(), e.getProject().getName() + File.separator + relativeFile.getParent());
                    fileSynchronizer.disconnect();
                }
            }
        });

    }

    private void showNoConnectionSpecifiedError(AnActionEvent e, String moduleName) {
        StringBuilder message = new StringBuilder();
        message.append("There is no connection type associated to <b>").append(moduleName)
                .append("</b> module.\nPlease right click on module name and then select <b>Module Connection Configuration</b> to select connection type!");
        BalloonLogger.logBalloonError(message.toString(), e.getProject());
        EventDataLogger.logError(message.toString(), e.getProject());


    }
}
