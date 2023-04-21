package org.wavescale.sourcesync.configurations

import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.Transient

sealed class SyncConfigurationWithPublicKeyAuthenticationState(name: String, host: String, port: String, username: String) : BaseSyncConfigurationState(name, host, port, username) {
    @get:Attribute("passwordless")
    var isPasswordless by property(false)

    @get:Attribute("certificate_path")
    var certificatePath: String? by string()

    @get:Attribute("certificate_with_passphrase")
    var isCertificateProtectedByPassphrase by property(false)

    @get:Transient
    var passphrase: String? by string()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as SyncConfigurationWithPublicKeyAuthenticationState

        if (isPasswordless != other.isPasswordless) return false
        if (certificatePath != other.certificatePath) return false
        return isCertificateProtectedByPassphrase == other.isCertificateProtectedByPassphrase
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + isPasswordless.hashCode()
        result = 31 * result + (certificatePath?.hashCode() ?: 0)
        result = 31 * result + isCertificateProtectedByPassphrase.hashCode()
        return result
    }
}

class SshSyncConfigurationState(name: String, host: String, port: String, username: String) : SyncConfigurationWithPublicKeyAuthenticationState(name, host, port, username)

class ScpSyncConfigurationState(name: String, host: String, port: String, username: String) : SyncConfigurationWithPublicKeyAuthenticationState(name, host, port, username)