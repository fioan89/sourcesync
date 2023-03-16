package org.wavescale.sourcesync.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.vcs.changes.ChangeListManager
import com.intellij.openapi.vcs.changes.LocalChangeList
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.project.stateStore
import com.intellij.ui.ExperimentalUI
import org.wavescale.sourcesync.SourceSyncIcons
import org.wavescale.sourcesync.SourcesyncBundle
import org.wavescale.sourcesync.api.FileSynchronizer
import org.wavescale.sourcesync.api.SynchronizationQueue
import org.wavescale.sourcesync.api.Utils
import org.wavescale.sourcesync.factory.ConfigConnectionFactory
import org.wavescale.sourcesync.factory.ConnectionConfig
import org.wavescale.sourcesync.notifications.Notifier
import java.util.concurrent.Semaphore

class ActionChangedFilesToRemote : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        // first check if there's a connection type associated to this module.
        // If not alert the user and get out
        val project = e.project ?: return
        val projectName = project.name
        val associationName = ConnectionConfig.getInstance().getAssociationFor(projectName)

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

        // start sync
        val connectionConfiguration = ConfigConnectionFactory.getInstance().getConnectionConfiguration(associationName)
        val semaphores = Semaphore(connectionConfiguration.simultaneousJobs)
        val allowedSessions =
            if (changedFiles.size <= connectionConfiguration.simultaneousJobs) changedFiles.size else connectionConfiguration.simultaneousJobs
        val synchronizationQueue = SynchronizationQueue(e.project, connectionConfiguration, allowedSessions)
        synchronizationQueue.startCountingTo(changedFiles.size)
        val queue = synchronizationQueue.syncQueue
        for (virtualFile in changedFiles) {
            if (virtualFile != null && Utils.canBeUploaded(
                    virtualFile.name,
                    connectionConfiguration.getExcludedFiles()
                )
            ) {
                val uploadLocation = Utils.relativeLocalUploadDirs(virtualFile, project.stateStore)
                ProgressManager.getInstance().run(object : Task.Backgroundable(e.project, "Uploading", false) {
                    override fun run(indicator: ProgressIndicator) {
                        val fileSynchronizer: FileSynchronizer?
                        try {
                            semaphores.acquire()
                            fileSynchronizer = queue.take()
                            fileSynchronizer!!.indicator = indicator
                            if (fileSynchronizer != null && fileSynchronizer.connect()) {
                                fileSynchronizer.syncFile(virtualFile.path, uploadLocation)
                            }
                            queue.put(fileSynchronizer)
                            synchronizationQueue.count()
                        } catch (e1: InterruptedException) {
                            e1.printStackTrace()
                        } finally {
                            semaphores.release()
                        }
                    }
                })
            } else {
                if (virtualFile != null) {
                    logger.info("Skipping upload of ${virtualFile.name} because it matches the exclusion file pattern")
                    synchronizationQueue.count()
                }
            }
        }
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

        val project = e.project ?: return
        val associationName = ConnectionConfig.getInstance().getAssociationFor(project.name)
        if (associationName != null) {
            e.presentation.apply {
                text = "Sync changed files to $associationName"
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