package org.wavescale.sourcesync.ui;

import javax.swing.*;
import java.awt.*;

/**
 * ****************************************************************************
 * Copyright (c) 2005-2013 Faur Ioan-Aurel.                                     *
 * All rights reserved. This program and the accompanying materials             *
 * are made available under the terms of the MIT License                        *
 * which accompanies this distribution, and is available at                     *
 * http://opensource.org/licenses/MIT                                           *
 * *
 * For any issues or questions send an email at: fioan89@gmail.com              *
 * *****************************************************************************
 */
public class ConnectionConfigPanel {
    private JPanel panel1;
    private JLabel lbConnType;
    private JTextField tfHost;
    private JTextField tfPort;
    private JTextField tfRootPath;
    private JTextField tfUserName;
    private JPasswordField pfUserPassword;
    private JRadioButton rbImplicit;
    private JRadioButton rbExplicit;
    private JTextField crtImlTextField;

    public ConnectionConfigPanel() {
        // group radio buttons
        ButtonGroup group = new ButtonGroup();
        group.add(rbImplicit);
        group.add(rbExplicit);
        tfPort.setMinimumSize(new Dimension(50, 20));
        tfPort.setMaximumSize(new Dimension(50, 20));
    }

    /**
     * Gets the config panel instance.
     * @return a <code>JPanel</code> instance.
     */
    public JPanel getConfigPanel() {
        return this.panel1;
    }

    /**
     * Sets the connection type label to the given string.
     * @param connectionType a <code>String</code> representing the connection type.
     */
    public void setConnectionType(String connectionType) {
        lbConnType.setText(connectionType);
    }

    /**
     * Returns the port value stored in the port text field.
     * @return an int value representing the target port.
     */
    public int getPort() {
        return Integer.valueOf(tfPort.getText());
    }

    /**
     * Sets the target port.
     * @param port an int value representing the target port
     */
    public void setPort(int port) {
        tfPort.setText(String.valueOf(port));
    }

    /**
     * Returns target hostname.
     * @return a <code>String</code> representing the address of a remote host.
     */
    public String getHost() {
        return tfHost.getText();
    }

    /**
     * Sets target hostname.
     * @param host a <code>String</code> representing the address of a remote host.
     */
    public void setHost(String host) {
        tfHost.setText(host);
    }

    /**
     * Gets the target root path. This is the root where we will sync files.
     * @return a <code>String</code> representing a path on the remote target.
     */
    public String getRootPath() {
        return tfRootPath.getText();
    }

    /**
     * Sets the target root path. This is the root where we will sync files.
     * @param rootPath a <code>String</code> representing a path on the remote target.
     */
    public void setRootPath(String rootPath) {
        tfRootPath.setText(rootPath);
    }


    public String getUserName() {
        return tfUserName.getText();
    }

    public void setUserName(String userName) {
        this.tfUserName.setText(userName);
    }

    public String getUserPassword() {
        return new String(pfUserPassword.getPassword());
    }

    public void setUserPassword(String userPassword) {
        this.pfUserPassword.setText(userPassword);
    }

    public boolean isImplicit() {
        return rbImplicit.isSelected();
    }

    public void setImplicit(boolean implicit) {
        this.rbImplicit.setSelected(implicit);
    }

    public boolean isExplicit() {
        return rbExplicit.isSelected();
    }

    public void setExplicit(boolean explicit) {
        this.rbExplicit.setSelected(explicit);
    }

    public String getExludedFiles() {
        return crtImlTextField.getText();
    }

    public void setExcludedFiles(String excludedFiles) {
        this.crtImlTextField.setText(excludedFiles);
    }
}
