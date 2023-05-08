package org.wavescale.sourcesync.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.ProjectManager
import com.intellij.project.stateStore
import com.intellij.ui.ExperimentalUI
import org.wavescale.sourcesync.SourceSyncIcons
import org.wavescale.sourcesync.SourcesyncBundle
import org.wavescale.sourcesync.api.Utils
import org.wavescale.sourcesync.configurations.ScpSyncConfiguration
import org.wavescale.sourcesync.configurations.SshSyncConfiguration
import org.wavescale.sourcesync.configurations.SyncConfigurationType
import org.wavescale.sourcesync.notifications.Notifier
import org.wavescale.sourcesync.services.SyncRemoteConfigurationsService
import org.wavescale.sourcesync.synchronizer.SCPFileSynchronizer
import org.wavescale.sourcesync.synchronizer.SFTPFileSynchronizer
import org.wavescale.sourcesync.synchronizer.Synchronizer
import java.io.File

class ActionSelectedFilesToRemote : AnAction() {
    private val syncConfigurationsService = ProjectManager.getInstance().openProjects[0].getService(SyncRemoteConfigurationsService::class.java)
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        // first check if there's a connection type associated to this module.
        // If not alert the user and get out
        val project = e.project ?: return

        // get a list of selected virtual files
        val virtualFiles = PlatformDataKeys.VIRTUAL_FILE_ARRAY.getData(e.dataContext)!!
        if (virtualFiles.isEmpty()) {
            Notifier.notifyInfo(
                e.project!!,
                SourcesyncBundle.message("no.files.selected.to.sync")
            )
            return
        }

        val mainConfiguration = syncConfigurationsService.mainConnection()
        if (mainConfiguration == null) {
            Notifier.notifyError(
                e.project!!,
                SourcesyncBundle.message("no.remote.sync.connection.configured.title"),
                SourcesyncBundle.message("no.remote.sync.connection.configured.message")
            )
            return
        }

        val fileSynchronizer: Synchronizer = when (mainConfiguration.protocol) {
            SyncConfigurationType.SCP -> {
                SCPFileSynchronizer(mainConfiguration as ScpSyncConfiguration, project)
            }

            SyncConfigurationType.SFTP -> {
                SFTPFileSynchronizer(mainConfiguration as SshSyncConfiguration, project)
            }
        }
        try {
            for (virtualFile in virtualFiles) {
                if (virtualFile != null && File(virtualFile.path).isFile) {
                    if (Utils.canBeUploaded(virtualFile.name, mainConfiguration.excludedFiles)) {
                        val uploadLocation = Utils.relativeLocalUploadDirs(virtualFile, project.stateStore)
                        ProgressManager.getInstance().run(object : Task.Backgroundable(e.project, "Uploading", false) {
                            override fun run(indicator: ProgressIndicator) {
                                if (fileSynchronizer.connect()) {
                                    fileSynchronizer.syncFile(virtualFile.path, uploadLocation, indicator)
                                }
                            }
                        })
                    } else {
                        logger.info("Skipping upload of ${virtualFile.name} because it matches the exclusion file pattern")
                    }
                } else {
                    if (virtualFile != null) {
                        logger.info("Skipping upload of ${virtualFile.name} because it's a directory")
                    }
                }
            }
        } finally {
            fileSynchronizer.disconnect()
        }
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        if (ExperimentalUI.isNewUI()) {
            e.presentation.icon = SourceSyncIcons.ExpUI.SOURCESYNC
        }

        val mainConnectionName = syncConfigurationsService.mainConnectionName()
        if (mainConnectionName != null) {
            e.presentation.apply {
                text = "Sync selected files to $mainConnectionName"
                isEnabled = true
            }
        } else {
            e.presentation.apply {
                text = "Sync selected files to Remote target"
                isEnabled = false
            }
        }
    }

    companion object {
        val logger = logger<ActionSelectedFilesToRemote>()
    }
}