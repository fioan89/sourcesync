package org.wavescale.sourcesync.services

import com.intellij.openapi.components.Service

@Service(Service.Level.APP)
class SyncStatusService {
    private val syncJobs = mutableSetOf<String>()

    fun addRunningSync(connectionName: String) {
        syncJobs.add(connectionName)
    }

    fun removeRunningSync(connectionName: String) {
        syncJobs.remove(connectionName)
    }

    fun isAnySyncJobRunning() = syncJobs.isNotEmpty()

}