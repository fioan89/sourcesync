package org.wavescale.sourcesync.ui;

import javax.swing.*;

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

    public JPanel getConfigPanel() {
        return this.panel1;
    }
}
