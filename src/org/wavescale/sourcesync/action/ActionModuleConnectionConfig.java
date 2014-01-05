package org.wavescale.sourcesync.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import org.wavescale.sourcesync.factory.ConfigConnectionFactory;
import org.wavescale.sourcesync.ui.ModuleConnectionConfigDialog;

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
public class ActionModuleConnectionConfig extends AnAction {
    public void actionPerformed(AnActionEvent e) {
        String moduleName = DataKeys.PROJECT.getData(e.getDataContext()).getName();
        String[] configConnections = ConfigConnectionFactory.getInstance().getConnectionNames().toArray(new String[0]);
        ModuleConnectionConfigDialog moduleConnectionConfigDialog = new ModuleConnectionConfigDialog(moduleName, configConnections);
    }
}
