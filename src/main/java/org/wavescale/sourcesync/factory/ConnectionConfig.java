package org.wavescale.sourcesync.factory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;
import org.wavescale.sourcesync.services.SyncRemoteConfigurationsService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;

/**
 * ****************************************************************************
 * Copyright (c) 2014-2017 Faur Ioan-Aurel.                                     *
 * All rights reserved. This program and the accompanying materials             *
 * are made available under the terms of the MIT License                        *
 * which accompanies this distribution, and is available at                     *
 * http://opensource.org/licenses/MIT                                           *
 * *
 * For any issues or questions send an email at: fioan89@gmail.com              *
 * *****************************************************************************
 */

/**
 * Class for holding the name of connection configuration a project has been associated with.
 */
public class ConnectionConfig
{
    private static final ConnectionConfig instance = new ConnectionConfig();
    private static final String CONFIG_FILE = ".modulesconfig.ser";
    String fileSeparator;
    private Map<String, String> projectToConnection;
    private final String userHome;

    private ConnectionConfig()
    {
        projectToConnection = new HashMap<>();
        userHome = System.getProperty("user.home");
        fileSeparator = System.getProperty("file.separator");
    }

    public static ConnectionConfig getInstance()
    {
        return instance;
    }

    /**
     * Finds and returns a config connection name associated with a given project name.
     *
     * @param projectName a string representing a project name.
     * @return a string representing a config connection name associated with the given
     * project name, or <code>null</code> if no connection was associated.
     */
    private String getAssociationFor(String projectName)
    {
        return projectToConnection.get(projectName);
    }

    @SuppressWarnings("unchecked")
    public void migrate()
    {
        // try to load the persistence file.
        if (new File(userHome.concat(fileSeparator).concat(CONFIG_FILE)).exists())
        {
            try
            {
                FileInputStream inputStream = new FileInputStream(userHome.concat(fileSeparator).concat(CONFIG_FILE));
                ObjectInputStream in = new ObjectInputStream(inputStream);
                projectToConnection = (Map<String, String>) in.readObject();

                Project project = ProjectManager.getInstance().getOpenProjects()[0];
                SyncRemoteConfigurationsService remoteSyncConfigurationsService = project.getService(SyncRemoteConfigurationsService.class);
                if (remoteSyncConfigurationsService.hasNoMainConnectionConfigured())
                {
                    remoteSyncConfigurationsService.setMainConnection(getAssociationFor(project.getName()));
                }
                in.close();
                inputStream.close();
            }
            catch (IOException | ClassNotFoundException i)
            {
                i.printStackTrace();
            }
        }
    }
}
