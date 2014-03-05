package editor.model;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TileTypeComponent extends Box {
    private ActionListener actionListener;
    private TileType tileType;
    private JButton button;
    private boolean isSelected;

    public static TileTypeComponent createTileTypeComponentWithRadioButton(TileType _tileType, final JRadioButton _radioButton) {
        final TileTypeComponent component = new TileTypeComponent(_tileType);

        component.add(_radioButton);
        component.add(component.button);

        component.button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _radioButton.doClick();
            }
        });
        component.actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                component.isSelected = _radioButton.isSelected();
            }
        };

        return component;
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

    private TileTypeComponent(TileType _tileType) {
        super(BoxLayout.X_AXIS);
        tileType = _tileType;
        button = new JButton();
        ImageIcon icon = new ImageIcon(ModelManager.IMAGES_DIR + tileType.getTexture());
        button.setIcon(icon);
        button.setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
        button.setToolTipText(tileType.tooltipText());
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
    }

    public TileType getTileType() {
        return tileType;
    }
}
