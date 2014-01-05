package org.wavescale.sourcesync.config;

import org.wavescale.sourcesync.api.ConnectionConfiguration;
import org.wavescale.sourcesync.api.ConnectionConstants;

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
public class SFTPConfiguration extends ConnectionConfiguration {

    public SFTPConfiguration(String connectionName) {
        super(connectionName);
        this.connectionType = ConnectionConstants.CONN_TYPE_SFTP;
        this.port = 22;
        this.rootPath = "";
        this.host = "sftp://";
        this.userName = "";
        this.userPassword = "";
    }
}
