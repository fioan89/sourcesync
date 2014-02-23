package org.wavescale.sourcesync.factory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

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

/**
 * Class for holding the name of connection configuration a modules has been
 * associated with.
 */
public class ModuleConnectionConfig {
    private static final ModuleConnectionConfig instance = new ModuleConnectionConfig();
    private static final String MODULES_FILE = ".modulesconfig.ser";
    private Map<String, String> moduleConnection;
    private String userHome;
    String fileSeparator;

    private ModuleConnectionConfig() {
        moduleConnection = new HashMap<String, String>();
        userHome = System.getProperty("user.home");
        fileSeparator = System.getProperty("file.separator");
        tryLoadModulesAssociatedConn();
    }

    public static ModuleConnectionConfig getInstance() {
        return instance;
    }

    /**
     * Links moduleName with the given connection name.
     *
     * @param moduleName     a string representing a module name.
     * @param connectionName a string representing a connection config.
     */
    public void associateModuleWithConnection(String moduleName, String connectionName) {
        moduleConnection.put(moduleName, connectionName);
    }

    /**
     * Removes any association for moduleName with a config connection.
     *
     * @param moduleName a string representing a module name.
     */
    public void removeAssociationFor(String moduleName) {
        moduleConnection.remove(moduleName);
    }

    /**
     * Finds and returns a config connection name associated with a given module name.
     *
     * @param moduleName a string representing a module name.
     * @return a string representing a config connection name associated with the given
     * module name, or <code>null</code> if no connection was associated.
     */
    public String getAssociationFor(String moduleName) {
        return moduleConnection.get(moduleName);
    }

    @SuppressWarnings("unchecked")
    private void tryLoadModulesAssociatedConn() {
        // try to load the persistence file.
        if (new File(userHome.concat(fileSeparator).concat(MODULES_FILE)).exists()) {
            try {
                FileInputStream inputStream = new FileInputStream(userHome.concat(fileSeparator).concat(MODULES_FILE));
                ObjectInputStream in = new ObjectInputStream(inputStream);
                moduleConnection = (Map<String, String>) in.readObject();
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
            FileOutputStream outputStream = new FileOutputStream(userHome.concat(fileSeparator).concat(MODULES_FILE));
            ObjectOutputStream out = new ObjectOutputStream(outputStream);
            out.writeObject(moduleConnection);
            out.close();
            outputStream.close();
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

}
