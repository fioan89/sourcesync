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

import java.io.Serializable;

/**
 * Abstract class to hold config info about different type of connections.
 */

public abstract class ConnectionConfiguration implements Serializable {
    public String connectionName;
    public String connectionType;
    public String host;
    public int port;
    public String rootPath;
    public String userName;
    public String userPassword;
    /**
     * A string containing the ".ext" separated by ";" character,
     * representing a list of file types to be excluded from the sync.
     */
    public String excludedFiles;

    public ConnectionConfiguration(String connectionName) {
        this.connectionName = connectionName;
        this.excludedFiles = ".crt;.iml";
    }

    public String getConnectionName() {
        return connectionName;
    }

    public void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }


    /**
     * Gets a list of file types to be excluded from the sync.
     * @return a string containing the ".ext" separated by ";" character,
     * representing a list of file types to be excluded from the sync.
     */
    public String getExcludedFiles() {
        return excludedFiles;
    }

    /**
     * Sets a list of file types to be excluded from the sync.
     * @param excludedFiles a string containing the ".ext" separated by ";" character,
     * representing a list of file types to be excluded from the sync.
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
        this.host = host;
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
}
