package org.wavescale.sourcesync.synchronizer

import com.intellij.openapi.progress.ProgressIndicator
import java.nio.file.Path

sealed interface Synchronizer {
    fun connect(): Boolean
    fun disconnect()
    fun syncFiles(src: Collection<Pair<String, Path>>, indicator: ProgressIndicator) {
        src.forEach { (src, dst) ->
            syncFile(src, dst, indicator)
        }
    }

    fun syncFile(src: String, remoteDest: Path, indicator: ProgressIndicator)
}