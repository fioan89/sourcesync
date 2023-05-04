package org.wavescale.sourcesync.ui.tree

import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.SimpleTextAttributes
import org.wavescale.sourcesync.ui.ConnectionConfigurationComponent
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode

class SyncConfigurationTreeRenderer : ColoredTreeCellRenderer() {
    override fun customizeCellRenderer(tree: JTree, value: Any, selected: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean) {
        if (value !is DefaultMutableTreeNode) {
            return
        }

        val nameToRender = getNameToRender(value.userObject)
        if (leaf) {
            append(nameToRender, SimpleTextAttributes.REGULAR_ATTRIBUTES)
        } else {
            // connection types
            append(nameToRender, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
        }
    }

    private fun getNameToRender(userObject: Any) = when (userObject) {
        is ConnectionConfigurationComponent -> userObject.displayName
        else -> userObject as String
    }
}