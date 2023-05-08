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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.wavescale.sourcesync.api.ConnectionConfiguration;
import org.wavescale.sourcesync.api.ConnectionConstants;
import org.wavescale.sourcesync.api.PasswordlessSSH;
import org.wavescale.sourcesync.configurations.ScpSyncConfiguration;
import org.wavescale.sourcesync.configurations.SshSyncConfiguration;
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
    private static ScpSyncConfiguration toScpSyncConfigurationState(ConnectionConfiguration configuration)
    {
        ScpSyncConfiguration c = new ScpSyncConfiguration();
        c.setName(configuration.getConnectionName());
        c.setHostname(configuration.getHost());
        c.setPort(String.valueOf(configuration.getPort()));

        c.setUsername(configuration.getUserName());
        if (((PasswordlessSSH) configuration).isPasswordlessSSHSelected())
        {
            c.setPrivateKey(((PasswordlessSSH) configuration).getCertificatePath());
            if (((PasswordlessSSH) configuration).isPasswordlessWithPassphrase())
            {
                c.setPassphrase(configuration.getUserPassword());
            }
        }
        else
        {
            c.setPassword(configuration.getUserPassword());
        }

        c.setExcludedFiles(configuration.getExcludedFiles());
        c.setPreserveTimestamps(configuration.isPreserveTime());
        return c;
    }

    @NotNull
    private static SshSyncConfiguration toSshSyncConfigurationState(ConnectionConfiguration configuration)
    {
        SshSyncConfiguration c = new SshSyncConfiguration();
        c.setName(configuration.getConnectionName());
        c.setHostname(configuration.getHost());
        c.setPort(String.valueOf(configuration.getPort()));

        c.setUsername(configuration.getUserName());
        if (((PasswordlessSSH) configuration).isPasswordlessSSHSelected())
        {
            c.setPrivateKey(((PasswordlessSSH) configuration).getCertificatePath());
            if (((PasswordlessSSH) configuration).isPasswordlessWithPassphrase())
            {
                c.setPassphrase(configuration.getUserPassword());
            }
        }
        else
        {
            c.setPassword(configuration.getUserPassword());
        }

        c.setExcludedFiles(configuration.getExcludedFiles());
        c.setPreserveTimestamps(configuration.isPreserveTime());
        return c;
    }
}
