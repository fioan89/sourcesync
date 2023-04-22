package org.wavescale.sourcesync.configurations

import com.intellij.openapi.components.BaseState
import com.intellij.util.xmlb.Accessor
import com.intellij.util.xmlb.annotations.Tag
import com.intellij.util.xmlb.annotations.XCollection

@Tag("source_sync_configurations")
class SyncConfigurationsState : BaseState() {
    @get:XCollection
    var connections by list<BaseSyncConfigurationState>()

    fun add(connection: BaseSyncConfigurationState) {
        connections.add(connection)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as SyncConfigurationsState

        return connections == other.connections
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + connections.hashCode()
        return result
    }

    override fun accepts(accessor: Accessor, bean: Any): Boolean {
        val isAccepted = super.accepts(accessor, bean)
        return isAccepted
    }
}