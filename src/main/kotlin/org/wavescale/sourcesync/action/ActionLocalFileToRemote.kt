package org.wavescale.sourcesync.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.project.stateStore
import com.intellij.ui.ExperimentalUI
import com.intellij.ui.GotItTooltip
import org.wavescale.sourcesync.SourceSyncIcons
import org.wavescale.sourcesync.api.ConnectionConstants
import org.wavescale.sourcesync.api.FileSynchronizer
import org.wavescale.sourcesync.api.Utils
import org.wavescale.sourcesync.config.FTPConfiguration
import org.wavescale.sourcesync.config.FTPSConfiguration
import org.wavescale.sourcesync.config.SCPConfiguration
import org.wavescale.sourcesync.config.SFTPConfiguration
import org.wavescale.sourcesync.factory.ConfigConnectionFactory
import org.wavescale.sourcesync.factory.ConnectionConfig
import org.wavescale.sourcesync.logger.BalloonLogger
import org.wavescale.sourcesync.logger.EventDataLogger
import org.wavescale.sourcesync.synchronizer.FTPFileSynchronizer
import org.wavescale.sourcesync.synchronizer.FTPSFileSynchronizer
import org.wavescale.sourcesync.synchronizer.SCPFileSynchronizer
import org.wavescale.sourcesync.synchronizer.SFTPFileSynchronizer
import java.net.URL

class ActionLocalFileToRemote : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        // first check if there's a connection type associated to this module.
        // If not alert the user and get out
        val project = e.project ?: return
        val projectName = project.name
        val associationName = ConnectionConfig.getInstance().getAssociationFor(projectName)
        if (associationName == null) {
            Utils.showNoConnectionSpecifiedError(projectName)
            return
        }
        val virtualFile = PlatformDataKeys.VIRTUAL_FILE.getData(e.dataContext)
        if (virtualFile == null || virtualFile.isDirectory) {
            val builder = StringBuilder("Project <b>")
            builder.append(projectName).append("</b>! does not have a selected file!")
            BalloonLogger.logBalloonInfo(builder.toString(), project)
            EventDataLogger.logInfo(builder.toString(), project)
            return
        }
        val connectionConfiguration = ConfigConnectionFactory.getInstance().getConnectionConfiguration(associationName)
        if (Utils.canBeUploaded(virtualFile.name, connectionConfiguration.getExcludedFiles())) {
            val uploadLocation = Utils.relativeLocalUploadDirs(virtualFile, project.stateStore)
            ProgressManager.getInstance().run(object : Task.Backgroundable(e.project, "Uploading", false) {
                override fun run(indicator: ProgressIndicator) {
                    var fileSynchronizer: FileSynchronizer? = null
                    if (ConnectionConstants.CONN_TYPE_SCP == connectionConfiguration.connectionType) {
                        fileSynchronizer = SCPFileSynchronizer(
                            (connectionConfiguration as SCPConfiguration),
                            project, indicator
                        )
                    } else if (ConnectionConstants.CONN_TYPE_SFTP == connectionConfiguration.connectionType) {
                        fileSynchronizer = SFTPFileSynchronizer(
                            (connectionConfiguration as SFTPConfiguration),
                            project, indicator
                        )
                    } else if (ConnectionConstants.CONN_TYPE_FTP == connectionConfiguration.connectionType) {
                        fileSynchronizer = FTPFileSynchronizer(
                            (connectionConfiguration as FTPConfiguration),
                            project, indicator
                        )
                        GotItTooltip("sourcesync.ftp.sync", "FTP is deprecated", project)
                            .withBrowserLink("Github Discussion", URL("https://github.com"))
//                            .show(e.presentation, GotItTooltip.BOTTOM_LEFT)
                    } else if (ConnectionConstants.CONN_TYPE_FTPS == connectionConfiguration.connectionType) {
                        fileSynchronizer = FTPSFileSynchronizer(
                            (connectionConfiguration as FTPSConfiguration),
                            project, indicator
                        )
                    }
                    if (fileSynchronizer != null) {
                        fileSynchronizer.connect()
                        // so final destination will look like this:
                        // root_home/ + project_relative_path_to_file/
                        fileSynchronizer.syncFile(virtualFile.path, uploadLocation)
                        fileSynchronizer.disconnect()
                    }
                }
            })
        } else {
            EventDataLogger.logWarning("File <b>" + virtualFile.name + "</b> is filtered out!", e.project)
        }
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        if (ExperimentalUI.isNewUI()) {
            this.templatePresentation.icon = SourceSyncIcons.ExpUI.SOURCESYNC
        }

        val project = e.project ?: return
        val associationName = ConnectionConfig.getInstance().getAssociationFor(project.name)
        if (associationName != null) {
            templatePresentation.text = "Sync this file to $associationName"
        } else {
            templatePresentation.text = "Sync this file to Remote target"
        }
    }

}