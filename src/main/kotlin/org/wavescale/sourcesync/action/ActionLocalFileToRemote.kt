package org.wavescale.sourcesync.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.project.stateStore
import com.intellij.ui.ExperimentalUI
import org.wavescale.sourcesync.SourceSyncIcons
import org.wavescale.sourcesync.SourcesyncBundle
import org.wavescale.sourcesync.api.ConnectionConstants
import org.wavescale.sourcesync.api.FileSynchronizer
import org.wavescale.sourcesync.api.Utils
import org.wavescale.sourcesync.config.SCPConfiguration
import org.wavescale.sourcesync.config.SFTPConfiguration
import org.wavescale.sourcesync.factory.ConfigConnectionFactory
import org.wavescale.sourcesync.factory.ConnectionConfig
import org.wavescale.sourcesync.notifications.Notifier
import org.wavescale.sourcesync.synchronizer.SCPFileSynchronizer
import org.wavescale.sourcesync.synchronizer.SFTPFileSynchronizer

class ActionLocalFileToRemote : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        // first check if there's a connection type associated to this module.
        // If not alert the user and get out
        val project = e.project ?: return
        val projectName = project.name
        val associationName = ConnectionConfig.getInstance().getAssociationFor(projectName)
        val virtualFile = PlatformDataKeys.VIRTUAL_FILE.getData(e.dataContext)
        if (virtualFile == null || virtualFile.isDirectory) {
            Notifier.notifyInfo(
                e.project!!,
                SourcesyncBundle.message("no.file.selected.to.sync")
            )
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
                    }

                    if (fileSynchronizer != null && fileSynchronizer.connect()) {
                        fileSynchronizer.syncFile(virtualFile.path, uploadLocation)
                        fileSynchronizer.disconnect()
                    }
                }
            })
        } else {
            logger.info("Skipping upload of ${virtualFile.name} because it matches the exclusion file pattern")
        }
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        if (ExperimentalUI.isNewUI()) {
            e.presentation.icon = SourceSyncIcons.ExpUI.SOURCESYNC
        }

        val project = e.project ?: return
        val associationName = ConnectionConfig.getInstance().getAssociationFor(project.name)
        if (associationName != null) {
            e.presentation.apply {
                text = "Sync this file to $associationName"
                isEnabled = true
            }
        } else {
            e.presentation.apply {
                text = "Sync this file to Remote target"
                isEnabled = false
            }
        }
    }

    companion object {
        val logger = Logger.getInstance(ActionSelectedFilesToRemote.javaClass.simpleName)
    }

}