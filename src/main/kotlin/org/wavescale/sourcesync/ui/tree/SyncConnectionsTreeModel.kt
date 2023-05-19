package org.wavescale.sourcesync.ui.tree

import org.wavescale.sourcesync.configurations.SyncConfigurationType
import org.wavescale.sourcesync.ui.ConnectionConfigurationComponent
import java.util.*
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

class SyncConnectionsTreeModel(private val rootNode: DefaultMutableTreeNode) : DefaultTreeModel(rootNode) {
    fun getOrCreateNodeFor(syncType: SyncConfigurationType): DefaultMutableTreeNode {
        var node = rootNode.children().asSequence().find { (it as DefaultMutableTreeNode).userObject.equals(syncType.prettyName) }
        if (node == null) {
            node = DefaultMutableTreeNode(syncType.prettyName)

            rootNode.add(node)
            reload()
        }
        return node as DefaultMutableTreeNode
    }

    fun getAllComponents(): List<ConnectionConfigurationComponent> {
        val stack = Stack<DefaultMutableTreeNode>()
        stack.push(rootNode)

        val components = mutableListOf<ConnectionConfigurationComponent>()
        while (!stack.isEmpty()) {
            val node = stack.pop()
            if (node.userObject is ConnectionConfigurationComponent) {
                components.add(node.userObject as ConnectionConfigurationComponent)
            } else {
                node.children().asIterator().forEach {
                    stack.push(it as DefaultMutableTreeNode)
                }
            }
        }

        return components
    }
}