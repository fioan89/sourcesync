package org.wavescale.sourcesync.configurations

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("ssh_configuration")
class SshSyncConfiguration : BaseSyncConfiguration() {
    override val protocol = SyncConfigurationType.SFTP

    override fun clone() = SshSyncConfiguration().apply {
        name = this@SshSyncConfiguration.name
        hostname = this@SshSyncConfiguration.hostname
        port = this@SshSyncConfiguration.port
        username = this@SshSyncConfiguration.username
        authenticationType = this@SshSyncConfiguration.authenticationType
        password = this@SshSyncConfiguration.password
        workspaceBasePath = this@SshSyncConfiguration.workspaceBasePath
        excludedFiles = this@SshSyncConfiguration.excludedFiles
        preserveTimestamps = this@SshSyncConfiguration.preserveTimestamps
        certificatePath = this@SshSyncConfiguration.certificatePath
        passphrase = this@SshSyncConfiguration.passphrase
    }
}

@Serializable
@SerialName("scp_configuration")
class ScpSyncConfiguration : BaseSyncConfiguration() {
    override val protocol = SyncConfigurationType.SCP

    override fun clone() = ScpSyncConfiguration().apply {
        name = this@ScpSyncConfiguration.name
        hostname = this@ScpSyncConfiguration.hostname
        port = this@ScpSyncConfiguration.port
        username = this@ScpSyncConfiguration.username
        authenticationType = this@ScpSyncConfiguration.authenticationType
        password = this@ScpSyncConfiguration.password
        workspaceBasePath = this@ScpSyncConfiguration.workspaceBasePath
        excludedFiles = this@ScpSyncConfiguration.excludedFiles
        preserveTimestamps = this@ScpSyncConfiguration.preserveTimestamps
        certificatePath = this@ScpSyncConfiguration.certificatePath
        passphrase = this@ScpSyncConfiguration.passphrase
    }
}