package editor.gui;

import editor.model.ModelManager;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by NOTADMIN on 03.07.2014.
 */
public class MinimapDialog extends Dialog implements Observer {
    private WorkingAreaPanel componentToWatch;
    private MinimapPanel minimapPanel;
    private static final int PIXELS_PER_TILE_SIDE = 8;
    private Dimension mapSize;

    public MinimapDialog(Frame owner, WorkingAreaPanel component) {
        super(owner, "Minimap", false);
        componentToWatch = component;
        mapSize = ModelManager.getInstance().getMapSize();
        minimapPanel = new MinimapPanel();
        add(minimapPanel);
        updateSize();
    }

    private void updateSize() {
        int panelWidth = ModelManager.getInstance().getMapWidth() * PIXELS_PER_TILE_SIDE;
        int panelHeight = ModelManager.getInstance().getMapHeight() * PIXELS_PER_TILE_SIDE;
        Dimension newDimension = new Dimension(panelWidth, panelHeight);
        minimapPanel.setSize(newDimension);
        minimapPanel.setPreferredSize(newDimension);
        pack();
    }

    @Override
    public void update(Observable o, Object arg) {
        if (!mapSize.equals(ModelManager.getInstance().getMapSize())) {
            updateSize();
            mapSize = ModelManager.getInstance().getMapSize();
        }
        minimapPanel.repaint();
    }

    private class MinimapPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Image mapImage = componentToWatch.getMapImage();
            if (mapImage != null) {
                int minimapWidth = MinimapDialog.this.minimapPanel.getWidth();
                int minimapHeight = MinimapDialog.this.minimapPanel.getHeight();
//               g.drawImage(mapImage.getScaledInstance(minimapWidth, minimapHeight, Image.SCALE_SMOOTH), 0, 0, null);
                g.drawImage(mapImage, 0,0,minimapWidth, minimapHeight, null);
                Dimension extentSize = componentToWatch.getExtentSize();
                Dimension viewSize = componentToWatch.getViewSize();
                if (!extentSize.equals(viewSize)) {
                    double xScale = (minimapWidth + 0.0) / viewSize.getWidth();
                    double yScale = (minimapHeight + 0.0) / viewSize.getHeight();
                    Point viewPosition = componentToWatch.getViewPosition();
                    ((Graphics2D)g).draw(new Rectangle2D.Double(viewPosition.getX() * xScale, viewPosition.getY() * yScale, extentSize.getWidth() * xScale, extentSize.getHeight() * yScale));
                }
                mapImage.flush();
            }
        }
    }
}
