package org.wavescale.sourcesync.ui

import com.intellij.CommonBundle
import com.intellij.icons.AllIcons
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.vfs.VirtualFile
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
import org.wavescale.sourcesync.api.ConnectionConfiguration
import org.wavescale.sourcesync.api.ConnectionConstants
import org.wavescale.sourcesync.api.PasswordlessSSH
import org.wavescale.sourcesync.config.FTPConfiguration
import org.wavescale.sourcesync.config.FTPSConfiguration
import org.wavescale.sourcesync.config.SCPConfiguration
import org.wavescale.sourcesync.config.SFTPConfiguration
import org.wavescale.sourcesync.factory.ConfigConnectionFactory
import org.wavescale.sourcesync.factory.ConnectionConfig
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ItemEvent
import javax.swing.Action
import javax.swing.ButtonGroup
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JPasswordField
import javax.swing.JRadioButton
import javax.swing.JSpinner
import javax.swing.JTextField
import javax.swing.ListSelectionModel
import javax.swing.ScrollPaneConstants
import javax.swing.SpinnerNumberModel
import javax.swing.SwingConstants
import javax.swing.SwingUtilities
import javax.swing.border.Border
import javax.swing.event.ListSelectionEvent

class ConnectionConfigurationDialog(val project: Project) : DialogWrapper(project, true) {
    private var listModel = CollectionListModel<String>()
    private var configurationsList = JBList(listModel)

    private val lbConnType = JLabel("------")
    private val tfHost = JTextField()
    private val tfPort = JTextField("21").apply { horizontalAlignment = SwingConstants.CENTER }
    private val tfProjectBasePath = JTextField("/").apply { horizontalAlignment = SwingConstants.LEFT }
    private val cbUseSSHKeys = JCheckBox(SourcesyncBundle.message("useSSHKeyCheckBox"), false)
    private val tfCertFile = JTextField()
    private val btnBrowse = JButton(SourcesyncBundle.message("browseButton"))
    private val cbSSHPassphrase = JCheckBox(SourcesyncBundle.message("sshPassphraseCheckBox"), false)
    private val tfUserName = JTextField()
    private val lbPassword = JLabel(SourcesyncBundle.message("passwordLabel"))
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

        // group radio buttons
        ButtonGroup().apply {
            add(rbImplicit)
            add(rbExplicit)
        }

        // set model and limits for simultaneous jobs
        simultaneousJobs.model = SpinnerNumberModel(2, 1, 10, 1)

        // configure the certificate file chooser and show it when browse is hit
        btnBrowse.apply {
            addActionListener {
                val descriptor = FileChooserDescriptor(true, false, false, false, false, false)
                        .withTitle(SourcesyncBundle.message("certificateFileChooserDialogTitle"))
                var result: VirtualFile? = null
                FileChooser.chooseFile(descriptor, project, null) { result = it }
                tfCertFile.text = result?.canonicalPath
            }
        }

        cbUseSSHKeys.addItemListener(updateConnectionAuthFormsCallback())
        cbSSHPassphrase.addItemListener(updateConnectionAuthFormsCallback())

        configurationsList.selectionModel.addListSelectionListener(highlightConnectionConfigurationCallback())

        val connectionFactory = ConfigConnectionFactory.getInstance()
        loadConnections(connectionFactory)
    }

    private fun enableCertificateWidgets(isEnabled: Boolean) {
        tfCertFile.isEnabled = isEnabled
        btnBrowse.isEnabled = isEnabled
        cbSSHPassphrase.isEnabled = isEnabled
        pfUserPassword.isEnabled = isEnabled
    }

    private fun updateConnectionAuthFormsCallback(): (e: ItemEvent) -> Unit = {
        val source = it.source as JCheckBox
        enableCertificateWidgets(source.isEnabled)
        // the password field can be enabled only when ssh with password is configured
        if (cbUseSSHKeys.isSelected && cbSSHPassphrase.isSelected) lbPassword.text = SourcesyncBundle.message("passphraseLabel") else lbPassword.text = SourcesyncBundle.message("passwordLabel")
    }

    private fun highlightConnectionConfigurationCallback(): (event: ListSelectionEvent) -> Unit = {
        val selectionModel = it.source as ListSelectionModel
        val index = selectionModel.minSelectionIndex
        if (configurationsList.model != null && index >= 0) {
            val target: String = configurationsList.model.getElementAt(index)
            val connectionConfiguration = ConfigConnectionFactory.getInstance().getConnectionConfiguration(target)
            uploadConfigurationFromPersistance(connectionConfiguration)
        }
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
                            .setAddAction {
                                val parentWindow = SwingUtilities.windowForComponent(configurationsList)
                                val targetConfig = TargetLocation(parentWindow)
                                val name = targetConfig.targetName
                                val type = targetConfig.targetType
                                listModel.add(name)
                                configurationsList.selectedIndex = listModel.getElementIndex(name)
                                createConnection(name, type)
                            }
                            .setAddIcon(AllIcons.General.Add)
                            .setRemoveAction {
                                val target: String = configurationsList.selectedValue
                                val index = listModel.getElementIndex(target)
                                if (index >= 0) {
                                    listModel.remove(target)
                                    // remove from config
                                    val configConnectionFactory = ConfigConnectionFactory.getInstance()
                                    configConnectionFactory.removeConnectionConfiguration(target)
                                    // select the bottom index
                                    if (index > 0) {
                                        configurationsList.selectedIndex = index - 1
                                    }
                                }
                                if (listModel.size == 0) {
                                    ConnectionConfig.getInstance().apply {
                                        removeAssociations()
                                        saveModuleAssociatedConn()
                                    }
                                }
                            }.createPanel()
            )
        }
    }

    override fun createActions(): Array<Action> {
        return arrayOf(okAction, ApplyAction(), cancelAction)
    }

    private inner class ApplyAction : DialogWrapperAction(CommonBundle.getApplyButtonText()) {
        override fun doAction(e: ActionEvent?) {
            applyConnectionPreferences()
        }
    }

    private fun applyConnectionPreferences() {
        val connectionFactory = ConfigConnectionFactory.getInstance()
        val connectionConfiguration = connectionFactory.getConnectionConfiguration(configurationsList.selectedValue)
        downloadConfigurationToPersistence(connectionConfiguration)
        connectionFactory.saveConnections()
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

                    add(JLabel(SourcesyncBundle.message("workspaceDirectoyLabel")), GridConstraints().apply { row = 2; column = 0; anchor = ANCHOR_WEST; hSizePolicy = SIZEPOLICY_CAN_GROW or SIZEPOLICY_CAN_SHRINK })
                    add(tfProjectBasePath, GridConstraints().apply { row = 2; column = 1; anchor = ANCHOR_WEST; fill = FILL_HORIZONTAL; hSizePolicy = SIZEPOLICY_CAN_GROW or SIZEPOLICY_WANT_GROW })
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

                    add(lbPassword, GridConstraints().apply { row = 7; column = 0; anchor = ANCHOR_WEST; hSizePolicy = SIZEPOLICY_CAN_GROW or SIZEPOLICY_CAN_SHRINK })
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

    override fun doOKAction() {
        applyConnectionPreferences()
        super.doOKAction()
    }


    /**
     * Sets the connection type label to the given string.
     *
     * @param connectionType a `String` representing the connection type.
     */
    private fun setConnectionType(connectionType: String?) {
        lbConnType.text = connectionType
    }

    /**
     * Returns the port value stored in the port text field.
     *
     * @return an int value representing the target port.
     */
    private fun getPort(): Int {
        return try {
            Integer.valueOf(tfPort.text)
        } catch (e: NumberFormatException) {
            22
        }
    }

    /**
     * Sets the target port.
     *
     * @param port an int value representing the target port
     */
    private fun setPort(port: Int) {
        tfPort.text = port.toString()
    }

    /**
     * Returns target hostname.
     *
     * @return a `String` representing the address of a remote host.
     */
    private fun getHost(): String? {
        return tfHost.text
    }

    /**
     * Sets target hostname.
     *
     * @param host a `String` representing the address of a remote host.
     */
    private fun setHost(host: String?) {
        tfHost.text = host
    }

    /**
     * Gets the target root path. This is the root where we will sync files.
     *
     * @return a `String` representing a path on the remote target.
     */
    private fun getRootPath(): String? {
        return tfProjectBasePath.text
    }

    /**
     * Sets the target root path. This is the root where we will sync files.
     *
     * @param projectBasePath a `String` representing a path on the remote target.
     */
    private fun setProjectBasePath(projectBasePath: String?) {
        tfProjectBasePath.text = projectBasePath
    }

    private fun getUserName(): String? {
        return tfUserName.text
    }

    private fun setUserName(userName: String?) {
        tfUserName.text = userName
    }

    private fun getUserPassword(): String {
        return String(pfUserPassword.password)
    }

    private fun setUserPassword(userPassword: String?) {
        pfUserPassword.text = userPassword
    }

    private fun isImplicit(): Boolean {
        return rbImplicit.isSelected
    }

    private fun setImplicit(implicit: Boolean) {
        rbImplicit.isSelected = implicit
    }

    private fun setExplicit(explicit: Boolean) {
        rbExplicit.isSelected = explicit
    }

    private fun getSimultaneousJobs(): Int {
        return simultaneousJobs.value as Int
    }

    private fun setSimultaneousJobs(nrOfJobs: Int) {
        simultaneousJobs.value = nrOfJobs
    }

    private fun getExludedFiles(): String? {
        return crtImlJTextField.text
    }

    private fun isTimestampPreserved(): Boolean {
        return preserveTimestamp.isSelected
    }

    private fun setPreserveTimestamp(preserveTimestamp: Boolean) {
        this.preserveTimestamp.isSelected = preserveTimestamp
    }

    private fun setExcludedFiles(excludedFiles: String?) {
        crtImlJTextField.text = excludedFiles
    }

    private fun shouldUsePasswordlessSSH(): Boolean {
        return cbUseSSHKeys.isSelected
    }

    private fun usePasswordlessSSH(value: Boolean) {
        cbUseSSHKeys.isSelected = value
    }

    private fun shouldUsePasswordlessWithPassphrase(): Boolean {
        return cbSSHPassphrase.isSelected
    }

    private fun usePassphraseForPasswordlessSSH(value: Boolean) {
        cbSSHPassphrase.isSelected = value
    }

    private fun getSSHCertificatePath(): String? {
        return tfCertFile.text
    }

    private fun setSSHCertificatePath(certificatePath: String?) {
        tfCertFile.text = certificatePath
    }

    /**
     * Shows or hides the group of implicit and explicit radio buttons.
     *
     * @param visible `true` for visible, `false` otherwise
     */
    private fun setConnectionMethodVisible(visible: Boolean) {
        rbExplicit.apply {
            isVisible = visible
            isEnabled = visible
        }
        rbImplicit.apply {
            isVisible = visible
            isEnabled = visible
        }
    }

    /**
     * Shows or hides preserve timestamp checkbox.
     *
     * @param visible `true` if checkbox must be visible, `false` otherwise
     */
    private fun setPreserveTimestampVisible(visible: Boolean) {
        preserveTimestamp.apply {
            isVisible = visible
            isEnabled = visible
        }
    }

    /**
     * Shows or hides the ssh key related widgets.
     *
     * @param visible `true` if the connection supports ssh keys, `false` otherwise
     */
    private fun setSSHKeysVisible(visible: Boolean) {
        cbUseSSHKeys.apply {
            isVisible = visible
            isEnabled = visible
        }

        showCertificateWidgets(visible)
    }

    /**
     * Sets the visibility of certificate widgets (certificate text field, and select certificate button).
     *
     * @param visible
     */
    private fun showCertificateWidgets(visible: Boolean) {
        tfCertFile.isVisible = visible
        btnBrowse.isVisible = visible
        cbSSHPassphrase.isVisible = visible
    }

    /**
     * Loads connection stored in the connection factory and add them in the viewer.
     *
     * @param connectionFactory a [ConfigConnectionFactory] instance that contains
     * connections stored in the persistence layer.
     */
    private fun loadConnections(connectionFactory: ConfigConnectionFactory) {
        listModel.add(connectionFactory.connectionNames.toList())
        // select the firs index and trigger an action event.
        configurationsList.selectedIndex = 0
    }

    /**
     * Creates a new connection with the given name and type. The newly created connection is automatically
     * registered to the connection factory.
     *
     * @param connectionName a name for the connection.
     * @param connectionType a constant value from the [ConnectionConstants]
     */
    private fun createConnection(connectionName: String, connectionType: String) {
        val connectionFactory = ConfigConnectionFactory.getInstance()
        var connectionConfiguration = connectionFactory.getConnectionConfiguration(connectionName)
        if (connectionConfiguration == null) {
            connectionConfiguration = if (ConnectionConstants.CONN_TYPE_FTP == connectionType) {
                FTPConfiguration(connectionName)
            } else if (ConnectionConstants.CONN_TYPE_FTPS == connectionType) {
                FTPSConfiguration(connectionName)
            } else if (ConnectionConstants.CONN_TYPE_SFTP == connectionType) {
                SFTPConfiguration(connectionName)
            } else {
                SCPConfiguration(connectionName)
            }
            uploadConfigurationFromPersistance(connectionConfiguration)
            connectionFactory.addConnectionConfiguration(connectionName, connectionConfiguration)
        }
    }


    /**
     * Gets option stored in the configuration panel and stores them in the specified connection configuration instance.
     *
     * @param connectionConfiguration the actual implementation of the `ConnectionConfiguration`.
     */
    private fun downloadConfigurationToPersistence(connectionConfiguration: ConnectionConfiguration?) {
        if (connectionConfiguration != null) {
            connectionConfiguration.host = getHost()
            connectionConfiguration.projectBasePath = getRootPath()
            connectionConfiguration.port = getPort()
            connectionConfiguration.userName = getUserName()
            connectionConfiguration.userPassword = getUserPassword()
            connectionConfiguration.setExcludedFiles(getExludedFiles())
            connectionConfiguration.isPreserveTime = isTimestampPreserved()
            connectionConfiguration.simultaneousJobs = getSimultaneousJobs()
            if (ConnectionConstants.CONN_TYPE_FTPS == connectionConfiguration.connectionType) {
                val value: Boolean = isImplicit()
                (connectionConfiguration as FTPSConfiguration).isRequireImplicitTLS = value
                connectionConfiguration.isRequireExplicitTLS = !value
                connectionConfiguration.setPreserveTime(false)
            } else if (ConnectionConstants.CONN_TYPE_FTP == connectionConfiguration.connectionType) {
                connectionConfiguration.isPreserveTime = false
            } else if (ConnectionConstants.CONN_TYPE_SFTP == connectionConfiguration.connectionType || ConnectionConstants.CONN_TYPE_SCP == connectionConfiguration.connectionType) {
                (connectionConfiguration as PasswordlessSSH).isPasswordlessSSHSelected = shouldUsePasswordlessSSH()
                (connectionConfiguration as PasswordlessSSH).certificatePath = getSSHCertificatePath()
                (connectionConfiguration as PasswordlessSSH).isPasswordlessWithPassphrase = shouldUsePasswordlessWithPassphrase()
            }
        }
    }

    /**
     * Stores option in the configuration panel from the specified connection configuration instance.
     *
     * @param connectionConfiguration the actual implementation if the `ConnectionConfiguration`.
     */
    private fun uploadConfigurationFromPersistance(connectionConfiguration: ConnectionConfiguration?) {
        if (connectionConfiguration != null) {
            setConnectionType(connectionConfiguration.connectionType)
            setHost(connectionConfiguration.host)
            setProjectBasePath(connectionConfiguration.projectBasePath)
            setPort(connectionConfiguration.port)
            setUserName(connectionConfiguration.userName)
            setUserPassword(connectionConfiguration.userPassword)
            setExcludedFiles(connectionConfiguration.getExcludedFiles())
            setSimultaneousJobs(connectionConfiguration.simultaneousJobs)
            setPreserveTimestamp(connectionConfiguration.isPreserveTime)
            setSimultaneousJobs(connectionConfiguration.simultaneousJobs)
            setConnectionMethodVisible(false)
            setPreserveTimestampVisible(true)
            setSSHKeysVisible(false)
            if (ConnectionConstants.CONN_TYPE_FTPS == connectionConfiguration.connectionType) {
                val value = (connectionConfiguration as FTPSConfiguration).isRequireImplicitTLS
                setImplicit(value)
                setExplicit(!value)
                setConnectionMethodVisible(true)
                setPreserveTimestampVisible(false)
            } else if (ConnectionConstants.CONN_TYPE_FTP == connectionConfiguration.connectionType) {
                setPreserveTimestampVisible(false)
            } else if (ConnectionConstants.CONN_TYPE_SFTP == connectionConfiguration.connectionType || ConnectionConstants.CONN_TYPE_SCP == connectionConfiguration.connectionType) {
                val shouldUseSSHKeys = (connectionConfiguration as PasswordlessSSH).isPasswordlessSSHSelected
                val shouldUseSSHKeysWithPassphrase = (connectionConfiguration as PasswordlessSSH).isPasswordlessWithPassphrase
                val certFile = (connectionConfiguration as PasswordlessSSH).certificatePath
                setSSHKeysVisible(true)
                usePasswordlessSSH(shouldUseSSHKeys)
                usePassphraseForPasswordlessSSH(shouldUseSSHKeysWithPassphrase)
                setSSHCertificatePath(certFile)
            }
        }
    }
}