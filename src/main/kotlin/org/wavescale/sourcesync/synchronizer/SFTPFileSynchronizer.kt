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

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.JSchException
import com.jcraft.jsch.Session
import com.jcraft.jsch.SftpException
import com.jcraft.jsch.SftpProgressMonitor
import org.wavescale.sourcesync.api.FileSynchronizer
import org.wavescale.sourcesync.api.Utils
import org.wavescale.sourcesync.config.SFTPConfiguration
import org.wavescale.sourcesync.logger.EventDataLogger
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths


/**
 * Build a file synchronizer from general info contained by **connectionInfo** param.
 *
 * @param connectionInfo a [org.wavescale.sourcesync.config.SFTPConfiguration] instance
 * containing session info like hostname, user, password, etc...
 * @param project        a [com.intellij.openapi.project.Project] instance used to gather project relative
 * metadata like name, absoulte path, etc...
 * @param indicator      used to report progress on upload process.
 */
class SFTPFileSynchronizer(connectionInfo: SFTPConfiguration, project: Project, indicator: ProgressIndicator) : FileSynchronizer(connectionInfo, project, indicator) {
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
                EventDataLogger.logWarning(e.toString(), project)
                false
            }
        } else true
    }

    @Throws(JSchException::class)
    private fun initSession() {
        val configuration = connectionInfo as SFTPConfiguration
        session = jsch.getSession(connectionInfo.userName, connectionInfo.host,
                connectionInfo.port)
        session.setConfig("StrictHostKeyChecking", "no")
        if (configuration.isPasswordlessSSHSelected) {
            session.setConfig("PreferredAuthentications", "publickey")
            try {
                Utils.createFile(SSH_KNOWN_HOSTS)
            } catch (e: IOException) {
                EventDataLogger.logError("Could not identify nor create the ssh known hosts file at " + SSH_KNOWN_HOSTS + ". The returned error is:" + e.message, project)
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
        if (session != null) {
            session!!.disconnect()
            isConnected = false
        }
    }

    override fun syncFile(sourcePath: String, uploadLocation: Path) {
        val preserveTimestamp = connectionInfo.isPreserveTime
        val remotePath = Paths.get(connectionInfo.projectBasePath).resolve(uploadLocation)
        val channelSftp: ChannelSftp
        try {
            channelSftp = session!!.openChannel("sftp") as ChannelSftp
            channelSftp.connect()
        } catch (e: JSchException) {
            EventDataLogger.logError(e.toString(), project)
            return
        }

        // first try to create the path where this must be uploaded
        try {
            channelSftp.cd(remotePath.root.toString())
        } catch (e: SftpException) {
            EventDataLogger.logError("On remote we could not change directory into root: " + remotePath.root, project)
        }
        if (remotePath.parent != null) {
            for (current in remotePath.parent) {
                val location = current.toString()
                try {
                    channelSftp.mkdir(location)
                } catch (e: SftpException) {
                    // this dir probably exist so just ignore
                }
                try {
                    channelSftp.cd(location)
                } catch (e: SftpException) {
                    // probably it doesn't exist or maybe no permission
                    EventDataLogger.logError("Remote dir <b>" + remotePath +
                            "</b> might not exist or you don't have permission on this path!", project)
                    return
                }
            }
        }

        // upload file
        val toUpload = File(sourcePath)
        val progressMonitor: SftpProgressMonitor = SftpMonitor(toUpload.length())
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
        } catch (e: SftpException) {
            EventDataLogger.logWarning(e.toString(), project)
        } catch (e: FileNotFoundException) {
            EventDataLogger.logWarning(e.toString(), project)
        }
        channelSftp.disconnect()
    }

    private inner class SftpMonitor(totalLength: Long) : SftpProgressMonitor {
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
        }
    }

    companion object {
        val SSH_KNOWN_HOSTS = Paths.get(System.getProperty("user.home"), ".ssh", "known_hosts").toString()
    }
}