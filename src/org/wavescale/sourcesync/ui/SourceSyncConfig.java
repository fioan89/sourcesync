package org.wavescale.sourcesync.ui;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.wavescale.sourcesync.api.ConnectionConfiguration;
import org.wavescale.sourcesync.api.Constants;
import org.wavescale.sourcesync.config.FTPConfiguration;
import org.wavescale.sourcesync.config.FTPSConfiguration;
import org.wavescale.sourcesync.config.SFTPConfiguration;
import org.wavescale.sourcesync.factory.ConfigConnectionFactory;
import org.wavescale.sourcesync.factory.ConfigPanelFactory;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

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
public class SourceSyncConfig {
    private JPanel configPanel;
    private JList lstTargets;
    private JButton btnAdd;
    private JButton btnRemove;
    private JToolBar pnTargetName;
    private JPanel pnConfig;
    private JLabel lbTarget;

    private ConnectionConfigPanel connectionPanel;
    // connection name to the connection type
    private Map<String, String> connectionType;
    private JFrame frame;

    public SourceSyncConfig() {
        frame = new JFrame("SourceSyncConfig");
        frame.setLocationRelativeTo(null);
        lstTargets.setModel(new DefaultListModel());

        FormLayout layout = new FormLayout("fill:300px:grow(1)", "pref:grow(1)");
        CellConstraints cc = new CellConstraints();
        pnConfig.setLayout(layout);
        connectionPanel = ConfigPanelFactory.getInstance().getConnectionConfigPanel();
        pnConfig.add(connectionPanel.getConfigPanel(), cc.xy(1, 1));
        pnConfig.setVisible(false);

        frame.setContentPane(configPanel);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        connectionType = new HashMap<String, String>();
        btnAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                TargetLocation targetConfig = new TargetLocation();
                targetConfig.setModal(true);
                String name = targetConfig.getTargetName();
                String type = targetConfig.getTargetType();
                connectionType.put(name, type);
                ((DefaultListModel)lstTargets.getModel()).addElement(name);
                lstTargets.setSelectedIndex(((DefaultListModel) lstTargets.getModel()).lastIndexOf(name));
                updateConnection(name, type);
                pnConfig.setVisible(true);
            }
        });

        lstTargets.getSelectionModel().addListSelectionListener(new TargetListListener());
        frame.setSize(600, 400);
        frame.setVisible(true);
    }

    private void updateConnection(String connectionName, String connectionType) {
        ConfigConnectionFactory connectionFactory = ConfigConnectionFactory.getInstance();
        ConnectionConfiguration connectionConfiguration = connectionFactory.getConnectionConfiguration(connectionName);
        if (connectionConfiguration == null) {
            if (Constants.CONN_TYPE_FTP.equals(connectionType)) {
                connectionConfiguration = new FTPConfiguration();
            } else if (Constants.CONN_TYPE_FTPS.equals(connectionType)) {
                connectionConfiguration = new FTPSConfiguration();
            } else {
                connectionConfiguration = new SFTPConfiguration();
            }
            downloadConfigurationToPersistence(connectionConfiguration);
        }
    }

    /**
     * Gets option stored in the configuration panel and stores them in the specified connection configuration instance.
     * @param connectionConfiguration the actual implementation of the <code>ConnectionConfiguration</code>.
     */
    private void downloadConfigurationToPersistence(ConnectionConfiguration connectionConfiguration) {
        // TODO
    }

    /**
     * Stores option in the configuration panel from the specified connection configuration instance.
     * @param connectionConfiguration the actual implementation if the <code>ConnectionConfiguration</code>.
     */
    private void uploadConfiguration(ConnectionConfiguration connectionConfiguration) {
        // TODO
    }

    class TargetListListener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent listSelectionEvent) {
            DefaultListSelectionModel selectionModel = (DefaultListSelectionModel) listSelectionEvent.getSource();
            int index = selectionModel.getMinSelectionIndex();
            lbTarget.setText((String)lstTargets.getModel().getElementAt(index));
        }
    }
}
