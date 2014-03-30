package org.wavescale.sourcesync.synchronizer;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.jcraft.jsch.*;
import org.jetbrains.annotations.NotNull;
import org.wavescale.sourcesync.api.FileSynchronizer;
import org.wavescale.sourcesync.api.Utils;
import org.wavescale.sourcesync.config.SFTPConfiguration;
import org.wavescale.sourcesync.logger.EventDataLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

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
public class SFTPFileSynchronizer extends FileSynchronizer {

    private final JSch jsch;
    private Session session;

    /**
     * Build a file synchronizer from general info contained by <b>connectionInfo</b> param.
     *
     * @param connectionInfo a {@link org.wavescale.sourcesync.config.SFTPConfiguration} instance
     *                       containing session info like hostname, user, password, etc...
     * @param project        a {@link com.intellij.openapi.project.Project} instance used to gather project relative
     *                       metadata like name, absoulte path, etc...
     * @param indicator      used to report progress on upload process.
     */
    public SFTPFileSynchronizer(@NotNull SFTPConfiguration connectionInfo, @NotNull Project project, @NotNull ProgressIndicator indicator) {
        super(connectionInfo, project, indicator);
        this.jsch = new JSch();
        this.indicator.setIndeterminate(true);
    }

    @Override
    public boolean connect() {
        try {
            session = this.jsch.getSession(this.connectionInfo.getUserName(), this.connectionInfo.getHost(),
                    this.connectionInfo.getPort());
            session.setPassword(this.connectionInfo.getUserPassword());
            this.session.setConfig("StrictHostKeyChecking", "no");
            this.session.connect();
            return true;
        } catch (JSchException e) {
            EventDataLogger.logWarning(e.toString(), this.project);
            return false;
        }
    }

    @Override
    public void disconnect() {
        if (this.session != null) {
            this.session.disconnect();
        }
    }

    /**
     * Uploads the given file to the remote target.
     *
     * @param sourcePath      a <code>String</code> representing a file path to be uploaded. This is a relative path
     *                        to project base path.
     * @param destinationPath a <code>String</code> representing a location path on the remote target
     *                        where the source will be uploaded.
     */
    @Override
    public void syncFile(String sourcePath, String destinationPath) {
        boolean preserveTimestamp = this.connectionInfo.isPreserveTime();
        String finalSourcePath = new File(project.getBasePath(), sourcePath).getAbsolutePath();
        String remotePath = new File(this.connectionInfo.getRootPath(), destinationPath).getPath();

        String[] dirsToCreate = Utils.splitPath(destinationPath);
        ChannelSftp channelSftp;
        try {
            channelSftp = (ChannelSftp) this.session.openChannel("sftp");
            channelSftp.connect();
            channelSftp.cd(Utils.getUnixPath(this.connectionInfo.getRootPath()));
        } catch (JSchException e) {
            EventDataLogger.logError(e.toString(), this.project);
            return;
        } catch (SftpException e) {
            EventDataLogger.logError("Remote dir <b>" + this.connectionInfo.getRootPath() +
                    "</b> might not exist or you don't have permission on this path!", this.project);
            return;
        }
        // first try to create the path where this must be uploaded
        for (String dirToCreate : dirsToCreate) {
            try {
                channelSftp.mkdir(dirToCreate);
            } catch (SftpException e) {
                // this dir probably exist so just ignore
            }
            try {
                channelSftp.cd(dirToCreate);
            } catch (SftpException e) {
                // probably it doesn't exist or maybe no permission
                EventDataLogger.logError("Remote dir <b>" + remotePath +
                        "</b> might not exist or you don't have permission on this path!", this.project);
                return;
            }
        }
        // upload file
        File toUpload = new File(finalSourcePath);
        SftpProgressMonitor progressMonitor = new SftpMonitor(toUpload.length());
        try {
            channelSftp.put(new FileInputStream(toUpload), toUpload.getName(), progressMonitor, ChannelSftp.OVERWRITE);
            if (preserveTimestamp) {
                SftpATTRS sftpATTRS = channelSftp.lstat(toUpload.getName());
                int lastAcc = sftpATTRS.getATime();
                // this is a messed method: if lastModified is greater than Integer.MAX_VALUE
                // then timestamp will not be ok.
                sftpATTRS.setACMODTIME(lastAcc, new Long(toUpload.lastModified() / 1000).intValue());
                channelSftp.setStat(toUpload.getName(), sftpATTRS);
            }
        } catch (SftpException e) {
            EventDataLogger.logWarning(e.toString(), project);
        } catch (FileNotFoundException e) {
            EventDataLogger.logWarning(e.toString(), project);
        }

        channelSftp.disconnect();
    }

    private class SftpMonitor implements SftpProgressMonitor {
        final double totalLength;
        long totalUploaded;

        public SftpMonitor(long totalLength) {
            this.totalLength = totalLength + 0.0;
            this.totalUploaded = 0;
        }

        @Override
        public void init(int opcode, String src, String dest, long max) {
            File remoteFile = new File(dest);
            if (SftpProgressMonitor.PUT == opcode) {
                SFTPFileSynchronizer.this.indicator.setText("Uploading...[" + remoteFile.getName() + "]");
                SFTPFileSynchronizer.this.indicator.setIndeterminate(false);

            }

        }

        @Override
        public boolean count(long count) {
            totalUploaded += count;
            SFTPFileSynchronizer.this.indicator.setFraction(totalUploaded / totalLength);
            // false will kill the upload
            return true;
        }

        @Override
        public void end() {
            SFTPFileSynchronizer.this.indicator.setFraction(1.0);
        }
    }
}