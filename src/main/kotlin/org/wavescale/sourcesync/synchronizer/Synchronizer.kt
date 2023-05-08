package org.wavescale.sourcesync.synchronizer

import com.intellij.openapi.progress.ProgressIndicator
import java.nio.file.Path

sealed interface Synchronizer {
    fun connect(): Boolean
    fun disconnect()

    fun syncFile(src: String, remoteDest: Path, indicator: ProgressIndicator)
}