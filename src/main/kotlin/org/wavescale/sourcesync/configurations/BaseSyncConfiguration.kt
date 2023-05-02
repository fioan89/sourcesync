package org.wavescale.sourcesync.configurations

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.credentialStore.generateServiceName
import com.intellij.ide.passwordSafe.PasswordSafe
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class BaseSyncConfiguration : Cloneable {
    var name = "Unnamed"
    abstract val protocol: SyncConfigurationType
    var hostname = "localhost"
    var port = "11"
    var username = "username"

    var password: String?
        get() = PasswordSafe.instance.getPassword(credentialsAttributesFor(username, hostname, port))
        set(pass) = PasswordSafe.instance.set(credentialsAttributesFor(username, hostname, port), Credentials(username, pass))

    @SerialName("remote_workspace_path")
    var workspaceBasePath = "/home"

    @SerialName("excluded_files")
    var excludedFiles = ".crt;.iml"

    @SerialName("preserve_timestamps")
    var preserveTimestamps = false

    private fun credentialsAttributesFor(username: String, hostname: String, port: String) = CredentialAttributes(
        generateServiceName("SourceSync - Password", "${hostname}:${port}"),
        username
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BaseSyncConfiguration

        if (name != other.name) return false
        if (protocol != other.protocol) return false
        if (hostname != other.hostname) return false
        if (port != other.port) return false
        if (username != other.username) return false
        if (workspaceBasePath != other.workspaceBasePath) return false
        if (excludedFiles != other.excludedFiles) return false
        return preserveTimestamps == other.preserveTimestamps
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + protocol.hashCode()
        result = 31 * result + hostname.hashCode()
        result = 31 * result + port.hashCode()
        result = 31 * result + username.hashCode()
        result = 31 * result + workspaceBasePath.hashCode()
        result = 31 * result + excludedFiles.hashCode()
        result = 31 * result + preserveTimestamps.hashCode()
        return result
    }

    public abstract override fun clone(): BaseSyncConfiguration
}