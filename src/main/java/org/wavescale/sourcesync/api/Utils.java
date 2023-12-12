package org.wavescale.sourcesync.api;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import com.intellij.openapi.components.impl.stores.IProjectStore;
import com.intellij.openapi.vfs.VirtualFile;

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
public class Utils
{

    /**
     * Checks if a given filename can be uploaded or not.
     *
     * @param fileName           a string representing a file name plus extension.
     * @param extensionsToFilter a string that contains file extensions separated by
     *                           by space, comma or ";" character that are not to be uploaded. The
     *                           extension MUST contain the dot character - ex: ".crt .iml .etc"
     * @return <code>true</code> if file extension is not on the extensionsToFilter, <code>False</code> otherwise.
     */
    public static boolean canBeUploaded(String fileName, String extensionsToFilter)
    {
        String extension = ".";

        if (fileName != null)
        {
            int i = fileName.lastIndexOf('.');
            if (i >= 0)
            {
                extension += fileName.substring(i + 1);
                return !extensionsToFilter.contains(extension);
            }
            return true;
        }
        return false;
    }

    /**
     * Returns a path that is now relative to the local project base path.
     * </br>
     * Example:
     * <pre>
     * - project base bath is: C:\Users\ifaur\workspace\sourcesync\
     * - selected file/folder: C:\Users\ifaur\workspace\sourcesync\src\main\kotlin\Example.kt
     * - result is: src\main\kotlin
     * </pre>
     */
    public static Path relativeToProjectPath(VirtualFile virtualFile, IProjectStore projectStore)
    {
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
    public static boolean createFile(String path) throws IOException
    {
        File fileToCreate = new File(path);
        if (fileToCreate.exists())
        {
            return false;
        }
        // the file doesn't exist so try to create it
        String dirPath = fileToCreate.getParent();
        // try to  create the path
        new File(dirPath).mkdirs();
        // try to create the file
        return fileToCreate.createNewFile();
    }

}
