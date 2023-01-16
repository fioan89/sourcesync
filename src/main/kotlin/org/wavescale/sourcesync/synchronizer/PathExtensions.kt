package org.wavescale.sourcesync.synchronizer

import java.nio.file.Path
import kotlin.io.path.pathString

private const val UNIX_SEPARATOR = "/"
private const val WIN_SEPARATOR = """\"""

/**
 * Returns the string representation of this path.
 *
 * The returned path string uses [other] separator. [other] should contain only
 * one type of separator. If there are two separators or none then the path is returned as is.
 */
fun Path.pathStringLike(other: String): String {
    return if (other.isUnixPath()) {
        this.pathString.replace(WIN_SEPARATOR, UNIX_SEPARATOR)
    } else if (other.isWindowsPath()) {
        this.pathString.replace(UNIX_SEPARATOR, WIN_SEPARATOR)
    } else this.pathString
}

private fun String.isUnixPath() = this.contains(UNIX_SEPARATOR) && !this.contains(WIN_SEPARATOR)
private fun String.isWindowsPath() = this.contains(WIN_SEPARATOR) && !this.contains(UNIX_SEPARATOR)