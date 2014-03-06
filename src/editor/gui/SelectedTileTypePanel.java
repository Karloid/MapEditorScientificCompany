package editor.gui;

import editor.model.ModelManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Observable;
import java.util.Observer;

public class SelectedTileTypePanel extends JPanel implements Observer {
    //fields
    private int primaryMaterialID;
    private int secondaryMaterialID;

    //GUI
    private final JLabel primaryTileTypeComponent;
    private final JLabel secondaryTileTypeComponent;
    private final JButton swapButton;

    //static constants
    private final static int WIDTH = 4;

    public SelectedTileTypePanel() {
        final int initialMaterialID = ModelManager.getInstance().getAllTileTypes().get(0).getId();
        primaryMaterialID = secondaryMaterialID = initialMaterialID;
        primaryTileTypeComponent = new JLabel();
        secondaryTileTypeComponent = new JLabel();

        swapButton = new JButton("\u2514");
        swapButton.setBorder(null);
        swapButton.setContentAreaFilled(false);
        swapButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ModelManager.getInstance().swapMaterials();
            }
        });
        swapButton.registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                swapButton.doClick();
            }
        }, KeyStroke.getKeyStroke('x'), JComponent.WHEN_IN_FOCUSED_WINDOW);

        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEtchedBorder());
        rebuildUI();
    }

    private void rebuildUI() {
        removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = 2;
        gbc.gridheight = 2;
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 1, 1);
        ImageIcon secondaryIcon = new ImageIcon(ModelManager.IMAGES_DIR + ModelManager.getInstance().getTextureForTileID(secondaryMaterialID));
        secondaryTileTypeComponent.setIcon(secondaryIcon);
        secondaryTileTypeComponent.setPreferredSize(new Dimension(secondaryIcon.getIconWidth(), secondaryIcon.getIconHeight()));
        add(secondaryTileTypeComponent, gbc);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 0);
        ImageIcon primaryIcon = new ImageIcon(ModelManager.IMAGES_DIR + ModelManager.getInstance().getTextureForTileID(primaryMaterialID));
        primaryTileTypeComponent.setIcon(primaryIcon);
        primaryTileTypeComponent.setPreferredSize(new Dimension(primaryIcon.getIconWidth(), primaryIcon.getIconHeight()));
        add(primaryTileTypeComponent, gbc);

        gbc.gridy = 2;
        add(swapButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weighty = 100.0;
        gbc.gridwidth = WIDTH;
        gbc.gridheight = 1;
        add(new JLabel(), gbc);
        updateUI();
    }

    @Override
    public void update(Observable o, Object arg) {
        ModelManager.ModelManagerUpdateInfo updateInfo = (ModelManager.ModelManagerUpdateInfo) arg;
        if (updateInfo.getUpdateType() == ModelManager.ModelManagerUpdateType.MATERIAL_UPDATE) {
            primaryMaterialID = ModelManager.getInstance().getPrimaryMaterialID();
            secondaryMaterialID = ModelManager.getInstance().getSecondaryMaterialID();
            rebuildUI();
        }
    }
}