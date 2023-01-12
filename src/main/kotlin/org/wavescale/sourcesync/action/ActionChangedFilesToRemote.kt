package org.wavescale.sourcesync.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.vcs.changes.ChangeListManager
import com.intellij.openapi.vcs.changes.LocalChangeList
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.project.stateStore
import org.wavescale.sourcesync.api.FileSynchronizer
import org.wavescale.sourcesync.api.SynchronizationQueue
import org.wavescale.sourcesync.api.Utils
import org.wavescale.sourcesync.factory.ConfigConnectionFactory
import org.wavescale.sourcesync.factory.ConnectionConfig
import org.wavescale.sourcesync.logger.BalloonLogger
import org.wavescale.sourcesync.logger.EventDataLogger
import java.util.concurrent.Semaphore

class ActionChangedFilesToRemote : AnAction() {
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

        // there's this possibility that the project might not be versioned, therefore no changes can be detected.
        val changeLists = ChangeListManager.getInstance(e.project!!).changeLists
        if (!hasModifiedFiles(changeLists)) {
            val builder = StringBuilder("Could not find any changes on project <b>")
            builder.append(e.project!!.name).append("</b>! You might want to check if this project is imported in any")
                .append(" version control system that is supported by IDEA!")
            BalloonLogger.logBalloonInfo(builder.toString(), e.project)
            EventDataLogger.logInfo(builder.toString(), e.project)
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
        val allowed_sessions =
            if (changedFiles.size <= connectionConfiguration.simultaneousJobs) changedFiles.size else connectionConfiguration.simultaneousJobs
        val synchronizationQueue = SynchronizationQueue(e.project, connectionConfiguration, allowed_sessions)
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
                        var fileSynchronizer: FileSynchronizer?
                        try {
                            semaphores.acquire()
                            fileSynchronizer = queue.take()
                            fileSynchronizer!!.indicator = indicator
                            if (fileSynchronizer != null) {
                                fileSynchronizer.connect()
                                // so final destination will look like this:
                                // root_home/ + project_relative_path_to_file/
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
                    EventDataLogger.logWarning("File <b>" + virtualFile.name + "</b> is filtered out!", e.project)
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
}