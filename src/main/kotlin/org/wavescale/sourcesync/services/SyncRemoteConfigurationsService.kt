package org.wavescale.sourcesync.services

import org.wavescale.sourcesync.configurations.BaseSyncConfiguration
import org.wavescale.sourcesync.configurations.SyncConfigurationType

interface SyncRemoteConfigurationsService {

    fun hasNoConfiguration(): Boolean

    fun hasNoMainConnectionConfigured(): Boolean
    fun setMainConnection(connectionName: String)

    fun add(connection: BaseSyncConfiguration)
    fun addAll(connections: Set<BaseSyncConfiguration>)
    fun findAllOfType(type: SyncConfigurationType): Set<BaseSyncConfiguration>
    fun clear()
}