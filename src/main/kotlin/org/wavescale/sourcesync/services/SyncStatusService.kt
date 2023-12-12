package org.wavescale.sourcesync.services

import com.intellij.ide.ActivityTracker
import com.intellij.openapi.components.Service
import java.util.Collections


@Service(Service.Level.APP)
class SyncStatusService {
    private val syncJobs = Collections.synchronizedSet(mutableSetOf<String>())

    fun addRunningSync(connectionName: String) {
        syncJobs.add(connectionName)
        ActivityTracker.getInstance().inc()
    }

    fun removeRunningSync(connectionName: String) {
        syncJobs.remove(connectionName)
        ActivityTracker.getInstance().inc()
    }

    fun isAnySyncJobRunning() = syncJobs.isNotEmpty()
}