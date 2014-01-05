package org.wavescale.sourcesync.api;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.wavescale.sourcesync.logger.BalloonLogger;
import org.wavescale.sourcesync.logger.EventDataLogger;

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

        int i = fileName.lastIndexOf('.');
        if (i >= 0) {
            extension += fileName.substring(i + 1);
            if (extensionsToFilter.contains(extension)) {
                return false;
            }
        }

        return true;
    }

    public static void showNoConnectionSpecifiedError(AnActionEvent e, String moduleName) {
        StringBuilder message = new StringBuilder();
        message.append("There is no connection type associated to <b>").append(moduleName)
                .append("</b> module.\nPlease right click on module name and then select <b>Module Connection Configuration</b> to select connection type!");
        BalloonLogger.logBalloonError(message.toString(), e.getProject());
        EventDataLogger.logError(message.toString(), e.getProject());
    }
}
