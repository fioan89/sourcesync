package org.wavescale.sourcesync.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import org.wavescale.sourcesync.factory.ConfigConnectionFactory;
import org.wavescale.sourcesync.ui.ConnectionConfigDialog;

import java.util.Set;

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
public class ActionProjectConnectionConfig extends AnAction {
    public void actionPerformed(AnActionEvent e) {
        String projectName = PlatformDataKeys.PROJECT.getData(e.getDataContext()).getName();
        Set<String> connectionNames = ConfigConnectionFactory.getInstance().getConnectionNames();
        String[] configConnections = connectionNames.toArray(new String[connectionNames.size()]);
        new ConnectionConfigDialog(projectName, configConnections).openDialog();
    }
}
