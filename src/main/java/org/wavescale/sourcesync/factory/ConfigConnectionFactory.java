package org.wavescale.sourcesync.factory;

/**
 * ****************************************************************************
 * Copyright (c) 2014-2107 Faur Ioan-Aurel.                                     *
 * All rights reserved. This program and the accompanying materials             *
 * are made available under the terms of the MIT License                        *
 * which accompanies this distribution, and is available at                     *
 * http://opensource.org/licenses/MIT                                           *
 * *
 * For any issues or questions send an email at: fioan89@gmail.com              *
 * *****************************************************************************
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.wavescale.sourcesync.api.ConnectionConfiguration;
import org.wavescale.sourcesync.api.ConnectionConstants;
import org.wavescale.sourcesync.api.PasswordlessSSH;
import org.wavescale.sourcesync.configurations.ScpSyncConfigurationState;
import org.wavescale.sourcesync.configurations.SshSyncConfigurationState;
import org.wavescale.sourcesync.services.SyncRemoteConfigurationsService;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.ProjectManager;

/**
 * Load connection settings from the persistence layout, instantiate them and keep them
 * during the runtime.
 */
public class ConfigConnectionFactory
{
    private static final Logger logger = Logger.getInstance(ConfigConnectionFactory.class);
    private static final ConfigConnectionFactory CONFIG_CONNECTION_FACTORY = new ConfigConnectionFactory();
    private static final String CONNECTIONS_FILE = ".connectionconfig.ser";
    String fileSeparator;
    private String userHome;
    private Map<String, ConnectionConfiguration> connectionConfigurationMap;

    private SyncRemoteConfigurationsService remoteSyncConfigurationsService = ProjectManager.getInstance().getOpenProjects()[0].getService(SyncRemoteConfigurationsService.class);

    private ConfigConnectionFactory()
    {
        connectionConfigurationMap = new HashMap<>();
        userHome = System.getProperty("user.home");
        fileSeparator = System.getProperty("file.separator");
        initComponent();
    }

    public static ConfigConnectionFactory getInstance()
    {
        return CONFIG_CONNECTION_FACTORY;
    }

    /**
     * Searches for the connection configuration that is assigned to the given name. If not found
     * null is returned.
     *
     * @param connectionName a <code>String</code> representing the connection name.
     * @return an implementation of the <code>ConnectionConfiguration</code>
     */
    public ConnectionConfiguration getConnectionConfiguration(String connectionName)
    {
        return connectionConfigurationMap.get(connectionName);
    }

    /**
     * Gets a set of all living connection names from the factory.
     *
     * @return a set of strings representing connection names that live inside the factory.
     */
    public Set<String> getConnectionNames()
    {
        return connectionConfigurationMap.keySet();
    }

    public void addConnectionConfiguration(String connectionName, ConnectionConfiguration connectionConfiguration)
    {
        connectionConfigurationMap.put(connectionName, connectionConfiguration);
    }

    public void removeConnectionConfiguration(String connectionName)
    {
        connectionConfigurationMap.remove(connectionName);
    }

    @SuppressWarnings("unchecked")
    private void initComponent()
    {
        // try to load the persistence data.
        if (new File(userHome.concat(fileSeparator).concat(CONNECTIONS_FILE)).exists())
        {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(userHome.concat(fileSeparator).concat(CONNECTIONS_FILE))))
            {
                connectionConfigurationMap = (Map<String, ConnectionConfiguration>) in.readObject();
                if (remoteSyncConfigurationsService.hasNoConfiguration())
                {
                    connectionConfigurationMap.values().forEach(configuration -> {
                        if (configuration.getConnectionType().equals(ConnectionConstants.CONN_TYPE_SCP))
                        {
                            remoteSyncConfigurationsService.add(toScpSyncConfigurationState(configuration));
                        }
                        else if (configuration.getConnectionType().equals(ConnectionConstants.CONN_TYPE_SFTP))
                        {
                            remoteSyncConfigurationsService.add(toSshSyncConfigurationState(configuration));
                        }
                    });
                }
            }
            catch (IOException | ClassNotFoundException e)
            {
                logger.warn("Could not load connections because", e);
            }
        }
    }

    @NotNull
    private static ScpSyncConfigurationState toScpSyncConfigurationState(ConnectionConfiguration configuration)
    {
        ScpSyncConfigurationState c = new ScpSyncConfigurationState(
            configuration.getConnectionName(),
            configuration.getHost(),
            String.valueOf(configuration.getPort()),
            configuration.getUserName()
        );

        c.setPassword(configuration.getUserPassword());
        c.setExcludedFiles(configuration.getExcludedFiles());
        c.setPreserveTimestamps(configuration.isPreserveTime());

        c.setPasswordless(((PasswordlessSSH) configuration).isPasswordlessSSHSelected());
        c.setCertificatePath(((PasswordlessSSH) configuration).getCertificatePath());
        c.setCertificateProtectedByPassphrase(((PasswordlessSSH) configuration).isPasswordlessWithPassphrase());
        c.setPassphrase(configuration.getUserPassword());
        return c;
    }

    @NotNull
    private static SshSyncConfigurationState toSshSyncConfigurationState(ConnectionConfiguration configuration)
    {
        SshSyncConfigurationState c = new SshSyncConfigurationState(
            configuration.getConnectionName(),
            configuration.getHost(),
            String.valueOf(configuration.getPort()),
            configuration.getUserName()
        );

        c.setPassword(configuration.getUserPassword());
        c.setExcludedFiles(configuration.getExcludedFiles());
        c.setPreserveTimestamps(configuration.isPreserveTime());

        c.setPasswordless(((PasswordlessSSH) configuration).isPasswordlessSSHSelected());
        c.setCertificatePath(((PasswordlessSSH) configuration).getCertificatePath());
        c.setCertificateProtectedByPassphrase(((PasswordlessSSH) configuration).isPasswordlessWithPassphrase());
        c.setPassphrase(configuration.getUserPassword());
        return c;
    }

    public void saveConnections()
    {
        // try to write the persistence data
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(userHome.concat(fileSeparator).concat(CONNECTIONS_FILE))))
        {
            out.writeObject(connectionConfigurationMap);
        }
        catch (IOException e)
        {
            logger.warn("Could not save connections because", e);
        }
    }
}
