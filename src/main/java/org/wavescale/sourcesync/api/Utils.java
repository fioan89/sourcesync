package org.wavescale.sourcesync.api;

import com.intellij.openapi.components.impl.stores.IProjectStore;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

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
public class Utils {

    /**
     * Checks if a given filename can be uploaded or not.
     *
     * @param fileName           a string representing a file name plus extension.
     * @param extensionsToFilter a string that contains file extensions separated by
     *                           by space, comma or ";" character that are not to be uploaded. The
     *                           extension MUST contain the dot character - ex: ".crt .iml .etc"
     * @return <code>true</code> if file extension is not on the extensionsToFilter, <code>False</code> otherwise.
     */
    public static boolean canBeUploaded(String fileName, String extensionsToFilter) {
        String extension = ".";

        if (fileName != null) {
            int i = fileName.lastIndexOf('.');
            if (i >= 0) {
                extension += fileName.substring(i + 1);
                return !extensionsToFilter.contains(extension);
            }
            return true;
        }
        return false;
    }

    /**
     * Logs an error message about no connection specified.
     *
     * @param projectName module or project name.
     */
    public static void showNoConnectionSpecifiedError(String projectName) {
        String message = "There is no connection type associated to <b>" +
                projectName +
                "</b> module.\nPlease right click on module name and then select <b>Module Connection Configuration</b> to select connection type!";
        Messages.showErrorDialog(message, "No connection specified for project " + projectName);
    }

    /**
     * Extracts an ordered list of all parent directories from project base path (including the project) for the file
     */
    public static Path relativeLocalUploadDirs(VirtualFile virtualFile, IProjectStore projectStore) {
        return projectStore.getProjectBasePath().getParent().relativize(virtualFile.toNioPath().getParent());
    }

    /**
     * Tries to create a file with the given absolute path, even if the parent directories do not exist.
     *
     * @param path absolute file path name to create
     * @return <code>true</code> if the path was created, <code>false</code> otherwise. If false is returned it might
     * be that the file already exists
     * @throws IOException if an I/O error occurred
     */
    public static boolean createFile(String path) throws IOException {
        File fileToCreate = new File(path);
        if (fileToCreate.exists()) {
            return false;
        }
        // the file doesn't exist so try create it
        String dirPath = fileToCreate.getParent();
        // try create the path
        new File(dirPath).mkdirs();
        // try create the file
        return fileToCreate.createNewFile();
    }

}
