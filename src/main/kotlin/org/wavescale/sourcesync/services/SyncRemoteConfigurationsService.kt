package org.wavescale.sourcesync.services

import org.wavescale.sourcesync.configurations.BaseSyncConfiguration
import org.wavescale.sourcesync.configurations.SyncConfigurationType

interface SyncRemoteConfigurationsService {
    fun hasNoConfiguration(): Boolean
    fun add(connection: BaseSyncConfiguration)
    fun addAll(connections: Set<BaseSyncConfiguration>)
    fun findAllOfType(type: SyncConfigurationType): Set<BaseSyncConfiguration>
    fun clear()

    fun mainConnection(): BaseSyncConfiguration?
    fun allConnectionNames(): Set<String>
    fun findFirstWithName(name: String): BaseSyncConfiguration?
    fun mainConnectionName(): String?
    fun hasNoMainConnectionConfigured(): Boolean
    fun setMainConnection(connectionName: String)
    fun resetMainConnection()
}