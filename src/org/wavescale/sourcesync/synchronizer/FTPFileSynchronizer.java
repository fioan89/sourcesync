package org.wavescale.sourcesync.synchronizer;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.jetbrains.annotations.NotNull;
import org.wavescale.sourcesync.api.FileSynchronizer;
import org.wavescale.sourcesync.api.Utils;
import org.wavescale.sourcesync.config.FTPConfiguration;
import org.wavescale.sourcesync.logger.EventDataLogger;

import java.io.*;

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
public class FTPFileSynchronizer extends FileSynchronizer {

    private final FTPClient ftp;

    public FTPFileSynchronizer(@NotNull FTPConfiguration connectionInfo, @NotNull Project project, @NotNull ProgressIndicator indicator) {
        super(connectionInfo, project, indicator);
        this.ftp = new FTPClient();
        this.getIndicator().setIndeterminate(true);
    }

    @Override
    public boolean connect() {
        if (!isConnected()) {
            try {
                this.ftp.connect(this.getConnectionInfo().getHost(), this.getConnectionInfo().getPort());
                this.ftp.login(this.getConnectionInfo().getUserName(), this.getConnectionInfo().getUserPassword());
                // use passive mode to bypass firewall conflicts
                this.ftp.enterLocalPassiveMode();
            } catch (IOException e) {
                EventDataLogger.logWarning(e.toString(), this.getProject());
                return false;
            }
            // check if successful connection
            if (!FTPReply.isPositiveCompletion(this.ftp.getReplyCode())) {
                EventDataLogger.logWarning("Connection to <b>" + this.getConnectionInfo().getHost() + "</b> failed!", this.getProject());
                return false;
            }
            this.setConnected(true);
        }
        return true;
    }

    @Override
    public void disconnect() {
        if (this.ftp != null) {
            try {
                ftp.disconnect();
                this.setConnected(false);
            } catch (IOException e) {
                EventDataLogger.logWarning(e.toString(), this.getProject());
            }
        }
    }

    @Override
    public void syncFile(String sourcePath, String destinationPath) {
        // preserve timestamp for now
        boolean preserveTimestamp = true;
        File localFile = new File(getProject().getBasePath(), sourcePath);
        String finalSourcePath = Utils.getUnixPath(localFile.getAbsolutePath());
        String remotePath = Utils.buildUnixPath(this.getConnectionInfo().getRootPath(), destinationPath);
        String[] dirsToCreate = Utils.splitPath(destinationPath);
        // change location to root path
        try {
            this.ftp.changeWorkingDirectory(Utils.getUnixPath(this.getConnectionInfo().getRootPath()));
        } catch (IOException e) {
            EventDataLogger.logError("Remote dir <b>" + this.getConnectionInfo().getRootPath() +
                    "</b> might not exist or you don't have permission on this path!", this.getProject());
            return;
        }
        // try to create
        for (String dirToCreate : dirsToCreate) {
            try {
                this.ftp.makeDirectory(dirToCreate);
            } catch (IOException e) {
                // this dir probably exist so just ignore now it will fail later
                // if there are other reasons this could not be executed.
            }
            try {
                this.ftp.changeWorkingDirectory(dirToCreate);
            } catch (IOException e) {
                // probably it doesn't exist or maybe no permission
                EventDataLogger.logError("Remote dir <b>" + remotePath +
                        "</b> might not exist or you don't have permission on this path!", this.getProject());
                return;
            }
        }

        // upload
        try {
            this.ftp.setFileType(FTP.BINARY_FILE_TYPE);
            FileInputStream in = new FileInputStream(finalSourcePath);
            OutputStream outputStream = this.ftp.storeFileStream(localFile.getName());
            this.getIndicator().setIndeterminate(false);
            this.getIndicator().setText("Uploading...[" + localFile.getName() + "]");
            byte[] buffer = new byte[1024];
            int len;
            double totalSize = localFile.length() + 0.0;
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
