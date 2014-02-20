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
import org.wavescale.sourcesync.api.Utils;
import org.wavescale.sourcesync.config.FTPConfiguration;
import org.wavescale.sourcesync.config.FTPSConfiguration;
import org.wavescale.sourcesync.config.SCPConfiguration;
import org.wavescale.sourcesync.config.SFTPConfiguration;
import org.wavescale.sourcesync.factory.ConfigConnectionFactory;
import org.wavescale.sourcesync.factory.ModuleConnectionConfig;
import org.wavescale.sourcesync.logger.EventDataLogger;
import org.wavescale.sourcesync.synchronizer.FTPFileSynchronizer;
import org.wavescale.sourcesync.synchronizer.FTPSFileSynchronizer;
import org.wavescale.sourcesync.synchronizer.SCPFileSynchronizer;
import org.wavescale.sourcesync.synchronizer.SFTPFileSynchronizer;

import java.io.File;

/**
 * ****************************************************************************
 * Copyright (c) 2005-2014 Faur Ioan-Aurel.                                     *
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
        Project currentProject = e.getProject();

        String moduleName = currentProject.getName();
        String associationName = ModuleConnectionConfig.getInstance().getAssociationFor(moduleName);
        if (associationName == null) {
            Utils.showNoConnectionSpecifiedError(e, moduleName);
            return;
        }
        VirtualFile virtualFile = DataKeys.VIRTUAL_FILE.getData(e.getDataContext());

        final ConnectionConfiguration connectionConfiguration = ConfigConnectionFactory.getInstance().
                getConnectionConfiguration(associationName);
        if (Utils.canBeUploaded(virtualFile.getName(), connectionConfiguration.getExcludedFiles())) {
            EventDataLogger.logInfo("virtualFile:" + virtualFile.getPath(), currentProject);
            EventDataLogger.logInfo("projecteBase:" + currentProject.getBasePath(), currentProject);
            final File relativeFile = new File(virtualFile.getPath().replaceFirst(currentProject.getBasePath().replace("\\", "/"), ""));
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
        } else {
            EventDataLogger.logWarning("File <b>" + virtualFile.getName() + "</b> is filtered out!", e.getProject());
        }
    }

}
