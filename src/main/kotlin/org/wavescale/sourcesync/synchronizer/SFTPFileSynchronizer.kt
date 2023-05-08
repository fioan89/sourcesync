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
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.JSchException
import com.jcraft.jsch.Session
import com.jcraft.jsch.SftpProgressMonitor
import org.wavescale.sourcesync.SourcesyncBundle
import org.wavescale.sourcesync.api.Utils
import org.wavescale.sourcesync.configurations.AuthenticationType
import org.wavescale.sourcesync.configurations.SshSyncConfiguration
import org.wavescale.sourcesync.notifications.Notifier
import org.wavescale.sourcesync.services.StatsService
import org.wavescale.sourcesync.services.SyncStatusService
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths


/**
 * Build a file synchronizer from general info contained by **configuration** param.
 */
class SFTPFileSynchronizer(private val configuration: SshSyncConfiguration, val project: Project) : Synchronizer {
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
                    SourcesyncBundle.message("ssh.upload.fail.text"),
                    "Can't open SSH connection to ${configuration.hostname}. Reason: ${e.message}",
                )
                false
            }
        } else true
    }

    @Throws(JSchException::class)
    private fun initSession() {
        syncStatusService.addRunningSync(configuration.name)
        session = jsch.getSession(configuration.username, configuration.hostname, configuration.port.toInt())
        session!!.setConfig("StrictHostKeyChecking", "no")
        if (configuration.authenticationType == AuthenticationType.KEY_PAIR) {
            session!!.setConfig("PreferredAuthentications", "publickey")
            try {
                Utils.createFile(SSH_KNOWN_HOSTS)
            } catch (e: IOException) {
                syncStatusService.removeRunningSync(configuration.name)
                Notifier.notifyError(
                    project,
                    SourcesyncBundle.message("ssh.upload.fail.text"),
                    "Could not identify nor create the SSH known hosts file at ${SCPFileSynchronizer.SSH_KNOWN_HOSTS}. Reason: ${e.message}",
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

    override fun syncFile(sourcePath: String, uploadLocation: Path, indicator: ProgressIndicator) {
        val preserveTimestamp = configuration.preserveTimestamps
        val channelSftp: ChannelSftp
        try {
            channelSftp = session!!.openChannel("sftp") as ChannelSftp
            channelSftp.connect()
        } catch (e: JSchException) {
            syncStatusService.removeRunningSync(configuration.name)
            Notifier.notifyError(
                project,
                SourcesyncBundle.message("ssh.upload.fail.text"),
                "An error was encountered while trying to open a SSH connection to ${configuration.hostname}. Reason: ${e.message}",
            )
            return
        }

        if (!channelSftp.absoluteDirExists(configuration.workspaceBasePath)) {
            syncStatusService.removeRunningSync(configuration.name)
            Notifier.notifyError(
                project,
                SourcesyncBundle.message("ssh.upload.fail.text"),
                "Remote project base path ${configuration.workspaceBasePath} does not exist or is not a directory. Please make sure the value is a valid absolute directory path on ${configuration.hostname}",
            )
            return
        }

        channelSftp.cd(configuration.workspaceBasePath)

        if (!channelSftp.localDirExistsOnRemote(uploadLocation.toString())) {
            logger.info("Upload path $uploadLocation does not exist or is not a directory. Going to create it.")
            val exists = channelSftp.mkLocalDirsOnRemote(uploadLocation.toString())
            if (!exists) {
                syncStatusService.removeRunningSync(configuration.name)
                Notifier.notifyError(
                    project,
                    SourcesyncBundle.message("ssh.upload.fail.text"),
                    "Upload path $uploadLocation could not be created on ${configuration.hostname}"
                )
                return
            }
        }
        if (!channelSftp.cdLocalDirsOnRemote(uploadLocation.toString())) {
            syncStatusService.removeRunningSync(configuration.name)
            Notifier.notifyError(
                project,
                SourcesyncBundle.message("ssh.upload.fail.text"),
                "Could not change directory to $uploadLocation"
            )
            return
        }

        // upload file
        val toUpload = File(sourcePath)
        val progressMonitor: SftpProgressMonitor = SftpMonitor(toUpload.length(), indicator)
        try {
            channelSftp.put(FileInputStream(toUpload), toUpload.name, progressMonitor, ChannelSftp.OVERWRITE)
            if (preserveTimestamp) {
                val sftpATTRS = channelSftp.lstat(toUpload.name)
                val lastAcc = sftpATTRS.aTime
                // this is a messed method: if lastModified is greater than Integer.MAX_VALUE
                // then timestamp will not be ok.
                sftpATTRS.setACMODTIME(lastAcc, java.lang.Long.valueOf(toUpload.lastModified() / 1000).toInt())
                channelSftp.setStat(toUpload.name, sftpATTRS)
            }
        } catch (e: Exception) {
            syncStatusService.removeRunningSync(configuration.name)
            Notifier.notifyError(
                project,
                SourcesyncBundle.message("ssh.upload.fail.text"),
                "Upload to ${configuration.hostname} failed. Reason: ${e.message}"
            )
        }
        channelSftp.disconnect()
    }

    private inner class SftpMonitor(totalLength: Long, private val indicator: ProgressIndicator) : SftpProgressMonitor {
        val totalLength: Double
        var totalUploaded: Long

        init {
            this.totalLength = totalLength + 0.0
            totalUploaded = 0
        }

        override fun init(opcode: Int, src: String, dest: String, max: Long) {
            val remoteFile = File(dest)
            if (SftpProgressMonitor.PUT == opcode) {
                indicator.text = "Uploading...[" + remoteFile.name + "]"
                indicator.isIndeterminate = false
            }
        }

        override fun count(count: Long): Boolean {
            totalUploaded += count
            indicator.fraction = totalUploaded / totalLength
            // false will kill the upload
            return true
        }

        override fun end() {
            indicator.fraction = 1.0
            statsService.registerSuccessfulUpload()
            if (statsService.eligibleForDonations()) {
                Notifier.notifyDonation(project)
            }
        }
    }

    companion object {
        val SSH_KNOWN_HOSTS = Paths.get(System.getProperty("user.home"), ".ssh", "known_hosts").toString()

        private val logger = logger<SFTPFileSynchronizer>()
    }
}