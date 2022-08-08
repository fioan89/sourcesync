package org.wavescale.sourcesync.factory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

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
public class ConnectionConfig {
    private static final ConnectionConfig instance = new ConnectionConfig();
    private static final String CONFIG_FILE = ".modulesconfig.ser";
    String fileSeparator;
    private Map<String, String> projectToConnection;
    private final String userHome;

    private ConnectionConfig() {
        projectToConnection = new HashMap<>();
        userHome = System.getProperty("user.home");
        fileSeparator = System.getProperty("file.separator");
        tryLoadModulesAssociatedConn();
    }

    public static ConnectionConfig getInstance() {
        return instance;
    }

    /**
     * Links project with the given connection name.
     *
     * @param projectName    a string representing a project name.
     * @param connectionName a string representing a connection config.
     */
    public void associateProjectWithConnection(String projectName, String connectionName) {
        projectToConnection.put(projectName, connectionName);
    }

    /**
     * Finds and returns a config connection name associated with a given project name.
     *
     * @param projectName a string representing a project name.
     * @return a string representing a config connection name associated with the given
     * project name, or <code>null</code> if no connection was associated.
     */
    public String getAssociationFor(String projectName) {
        return projectToConnection.get(projectName);
    }

    public void removeAssociationFor(String projectName) {
        projectToConnection.remove(projectName);
    }

    public void removeAssociations() {
        projectToConnection.clear();
    }

    @SuppressWarnings("unchecked")
    private void tryLoadModulesAssociatedConn() {
        // try to load the persistence file.
        if (new File(userHome.concat(fileSeparator).concat(CONFIG_FILE)).exists()) {
            try {
                FileInputStream inputStream = new FileInputStream(userHome.concat(fileSeparator).concat(CONFIG_FILE));
                ObjectInputStream in = new ObjectInputStream(inputStream);
                projectToConnection = (Map<String, String>) in.readObject();
                in.close();
                inputStream.close();
            } catch (IOException i) {
                i.printStackTrace();
            } catch (ClassNotFoundException c) {
                c.printStackTrace();
            }
        }
    }

    public void saveModuleAssociatedConn() {
        // try to write the persistence file
        try {
            FileOutputStream outputStream = new FileOutputStream(userHome.concat(fileSeparator).concat(CONFIG_FILE));
            ObjectOutputStream out = new ObjectOutputStream(outputStream);
            out.writeObject(projectToConnection);
            out.close();
            outputStream.close();
        } catch (IOException i) {
            i.printStackTrace();
        }
    }
}
