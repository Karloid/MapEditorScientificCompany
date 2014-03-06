package editor.gui;

import editor.model.ModelManager;
import editor.model.TileType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class TileTypeSelectorPanel extends JPanel {
    private final int width;

    public TileTypeSelectorPanel(int _width) {
        width = _width;
        setLayout(new GridBagLayout());
    }

    public void loadTileTypes(List<TileType> tileTypes) {
        removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        int counter = 0;
        for (TileType tileType : tileTypes) {
            if (counter == width) {
                gbc.gridx = 0;
                gbc.gridy++;
                counter = 0;
            }
            add(createTileTypeSelector(tileType), gbc);
            gbc.gridx++;
            counter++;
        }
        if (tileTypes.size() > 0)
            gbc.gridy++;

        gbc.gridx = 0;
        gbc.weighty = 100.0;
        gbc.gridwidth = width;
        add(new JLabel(), gbc);

        updateUI();
    }

    private static JLabel createTileTypeSelector(TileType t) {
        ImageIcon imageIcon = new ImageIcon(ModelManager.IMAGES_DIR + t.getTexture());
        JLabel selector = new JLabel(imageIcon);
        selector.setToolTipText(t.tooltipText());
        selector.setPreferredSize(new Dimension(imageIcon.getIconWidth() + 4, imageIcon.getIconHeight() + 4));
        TileTypeSelectorMouseListener listener = new TileTypeSelectorMouseListener(t.getId(), selector);
        selector.addMouseListener(listener);
        selector.addMouseMotionListener(listener);
        return selector;
    }

    private static class TileTypeSelectorMouseListener implements MouseListener, MouseMotionListener {
        private int tileTypeID;
        private JComponent component;

        public TileTypeSelectorMouseListener(int _tileTypeID, JComponent _component) {
            tileTypeID = _tileTypeID;
            component = _component;
        }

        @Override
        public void mouseClicked(MouseEvent e) {

        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1)
                ModelManager.getInstance().setPrimaryMaterialID(tileTypeID);
            else if (e.getButton() == MouseEvent.BUTTON3)
                ModelManager.getInstance().setSecondaryMaterialID(tileTypeID);
        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {
            component.repaint();
        }

        @Override
        public void mouseDragged(MouseEvent e) {

        }

        @Override
        public void mouseMoved(MouseEvent e) {
            component.getGraphics().drawRect(0, 0, component.getWidth() - 1, component.getHeight() - 1);
        }
    }
}