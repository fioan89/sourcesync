package org.wavescale.sourcesync.configurations

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.credentialStore.generateServiceName
import com.intellij.ide.passwordSafe.PasswordSafe
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class SyncConfigurationWithPublicKeyAuthentication : BaseSyncConfiguration() {

    @SerialName("passwordless")
    var isPasswordless = false

    @SerialName("certificate_path")
    var certificatePath: String? = null

    @SerialName("certificate_with_passphrase")
    var isCertificateProtectedByPassphrase = false

    var passphrase: String?
        get() = PasswordSafe.instance.getPassword(credentialsAttributesFor(username, hostname, port))
        set(passPhrase) = PasswordSafe.instance.set(credentialsAttributesFor(username, hostname, port), Credentials(username, passPhrase))

    private fun credentialsAttributesFor(username: String, hostname: String, port: String) = CredentialAttributes(
        generateServiceName("SourceSync - Passphrase", "${hostname}:${port}"),
        username
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as SyncConfigurationWithPublicKeyAuthentication

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

@Serializable
@SerialName("ssh_configuration")
class SshSyncConfiguration : SyncConfigurationWithPublicKeyAuthentication() {
    override val protocol = SyncConfigurationType.SFTP
}

@Serializable
@SerialName("scp_configuration")
class ScpSyncConfiguration : SyncConfigurationWithPublicKeyAuthentication() {
    override val protocol = SyncConfigurationType.SCP
}