package org.wavescale.sourcesync.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.CollectionListModel
import com.intellij.ui.IdeBorderFactory
import com.intellij.ui.JBSplitter
import com.intellij.ui.SideBorder
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBViewport
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridConstraints.ALIGN_CENTER
import com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST
import com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST
import com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH
import com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL
import com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW
import com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK
import com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW
import com.intellij.uiDesigner.core.GridLayoutManager
import com.intellij.util.ui.JBUI
import org.wavescale.sourcesync.SourcesyncBundle
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JPasswordField
import javax.swing.JRadioButton
import javax.swing.JSpinner
import javax.swing.JTextField
import javax.swing.ScrollPaneConstants
import javax.swing.SwingConstants
import javax.swing.border.Border

class ConnectionConfigurationDialog(project: Project) : DialogWrapper(project, true) {
    private var listModel = CollectionListModel<String>()
    private var configurationsList = JBList(listModel)

    private val lbConnType = JLabel("------")
    private val tfHost = JTextField()
    private val tfPort = JTextField("21").apply { horizontalAlignment = SwingConstants.CENTER }
    private val tfRootPath = JTextField("/").apply { horizontalAlignment = SwingConstants.LEFT }
    private val cbUseSSHKeys = JCheckBox(SourcesyncBundle.message("useSSHKeyCheckBox"), false)
    private val tfCertFile = JTextField()
    private val btnBrowse = JButton(SourcesyncBundle.message("browseButton"))
    private val cbSSHPassphrase = JCheckBox(SourcesyncBundle.message("sshPassphraseCheckBox"), false)
    private val tfUserName = JTextField()
    private val pfUserPassword = JPasswordField().apply { echoChar = '*' }
    private val rbExplicit = JRadioButton(SourcesyncBundle.message("explicitRadioButton"), true)
    private val rbImplicit = JRadioButton(SourcesyncBundle.message("implicitRadioButton"), false)
    private val crtImlJTextField = JTextField(".crt;.iml")
    private val preserveTimestamp = JCheckBox(SourcesyncBundle.message("preserveTimestampCheckBox"), true)
    private val simultaneousJobs = JSpinner()


    init {
        init()
    }

    override fun init() {
        super.init()
        title = SourcesyncBundle.message("connectionConfigurationDialogTitle")
    }

    @Suppress("UnstableApiUsage")
    override fun createCenterPanel(): JComponent {
        val splitter = JBSplitter()
        val leftPanel = leftSidePanel()
        leftPanel.border = IdeBorderFactory.createBorder(SideBorder.RIGHT)

        splitter.firstComponent = leftPanel
        splitter.secondComponent = rightSidePanel()
        splitter.setHonorComponentsMinimumSize(true)
        splitter.putClientProperty(IS_VISUAL_PADDING_COMPENSATED_ON_COMPONENT_LEVEL_KEY, true)
        return splitter
    }

    override fun createContentPaneBorder(): Border = JBUI.Borders.empty()

    private fun leftSidePanel(): JComponent {
        return JPanel(BorderLayout()).apply {
            add(JBScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED).apply {
                add(JBViewport().apply {
                    add(configurationsList)
                    minimumSize = Dimension(300, -1)
                })
                border = JBUI.Borders.empty()
            })
            add(
                ToolbarDecorator.createDecorator(configurationsList)
                    .disableUpDownActions()
                    .setAddAction { println(">>> add button was not hit") }
                    .setAddIcon(AllIcons.General.Add)
                    .setRemoveAction { println(">>> remove button was hit") }
                    .createPanel()
            )
        }
    }

    private fun rightSidePanel(): JComponent {
        return JPanel(BorderLayout()).apply {
            border = JBUI.Borders.empty(15, 5, 0, 15)
            add(JPanel(GridLayoutManager(2, 2)).apply {

                add(JPanel(GridLayoutManager(1, 2)).apply {
                    add(JLabel(SourcesyncBundle.message("typeLabel")), GridConstraints().apply { row = 0; column = 0; anchor = ANCHOR_WEST; hSizePolicy = SIZEPOLICY_CAN_GROW or SIZEPOLICY_CAN_SHRINK })
                    add(lbConnType, GridConstraints().apply { row = 0; column = 1; anchor = ALIGN_CENTER; fill = FILL_HORIZONTAL; hSizePolicy = SIZEPOLICY_CAN_GROW })
                }, GridConstraints().apply { row = 0; column = 0; colSpan = 2; anchor = ANCHOR_WEST; fill = FILL_HORIZONTAL; hSizePolicy = SIZEPOLICY_CAN_GROW })

                add(JPanel(GridLayoutManager(12, 2)).apply {
                    add(JLabel(SourcesyncBundle.message("hostLabel")), GridConstraints().apply { row = 0; column = 0; anchor = ANCHOR_WEST; hSizePolicy = SIZEPOLICY_CAN_GROW or SIZEPOLICY_CAN_SHRINK })
                    add(tfHost, GridConstraints().apply { row = 0; column = 1; anchor = ANCHOR_WEST; fill = FILL_HORIZONTAL; hSizePolicy = SIZEPOLICY_CAN_GROW or SIZEPOLICY_WANT_GROW })

                    add(JLabel(SourcesyncBundle.message("portLabel")), GridConstraints().apply { row = 1; column = 0; anchor = ANCHOR_WEST; hSizePolicy = SIZEPOLICY_CAN_GROW or SIZEPOLICY_CAN_SHRINK })
                    add(tfPort, GridConstraints().apply { row = 1; column = 1; anchor = ANCHOR_WEST; fill = FILL_HORIZONTAL; hSizePolicy = SIZEPOLICY_CAN_GROW or SIZEPOLICY_WANT_GROW })

                    add(JLabel(SourcesyncBundle.message("rootPathLabel")), GridConstraints().apply { row = 2; column = 0; anchor = ANCHOR_WEST; hSizePolicy = SIZEPOLICY_CAN_GROW or SIZEPOLICY_CAN_SHRINK })
                    add(tfRootPath, GridConstraints().apply { row = 2; column = 1; anchor = ANCHOR_WEST; fill = FILL_HORIZONTAL; hSizePolicy = SIZEPOLICY_CAN_GROW or SIZEPOLICY_WANT_GROW })
                    // empty row
                    add(JPanel(), GridConstraints().apply { row = 3; column = 0; colSpan = 2; anchor = ANCHOR_WEST; fill = FILL_HORIZONTAL; hSizePolicy = SIZEPOLICY_CAN_GROW or SIZEPOLICY_WANT_GROW })

                    add(cbUseSSHKeys, GridConstraints().apply { row = 4; column = 0; anchor = ANCHOR_WEST; hSizePolicy = SIZEPOLICY_CAN_GROW or SIZEPOLICY_CAN_SHRINK })
                    add(JPanel((GridLayoutManager(1, 2))).apply {
                        add(tfCertFile, GridConstraints().apply { row = 0; column = 0; fill = FILL_HORIZONTAL; hSizePolicy = SIZEPOLICY_CAN_GROW or SIZEPOLICY_WANT_GROW; minimumSize = Dimension(300, -1) })
                        add(btnBrowse, GridConstraints().apply { row = 0; column = 1; anchor = ANCHOR_EAST; })
                    }, GridConstraints().apply { row = 4; column = 1; anchor = ANCHOR_WEST; fill = FILL_HORIZONTAL; hSizePolicy = SIZEPOLICY_CAN_GROW or SIZEPOLICY_WANT_GROW })

                    add(JPanel(), GridConstraints().apply { row = 5; column = 0; anchor = ANCHOR_WEST; hSizePolicy = SIZEPOLICY_CAN_GROW })
                    add(cbSSHPassphrase, GridConstraints().apply { row = 5; column = 1; anchor = ANCHOR_WEST; fill = FILL_HORIZONTAL; hSizePolicy = SIZEPOLICY_CAN_GROW or SIZEPOLICY_WANT_GROW })

                    add(JLabel(SourcesyncBundle.message("usernameLabel")), GridConstraints().apply { row = 6; column = 0; anchor = ANCHOR_WEST; hSizePolicy = SIZEPOLICY_CAN_GROW or SIZEPOLICY_CAN_SHRINK })
                    add(tfUserName, GridConstraints().apply { row = 6; column = 1; anchor = ANCHOR_WEST; fill = FILL_HORIZONTAL; hSizePolicy = SIZEPOLICY_CAN_GROW or SIZEPOLICY_WANT_GROW })

                    add(JLabel(SourcesyncBundle.message("passwordLabel")), GridConstraints().apply { row = 7; column = 0; anchor = ANCHOR_WEST; hSizePolicy = SIZEPOLICY_CAN_GROW or SIZEPOLICY_CAN_SHRINK })
                    add(pfUserPassword, GridConstraints().apply { row = 7; column = 1; anchor = ANCHOR_WEST; fill = FILL_HORIZONTAL; hSizePolicy = SIZEPOLICY_CAN_GROW or SIZEPOLICY_WANT_GROW })

                    add(rbExplicit, GridConstraints().apply { row = 8; column = 0; anchor = ANCHOR_EAST; hSizePolicy = SIZEPOLICY_CAN_GROW or SIZEPOLICY_CAN_SHRINK })
                    add(JLabel(SourcesyncBundle.message("excludeFilesLabel")), GridConstraints().apply { row = 8; column = 1; anchor = ANCHOR_WEST; fill = FILL_HORIZONTAL; hSizePolicy = SIZEPOLICY_CAN_GROW or SIZEPOLICY_WANT_GROW })

                    add(rbImplicit, GridConstraints().apply { row = 9; column = 0; anchor = ANCHOR_EAST; hSizePolicy = SIZEPOLICY_CAN_GROW or SIZEPOLICY_CAN_SHRINK })
                    add(crtImlJTextField, GridConstraints().apply { row = 9; column = 1; anchor = ANCHOR_WEST; fill = FILL_HORIZONTAL; hSizePolicy = SIZEPOLICY_CAN_GROW or SIZEPOLICY_WANT_GROW })

                    add(preserveTimestamp, GridConstraints().apply { row = 10; column = 1; anchor = ANCHOR_WEST; fill = FILL_HORIZONTAL; hSizePolicy = SIZEPOLICY_CAN_GROW or SIZEPOLICY_WANT_GROW })

                    add(JPanel((GridLayoutManager(1, 2))).apply {
                        add(JLabel(SourcesyncBundle.message("syncJobsLabel")), GridConstraints().apply { row = 0; column = 0; anchor = ANCHOR_WEST; hSizePolicy = SIZEPOLICY_CAN_GROW })
                        add(simultaneousJobs, GridConstraints().apply { row = 0; column = 1; anchor = ANCHOR_WEST; hSizePolicy = SIZEPOLICY_CAN_GROW })
                    }, GridConstraints().apply { row = 11; column = 0; colSpan = 2; anchor = ANCHOR_WEST; hSizePolicy = SIZEPOLICY_CAN_GROW })

                }, GridConstraints().apply { row = 1; column = 0; colSpan = 2; anchor = ANCHOR_WEST; fill = FILL_BOTH; hSizePolicy = SIZEPOLICY_CAN_GROW or SIZEPOLICY_WANT_GROW; vSizePolicy = SIZEPOLICY_CAN_GROW or SIZEPOLICY_WANT_GROW })


            }, BorderLayout.CENTER)
        }
    }
}