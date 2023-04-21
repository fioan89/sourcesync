package org.wavescale.sourcesync.configurations

import com.intellij.openapi.components.BaseState
import com.intellij.util.xmlb.annotations.Tag
import com.intellij.util.xmlb.annotations.XCollection

@Tag("source_sync_configurations")
class SyncConfigurationsState : BaseState() {
    @get:XCollection
    var connections by list<BaseSyncConfigurationState>()
}