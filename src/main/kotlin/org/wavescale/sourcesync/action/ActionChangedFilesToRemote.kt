package org.wavescale.sourcesync.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vcs.changes.ChangeListManager
import com.intellij.openapi.vcs.changes.LocalChangeList
import com.intellij.openapi.vfs.VirtualFile
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

class ActionChangedFilesToRemote : AnAction() {
    private val syncConfigurationsService = ProjectManager.getInstance().openProjects[0].getService(SyncRemoteConfigurationsService::class.java)
    override fun getActionUpdateThread() = ActionUpdateThread.BGT
    override fun actionPerformed(e: AnActionEvent) {
        // first check if there's a connection type associated to this module.
        // If not alert the user and get out
        val project = e.project ?: return

        // there's this possibility that the project might not be versioned, therefore no changes can be detected.
        val changeLists = ChangeListManager.getInstance(e.project!!).changeLists
        if (!hasModifiedFiles(changeLists)) {
            Notifier.notifyInfo(
                e.project!!,
                SourcesyncBundle.message("no.vcs.changes.to.sync"),
            )
            return
        }

        // get a list of changed virtual files
        val changedFiles: MutableList<VirtualFile?> = ArrayList()
        for (localChangeList in changeLists) {
            for (change in localChangeList.changes) {
                changedFiles.add(change.virtualFile)
            }
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

        val (files, rest) = changedFiles.filterNotNull().partition { File(it.path).isFile }
        rest.forEach {
            ActionSelectedFilesToRemote.logger.info("Skipping upload of ${it.name} because it's a directory")
        }

        val (acceptedFiles, excludedFiles) = files.partition { Utils.canBeUploaded(it.name, mainConfiguration.excludedFiles) }
        excludedFiles.forEach {
            ActionSelectedFilesToRemote.logger.info("Skipping upload of ${it.name} because it matches the exclusion file pattern")
        }
        ProgressManager.getInstance().run(object : Task.Backgroundable(e.project, "Uploading", false) {
            override fun run(indicator: ProgressIndicator) {
                try {
                    if (fileSynchronizer.connect()) {
                        fileSynchronizer.syncFiles(acceptedFiles.map { Pair(it.path, Utils.relativeLocalUploadDirs(it, project.stateStore)) }.toSet(), indicator)
                    }
                } finally {
                    fileSynchronizer.disconnect()
                }
            }
        })

    }

    private fun hasModifiedFiles(changeLists: List<LocalChangeList>): Boolean {
        for (changeList in changeLists) {
            val changes = changeList.changes
            for (change in changes) {
                if (change.virtualFile != null) {
                    return true
                }
            }
        }
        return false
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        if (ExperimentalUI.isNewUI()) {
            e.presentation.icon = SourceSyncIcons.ExpUI.SOURCESYNC
        }

        val mainConnectionName = syncConfigurationsService.mainConnectionName()
        if (mainConnectionName != null) {
            e.presentation.apply {
                text = "Sync changed files to $mainConnectionName"
                isEnabled = true
            }
        } else {
            e.presentation.apply {
                text = "Sync changed files to Remote target"
                isEnabled = false
            }
        }
    }

    companion object {
        private val logger = logger<ActionChangedFilesToRemote>()
    }
}