package org.wavescale.sourcesync.services

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import org.wavescale.sourcesync.configurations.BaseSyncConfigurationState
import org.wavescale.sourcesync.configurations.SyncConfigurationsState

@Service(Service.Level.PROJECT)
@State(name = "SourceSyncRemoteConfigurationsService", storages = [Storage(value = "sourcesync.xml")])
class SyncRemoteConfigurationsServiceImpl(val project: Project) : PersistentStateComponent<SyncConfigurationsState>, SyncRemoteConfigurationsService {

    private var myState = SyncConfigurationsState()

    override fun getState() = myState

    override fun hasNoConfiguration() = myState.connections.size == 0

    override fun add(connection: BaseSyncConfigurationState) {
        myState.add(connection)
        logger.info("Added ${connection.type.prettyName} remote connection configuration with name ${connection.name}")
    }

    override fun loadState(state: SyncConfigurationsState) {
        myState = state
    }

    override fun noStateLoaded() {
        super.noStateLoaded()
        logger.info("No SourceSync connections were loaded for project ${project.name}")
    }

    companion object {
        val logger = Logger.getInstance(SyncRemoteConfigurationsServiceImpl::class.java.simpleName)
    }
}