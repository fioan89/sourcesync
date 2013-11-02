package org.wavescale.sourcesync.config;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import static javax.swing.ListSelectionModel.*;

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
    // connection name to the connection type
    private Map<String, String> connectionType;

    public SourceSyncConfig() {
        JFrame frame = new JFrame("SourceSyncConfig");
        frame.setLocationRelativeTo(null);
        lstTargets.setModel(new DefaultListModel());
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
            }
        });

        lstTargets.getSelectionModel().addListSelectionListener(new TargetListListener());

        frame.pack();
        frame.setVisible(true);
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
