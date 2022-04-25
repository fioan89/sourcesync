package org.wavescale.sourcesync.synchronizer;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.jetbrains.annotations.NotNull;
import org.wavescale.sourcesync.api.FileSynchronizer;
import org.wavescale.sourcesync.config.FTPSConfiguration;
import org.wavescale.sourcesync.logger.EventDataLogger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

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
public class FTPSFileSynchronizer extends FileSynchronizer {
    private final FTPSClient ftps;

    public FTPSFileSynchronizer(@NotNull FTPSConfiguration connectionInfo, @NotNull Project project, @NotNull ProgressIndicator indicator) {
        super(connectionInfo, project, indicator);
        this.ftps = new FTPSClient(connectionInfo.isRequireImplicitTLS());
        this.getIndicator().setIndeterminate(true);
    }

    @Override
    public boolean connect() {
        if (!isConnected()) {
            try {
                this.ftps.connect(this.getConnectionInfo().getHost(), this.getConnectionInfo().getPort());
                this.ftps.login(this.getConnectionInfo().getUserName(), this.getConnectionInfo().getUserPassword());
                // use passive mode to bypass firewall conflicts
                this.ftps.execPBSZ(0);  // Set protection buffer size
                this.ftps.execPROT("P"); // Set data channel protection to private
                this.ftps.enterLocalPassiveMode();
            } catch (IOException e) {
                EventDataLogger.logWarning(e.toString(), this.getProject());
                return false;
            }
            // check if successful connection
            if (!FTPReply.isPositiveCompletion(this.ftps.getReplyCode())) {
                EventDataLogger.logWarning("Connection to <b>" + this.getConnectionInfo().getHost() + "</b> failed!", this.getProject());
                return false;
            }
            this.setConnected(true);
        }
        return true;
    }

    @Override
    public void disconnect() {
        if (this.ftps != null) {
            try {
                ftps.logout();
                ftps.disconnect();
                this.setConnected(false);
            } catch (IOException e) {
                EventDataLogger.logWarning(e.toString(), this.getProject());
            }
        }
    }

    @Override
    public void syncFile(String sourceLocation, Path uploadLocation) {
        // preserve timestamp for now
        boolean preserveTimestamp = true;
        Path sourcePathLocation = Paths.get(sourceLocation);
        String sourceFileName = sourcePathLocation.getFileName().toString();
        Path remotePath = Paths.get(this.getConnectionInfo().getRootPath()).resolve(uploadLocation);

        // first try to create the path where this must be uploaded
        try {
            this.ftps.changeWorkingDirectory(remotePath.getRoot().toString());
        } catch (IOException e) {
            EventDataLogger.logError("On remote we could not change directory into root: " + remotePath.getRoot(), this.getProject());
        }
        for (Path current : remotePath) {
            String location = current.toString();
            try {
                this.ftps.makeDirectory(location);
            } catch (IOException e) {
                // this dir probably exist so just ignore now it will fail later
                // if there are other reasons this could not be executed.
            }
            try {
                this.ftps.changeWorkingDirectory(location);
            } catch (IOException e) {
                // probably it doesn't exist or maybe no permission
                EventDataLogger.logError("Remote dir <b>" + remotePath +
                        "</b> might not exist or you don't have permission on this path!", this.getProject());
                return;
            }
        }

        // upload
        try {
            this.ftps.setFileType(FTP.BINARY_FILE_TYPE);
            FileInputStream in = new FileInputStream(sourceLocation);
            OutputStream outputStream = this.ftps.storeFileStream(sourceFileName);
            this.getIndicator().setIndeterminate(false);
            this.getIndicator().setText("Uploading...[" + sourceFileName + "]");
            byte[] buffer = new byte[1024];
            int len;
            double totalSize = sourcePathLocation.toFile().length() + 0.0;
            long totalUploaded = 0;
            while (true) {
                len = in.read(buffer, 0, buffer.length);
                if (len <= 0) {
                    break;
                }
                outputStream.write(buffer, 0, len);
                totalUploaded += len;
                this.getIndicator().setFraction(totalUploaded / totalSize);
            }
            if (preserveTimestamp) {
                // TODO - implement preserve timestamp mechanism
            }
            in.close();
            outputStream.close();
        } catch (FileNotFoundException e) {
            EventDataLogger.logWarning(e.toString(), getProject());
        } catch (IOException e) {
            EventDataLogger.logError(e.toString(), getProject());
        }

    }
}
