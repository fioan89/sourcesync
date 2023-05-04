package org.wavescale.sourcesync.services

import com.intellij.openapi.components.*
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import org.wavescale.sourcesync.configurations.BaseSyncConfiguration
import org.wavescale.sourcesync.configurations.SyncConfigurationType
import org.wavescale.sourcesync.configurations.SyncConfigurations

@Suppress("UnstableApiUsage")
@Service(Service.Level.PROJECT)
@State(name = "SourceSyncRemoteConfigurationsService", storages = [Storage(value = "sourcesync.xml")])
class SyncRemoteConfigurationsServiceImpl(val project: Project) : SerializablePersistentStateComponent<SyncConfigurations>(SyncConfigurations()), SyncRemoteConfigurationsService {
    override fun hasNoConfiguration() = state.connections.isEmpty()

    override fun add(connection: BaseSyncConfiguration) {
        updateState { oldState ->
            logger.info("Added ${connection.protocol.prettyName} remote connection configuration with name ${connection.name}")
            SyncConfigurations(oldState.connections union setOf(connection), oldState.mainConnection)
        }
    }

    override fun addAll(connections: Set<BaseSyncConfiguration>) {
        updateState { oldState ->
            connections.forEach {
                logger.info("Added ${it.protocol.prettyName} remote connection configuration with name ${it.name}")
            }
            SyncConfigurations(oldState.connections union connections, oldState.mainConnection)
        }
    }

    override fun findAllOfType(type: SyncConfigurationType) = state.connections.filter { type == it.protocol }.toSet()

    override fun clear() {
        logger.info("Removed all remote connection configurations")
        updateState { oldState ->
            SyncConfigurations(emptySet(), oldState.mainConnection)
        }
    }

    override fun hasNoMainConnectionConfigured(): Boolean = state.mainConnection.isNullOrEmpty()

    override fun setMainConnection(connectionName: String) {
        updateState { oldState ->
            logger.info("Marked $connectionName as main remote sync connection for project ${project.name}")
            SyncConfigurations(oldState.connections, connectionName)
        }
    }

    override fun noStateLoaded() {
        super.noStateLoaded()
        logger.info("No SourceSync connections were loaded for project ${project.name}")
    }

    companion object {
        val logger = Logger.getInstance(SyncRemoteConfigurationsServiceImpl::class.java.simpleName)
    }
}