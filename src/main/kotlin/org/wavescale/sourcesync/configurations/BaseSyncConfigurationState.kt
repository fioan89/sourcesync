package org.wavescale.sourcesync.configurations

import com.intellij.openapi.components.BaseState
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.Transient


sealed class BaseSyncConfigurationState() : BaseState() {

    constructor(name: String, host: String, port: String, username: String) : this() {
        this.name = name
        this.hostname = host
        this.port = port
        this.username = username
    }

    @get:Attribute
    var name by string()

    @get:Attribute
    var type by enum(SyncConfigurationType.SFTP)

    @get:Attribute
    var hostname by string()

    @get:Attribute
    var port by string()

    @get:Attribute
    var username by string()

    @get:Transient
    var password: String? by string()

    @get:Attribute("remote_workspace_path")
    var workspaceBasePath by string("/home")

    @get:Attribute("excluded_files")
    var excludedFiles by string(".crt;.iml")

    @get:Attribute("preserve_timestamps")
    var preserveTimestamps by property(false)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as BaseSyncConfigurationState

        if (name != other.name) return false
        if (type != other.type) return false
        if (hostname != other.hostname) return false
        if (port != other.port) return false
        if (username != other.username) return false
        if (workspaceBasePath != other.workspaceBasePath) return false
        if (excludedFiles != other.excludedFiles) return false
        return preserveTimestamps == other.preserveTimestamps
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (type?.hashCode() ?: 0)
        result = 31 * result + (hostname?.hashCode() ?: 0)
        result = 31 * result + (port?.hashCode() ?: 0)
        result = 31 * result + (username?.hashCode() ?: 0)
        result = 31 * result + (workspaceBasePath?.hashCode() ?: 0)
        result = 31 * result + (excludedFiles?.hashCode() ?: 0)
        result = 31 * result + preserveTimestamps.hashCode()
        return result
    }
}