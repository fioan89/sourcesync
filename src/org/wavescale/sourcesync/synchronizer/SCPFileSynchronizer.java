package org.wavescale.sourcesync.synchronizer;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.jcraft.jsch.*;
import org.jetbrains.annotations.NotNull;
import org.wavescale.sourcesync.api.FileSynchronizer;
import org.wavescale.sourcesync.config.SCPConfiguration;
import org.wavescale.sourcesync.logger.BalloonLogger;
import org.wavescale.sourcesync.logger.EventDataLogger;

import java.io.*;

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
public class SCPFileSynchronizer extends FileSynchronizer {
    private JSch jsch;
    private Session session;


    public SCPFileSynchronizer(@NotNull SCPConfiguration connectionInfo, @NotNull Project project, @NotNull ProgressIndicator indicator) {
        super(connectionInfo, project, indicator);
        this.jsch = new JSch();
        this.indicator.setIndeterminate(true);
    }

    @Override
    public boolean connect() {
        try {
            this.session = jsch.getSession(this.connectionInfo.getUserName(), this.connectionInfo.getHost(),
                    this.connectionInfo.getPort());
            this.session.setPassword(this.connectionInfo.getUserPassword());
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
        // preserve timestamp for now
        boolean preserveTimestamp = true;
        // exec 'scp -t rfile' remotely
        String finalSourcePath = new File(project.getBasePath(), sourcePath).getAbsolutePath();
        String remotePath = new File(this.connectionInfo.getRootPath(), destinationPath).getPath();

        try {
            String command = "scp " + (preserveTimestamp ? "-p" : "") + " -t -C " + remotePath;
            Channel channel = this.session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);

            // get I/O streams for remote scp
            OutputStream out = channel.getOutputStream();
            InputStream in = channel.getInputStream();

            channel.connect();

            if (checkAck(in) != 0) {
                return;
            }

            File _lfile = new File(finalSourcePath);
            this.indicator.setIndeterminate(false);
            this.indicator.setText("Uploading...[" + _lfile.getName() + "]");
            if (preserveTimestamp) {
                command = "T " + (_lfile.lastModified() / 1000) + " 0";
                // The access time should be sent here,
                // but it is not accessible with JavaAPI ;-<
                command += (" " + (_lfile.lastModified() / 1000) + " 0\n");
                out.write(command.getBytes());
                out.flush();
                if (checkAck(in) != 0) {
                    return;
                }
            }
            // send "C0644 filesize filename", where filename should not include '/'
            long filesize = _lfile.length();
            command = "C0644 " + filesize + " ";
            if (finalSourcePath.lastIndexOf('/') > 0) {
                command += finalSourcePath.substring(finalSourcePath.lastIndexOf('/') + 1);
            } else {
                command += finalSourcePath;
            }
            command += "\n";
            out.write(command.getBytes());
            out.flush();
            if (checkAck(in) != 0) {
                return;
            }

            // send content of finalSourcePath
            FileInputStream fis = new FileInputStream(finalSourcePath);
            double totalUploaded = 0.0;
            byte[] buf = new byte[1024];
            while (true) {
                int len = fis.read(buf, 0, buf.length);
                if (len <= 0) break;
                out.write(buf, 0, len); //out.flush();
                totalUploaded += len;
                this.indicator.setFraction(totalUploaded / filesize);
            }
            fis.close();
            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();
            if (checkAck(in) != 0) {
                return;
            }
            out.close();
            channel.disconnect();
        } catch (IOException e) {
            EventDataLogger.logWarning(e.toString(), project);
        } catch (JSchException e) {
            EventDataLogger.logWarning(e.toString(), project);
        }

    }

    private int checkAck(InputStream in) throws IOException {
        int b = in.read();
        // b may be 0 for success,
        // 1 for error,
        // 2 for fatal error,
        // -1
        if (b == 0) return b;
        if (b == -1) return b;

        if (b == 1 || b == 2) {
            StringBuilder sb = new StringBuilder();
            int c;
            do {
                c = in.read();
                sb.append((char) c);
            }
            while (c != '\n');
            if (b == 1) { // error
                BalloonLogger.logBalloonError(sb.toString(), this.project);
                EventDataLogger.logError(sb.toString(), project);
            }
            if (b == 2) { // fatal error
                BalloonLogger.logBalloonError(sb.toString(), this.project);
                EventDataLogger.logError(sb.toString(), project);
            }
        }
        return b;
    }
}
