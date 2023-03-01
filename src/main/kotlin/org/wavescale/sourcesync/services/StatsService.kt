package org.wavescale.sourcesync.services

import com.intellij.openapi.components.Service

@Service(Service.Level.APP)
class StatsService {
    private var successfulUploads = 0
    fun registerSuccessfulUpload() {
        successfulUploads++
    }

    fun eligibleForDonations() =
        successfulUploads == 10 || successfulUploads == 50 || successfulUploads == 120 || successfulUploads == 200 || successfulUploads == 500
}