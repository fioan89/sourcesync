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
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.Path
import java.nio.file.Paths
import org.wavescale.sourcesync.SourcesyncBundle
import org.wavescale.sourcesync.api.Utils
import org.wavescale.sourcesync.configurations.AuthenticationType
import org.wavescale.sourcesync.configurations.ScpSyncConfiguration
import org.wavescale.sourcesync.notifications.Notifier
import org.wavescale.sourcesync.services.StatsService
import org.wavescale.sourcesync.services.SyncStatusService

class SCPFileSynchronizer(private val configuration: ScpSyncConfiguration, val project: Project) : Synchronizer {
    private val syncStatusService = service<SyncStatusService>()
    private val statsService = service<StatsService>()
    private val jsch: JSch = JSch()
    private var session: Session? = null

    private var isConnected: Boolean = false

    override fun connect(): Boolean {
        return if (!isConnected) {
            try {
                initSession()
                session!!.connect()
                isConnected = true
                true
            } catch (e: JSchException) {
                syncStatusService.removeRunningSync(configuration.name)
                Notifier.notifyError(
                    project,
                    SourcesyncBundle.message("scp.upload.fail.title"),
                    "Can't open SCP connection to ${configuration.hostname}. Reason: ${e.message}",
                )
                false
            }
        } else true
    }

    @Throws(JSchException::class)
    private fun initSession() {
        syncStatusService.addRunningSync(configuration.name)
        session = jsch.getSession(
            configuration.username, configuration.hostname,
            configuration.port.toInt()
        )
        session!!.setConfig("StrictHostKeyChecking", "no")
        if (configuration.authenticationType == AuthenticationType.KEY_PAIR) {
            session!!.setConfig("PreferredAuthentications", "publickey")
            try {
                Utils.createFile(SSH_KNOWN_HOSTS)
            } catch (e: IOException) {
                syncStatusService.removeRunningSync(configuration.name)
                Notifier.notifyError(
                    project,
                    SourcesyncBundle.message("scp.upload.fail.title"),
                    "Could not identify nor create the SSH known hosts file at $SSH_KNOWN_HOSTS. Reason: ${e.message}",
                )
                return
            }
            jsch.setKnownHosts(SSH_KNOWN_HOSTS)
            // add private key and passphrase if exists
            if (configuration.passphrase.isNullOrEmpty().not()) {
                jsch.addIdentity(configuration.privateKey, configuration.passphrase)
            } else {
                jsch.addIdentity(configuration.privateKey)
            }
        } else {
            session!!.setPassword(configuration.password)
        }
    }

    override fun disconnect() {
        try {
            session?.disconnect()
        } finally {
            isConnected = false
            syncStatusService.removeRunningSync(configuration.name)
        }
    }

    /**
     * Uploads the given file to the remote target.
     *
     * @param src     a `String` representing a file path to be uploaded. This is a relative path
     * to project base path.
     * @param uploadLocation a `String` representing a location path on the remote target
     * where the source will be uploaded.
     */
    override fun syncFile(src: String, uploadLocation: Path, indicator: ProgressIndicator) {
        val srcAsFile = File(src)
        var remotePath = Paths
            .get(configuration.workspaceBasePath)
            .resolve(uploadLocation)
            .pathStringLike(configuration.workspaceBasePath)

        var command = "scp " + (if (configuration.preserveTimestamps) "-p" else "") + " -t -C "
        if (srcAsFile.isFile) {
            remotePath = Paths
                .get(remotePath)
                .resolve(srcAsFile.name)
                .pathStringLike(configuration.workspaceBasePath)
            command += remotePath
        }
        try {
            val channel = session!!.openChannel("exec")
            (channel as ChannelExec).setCommand(command)

            // get I/O streams for remote scp
            val out = channel.getOutputStream()
            val inputStream = channel.getInputStream()
            channel.connect()
            if (checkAck(inputStream, this::onChannelConnectError) != 0) {
                return
            }
            indicator.isIndeterminate = false
            indicator.text = "Uploading...[" + srcAsFile.name + "]"
            if (configuration.preserveTimestamps) {
                command = "T " + srcAsFile.lastModified() / 1000 + " 0"
                // The access time should be sent here,
                // but it is not accessible with JavaAPI ;-<
                command += " " + (srcAsFile.lastModified() / 1000) + " 0\n"
                out.write(command.toByteArray())
                out.flush()
                if (checkAck(inputStream, this::onPreservingTimestampsError) != 0) {
                    return
                }
            }
            // send "C0644 filesize filename", where filename should not include '/'
            val filesize = srcAsFile.length()
            command = "C0644 $filesize "
            command += Paths.get(src).fileName.toString()
            command += "\n"
            out.write(command.toByteArray())
            out.flush()
            if (checkAck(inputStream, this::onSendingFileModesError) != 0) {
                return
            }

            // send content of finalSourcePath
            val fis = FileInputStream(src)
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
            if (checkAck(inputStream, this::onSendFileContentError) != 0) {
                return
            }
            out.close()
            channel.disconnect()
            statsService.registerSuccessfulUpload()
            if (statsService.eligibleForDonations()) {
                Notifier.notifyDonation(project)
            }
        } catch (e: Exception) {
            syncStatusService.removeRunningSync(configuration.name)
            Notifier.notifyError(
                project,
                SourcesyncBundle.message("scp.upload.fail.title"),
                "Upload to ${configuration.hostname} failed. Reason: ${e.message}",
            )
        }
    }

    private fun onChannelConnectError(errorCode: AckError, reason: String) {
        when (errorCode) {
            AckError.ERROR -> Notifier.notifyError(
                project,
                SourcesyncBundle.message("scp.upload.fail.title"),
                SourcesyncBundle.message("scp.upload.fail.channel.connect.error.message", configuration.hostname, reason)
            )

            AckError.FATAL_ERROR -> Notifier.notifyError(
                project,
                SourcesyncBundle.message("scp.upload.fail.title"),
                SourcesyncBundle.message("scp.upload.fail.channel.connect.fatal.error.message", configuration.hostname, reason)
            )

            AckError.UNKNOWN -> Unit
        }
    }

    private fun onPreservingTimestampsError(errorCode: AckError, reason: String) {
        when (errorCode) {
            AckError.ERROR -> Notifier.notifyError(
                project,
                SourcesyncBundle.message("scp.upload.fail.title"),
                SourcesyncBundle.message("scp.upload.fail.preserve.timestamps.error.message", reason)
            )

            AckError.FATAL_ERROR -> Notifier.notifyError(
                project,
                SourcesyncBundle.message("scp.upload.fail.title"),
                SourcesyncBundle.message("scp.upload.fail.preserve.timestamps.fatal.error.message", reason)
            )

            AckError.UNKNOWN -> Unit
        }
    }

    private fun onSendingFileModesError(errorCode: AckError, reason: String) {
        when (errorCode) {
            AckError.ERROR -> Notifier.notifyError(
                project,
                SourcesyncBundle.message("scp.upload.fail.title"),
                SourcesyncBundle.message("scp.upload.fail.file.mode.error.message", configuration.hostname, reason)
            )

            AckError.FATAL_ERROR -> Notifier.notifyError(
                project,
                SourcesyncBundle.message("scp.upload.fail.title"),
                SourcesyncBundle.message("scp.upload.fail.file.mode.fatal.error.message", configuration.hostname, reason)
            )

            AckError.UNKNOWN -> Unit
        }
    }

    private fun onSendFileContentError(errorCode: AckError, reason: String) {
        when (errorCode) {
            AckError.ERROR -> Notifier.notifyError(
                project,
                SourcesyncBundle.message("scp.upload.fail.title"),
                SourcesyncBundle.message("scp.upload.fail.file.content.error.message", configuration.hostname, reason)
            )

            AckError.FATAL_ERROR -> Notifier.notifyError(
                project,
                SourcesyncBundle.message("scp.upload.fail.title"),
                SourcesyncBundle.message("scp.upload.fail.file.content.fatal.error.message", configuration.hostname, reason)
            )

            AckError.UNKNOWN -> Unit
        }
    }

    @Throws(IOException::class)
    /**
     * Reads the server response (ack) for a client command
     */
    private fun checkAck(inStream: InputStream, onError: (errorCode: AckError, reason: String) -> Unit): Int {
        val b = inStream.read()
        // b may be 0 for success,
        // 1 for error,
        // 2 for fatal error,
        // -1
        if (b == 0) return b
        if (b == -1) return b

        val sb = StringBuilder()
        var c: Int
        do {
            c = inStream.read()
            sb.append(c.toChar())
        } while (c != '\n'.code)
        syncStatusService.removeRunningSync(configuration.name)
        onError(AckError.from(b), sb.toString())
        return b
    }

    private enum class AckError(private val code: Int) {
        ERROR(1), FATAL_ERROR(2), UNKNOWN(3);

        companion object {
            infix fun from(value: Int) = AckError.values().firstOrNull { it.code == value } ?: UNKNOWN
        }
    }

    companion object {
        val SSH_KNOWN_HOSTS = Paths.get(System.getProperty("user.home"), ".ssh", "known_hosts").toString()
    }
}