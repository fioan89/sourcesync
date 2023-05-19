package org.wavescale.sourcesync.configurations

import kotlinx.serialization.Serializable

@Serializable
data class SyncConfigurations(val connections: Set<BaseSyncConfiguration> = emptySet(), val mainConnection: String? = null)