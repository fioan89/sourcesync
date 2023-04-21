package org.wavescale.sourcesync.configurations

import com.intellij.openapi.components.BaseState
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.Tag
import com.intellij.util.xmlb.annotations.Transient


@Tag("configuration")
sealed class BaseSyncConfigurationState(name: String, host: String, port: String, username: String) : BaseState() {
    @get:Attribute
    var name by string(name)

    @get:Attribute
    var type by enum<SyncConfigurationType>()

    @get:Attribute
    var hostname by string(host)

    @get:Attribute
    var port by string(port)

    @get:Attribute
    var username by string(username)

    @get:Transient
    var password: String? by string()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as BaseSyncConfigurationState

        if (name != other.name) return false
        if (type != other.type) return false
        if (hostname != other.hostname) return false
        if (port != other.port) return false
        return username == other.username
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (type?.hashCode() ?: 0)
        result = 31 * result + (hostname?.hashCode() ?: 0)
        result = 31 * result + (port?.hashCode() ?: 0)
        result = 31 * result + (username?.hashCode() ?: 0)
        return result
    }

}