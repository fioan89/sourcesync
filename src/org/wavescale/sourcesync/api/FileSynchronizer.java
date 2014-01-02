package org.wavescale.sourcesync.api;

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


import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract class for uploading a local file to a remote target.
 */
public abstract class FileSynchronizer {
    protected ConnectionConfiguration connectionInfo;
    public Project project;
    public ProgressIndicator indicator;

    /**
     * Build a file synchronizer from general info contained by <b>connectionInfo</b> param.
     *
     * @param connectionInfo a {@link org.wavescale.sourcesync.api.ConnectionConfiguration} instance
     *                       containing session info like hostname, user, password, etc...
     * @param project        a {@link com.intellij.openapi.project.Project} instance used to gather project relative
     *                       metadata like name, absoulte path, etc...
     * @param indicator      used to report progress on upload process.
     */
    public FileSynchronizer(@NotNull ConnectionConfiguration connectionInfo, @NotNull Project project, @NotNull ProgressIndicator indicator) {
        this.connectionInfo = connectionInfo;
        this.project = project;
        this.indicator = indicator;
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
     * @param destinationPath a <code>String</code> representing a location path on the remote target
     *                        where the source will be uploaded.
     */
    public abstract void syncFile(String sourcePath, String destinationPath);
}
