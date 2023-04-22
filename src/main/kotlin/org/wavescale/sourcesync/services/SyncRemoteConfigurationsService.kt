package org.wavescale.sourcesync.services

import org.wavescale.sourcesync.configurations.BaseSyncConfigurationState

interface SyncRemoteConfigurationsService {
    fun hasNoConfiguration(): Boolean
    fun add(connection: BaseSyncConfigurationState)
}