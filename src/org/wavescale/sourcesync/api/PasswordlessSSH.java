package org.wavescale.sourcesync.api;

/**
 * Created by fauri on 03/05/16.
 */
public interface PasswordlessSSH {
    boolean isPasswordlessSSHSelected();

    void setPasswordlessSSHSelected(boolean shouldUseCertificate);

    boolean isPasswordlessWithPassphrase();

    void setPasswordlessWithPassphrase(boolean shouldUseCertificateWithPassphrase);

    String getCertificatePath();

    void setCertificatePath(String certificatePath);
}
