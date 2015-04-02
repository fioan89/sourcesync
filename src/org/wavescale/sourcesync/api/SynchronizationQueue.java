package org.wavescale.sourcesync.api;

import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.wavescale.sourcesync.config.FTPConfiguration;
import org.wavescale.sourcesync.config.FTPSConfiguration;
import org.wavescale.sourcesync.config.SCPConfiguration;
import org.wavescale.sourcesync.config.SFTPConfiguration;
import org.wavescale.sourcesync.logger.EventDataLogger;
import org.wavescale.sourcesync.synchronizer.FTPFileSynchronizer;
import org.wavescale.sourcesync.synchronizer.FTPSFileSynchronizer;
import org.wavescale.sourcesync.synchronizer.SCPFileSynchronizer;
import org.wavescale.sourcesync.synchronizer.SFTPFileSynchronizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

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

/**
 * A blocking queue manager for {@link org.wavescale.sourcesync.api.FileSynchronizer} instances.
 * Besides providing access to a pool of sync instances, this class also automatically closes the connections
 * and cleans the pool after a certain threshold value is reached.
 */
public class SynchronizationQueue {
    private final Project project;
    private final ConnectionConfiguration connectionType;
    private int allowed_sessions;
    private BlockingQueue<FileSynchronizer> syncQueue;

    private int countTo = 0;
    private int counter = 0;

    public SynchronizationQueue(Project project, ConnectionConfiguration connectionType, int allowed_sessions) {
        this.project = project;
        this.connectionType = connectionType;
        this.allowed_sessions = allowed_sessions;
        this.syncQueue = initSyncQueue();
    }

    private BlockingQueue initSyncQueue() {
        BlockingQueue<FileSynchronizer> queue = new ArrayBlockingQueue<FileSynchronizer>(this.allowed_sessions);
        DummyProgressIndicator indicator = new DummyProgressIndicator();
        for (int i = 0; i < allowed_sessions; i++) {
            FileSynchronizer fileSynchronizer = null;
            if (ConnectionConstants.CONN_TYPE_SCP.equals(connectionType.getConnectionType())) {
                fileSynchronizer = new SCPFileSynchronizer((SCPConfiguration) connectionType,
                        project, indicator);
            } else if (ConnectionConstants.CONN_TYPE_SFTP.equals(connectionType.getConnectionType())) {
                fileSynchronizer = new SFTPFileSynchronizer((SFTPConfiguration) connectionType,
                        project, indicator);
            } else if (ConnectionConstants.CONN_TYPE_FTP.equals(connectionType.getConnectionType())) {
                fileSynchronizer = new FTPFileSynchronizer((FTPConfiguration) connectionType,
                        project, indicator);
            } else if (ConnectionConstants.CONN_TYPE_FTPS.equals(connectionType.getConnectionType())) {
                fileSynchronizer = new FTPSFileSynchronizer((FTPSConfiguration) connectionType,
                        project, indicator);
            }

            try {
                queue.put(fileSynchronizer);
            } catch (InterruptedException e1) {
                EventDataLogger.logError(e1.toString(), project);
            }
        }
        return queue;
    }

    /**
     * Returns file sync pool.
     *
     * @return
     */
    public BlockingQueue<FileSynchronizer> getSyncQueue() {
        return syncQueue;
    }

    public void setSyncQueue(BlockingQueue<FileSynchronizer> syncQueue) {
        this.syncQueue = syncQueue;
    }

    /**
     * Empties the internal queue while at the same time closes any remnant connection.
     */
    public synchronized void cleanSyncQueue() {
        Collection<FileSynchronizer> drainedSyncs = new ArrayList<FileSynchronizer>();
        this.syncQueue.drainTo(drainedSyncs);
        for (FileSynchronizer fileSynchronizer : drainedSyncs) {
            fileSynchronizer.disconnect();
        }
    }

    /**
     * Sets the threshold value to clean the pool. When this value is reached, connections
     * are closed and pool is cleaned. {@link SynchronizationQueue#count()} must be called whenever you
     * want to increase the internal counter.
     *
     * @param countTo an int value, greater or equal to 0.
     */
    public void startCountingTo(int countTo) {
        if (countTo >= 0) {
            this.countTo = countTo;
        }
    }

    /**
     * Increases the counter and when this is equal or greater than the threshold value, {@link SynchronizationQueue#cleanSyncQueue()}
     * is triggered.
     */
    public synchronized void count() {
        this.counter++;
        if (this.counter >= this.countTo) {
            this.cleanSyncQueue();
        }
    }

    private class DummyProgressIndicator implements ProgressIndicator {

        @Override
        public void start() {

        }

        @Override
        public void stop() {

        }

        @Override
        public boolean isRunning() {
            return false;
        }

        @Override
        public void cancel() {

        }

        @Override
        public boolean isCanceled() {
            return false;
        }

        @Override
        public String getText() {
            return null;
        }

        @Override
        public void setText(String text) {

        }

        @Override
        public String getText2() {
            return null;
        }

        @Override
        public void setText2(String text) {

        }

        @Override
        public double getFraction() {
            return 0;
        }

        @Override
        public void setFraction(double fraction) {

        }

        @Override
        public void pushState() {

        }

        @Override
        public void popState() {

        }

        @Override
        public void startNonCancelableSection() {

        }

        @Override
        public void finishNonCancelableSection() {

        }

        @Override
        public boolean isModal() {
            return false;
        }

        @NotNull
        @Override
        public ModalityState getModalityState() {
            return null;
        }

        @Override
        public void setModalityProgress(ProgressIndicator modalityProgress) {

        }

        @Override
        public boolean isIndeterminate() {
            return false;
        }

        @Override
        public void setIndeterminate(boolean indeterminate) {

        }

        @Override
        public void checkCanceled() throws ProcessCanceledException {

        }

        @Override
        public boolean isPopupWasShown() {
            return false;
        }

        @Override
        public boolean isShowing() {
            return false;
        }
    }

}
