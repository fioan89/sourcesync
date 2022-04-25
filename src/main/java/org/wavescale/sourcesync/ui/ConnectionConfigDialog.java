package org.wavescale.sourcesync.ui;

import org.wavescale.sourcesync.factory.ConnectionConfig;

import javax.swing.*;
import java.awt.event.*;

public class ConnectionConfigDialog extends CenterDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox cbModuleConnection;
    private String projectName;

    @SuppressWarnings("unchecked")
    public ConnectionConfigDialog(String projectName, String[] connectionConfigNames) {
        this.setTitle("Choose connection for " + projectName);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        this.projectName = projectName;

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        for (String item : connectionConfigNames) {
            this.cbModuleConnection.addItem(item);
        }
    }

    public void openDialog() {
        pack();
        centerOnParent();
        setVisible(true);
    }

    private void onOK() {
        String connectionConfig = (String) cbModuleConnection.getSelectedItem();
        ConnectionConfig.getInstance().associateProjectWithConnection(projectName, connectionConfig);
        ConnectionConfig.getInstance().saveModuleAssociatedConn();

        dispose();

    }

    private void onCancel() {
// add your code here if necessary
        dispose();
    }
}
