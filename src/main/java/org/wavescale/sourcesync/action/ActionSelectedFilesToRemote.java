package org.wavescale.sourcesync.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.wavescale.sourcesync.api.ConnectionConfiguration;
import org.wavescale.sourcesync.api.FileSynchronizer;
import org.wavescale.sourcesync.api.SynchronizationQueue;
import org.wavescale.sourcesync.api.Utils;
import org.wavescale.sourcesync.factory.ConfigConnectionFactory;
import org.wavescale.sourcesync.factory.ConnectionConfig;
import org.wavescale.sourcesync.logger.BalloonLogger;
import org.wavescale.sourcesync.logger.EventDataLogger;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

/**
 * ****************************************************************************
 * Copyright (c) 2014-2107 Faur Ioan-Aurel.                                     *
 * All rights reserved. This program and the accompanying materials             *
 * are made available under the terms of the MIT License                        *
 * which accompanies this distribution, and is available at                     *
 * http://opensource.org/licenses/MIT                                           *
 * *
 * For any issues or questions send an email at: fioan89@gmail.com              *
 * *****************************************************************************
 */
public class ActionSelectedFilesToRemote extends AnAction {
    public void actionPerformed(final AnActionEvent e) {
        // first check if there's a connection type associated to this module.
        // If not alert the user and get out
        Project project = PlatformDataKeys.PROJECT.getData(e.getDataContext());

        String projectName = project.getName();
        String associationName = ConnectionConfig.getInstance().getAssociationFor(projectName);
        if (associationName == null) {
            Utils.showNoConnectionSpecifiedError(projectName);
            return;
        }

        // get a list of selected virtual files
        VirtualFile[] virtualFiles = PlatformDataKeys.VIRTUAL_FILE_ARRAY.getData(e.getDataContext());
        if (virtualFiles.length <= 0) {
            StringBuilder builder = new StringBuilder("Project <b>");
            builder.append(e.getProject().getName()).append("</b>! does not have files selected!");
            BalloonLogger.logBalloonInfo(builder.toString(), e.getProject());
            EventDataLogger.logInfo(builder.toString(), e.getProject());
            return;
        }

        // start sync
        final ConnectionConfiguration connectionConfiguration = ConfigConnectionFactory.getInstance().
                getConnectionConfiguration(associationName);
        final Semaphore semaphores = new Semaphore(connectionConfiguration.getSimultaneousJobs());
        final int allowed_sessions = virtualFiles.length <= connectionConfiguration.getSimultaneousJobs() ?
                virtualFiles.length : connectionConfiguration.getSimultaneousJobs();
        final SynchronizationQueue synchronizationQueue = new SynchronizationQueue(e.getProject(), connectionConfiguration, allowed_sessions);
        synchronizationQueue.startCountingTo(virtualFiles.length);
        final BlockingQueue<FileSynchronizer> queue = synchronizationQueue.getSyncQueue();
        for (final VirtualFile virtualFile : virtualFiles) {
            if (virtualFile != null && new File(virtualFile.getPath()).isFile()) {
                if (Utils.canBeUploaded(virtualFile.getName(), connectionConfiguration.getExcludedFiles())) {
                    final Path uploadLocation = Utils.dirsToFileFromProjectRoot(virtualFile, project);
                    ProgressManager.getInstance().run(new Task.Backgroundable(e.getProject(), "Uploading", false) {
                        @Override
                        public void run(@NotNull ProgressIndicator indicator) {
                            FileSynchronizer fileSynchronizer = null;
                            try {
                                semaphores.acquire();
                                fileSynchronizer = queue.take();
                                fileSynchronizer.setIndicator(indicator);
                                if (fileSynchronizer != null) {
                                    fileSynchronizer.connect();
                                    // so final destination will look like this:
                                    // root_home/ + project_relative_path_to_file/
                                }
                                fileSynchronizer.syncFile(virtualFile.getPath(), uploadLocation);
                                queue.put(fileSynchronizer);
                                synchronizationQueue.count();
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            } finally {
                                semaphores.release();
                            }
                        }
                    });

                } else {
                    if (virtualFile != null) {
                        EventDataLogger.logWarning("File <b>" + virtualFile.getName() + "</b> is filtered out!", e.getProject());
                        synchronizationQueue.count();
                    }
                }
            } else {
                if (virtualFile != null) {
                    EventDataLogger.logWarning("File <b>" + virtualFile.getName() + "</b> is a directory!", e.getProject());
                    synchronizationQueue.count();
                }
            }
        }
    }
}
