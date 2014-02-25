package editor.gui;

import editor.model.MacroCommand;
import editor.model.ModelManager;
import editor.model.TileType;
import editor.model.UpdateTileAtCommand;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class WorkingAreaPanel extends JPanel implements MouseMotionListener, MouseWheelListener, MouseListener, Observer {
    private Graphics2D graphics2D;
    private int startXOfWorkingArea;
    private int startYOfWorkingArea;
    private int selectedX;
    private int selectedY;
    private int necessaryWidth;
    private int necessaryHeight;
    private Image[] images;
    private MacroCommand lastMacroCommand;

    public WorkingAreaPanel() {
        ModelManager.getInstance().registerObserver(this);
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
        necessaryWidth = modelManager.getTileSizeInPixels() * modelManager.getMapWidth();
        necessaryHeight = modelManager.getTileSizeInPixels() * modelManager.getMapHeight();
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
        for (int i = 0; i < modelManager.getMapWidth(); i++) {
            for (int j = 0; j < modelManager.getMapHeight(); j++) {
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

        drawSelectedTileBorder();
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
        ArrayList<TileType> tileTypes = ModelManager.getInstance().getTileTypes();
        Image[] images = new Image[tileTypes.size()];
        try {
            int i = 0;
            for (TileType t : tileTypes)
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
                lastMacroCommand.addCommand(new UpdateTileAtCommand(selectedX, selectedY));
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
        if (modelManager.increaseTileSizeInPixels(e.getWheelRotation()))
            doUpdate();
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
            lastMacroCommand.addCommand(new UpdateTileAtCommand(selectedX, selectedY));
            modelManager.performCommand(lastMacroCommand);
        }
        else {
            selectedX = -1;
            selectedY = -1;
        }
        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        lastMacroCommand = new MacroCommand();
        ModelManager.getInstance().performCommand(lastMacroCommand);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        lastMacroCommand.perform();

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void update(Observable o, Object arg) {
        doUpdate();
    }

    private void doUpdate() {
        computeNecessarySize();
        setNecessarySizeForComponent();
        repaint();
    }
}