package org.wavescale.sourcesync.services

import com.intellij.openapi.components.Service

@Service(Service.Level.APP)
class StatsService {
    private var successfulUploads = 0
    fun registerSuccessfulUpload() {
        successfulUploads++
    }

    fun eligibleForDonations(): Boolean = when (successfulUploads) {
        10, 30, 60, 100 -> true
        else -> successfulUploads % 100 == 0
    }
}