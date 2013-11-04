package org.wavescale.sourcesync.factory;

/**
 * ****************************************************************************
 * Copyright (c) 2005-2013 Faur Ioan-Aurel.                                     *
 * All rights reserved. This program and the accompanying materials             *
 * are made available under the terms of the MIT License                        *
 * which accompanies this distribution, and is available at                     *
 * http://opensource.org/licenses/MIT                                           *
 * *
 * For any issues or questions send an email at: fioan89@gmail.com              *
 * *****************************************************************************
 */

import org.wavescale.sourcesync.api.ConnectionConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * Load connection settings from the persistence layout, instantiate them and keep them
 * during the runtime.
 */
public class ConfigConnectionFactory {
    private static final ConfigConnectionFactory CONFIG_CONNECTION_FACTORY = new ConfigConnectionFactory();
    private Map<String, ConnectionConfiguration> connectionConfigurationMap;

    private ConfigConnectionFactory() {
        connectionConfigurationMap = new HashMap<String, ConnectionConfiguration>();
    }

    public static ConfigConnectionFactory getInstance() {
        return CONFIG_CONNECTION_FACTORY;
    }

    /**
     * Searches for the connection configuration that is assigned to the given name. If not found
     * null is returned.
     * @param connectionName a <code>String</code> representing the connection name.
     * @return an implementation of the <code>ConnectionConfiguration</code>
     */
    public ConnectionConfiguration getConnectionConfiguration(String connectionName) {
        return connectionConfigurationMap.get(connectionName);
    }

    public void addConnectionConfiguration(String connectionName, ConnectionConfiguration connectionConfiguration) {
        connectionConfigurationMap.put(connectionName, connectionConfiguration);
    }

}
