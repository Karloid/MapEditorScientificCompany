package editor;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class WorkingAreaPanel extends JPanel implements MouseMotionListener, MouseWheelListener, MouseListener {
    private Graphics2D graphics2D;
    private int startXOfWorkingArea;
    private int startYOfWorkingArea;
    private int selectedX;
    private int selectedY;
    private int necessaryWidth;
    private int necessaryHeight;
    private Image[] images;

    public WorkingAreaPanel() {
        images = getImages();
        computeNecessarySize();
        setNecessarySizeForComponent();
        addListeners();
    }

    private void addListeners() {
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        addMouseListener(this);
    }

    private void setNecessarySizeForComponent() {
        Dimension necessaryDimension = new Dimension(necessaryWidth, necessaryHeight);
        setSize(necessaryDimension);
        setPreferredSize(necessaryDimension);
        setMaximumSize(necessaryDimension);
    }

    private void computeNecessarySize() {
        ModelManager modelManager = ModelManager.getInstance();
        necessaryWidth = modelManager.getTileSizeInPixels() * ModelManager.MAP_WIDTH_IN_TILES;
        necessaryHeight = modelManager.getTileSizeInPixels() * ModelManager.MAP_HEIGHT_IN_TILES;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        graphics2D = (Graphics2D)g;
        drawBackground();
        drawWorkingArea();
    }

    private void drawWorkingArea() {
        ModelManager modelManager = ModelManager.getInstance();
        final int tileSizeInPixels = modelManager.getTileSizeInPixels();
        startXOfWorkingArea = (getWidth() - necessaryWidth) / 2;
        startYOfWorkingArea = (getHeight() - necessaryHeight) / 2;
        graphics2D.setColor(new Color(12, 255, 0));
        for (int i = 0; i < ModelManager.MAP_WIDTH_IN_TILES; i++) {
            for (int j = 0; j < ModelManager.MAP_HEIGHT_IN_TILES; j++) {
                final int X_TO_DRAW = startXOfWorkingArea + i * tileSizeInPixels;
                final int Y_TO_DRAW = startYOfWorkingArea + j * tileSizeInPixels;
                graphics2D.translate(X_TO_DRAW, Y_TO_DRAW);
                final Image IMAGE_TO_DRAW = images[modelManager.getTileAt(i, j) - 1];
                final double X_SIZE_MULTIPLIER = (tileSizeInPixels + 0.0)/ IMAGE_TO_DRAW.getWidth(this);
                final double Y_SIZE_MULTIPLIER = (tileSizeInPixels + 0.0)/ IMAGE_TO_DRAW.getHeight(this);
                graphics2D.scale(X_SIZE_MULTIPLIER, Y_SIZE_MULTIPLIER);
                graphics2D.drawImage(images[modelManager.getTileAt(i, j) - 1], null, null);
                graphics2D.scale(1 / X_SIZE_MULTIPLIER, 1 / Y_SIZE_MULTIPLIER);
           //     graphics2D.drawRect(0, 0, tileSizeInPixels, tileSizeInPixels);
                graphics2D.translate(-X_TO_DRAW, -Y_TO_DRAW);
            }
        }

     //   drawSelectedTileBorder();
    }

    private void drawSelectedTileBorder() {
        final int tileSizeInPixels = ModelManager.getInstance().getTileSizeInPixels();
        if (selectedX >= 0 && selectedY >= 0) {
            graphics2D.setColor(Color.BLACK);
            graphics2D.drawRect(startXOfWorkingArea + selectedX * tileSizeInPixels + 1,
                    startYOfWorkingArea + selectedY * tileSizeInPixels + 1,
                    tileSizeInPixels - 2,
                    tileSizeInPixels - 2);
        }
    }

    private Image[] getImages() {
        ArrayList<TileType> tyleTypes = ModelManager.getInstance().getTileTypes();
        Image[] images = new Image[tyleTypes.size()];
        try {
            int i = 0;
            for (TileType t : tyleTypes)
                images[i++] = ImageIO.read(new File(ModelManager.IMAGES_DIR + t.getTexture()));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return images;
    }

    private void drawBackground() {
        graphics2D.setColor(Color.DARK_GRAY);
        graphics2D.fillRect(0, 0, getWidth(), getHeight());
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if ((e.getModifiers() & 16) == 16) {
            ModelManager modelManager = ModelManager.getInstance();
            final int tileSizeInPixels = modelManager.getTileSizeInPixels();
            final int MOUSE_X = e.getX();
            final int MOUSE_Y = e.getY();
            if (coordinateBounds(MOUSE_X, MOUSE_Y)) {
                selectedX = (MOUSE_X - startXOfWorkingArea - ((MOUSE_X - startXOfWorkingArea) % tileSizeInPixels)) / tileSizeInPixels;
                selectedY = (MOUSE_Y - startYOfWorkingArea - ((MOUSE_Y - startYOfWorkingArea) % tileSizeInPixels)) / tileSizeInPixels;
                modelManager.updateTileAt(selectedX, selectedY);
            }
            else {
                selectedX = -1;
                selectedY = -1;
            }
            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        final int tileSizeInPixels = ModelManager.getInstance().getTileSizeInPixels();
        final int MOUSE_X = e.getX();
        final int MOUSE_Y = e.getY();
        if (coordinateBounds(MOUSE_X, MOUSE_Y)) {
            selectedX = (MOUSE_X - startXOfWorkingArea - ((MOUSE_X - startXOfWorkingArea) % tileSizeInPixels)) / tileSizeInPixels;
            selectedY = (MOUSE_Y - startYOfWorkingArea - ((MOUSE_Y - startYOfWorkingArea) % tileSizeInPixels)) / tileSizeInPixels;
        }
        else {
            selectedX = -1;
            selectedY = -1;
        }
        repaint();
    }

    private boolean coordinateBounds(int x, int y) {
        return x > startXOfWorkingArea && x < startXOfWorkingArea + necessaryWidth
                && y > startYOfWorkingArea && y < startYOfWorkingArea + necessaryHeight;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        ModelManager modelManager = ModelManager.getInstance();
        if (modelManager.increaseTileSizeInPixels(e.getWheelRotation())) {
            computeNecessarySize();
            setNecessarySizeForComponent();
            repaint();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        ModelManager modelManager = ModelManager.getInstance();
        final int tileSizeInPixels = modelManager.getTileSizeInPixels();
        final int MOUSE_X = e.getX();
        final int MOUSE_Y = e.getY();
        if (coordinateBounds(MOUSE_X, MOUSE_Y)) {
            selectedX = (MOUSE_X - startXOfWorkingArea - ((MOUSE_X - startXOfWorkingArea) % tileSizeInPixels)) / tileSizeInPixels;
            selectedY = (MOUSE_Y - startYOfWorkingArea - ((MOUSE_Y - startYOfWorkingArea) % tileSizeInPixels)) / tileSizeInPixels;
            modelManager.updateTileAt(selectedX, selectedY);
        }
        else {
            selectedX = -1;
            selectedY = -1;
        }
        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}