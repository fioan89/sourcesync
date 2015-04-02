package org.wavescale.sourcesync.api;

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

import java.io.Serializable;

/**
 * Abstract class to hold config info about different type of connections.
 */

public abstract class ConnectionConfiguration implements Serializable {
    /**
     * A string containing the ".ext" separated by ";" character,
     * representing a list of file types to be excluded from the sync.
     */
    public String excludedFiles;
    protected String connectionName;
    protected String connectionType;
    protected String host;
    protected int port;
    protected int simultaneousJobs;
    protected String rootPath;
    protected String userName;
    protected String userPassword;
    protected boolean preserveTime;

    public ConnectionConfiguration(String connectionName) {
        this.connectionName = connectionName;
        this.excludedFiles = ".crt;.iml";
        this.preserveTime = false;
        this.simultaneousJobs = 2;
    }

    public String getConnectionName() {
        return connectionName;
    }

    public void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }


    /**
     * Gets a list of file types to be excluded from the sync.
     *
     * @return a string containing the ".ext" separated by ";" character,
     * representing a list of file types to be excluded from the sync.
     */
    public String getExcludedFiles() {
        return excludedFiles;
    }

    /**
     * Sets a list of file types to be excluded from the sync.
     *
     * @param excludedFiles a string containing the ".ext" separated by ";" character,
     *                      representing a list of file types to be excluded from the sync.
     */
    public void setExcludedFiles(String excludedFiles) {
        this.excludedFiles = excludedFiles;
    }

    public String getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host.replace("scp://", "").replace("sftp://", "").replace("ftp://", "").replace("ftps://", "");
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public boolean isPreserveTime() {
        return this.preserveTime;
    }

    public void setPreserveTime(boolean shouldPreserveTimestamp) {
        this.preserveTime = shouldPreserveTimestamp;
    }

    public int getSimultaneousJobs() {
        return simultaneousJobs;
    }

    public void setSimultaneousJobs(int simultaneousJobs) {
        this.simultaneousJobs = simultaneousJobs;
    }


}
