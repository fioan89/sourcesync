package org.wavescale.sourcesync.configurations

import com.intellij.openapi.components.BaseState
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.Tag
import com.intellij.util.xmlb.annotations.XCollection

@Tag("source_sync_configurations")
class SyncConfigurationsState : BaseState() {
    @get:XCollection
    var connections by list<BaseSyncConfigurationState>()

    @get:Attribute("main_connection")
    var mainConnection by string()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as SyncConfigurationsState

        if (connections != other.connections) return false
        return mainConnection == other.mainConnection
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + connections.hashCode()
        result = 31 * result + (mainConnection?.hashCode() ?: 0)
        return result
    }


}