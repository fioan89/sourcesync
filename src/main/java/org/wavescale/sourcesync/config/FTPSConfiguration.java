package org.wavescale.sourcesync.config;

import org.wavescale.sourcesync.api.ConnectionConfiguration;
import org.wavescale.sourcesync.api.ConnectionConstants;

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
public class FTPSConfiguration extends ConnectionConfiguration {
    private boolean requireImplicitTLS;
    private boolean requireExplicitTLS;

    public FTPSConfiguration(String connectionName) {
        super(connectionName);
        this.connectionType = ConnectionConstants.CONN_TYPE_FTPS;
        this.port = 21;
        this.projectBasePath = "";
        this.host = "ftp://";
        this.userName = "";
        this.userPassword = "";
        this.requireImplicitTLS = false;
        this.requireExplicitTLS = true;
    }

    public boolean isRequireImplicitTLS() {
        return requireImplicitTLS;
    }

    public void setRequireImplicitTLS(boolean requireImplicitTLS) {
        this.requireImplicitTLS = requireImplicitTLS;
    }

    public boolean isRequireExplicitTLS() {
        return requireExplicitTLS;
    }

    public void setRequireExplicitTLS(boolean requireExplicitTLS) {
        this.requireExplicitTLS = requireExplicitTLS;
    }

}
