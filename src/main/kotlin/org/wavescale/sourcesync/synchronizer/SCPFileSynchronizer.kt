/**
 * ****************************************************************************
 * Copyright (c) 2014-2022 Faur Ioan-Aurel.                                     *
 * All rights reserved. This program and the accompanying materials             *
 * are made available under the terms of the MIT License                        *
 * which accompanies this distribution, and is available at                     *
 * http://opensource.org/licenses/MIT                                           *
 * *
 * For any issues or questions send an email at: fioan89@gmail.com              *
 * *****************************************************************************
 */
package org.wavescale.sourcesync.synchronizer

import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.JSchException
import com.jcraft.jsch.Session
import org.wavescale.sourcesync.SourcesyncBundle
import org.wavescale.sourcesync.api.FileSynchronizer
import org.wavescale.sourcesync.api.Utils
import org.wavescale.sourcesync.config.SCPConfiguration
import org.wavescale.sourcesync.notifications.Notifier
import org.wavescale.sourcesync.services.StatsService
import org.wavescale.sourcesync.services.SyncStatusService
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.Path
import java.nio.file.Paths

class SCPFileSynchronizer(connectionInfo: SCPConfiguration, project: Project, indicator: ProgressIndicator) :
    FileSynchronizer(connectionInfo, project, indicator) {
    private val syncStatusService = service<SyncStatusService>()
    private val statsService = service<StatsService>()
    private val jsch: JSch = JSch()
    private lateinit var session: Session

    init {
        this.indicator.isIndeterminate = true
    }

    override fun connect(): Boolean {
        return if (!isConnected) {
            try {
                initSession()
                session.connect()
                isConnected = true
                true
            } catch (e: JSchException) {
                syncStatusService.removeRunningSync(connectionInfo.connectionName)
                Notifier.notifyError(
                    project,
                    SourcesyncBundle.message("scp.upload.fail.text"),
                    "Can't open SCP connection to ${connectionInfo.host}. Reason: ${e.message}",
                )
                false
            }
        } else true
    }

    @Throws(JSchException::class)
    private fun initSession() {
        val configuration = connectionInfo as SCPConfiguration
        syncStatusService.addRunningSync(configuration.connectionName)
        session = jsch.getSession(
            connectionInfo.userName, connectionInfo.host,
            connectionInfo.port
        )
        session.setConfig("StrictHostKeyChecking", "no")
        if (configuration.isPasswordlessSSHSelected) {
            session.setConfig("PreferredAuthentications", "publickey")
            try {
                Utils.createFile(SSH_KNOWN_HOSTS)
            } catch (e: IOException) {
                syncStatusService.removeRunningSync(connectionInfo.connectionName)
                Notifier.notifyError(
                    project,
                    SourcesyncBundle.message("scp.upload.fail.text"),
                    "Could not identify nor create the SSH known hosts file at $SSH_KNOWN_HOSTS. Reason: ${e.message}",
                )
                return
            }
            jsch.setKnownHosts(SSH_KNOWN_HOSTS)
            // add private key and passphrase if exists
            if (configuration.isPasswordlessWithPassphrase) {
                jsch.addIdentity(configuration.certificatePath, configuration.userPassword)
            } else {
                jsch.addIdentity(configuration.certificatePath)
            }
        } else {
            session.setPassword(connectionInfo.userPassword)
        }
    }

    override fun disconnect() {
        session.disconnect()
        isConnected = false
        syncStatusService.removeRunningSync(connectionInfo.connectionName)
    }

    /**
     * Uploads the given file to the remote target.
     *
     * @param sourcePath     a `String` representing a file path to be uploaded. This is a relative path
     * to project base path.
     * @param uploadLocation a `String` representing a location path on the remote target
     * where the source will be uploaded.
     */
    override fun syncFile(sourcePath: String, uploadLocation: Path) {
        val preserveTimestamp = connectionInfo.isPreserveTime
        val remotePath = Paths.get(connectionInfo.workspaceBasePath).resolve(uploadLocation)
            .pathStringLike(connectionInfo.workspaceBasePath)
        try {
            var command = "scp " + (if (preserveTimestamp) "-p" else "") + " -t -C " + remotePath
            val channel = session.openChannel("exec")
            (channel as ChannelExec).setCommand(command)

            // get I/O streams for remote scp
            val out = channel.getOutputStream()
            val inputStream = channel.getInputStream()
            channel.connect()
            if (checkAck(inputStream) != 0) {
                return
            }
            val localFile = File(sourcePath)
            indicator.isIndeterminate = false
            indicator.text = "Uploading...[" + localFile.name + "]"
            if (preserveTimestamp) {
                command = "T " + localFile.lastModified() / 1000 + " 0"
                // The access time should be sent here,
                // but it is not accessible with JavaAPI ;-<
                command += " " + (localFile.lastModified() / 1000) + " 0\n"
                out.write(command.toByteArray())
                out.flush()
                if (checkAck(inputStream) != 0) {
                    return
                }
            }
            // send "C0644 filesize filename", where filename should not include '/'
            val filesize = localFile.length()
            command = "C0644 $filesize "
            command += Paths.get(sourcePath).fileName.toString()
            command += "\n"
            out.write(command.toByteArray())
            out.flush()
            if (checkAck(inputStream) != 0) {
                return
            }

            // send content of finalSourcePath
            val fis = FileInputStream(sourcePath)
            var totalUploaded = 0.0
            val buf = ByteArray(1024)
            while (true) {
                val len = fis.read(buf, 0, buf.size)
                if (len <= 0) break
                out.write(buf, 0, len) //out.flush();
                totalUploaded += len.toDouble()
                indicator.fraction = totalUploaded / filesize
            }
            fis.close()
            // send '\0'
            buf[0] = 0
            out.write(buf, 0, 1)
            out.flush()
            if (checkAck(inputStream) != 0) {
                return
            }
            out.close()
            channel.disconnect()
            statsService.registerSuccessfulUpload()
            if (statsService.eligibleForDonations()) {
                Notifier.notifyDonation(project)
            }
        } catch (e: Exception) {
            syncStatusService.removeRunningSync(connectionInfo.connectionName)
            Notifier.notifyError(
                project,
                SourcesyncBundle.message("scp.upload.fail.text"),
                "Upload to ${connectionInfo.host} failed. Reason: ${e.message}",
            )
        }
    }

    @Throws(IOException::class)
    private fun checkAck(inStream: InputStream): Int {
        val b = inStream.read()
        // b may be 0 for success,
        // 1 for error,
        // 2 for fatal error,
        // -1
        if (b == 0) return b
        if (b == -1) return b
        if (b == 1 || b == 2) {
            val sb = StringBuilder()
            var c: Int
            do {
                c = inStream.read()
                sb.append(c.toChar())
            } while (c != '\n'.code)
            if (b == 1) {
                // error
                syncStatusService.removeRunningSync(connectionInfo.connectionName)
                Notifier.notifyError(
                    project,
                    SourcesyncBundle.message("scp.upload.fail.text"),
                    "Could not initiate SCP connection to ${connectionInfo.host} because of an error."
                )
            }
            if (b == 2) {
                // fatal error
                syncStatusService.removeRunningSync(connectionInfo.connectionName)
                Notifier.notifyError(
                    project,
                    SourcesyncBundle.message("scp.upload.fail.text"),
                    "Could not initiate SCP connection to ${connectionInfo.host} because of a fatal error."
                )
            }
        }
        return b
    }

    companion object {
        val SSH_KNOWN_HOSTS = Paths.get(System.getProperty("user.home"), ".ssh", "known_hosts").toString()
    }
}