package org.wavescale.sourcesync.config;

import org.wavescale.sourcesync.api.ConnectionConfiguration;
import org.wavescale.sourcesync.api.ConnectionConstants;

import java.io.File;

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
    private boolean shouldUseCertificate;
    private String certificatePath;

    public SFTPConfiguration(String connectionName) {
        super(connectionName);
        this.connectionType = ConnectionConstants.CONN_TYPE_SFTP;
        this.port = 22;
        this.rootPath = "";
        this.host = "sftp://";
        this.userName = "";
        this.userPassword = "";
        this.shouldUseCertificate = false;
        this.certificatePath = new File(System.getProperty("user.home") + File.separator + ".ssh" + File.separator + "id_rsa").getAbsolutePath();

    }

    public boolean isPasswordlessSSHSelected() {
        return shouldUseCertificate;
    }

    public void setPasswordlessSSHSelected(boolean shouldUseCertificate) {
        this.shouldUseCertificate = shouldUseCertificate;
    }

    public String getCertificatePath() {
        return certificatePath;
    }

    public void setCertificatePath(String certificatePath) {
        this.certificatePath = certificatePath;
    }
}
