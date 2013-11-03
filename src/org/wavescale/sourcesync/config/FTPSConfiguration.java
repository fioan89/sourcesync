package org.wavescale.sourcesync.config;

import org.wavescale.sourcesync.api.ConnectionConfiguration;
import org.wavescale.sourcesync.api.Constants;

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
public class FTPSConfiguration extends ConnectionConfiguration {
    private boolean requireImplicitTLS;
    private boolean requireExplicitTLS;

    public FTPSConfiguration() {
        this.connectionType = Constants.CONN_TYPE_FTPS;
        this.port = 21;
        this.rootPath = "";
        this.host = "ftp://";
        this.userName = "";
        this.userPassword = "";
        this.excludedFiles = "";
        this.requireImplicitTLS = false;
        this.requireExplicitTLS = true;
    }

    public boolean isRequireImplicitTLS() {
        return requireImplicitTLS;
    }

    public void setRequireImplicitTLS(boolean requireImplicitTLS) {
        this.requireImplicitTLS = requireImplicitTLS;
        setRequireExplicitTLS(!requireImplicitTLS);
    }

    public boolean isRequireExplicitTLS() {
        return requireExplicitTLS;
    }

    public void setRequireExplicitTLS(boolean requireExplicitTLS) {
        this.requireExplicitTLS = requireExplicitTLS;
        setRequireImplicitTLS(!requireExplicitTLS);
    }

}
