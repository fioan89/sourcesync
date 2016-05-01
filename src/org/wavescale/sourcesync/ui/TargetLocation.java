package org.wavescale.sourcesync.ui;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;

public class TargetLocation extends CenterDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JTextField targetField;
    private JComboBox typeOption;
    private JLabel errorLabel;

    public TargetLocation(Window parent) {
        super(parent);
        setTitle("Add target");
        setContentPane(contentPane);
        setModal(true);
        setModalityType(ModalityType.APPLICATION_MODAL);
        getRootPane().setDefaultButton(buttonOK);
        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        targetField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                clearLabel();
            }

            @Override
            public void removeUpdate(DocumentEvent documentEvent) {
                clearLabel();
            }

            @Override
            public void changedUpdate(DocumentEvent documentEvent) {
                clearLabel();
            }
        });

        this.setSize(400, 140);
        centerOnParent();
        this.setResizable(false);
        this.setVisible(true);
    }

    private void clearLabel() {
        this.errorLabel.setText("");
    }

    private void onOK() {
        // add your code here
        if (!this.targetField.getText().equals("")) {
            dispose();
            return;
        }
        this.errorLabel.setText("Please choose a name for the target to be synchronized!");
    }

    /**
     * Gets the target name.
     *
     * @return returns a string containing the target name.
     */
    public String getTargetName() {
        return targetField.getText();
    }

    /**
     * Gets the connection type.
     *
     * @return a string that can have the following values: {@link org.wavescale.sourcesync.api.ConnectionConstants#CONN_TYPE_FTP}
     * , {@link org.wavescale.sourcesync.api.ConnectionConstants#CONN_TYPE_FTPS},
     * {@link org.wavescale.sourcesync.api.ConnectionConstants#CONN_TYPE_SFTP}, {@link org.wavescale.sourcesync.api.ConnectionConstants#CONN_TYPE_SCP}
     */
    public String getTargetType() {
        return (String) typeOption.getSelectedItem();
    }
}
