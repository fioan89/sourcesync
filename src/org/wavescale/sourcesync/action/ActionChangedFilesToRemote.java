package org.wavescale.sourcesync.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.LocalChangeList;
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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
public class ActionChangedFilesToRemote extends AnAction {
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

        // there's this possibility that the project might not be versioned, therefore no changes can be detected.
        List<LocalChangeList> changeLists = ChangeListManager.getInstance(e.getProject()).getChangeLists();
        if (!hasModifiedFiles(changeLists)) {
            StringBuilder builder = new StringBuilder("Could not find any changes on project <b>");
            builder.append(e.getProject().getName()).append("</b>! You might want to check if this project is imported in any")
                    .append(" version control system that is supported by IDEA!");
            BalloonLogger.logBalloonInfo(builder.toString(), e.getProject());
            EventDataLogger.logInfo(builder.toString(), e.getProject());
            return;
        }

        // get a list of changed virtual files
        List<VirtualFile> changedFiles = new ArrayList<VirtualFile>();
        for (LocalChangeList localChangeList : changeLists) {
            for (Change change : localChangeList.getChanges()) {
                changedFiles.add(change.getVirtualFile());
            }
        }

        // start sync
        final ConnectionConfiguration connectionConfiguration = ConfigConnectionFactory.getInstance().
                getConnectionConfiguration(associationName);
        final Semaphore semaphores = new Semaphore(connectionConfiguration.getSimultaneousJobs());
        final int allowed_sessions = changedFiles.size() <= connectionConfiguration.getSimultaneousJobs() ?
                changedFiles.size() : connectionConfiguration.getSimultaneousJobs();
        final SynchronizationQueue synchronizationQueue = new SynchronizationQueue(e.getProject(), connectionConfiguration, allowed_sessions);
        synchronizationQueue.startCountingTo(changedFiles.size());
        final BlockingQueue<FileSynchronizer> queue = synchronizationQueue.getSyncQueue();
        for (final VirtualFile virtualFile : changedFiles) {
            if (virtualFile != null && Utils.canBeUploaded(virtualFile.getName(), connectionConfiguration.getExcludedFiles())) {
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
                                fileSynchronizer.syncFile(virtualFile.getPath(), uploadLocation);
                            }
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
        }
    }

    private boolean hasModifiedFiles(List<LocalChangeList> changeLists) {
        for (LocalChangeList changeList : changeLists) {
            Collection<Change> changes = changeList.getChanges();
            for (Change change : changes) {
                if (change.getVirtualFile() != null) {
                    return true;
                }
            }
        }
        return false;
    }
}
