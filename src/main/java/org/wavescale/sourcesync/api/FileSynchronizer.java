package org.wavescale.sourcesync.api;

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


import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * Abstract class for uploading a local file to a remote target.
 */
public abstract class FileSynchronizer {
    private ConnectionConfiguration connectionInfo;
    private Project project;
    private ProgressIndicator indicator;
    private boolean isConnected;

    /**
     * Build a file synchronizer from general info contained by <b>connectionInfo</b> param.
     *
     * @param connectionInfo a {@link org.wavescale.sourcesync.api.ConnectionConfiguration} instance
     *                       containing session info like hostname, user, password, etc...
     * @param project        a {@link com.intellij.openapi.project.Project} instance used to gather project relative
     *                       metadata like name, absolute path, etc...
     * @param indicator      used to report progress on upload process.
     */
    public FileSynchronizer(@NotNull ConnectionConfiguration connectionInfo, @NotNull Project project, @NotNull ProgressIndicator indicator) {
        this.setConnectionInfo(connectionInfo);
        this.setProject(project);
        this.setIndicator(indicator);
        this.setConnected(false);
    }

    /**
     * Initiates the connection to the remote target.
     *
     * @return <code>true</code> if connection could be made, <code>false</code> otherwise.
     */
    public abstract boolean connect();

    /**
     * Disconnects current session from remote target.
     */
    public abstract void disconnect();

    /**
     * Uploads the given file to the remote target.
     *
     * @param sourcePath      a <code>String</code> representing a file path to be uploaded. This can be a
     *                        relative path or an absolute path, depending on the implementation.
     * @param uploadLocation a <code>Path</code> representing a location path on the remote target
     *                        where the source will be uploaded. This path is relative to root location.
     */
    public abstract void syncFile(String sourcePath, Path uploadLocation);

    public ConnectionConfiguration getConnectionInfo() {
        return connectionInfo;
    }

    public void setConnectionInfo(ConnectionConfiguration connectionInfo) {
        this.connectionInfo = connectionInfo;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public ProgressIndicator getIndicator() {
        return indicator;
    }

    public void setIndicator(ProgressIndicator indicator) {
        this.indicator = indicator;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }
}
