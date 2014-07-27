package org.wavescale.sourcesync.api;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.wavescale.sourcesync.logger.BalloonLogger;
import org.wavescale.sourcesync.logger.EventDataLogger;

import java.io.File;

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
                if (extensionsToFilter.contains(extension)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Logs an error message about no connection specified.
     *
     * @param e          action event instance.
     * @param moduleName module or project name.
     */
    public static void showNoConnectionSpecifiedError(AnActionEvent e, String moduleName) {
        StringBuilder message = new StringBuilder();
        message.append("There is no connection type associated to <b>").append(moduleName)
                .append("</b> module.\nPlease right click on module name and then select <b>Module Connection Configuration</b> to select connection type!");
        BalloonLogger.logBalloonError(message.toString(), e.getProject());
        EventDataLogger.logError(message.toString(), e.getProject());
    }

    /**
     * Normalizes Windows paths to Unix path.
     *
     * @param path a Unix or Windows path.
     * @return if a Unix like path.
     */
    public static String getUnixPath(String path) {
        return path.replace("\\", "/");
    }

    /**
     * Builds a Unix path from the list of specified strings. Array is taken in order.
     *
     * @param paths a list of relative string paths.
     * @return a string instance representing a Unix path. It may or may not be an absolute path
     * depending on the input array.
     */
    public static String buildUnixPath(String... paths) {
        StringBuilder toReturn = new StringBuilder();
        for (String path : paths) {
            toReturn.append(getUnixPath(path)).append("/");
        }
        String finalValue = toReturn.toString().replace("//", "/");
        if (finalValue.length() > 0 && finalValue.charAt(finalValue.length() - 1) == '/') {
            return finalValue.substring(0, finalValue.length() - 1);
        }
        return finalValue;
    }

    /**
     * Returns an array of folders that build up a file path
     *
     * @param path an absolute path.
     * @return an array of strings instances that represent a file or folder path.
     */
    public static String[] splitPath(String path) {
        return getUnixPath(path).split("/");
    }

    /**
     * Returns an array of folders that build up a file path
     *
     * @param path a {@link java.io.File} instance path.
     * @return an array of strings instances that represent a file or folder path.
     */
    public static String[] splitPath(File path) {
        return splitPath(path.getPath());
    }
}
