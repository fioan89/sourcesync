package org.wavescale.sourcesync.ui

import com.intellij.ui.DocumentAdapter
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBUI
import org.wavescale.sourcesync.SourcesyncBundle
import org.wavescale.sourcesync.configurations.BaseSyncConfiguration
import javax.swing.event.DocumentEvent

class ConnectionConfigurationComponent(private val model: BaseSyncConfiguration, onModification: () -> Unit) {
    private val original = model.clone()

    var displayName = model.name

    val component = panel {
        row {
            label("Name:")
            textField().bindText(model::name).applyToComponent {
                document.addDocumentListener(object : DocumentAdapter() {
                    override fun textChanged(e: DocumentEvent) {
                        displayName = text
                        onModification()
                    }
                })
            }

            label(SourcesyncBundle.message("typeLabel"))
            label(model.protocol.prettyName)
        }
    }.apply {
        border = JBUI.Borders.empty()
    }

    val isModified: Boolean
        get() {
            component.apply()
            return original != model
        }

    val snapshot: BaseSyncConfiguration
        get() {
            component.apply()
            return model.clone()
        }
}

fun Collection<ConnectionConfigurationComponent>.hasModifications(): Boolean {
    return this.map { it.isModified }.fold(false) { acc, isModified -> acc || isModified }
}

fun Collection<ConnectionConfigurationComponent>.toConfigurationSet(): Set<BaseSyncConfiguration> {
    return this.map { it.snapshot }.toSet()
}