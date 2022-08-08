package org.wavescale.sourcesync.synchronizer

import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.SftpException
import java.nio.file.Paths

/**
 * Checks if a remote and absolute path exists on remote
 */
fun ChannelSftp.absoluteDirExists(dirPath: String): Boolean {
    return try {
        val attrs = this.stat(dirPath)
        attrs != null && attrs.isDir
    } catch (e: SftpException) {
        false
    }
}

/**
 * Checks if a local path exists on the remote. The check is done against the working directory of the sftp session.
 */
fun ChannelSftp.localDirExistsOnRemote(dirPath: String): Boolean {
    val pwd = this.pwd()
    Paths.get(dirPath).forEach {
        try {
            val attrs = this.stat(it.toString())
            if (attrs != null && attrs.isDir) {
                this.cd(it.toString())
            } else {
                return@forEach
            }

        } catch (e: SftpException) {
            this.cd(pwd)
            return false
        }
    }

    this.cd(pwd)
    return true
}

/**
 * Creates a local path directory on the remote, with its parent directories as needed. The check is done against the working directory of the sftp session.
 */
fun ChannelSftp.mkLocalDirsOnRemote(dirPath: String): Boolean {
    if (this.localDirExistsOnRemote(dirPath)) {
        return true
    }

    val pwd = this.pwd()
    Paths.get(dirPath).forEach {
        if (this.localDirExistsOnRemote(it.toString())) {
            this.cd(it.toString())
        } else {
            try {
                this.mkdir(it.toString())
                this.cd(it.toString())
            } catch (e: SftpException) {
                this.cd(pwd)
                return false
            }
        }
    }
    this.cd(pwd)
    return true
}

/**
 * Changes working directory on remote to a local path value. The check is done against the working directory of the sftp session.
 */
fun ChannelSftp.cdLocalDirsOnRemote(dirPath: String): Boolean {
    if (!this.localDirExistsOnRemote(dirPath)) {
        return false
    }

    val pwd = this.pwd()
    Paths.get(dirPath).forEach {
        if (this.localDirExistsOnRemote(it.toString())) {
            this.cd(it.toString())
        } else {
            this.cd(pwd)
            return false
        }
    }
    return true
}