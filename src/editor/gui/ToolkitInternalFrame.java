package editor.gui;

import editor.model.ModelManager;
import editor.model.TileType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class ToolkitInternalFrame extends JInternalFrame {
    private JPanel detailsPanel;

    public ToolkitInternalFrame() {
        super("Toolkit", true, true, false, true);
        setFrameIcon(null);
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Bum-Burum!"));
        ButtonGroup buttonGroup = new ButtonGroup();
        ArrayList<TileType> basicTileTypes = ModelManager.getInstance().getBasicTileTypes();
        for (TileType t : basicTileTypes) {
            panel.add(getRegisteredRadioComponent(buttonGroup, t, true));
        }
        Box b = Box.createVerticalBox();
        panel.setBackground(Color.GREEN);
//        panel.setMaximumSize(panel.getPreferredSize());
        b.add(panel);
//        panel.get
//        b.add(Box.createVerticalGlue());

        detailsPanel = new JPanel();
        b.add(detailsPanel);
        add(b);


        rebuildDetailsPanel(null);
    }

    private void rebuildDetailsPanel(TileType tileType) {
        detailsPanel.removeAll();
        if (tileType != null) {
            ButtonGroup buttonGroup = new ButtonGroup();
            ArrayList<TileType> relatedTileTypes = ModelManager.getInstance().getRelatedTileTypes(tileType);
            for (TileType t : relatedTileTypes) {
                detailsPanel.add(getRegisteredRadioComponent(buttonGroup, t, false));
            }
        }

        detailsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLUE), "Details"));
        detailsPanel.updateUI();
//        detailsPanel.add(new Button("fsf"));
    }

    private JComponent getRegisteredRadioComponent(ButtonGroup buttonGroup, TileType t, final boolean flag) {
        final TileType tileType = t;
        Box box = Box.createHorizontalBox();
        final JRadioButton radioButton = new JRadioButton("", false);
        box.add(radioButton);
        ImageIcon imageIcon = new ImageIcon(ModelManager.IMAGES_DIR + tileType.getTexture());
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(imageIcon.getIconWidth(), imageIcon.getIconHeight()));
        button.setIcon(imageIcon);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                radioButton.setSelected(true);
                radioButton.doClick();
            }
        });
        box.add(button);
        buttonGroup.add(radioButton);
        radioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ModelManager.getInstance().setCurrentSelectedMaterialID(tileType.getId());
                if (flag)
                    rebuildDetailsPanel(tileType);
            }
        });
        return box;
    }
}
