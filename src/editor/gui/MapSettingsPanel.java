package editor.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MapSettingsPanel extends JPanel {
    private boolean isOk;

    private JTextField mapWidthTextField;
    private JTextField mapHeightTextField;
    private JButton okButton;
    private JDialog dialog;

    private final static int LEFT_BORDER_INSET = 7;
    private final static int RIGHT_BORDER_INSET = 7;
    private final static int TOP_BORDER_INSET = 5;
    private final static int BOTTOM_BORDER_INSET = 5;
    private final static int HORIZONTAL_COMPONENT_INSET = 3;
    private final static int VERTICAL_COMPONENT_INSET = 2;

    public MapSettingsPanel(int oldMapWidth, int oldMapHeight) {
        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;

        gbc.insets = new Insets(TOP_BORDER_INSET, LEFT_BORDER_INSET, VERTICAL_COMPONENT_INSET, HORIZONTAL_COMPONENT_INSET);
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Map width:"), gbc);
        gbc.insets = new Insets(TOP_BORDER_INSET, HORIZONTAL_COMPONENT_INSET, VERTICAL_COMPONENT_INSET, RIGHT_BORDER_INSET);
        gbc.gridx++;
        panel.add(mapWidthTextField = new JTextField(String.valueOf(oldMapWidth), 5), gbc);
        gbc.insets = new Insets(VERTICAL_COMPONENT_INSET, LEFT_BORDER_INSET, VERTICAL_COMPONENT_INSET, HORIZONTAL_COMPONENT_INSET);
        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Map height:"), gbc);
        gbc.insets = new Insets(VERTICAL_COMPONENT_INSET, HORIZONTAL_COMPONENT_INSET, VERTICAL_COMPONENT_INSET, RIGHT_BORDER_INSET);
        gbc.gridx++;
        panel.add(mapHeightTextField = new JTextField(String.valueOf(oldMapHeight),5), gbc);
        gbc.insets = new Insets(VERTICAL_COMPONENT_INSET, LEFT_BORDER_INSET, TOP_BORDER_INSET, HORIZONTAL_COMPONENT_INSET);
        gbc.gridx = 0;
        gbc.gridy++;

        okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isOk = true;
                dialog.setVisible(false);
            }
        });
        panel.add(okButton, gbc);


        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(false);
            }
        });
        gbc.insets = new Insets(VERTICAL_COMPONENT_INSET, HORIZONTAL_COMPONENT_INSET, BOTTOM_BORDER_INSET, RIGHT_BORDER_INSET);
        gbc.gridx++;
        panel.add(cancelButton, gbc);
        add(panel, BorderLayout.CENTER);
    }

    public boolean showDialog(Component parent, String title) {
        isOk = false;

        Frame owner;
        if (parent instanceof Frame)
            owner = (Frame)parent;
        else
            owner = (Frame)SwingUtilities.getAncestorOfClass(Frame.class, parent);

        if (dialog == null || dialog.getOwner() != owner) {
            dialog = new JDialog(owner, true);
            dialog.add(this);
            dialog.getRootPane().setDefaultButton(okButton);
            dialog.pack();
            dialog.setResizable(false);
        }

        dialog.setLocation(owner.getX() + (owner.getWidth() - dialog.getWidth()) / 2, owner.getY() + (owner.getHeight() - dialog.getHeight()) / 2);
        dialog.setTitle(title);
        dialog.setVisible(true);
        return isOk;
    }

    public int getMapWidth() {
        return Integer.parseInt(mapWidthTextField.getText());
    }

    public int getMapHeight() {
        return Integer.parseInt(mapHeightTextField.getText());
    }
}