package org.wavescale.sourcesync.ui

import com.intellij.CommonBundle
import com.intellij.icons.AllIcons
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.IdeBorderFactory
import com.intellij.ui.JBSplitter
import com.intellij.ui.SideBorder
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBViewport
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.tree.TreeUtil
import org.wavescale.sourcesync.SourcesyncBundle
import org.wavescale.sourcesync.configurations.ScpSyncConfiguration
import org.wavescale.sourcesync.configurations.SshSyncConfiguration
import org.wavescale.sourcesync.configurations.SyncConfigurationType
import org.wavescale.sourcesync.configurations.SyncConfigurationType.SCP
import org.wavescale.sourcesync.configurations.SyncConfigurationType.SFTP
import org.wavescale.sourcesync.services.SyncRemoteConfigurationsService
import org.wavescale.sourcesync.ui.tree.SyncConfigurationTreeRenderer
import org.wavescale.sourcesync.ui.tree.SyncConnectionsTree
import org.wavescale.sourcesync.ui.tree.SyncConnectionsTreeModel
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import javax.swing.Action
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.KeyStroke
import javax.swing.ScrollPaneConstants
import javax.swing.border.Border
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeNode
import javax.swing.tree.TreeSelectionModel
import kotlin.math.max

class ConnectionConfigurationDialog(val project: Project) : DialogWrapper(project, true) {
    private var syncRemoteConfigurationsService = project.service<SyncRemoteConfigurationsService>()

    private val splitter = JBSplitter(false, "SourcesyncConnectionConfiguration.dividerProportion", 0.3f, 0.5f)

    private val rootNode = DefaultMutableTreeNode("Root")
    private val treeModel = SyncConnectionsTreeModel(rootNode)
    private val tree = SyncConnectionsTree(treeModel).apply {
        selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        selectionModel.addTreeSelectionListener { event ->
            val node = event.path.lastPathComponent as DefaultMutableTreeNode
            when (node.userObject) {
                is ConnectionConfigurationComponent -> splitter.secondComponent = (node.userObject as ConnectionConfigurationComponent).component
            }
        }
    }

    private val applyAction = ApplyAction()

    init {
        init()
    }

    override fun init() {
        super.init()
        title = SourcesyncBundle.message("connectionConfigurationDialogTitle")
    }

    private fun onConfigModifications() {
        this.updateApplyButton()
        tree.updateUI()
    }

    private fun updateApplyButton() {
        applyAction.isEnabled = treeModel.getAllComponents().hasModifications()
    }

    @Suppress("UnstableApiUsage")
    override fun createCenterPanel(): JComponent {
        val leftPanel = leftSidePanel()
        leftPanel.border = IdeBorderFactory.createBorder(SideBorder.RIGHT)

        splitter.firstComponent = leftPanel
        splitter.secondComponent = JPanel()
        splitter.setHonorComponentsMinimumSize(true)
        splitter.putClientProperty(IS_VISUAL_PADDING_COMPENSATED_ON_COMPONENT_LEVEL_KEY, true)

        val d = splitter.preferredSize
        d.width = max(d.width, 860)
        d.height = max(d.height, 450)
        splitter.preferredSize = d

        initTree()
        return splitter
    }

    private fun initTree() {
        tree.apply {
            isRootVisible = false
            showsRootHandles = true
        }
        tree.cellRenderer = SyncConfigurationTreeRenderer()

        val firstNode = addRemoteSynConfigurations(rootNode)
        TreeUtil.installActions(tree)
        tree.registerKeyboardAction({ clickDefaultButton() }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_FOCUSED)
        tree.emptyText.appendText(SourcesyncBundle.message("status.text.no.sync.configurations.added"))
        tree.expandAll()

        if (firstNode != null) {
            tree.selectNode(firstNode)
        }
    }

    private fun addRemoteSynConfigurations(root: DefaultMutableTreeNode): TreeNode? {
        var firstNodeToSelectLater: TreeNode? = null
        SyncConfigurationType.values().forEach { type ->
            val connections = syncRemoteConfigurationsService.findAllOfType(type)
            if (connections.isNotEmpty()) {
                val typeNode = DefaultMutableTreeNode(type.prettyName)
                root.add(typeNode)
                connections.forEach { connection ->
                    val connectionComponentNode = DefaultMutableTreeNode(ConnectionConfigurationComponent(project, connection, this::onConfigModifications))
                    typeNode.add(connectionComponentNode)

                    if (firstNodeToSelectLater == null) {
                        firstNodeToSelectLater = connectionComponentNode
                    }
                }
            }
        }
        treeModel.reload()

        return firstNodeToSelectLater
    }

    override fun createContentPaneBorder(): Border = JBUI.Borders.empty()

    private fun leftSidePanel(): JComponent {

        val toolbarDecorator = ToolbarDecorator.createDecorator(tree)
        return panel {
            row {
                cell(JPanel(BorderLayout()))
                    .resizableColumn()
                    .align(Align.FILL)
                    .applyToComponent {
                        add(JBScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED).apply {
                            add(JBViewport().apply {
                                add(tree)
                            })
                            border = JBUI.Borders.empty()
                        })
                        add(
                            toolbarDecorator.disableUpDownActions()
                                .setAddAction { button ->
                                    AddSyncRemoteConfigurationPopUp.create(
                                        SyncConfigurationType.values().toList(),
                                        this@ConnectionConfigurationDialog::addRemoteSyncConfiguration
                                    ).show(button.preferredPopupPoint)
                                }
                                .setAddIcon(AllIcons.General.Add)
                                .setRemoveAction {
                                    val targetNode = tree.selectionPath?.lastPathComponent as DefaultMutableTreeNode
                                    val parentNode = targetNode.parent as DefaultMutableTreeNode

                                    treeModel.removeNodeFromParent(targetNode)
                                    if (parentNode.childCount == 0) {
                                        treeModel.removeNodeFromParent(parentNode)
                                    }
                                    splitter.secondComponent = JPanel()
                                    splitter.updateUI()
                                }.createPanel()
                        )
                        border = JBUI.Borders.empty()
                    }
            }.resizableRow()
        }
    }

    private fun addRemoteSyncConfiguration(syncConfigurationType: SyncConfigurationType) {
        val connectionTypeNode: DefaultMutableTreeNode
        val connectionNodeToAdd: DefaultMutableTreeNode
        when (syncConfigurationType) {
            SFTP -> {
                connectionTypeNode = treeModel.getOrCreateNodeFor(SFTP)
                connectionNodeToAdd = DefaultMutableTreeNode(ConnectionConfigurationComponent(project, SshSyncConfiguration(), this::onConfigModifications))
            }

            SCP -> {
                connectionTypeNode = treeModel.getOrCreateNodeFor(SCP)
                connectionNodeToAdd = DefaultMutableTreeNode(ConnectionConfigurationComponent(project, ScpSyncConfiguration(), this::onConfigModifications))
            }
        }
        connectionTypeNode.add(connectionNodeToAdd)
        treeModel.reload()
        tree.selectNode(connectionNodeToAdd)
    }

    override fun createActions(): Array<Action> {
        return arrayOf(okAction, applyAction, cancelAction)
    }

    override fun doOKAction() {
        applyChanges()
        super.doOKAction()
    }

    private fun applyChanges() {
        syncRemoteConfigurationsService.clear()
        syncRemoteConfigurationsService.addAll(treeModel.getAllComponents().toConfigurationSet())
    }

    private inner class ApplyAction : DialogWrapperAction(CommonBundle.getApplyButtonText()) {

        init {
            isEnabled = false
        }

        override fun doAction(e: ActionEvent?) {
            applyChanges()
            isEnabled = false
        }
    }

}