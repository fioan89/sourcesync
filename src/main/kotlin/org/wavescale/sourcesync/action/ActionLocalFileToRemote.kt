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
import com.intellij.ui.NewUI
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

class ActionLocalFileToRemote : AnAction() {
    private val syncConfigurationsService = ProjectManager.getInstance().openProjects[0].getService(SyncRemoteConfigurationsService::class.java)
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        // first check if there's a connection type associated to this module.
        // If not alert the user and get out
        val project = e.project ?: return

        val virtualFile = PlatformDataKeys.VIRTUAL_FILE.getData(e.dataContext)
        if (virtualFile == null || virtualFile.isDirectory) {
            Notifier.notifyInfo(
                e.project!!,
                SourcesyncBundle.message("no.file.selected.to.sync")
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

        if (Utils.canBeUploaded(virtualFile.name, mainConfiguration.excludedFiles)) {
            val uploadLocation = Utils.relativeToProjectPath(virtualFile, project.stateStore)
            ProgressManager.getInstance().run(object : Task.Backgroundable(e.project, "Uploading", false) {
                override fun run(indicator: ProgressIndicator) {
                    try {
                        if (fileSynchronizer.connect()) {
                            fileSynchronizer.syncFile(virtualFile.path, uploadLocation, indicator)
                        }
                    } finally {
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
        if (NewUI.isEnabled()) {
            e.presentation.icon = SourceSyncIcons.ExpUI.SOURCESYNC
        }

        val mainConnectionName = syncConfigurationsService.mainConnectionName()
        if (mainConnectionName != null) {
            e.presentation.apply {
                text = "Sync this file to $mainConnectionName"
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
        val logger = logger<ActionSelectedFilesToRemote>()
    }

}