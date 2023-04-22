package org.wavescale.sourcesync.configurations

enum class SyncConfigurationType(val prettyName: String) {
    SFTP("SSH"), SCP("SCP")
}