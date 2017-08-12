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
import java.io.IOException;
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
public class SFTPFileSynchronizer extends FileSynchronizer {

    public static final String SSH_KNOWN_HOSTS = Paths.get(System.getProperty("user.home"), ".ssh", "known_hosts").toString();
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
        this.getIndicator().setIndeterminate(true);
    }

    @Override
    public boolean connect() {
        if (!isConnected()) {
            try {
                initSession();
                session.setPassword(this.getConnectionInfo().getUserPassword());
                this.session.setConfig("StrictHostKeyChecking", "no");
                this.session.connect();
                this.setConnected(true);
                return true;
            } catch (JSchException e) {
                EventDataLogger.logWarning(e.toString(), this.getProject());
                return false;
            }
        }
        return true;
    }

    private void initSession() throws JSchException {
        SFTPConfiguration configuration = (SFTPConfiguration) this.getConnectionInfo();
        session = this.jsch.getSession(this.getConnectionInfo().getUserName(), this.getConnectionInfo().getHost(),
                this.getConnectionInfo().getPort());
        if (configuration.isPasswordlessSSHSelected()) {
            session.setConfig("PreferredAuthentications", "publickey");
            try {
                Utils.createFile(SSH_KNOWN_HOSTS);
            } catch (IOException e) {
                EventDataLogger.logError("Could not identify nor create the ssh known hosts file at " + SSH_KNOWN_HOSTS + ". The returned error is:" + e.getMessage(), this.getProject());
            }
            this.jsch.setKnownHosts(SSH_KNOWN_HOSTS);
            // add private key and passphrase if exists
            if (configuration.isPasswordlessWithPassphrase()) {
                this.jsch.addIdentity(configuration.getCertificatePath(), configuration.getUserPassword());
            } else {
                this.jsch.addIdentity(configuration.getCertificatePath());
            }

        } else {
            session.setPassword(this.getConnectionInfo().getUserPassword());
        }
    }

    @Override
    public void disconnect() {
        if (this.session != null) {
            this.session.disconnect();
            this.setConnected(false);
        }
    }

    @Override
    public void syncFile(String sourcePath, Path uploadLocation) {
        boolean preserveTimestamp = this.getConnectionInfo().isPreserveTime();
        Path remotePath = Paths.get(this.getConnectionInfo().getRootPath()).resolve(uploadLocation);

        ChannelSftp channelSftp;
        try {
            channelSftp = (ChannelSftp) this.session.openChannel("sftp");
            channelSftp.connect();
        } catch (JSchException e) {
            EventDataLogger.logError(e.toString(), this.getProject());
            return;
        }

        // first try to create the path where this must be uploaded
        try {
            channelSftp.cd("/");
        } catch (SftpException e) {
            EventDataLogger.logError("On remote we could not change directory into root: " + remotePath.getRoot(), this.getProject());
        }
        for (Path current : remotePath) {
            String location = current.toString();
            try {
                channelSftp.mkdir(location);
            } catch (SftpException e) {
                // this dir probably exist so just ignore
            }
            try {
                channelSftp.cd(location);
            } catch (SftpException e) {
                // probably it doesn't exist or maybe no permission
                EventDataLogger.logError("Remote dir <b>" + remotePath +
                        "</b> might not exist or you don't have permission on this path!", this.getProject());
                return;
            }
        }

        // upload file
        File toUpload = new File(sourcePath);
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
            EventDataLogger.logWarning(e.toString(), getProject());
        } catch (FileNotFoundException e) {
            EventDataLogger.logWarning(e.toString(), getProject());
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
                SFTPFileSynchronizer.this.getIndicator().setText("Uploading...[" + remoteFile.getName() + "]");
                SFTPFileSynchronizer.this.getIndicator().setIndeterminate(false);

            }

        }

        @Override
        public boolean count(long count) {
            totalUploaded += count;
            SFTPFileSynchronizer.this.getIndicator().setFraction(totalUploaded / totalLength);
            // false will kill the upload
            return true;
        }

        @Override
        public void end() {
            SFTPFileSynchronizer.this.getIndicator().setFraction(1.0);
        }
    }
}