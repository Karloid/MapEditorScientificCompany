package editor.gui;

import editor.model.ModelManager;
import editor.model.TileType;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class TileTypeComponent extends Box {
    private ActionListener actionListener;
    private TileType tileType;
    private JButton button;
    private boolean isSelected;

    private TileTypeComponent(TileType _tileType) {
        super(BoxLayout.X_AXIS);
        tileType = _tileType;
        button = new JButton();
        ImageIcon icon = null;
        try {
            BufferedImage bi = new BufferedImage(ModelManager.TOOL_IMAGE_ICON_SIZE, ModelManager.TOOL_IMAGE_ICON_SIZE, BufferedImage.TYPE_INT_ARGB);
            bi.getGraphics().drawImage(ImageIO.read(new File(ModelManager.getInstance().getImageDirectoryName() + File.separator + tileType.getTexture())), 0, 0, ModelManager.TOOL_IMAGE_ICON_SIZE, ModelManager.TOOL_IMAGE_ICON_SIZE, null);
            icon = new ImageIcon(bi);

        }
        catch (IOException e) {
            e.printStackTrace();
        }

        button.setIcon(icon);
        button.setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
        button.setToolTipText(tileType.tooltipText());
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
    }

    public static TileTypeComponent createTileTypeComponentWithCheckBox(TileType _tileType, final JCheckBox _checkBox) {
        final TileTypeComponent component = new TileTypeComponent(_tileType);

        component.add(_checkBox);
        component.add(component.button);

        component.button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _checkBox.doClick();
            }
        });
        component.actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                component.isSelected = _checkBox.isSelected();
            }
        };

        return component;
    }

    public boolean isSelected() {
        actionListener.actionPerformed(null);
        return isSelected;
    }

    public TileType getTileType() {
        return tileType;
    }
}
