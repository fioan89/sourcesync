package org.wavescale.sourcesync.ui

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.COLUMNS_LARGE
import com.intellij.ui.dsl.builder.COLUMNS_MEDIUM
import com.intellij.ui.dsl.builder.COLUMNS_TINY
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.TopGap
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.toNonNullableProperty
import com.intellij.ui.dsl.builder.toNullableProperty
import com.intellij.ui.layout.ComponentPredicate
import com.intellij.util.ui.JBUI
import org.wavescale.sourcesync.SourcesyncBundle
import org.wavescale.sourcesync.configurations.AuthenticationType
import org.wavescale.sourcesync.configurations.BaseSyncConfiguration
import javax.swing.JLabel
import javax.swing.event.DocumentEvent

class ConnectionConfigurationComponent(private val project: Project, private val model: BaseSyncConfiguration, onModification: () -> Unit) {
    private val original = model.clone()
    private lateinit var cb: ComboBox<AuthenticationType>

    var displayName = model.name


    val component = panel {
        row {
            label(SourcesyncBundle.message("sync.editor.name.label"))
            textField().columns(COLUMNS_MEDIUM).bindText(model::name).applyToComponent {
                document.addDocumentListener(object : DocumentAdapter() {
                    override fun textChanged(e: DocumentEvent) {
                        displayName = text
                        onModification()
                    }
                })
            }

        }.layout(RowLayout.LABEL_ALIGNED)

        separator()

        row {
            label(SourcesyncBundle.message("sync.editor.host.label"))
            textField().columns(COLUMNS_LARGE).bindText(model::hostname).applyToComponent {
                document.addDocumentListener(object : DocumentAdapter() {
                    override fun textChanged(e: DocumentEvent) {
                        onModification()
                    }
                })
            }

            cell(JLabel("")).resizableColumn().align(AlignX.FILL)

            label(SourcesyncBundle.message("sync.editor.port.label"))
            textField().columns(COLUMNS_TINY).bindText(model::port).applyToComponent {
                document.addDocumentListener(object : DocumentAdapter() {
                    override fun textChanged(e: DocumentEvent) {
                        onModification()
                    }
                })
            }
        }.layout(RowLayout.LABEL_ALIGNED)

        row {
            label(SourcesyncBundle.message("sync.editor.username.label"))
            textField().bindText(model::username).columns(COLUMNS_MEDIUM).applyToComponent {
                document.addDocumentListener(object : DocumentAdapter() {
                    override fun textChanged(e: DocumentEvent) {
                        onModification()
                    }
                })
            }

            cell(JLabel("")).resizableColumn().align(AlignX.FILL)

            label(SourcesyncBundle.message("sync.editor.authentication.type.label"))
            cb = comboBox(AuthenticationType.values().asList(), SimpleListCellRenderer.create("") { it.prettyName })
                .columns(12)
                .bindItem(model::authenticationType.toNullableProperty())
                .onChanged {
                    onModification()
                }
                .component
        }.layout(RowLayout.LABEL_ALIGNED)

        rowsRange {
            row {
                label(SourcesyncBundle.message("sync.editor.password.label"))
                passwordField()
                    .bindText(model::password.toNonNullableProperty(""))
                    .columns(COLUMNS_MEDIUM)
                    .resizableColumn()
                    .applyToComponent {
                        document.addDocumentListener(object : DocumentAdapter() {
                            override fun textChanged(e: DocumentEvent) {
                                onModification()
                            }
                        })
                    }
            }.layout(RowLayout.LABEL_ALIGNED)
        }.visibleIf(object : ComponentPredicate() {
            override fun invoke() = cb.selectedItem == AuthenticationType.PASSWORD
            override fun addListener(listener: (Boolean) -> Unit) {
                cb.addActionListener {
                    listener(cb.selectedItem == AuthenticationType.PASSWORD)
                }
            }
        })

        rowsRange {
            row {
                label(SourcesyncBundle.message("sync.editor.private.key.label"))
                textFieldWithBrowseButton(
                    SourcesyncBundle.message("sync.editor.private.key.dialog.title"),
                    project,
                    FileChooserDescriptorFactory.createSingleFileDescriptor()
                )
                    .bindText(model::privateKey.toNonNullableProperty("")).columns(COLUMNS_MEDIUM)
                    .columns(COLUMNS_LARGE)
                    .resizableColumn()
                    .applyToComponent {
                        textField.document.addDocumentListener(object : DocumentAdapter() {
                            override fun textChanged(e: DocumentEvent) {
                                onModification()
                            }
                        })
                    }
            }.layout(RowLayout.LABEL_ALIGNED)

            row {
                label(SourcesyncBundle.message("sync.editor.passphrase.label"))
                passwordField()
                    .bindText(model::passphrase.toNonNullableProperty(""))
                    .columns(COLUMNS_MEDIUM)
                    .resizableColumn()
                    .applyToComponent {
                        document.addDocumentListener(object : DocumentAdapter() {
                            override fun textChanged(e: DocumentEvent) {
                                onModification()
                            }
                        })
                    }
            }.layout(RowLayout.LABEL_ALIGNED)
        }.visibleIf(object : ComponentPredicate() {
            override fun invoke() = cb.selectedItem == AuthenticationType.KEY_PAIR
            override fun addListener(listener: (Boolean) -> Unit) {
                cb.addActionListener {
                    listener(cb.selectedItem == AuthenticationType.KEY_PAIR)
                }
            }
        })

        row {
            label(SourcesyncBundle.message("sync.editor.workspace.label"))
            textField().columns(COLUMNS_LARGE).bindText(model::workspaceBasePath).applyToComponent {
                toolTipText = SourcesyncBundle.message("sync.editor.workspace.tooltip")
                document.addDocumentListener(object : DocumentAdapter() {
                    override fun textChanged(e: DocumentEvent) {
                        onModification()
                    }
                })
            }
        }.topGap(TopGap.MEDIUM).layout(RowLayout.LABEL_ALIGNED)

        row {
            label(SourcesyncBundle.message("sync.editor.skip.extensions.label"))
            textField().columns(12).bindText(model::excludedFiles).applyToComponent {
                document.addDocumentListener(object : DocumentAdapter() {
                    override fun textChanged(e: DocumentEvent) {
                        onModification()
                    }
                })
            }
        }.layout(RowLayout.INDEPENDENT)
        row {
            checkBox(SourcesyncBundle.message("sync.editor.timestamps.label"))
                .bindSelected(model::preserveTimestamps)
                .onChanged {
                    onModification()
                }
        }.layout(RowLayout.INDEPENDENT)

    }.apply {
        border = JBUI.Borders.empty(15, 5, 0, 15)
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