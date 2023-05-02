package org.wavescale.sourcesync.services

import org.wavescale.sourcesync.configurations.BaseSyncConfiguration

interface SyncRemoteConfigurationsService {

    fun hasNoConfiguration(): Boolean
    fun add(connection: BaseSyncConfiguration)

    fun hasNoMainConnectionConfigured(): Boolean
    fun setMainConnection(connectionName: String)
}