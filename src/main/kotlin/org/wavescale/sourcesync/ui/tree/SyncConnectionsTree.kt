package org.wavescale.sourcesync.ui.tree

import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.tree.TreeUtil
import javax.swing.tree.TreeModel
import javax.swing.tree.TreeNode

class SyncConnectionsTree(model: TreeModel) : Tree(model) {

    fun selectNode(node: TreeNode) {
        TreeUtil.selectNode(this, node)
    }

    fun expandAll() {
        TreeUtil.expandAll(this)
    }
}