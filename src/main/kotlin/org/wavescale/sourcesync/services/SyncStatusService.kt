package org.wavescale.sourcesync.services

import com.intellij.openapi.components.Service
import io.ktor.util.collections.*
import java.util.*

@Service(Service.Level.APP)
class SyncStatusService {
    private val syncJobs = Collections.synchronizedSet(mutableSetOf<String>())

    fun addRunningSync(connectionName: String) {
        syncJobs.add(connectionName)
    }

    fun removeRunningSync(connectionName: String) {
        syncJobs.remove(connectionName)
    }

    fun isAnySyncJobRunning() = syncJobs.isNotEmpty()
}