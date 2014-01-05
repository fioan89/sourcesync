package org.wavescale.sourcesync.factory;

import org.wavescale.sourcesync.ui.ConnectionConfigPanel;

/**
 * ****************************************************************************
 * Copyright (c) 2005-2014 Faur Ioan-Aurel.                                     *
 * All rights reserved. This program and the accompanying materials             *
 * are made available under the terms of the MIT License                        *
 * which accompanies this distribution, and is available at                     *
 * http://opensource.org/licenses/MIT                                           *
 * *
 * For any issues or questions send an email at: fioan89@gmail.com              *
 * *****************************************************************************
 */
public class ConfigPanelFactory {
    private static final ConfigPanelFactory ourInstance = new ConfigPanelFactory();
    private ConnectionConfigPanel configPanel;

    public static ConfigPanelFactory getInstance() {
        return ourInstance;
    }

    private ConfigPanelFactory() {
        configPanel = new ConnectionConfigPanel();
    }

    public ConnectionConfigPanel getConnectionConfigPanel() {
        return configPanel;
    }

}
