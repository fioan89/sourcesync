package org.wavescale.sourcesync.synchronizer;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.jetbrains.annotations.NotNull;
import org.wavescale.sourcesync.api.FileSynchronizer;
import org.wavescale.sourcesync.api.Utils;
import org.wavescale.sourcesync.config.FTPSConfiguration;
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
public class FTPSFileSynchronizer extends FileSynchronizer {
    private final FTPSClient ftps;

    public FTPSFileSynchronizer(@NotNull FTPSConfiguration connectionInfo, @NotNull Project project, @NotNull ProgressIndicator indicator) {
        super(connectionInfo, project, indicator);
        this.ftps = new FTPSClient(connectionInfo.isRequireImplicitTLS());
        this.indicator.setIndeterminate(true);
    }

    @Override
    public boolean connect() {
        try {
            this.ftps.connect(this.connectionInfo.getHost(), this.connectionInfo.getPort());
            this.ftps.login(this.connectionInfo.getUserName(), this.connectionInfo.getUserPassword());
            // use passive mode to bypass firewall conflicts
            this.ftps.enterLocalPassiveMode();
        } catch (IOException e) {
            EventDataLogger.logWarning(e.toString(), this.project);
            return false;
        }
        // check if successful connection
        if (!FTPReply.isPositiveCompletion(this.ftps.getReplyCode())) {
            EventDataLogger.logWarning("Connection to <b>" + this.connectionInfo.getHost() + "</b> failed!", this.project);
            return false;
        }
        return true;
    }

    @Override
    public void disconnect() {
        if (this.ftps != null) {
            try {
                ftps.logout();
                ftps.disconnect();
            } catch (IOException e) {
                EventDataLogger.logWarning(e.toString(), this.project);
            }
        }
    }

    @Override
    public void syncFile(String sourcePath, String destinationPath) {
        // preserve timestamp for now
        boolean preserveTimestamp = true;
        File localFile = new File(project.getBasePath(), sourcePath);
        String finalSourcePath = Utils.getUnixPath(localFile.getAbsolutePath());
        String remotePath = Utils.buildUnixPath(this.connectionInfo.getRootPath(), destinationPath);
        String[] dirsToCreate = Utils.splitPath(destinationPath);
        // change location to root path
        try {
            this.ftps.changeWorkingDirectory(Utils.getUnixPath(this.connectionInfo.getRootPath()));
        } catch (IOException e) {
            EventDataLogger.logError("Remote dir <b>" + this.connectionInfo.getRootPath() +
                    "</b> might not exist or you don't have permission on this path!", this.project);
            return;
        }
        // try to create
        for (String dirToCreate : dirsToCreate) {
            try {
                this.ftps.makeDirectory(dirToCreate);
            } catch (IOException e) {
                // this dir probably exist so just ignore now it will fail later
                // if there are other reasons this could not be executed.
            }
            try {
                this.ftps.changeWorkingDirectory(dirToCreate);
            } catch (IOException e) {
                // probably it doesn't exist or maybe no permission
                EventDataLogger.logError("Remote dir <b>" + remotePath +
                        "</b> might not exist or you don't have permission on this path!", this.project);
                return;
            }
        }

        // upload
        try {
            this.ftps.setFileType(FTP.BINARY_FILE_TYPE);
            FileInputStream in = new FileInputStream(finalSourcePath);
            OutputStream outputStream = this.ftps.storeFileStream(localFile.getName());
            this.indicator.setIndeterminate(false);
            this.indicator.setText("Uploading...[" + localFile.getName() + "]");
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
                this.indicator.setFraction(totalUploaded / totalSize);
            }
            if (preserveTimestamp) {
                // TODO - implement preserve timestamp mechanism
            }
            in.close();
            outputStream.close();
        } catch (FileNotFoundException e) {
            EventDataLogger.logWarning(e.toString(), project);
        } catch (IOException e) {
            EventDataLogger.logError(e.toString(), project);
        }

    }
}
