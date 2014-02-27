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
    private int xOffset, yOffset;
    private int selectedX, selectedY;
    private int necessaryWidth, necessaryHeight;
    private Image[] images;
    private MacroCommand lastMacroCommand;
//    private Toolbox toolbox;

    public WorkingAreaPanel() {
        selectedX = selectedY = -1;
        ModelManager.getInstance().registerObserver(this);
        images = getImages();

        computeNecessarySize();
        setNecessarySizeForComponent();
        addListeners();

        setLayout(new BorderLayout());
//        toolbox = new Toolbox();
//        add(toolbox, BorderLayout.WEST);
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
        Graphics2D graphics2D = (Graphics2D)g;
        drawBackground(graphics2D);
        drawWorkingArea(graphics2D);
//        toolbox.repaint();
    }

    private void drawWorkingArea(Graphics2D graphics2D) {
        ModelManager modelManager = ModelManager.getInstance();
        final int tileSizeInPixels = modelManager.getTileSizeInPixels();
        xOffset = (getWidth() - necessaryWidth) / 2;
        yOffset = (getHeight() - necessaryHeight) / 2;
        graphics2D.setColor(new Color(12, 255, 0));
        for (int i = 0; i < modelManager.getMapWidth(); i++) {
            for (int j = 0; j < modelManager.getMapHeight(); j++) {
                final int X_TO_DRAW = xOffset + i * tileSizeInPixels;
                final int Y_TO_DRAW = yOffset + j * tileSizeInPixels;
                graphics2D.translate(X_TO_DRAW, Y_TO_DRAW);
                final Image IMAGE_TO_DRAW = images[modelManager.getTileAt(i, j) - 1];
                final double X_SIZE_MULTIPLIER = (tileSizeInPixels + 0.0) / IMAGE_TO_DRAW.getWidth(this);
                final double Y_SIZE_MULTIPLIER = (tileSizeInPixels + 0.0) / IMAGE_TO_DRAW.getHeight(this);
                graphics2D.scale(X_SIZE_MULTIPLIER, Y_SIZE_MULTIPLIER);
                graphics2D.drawImage(images[modelManager.getTileAt(i, j) - 1], null, null);
                graphics2D.scale(1.0 / X_SIZE_MULTIPLIER, 1.0 / Y_SIZE_MULTIPLIER);
                //     graphics2D.drawRect(0, 0, tileSizeInPixels, tileSizeInPixels);
                graphics2D.translate(-X_TO_DRAW, -Y_TO_DRAW);
            }
        }

        drawSelectedTileBorder(graphics2D);
    }

    private void drawSelectedTileBorder(Graphics2D graphics2D) {
        final int tileSizeInPixels = ModelManager.getInstance().getTileSizeInPixels();
        if (selectedX >= 0 && selectedY >= 0) {
            graphics2D.setColor(Color.BLACK);
            graphics2D.drawRect(xOffset + selectedX * tileSizeInPixels + 1,
                    yOffset + selectedY * tileSizeInPixels + 1,
                    tileSizeInPixels - 2,
                    tileSizeInPixels - 2);
        }
    }

    private Image[] getImages() {
        ArrayList<TileType> tileTypes = ModelManager.getInstance().getAllTileTypes();
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

    private void drawBackground(Graphics2D graphics2D) {
        graphics2D.setColor(Color.DARK_GRAY);
        graphics2D.fillRect(0, 0, getWidth(), getHeight());
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        handleMouseMoveAt(e.getX(), e.getY());
        if (isLeftMousePressed(e.getModifiers()))
            handleLeftMousePress();
    }

    private void handleLeftMousePress() {
        ModelManager modelManager = ModelManager.getInstance();
        if (selectedX != -1 && selectedY != -1 && modelManager.getTileAt(selectedX, selectedY) != modelManager.getCurrentSelectedMaterialID())
            lastMacroCommand.addCommand(new UpdateTileAtCommand(selectedX, selectedY));
    }

    private boolean isLeftMousePressed(int modifiers) {
        return (modifiers & 16) == 16;
    }

    private void handleMouseMoveAt(int xCoord, int yCoord) {
        final int tileSizeInPixels = ModelManager.getInstance().getTileSizeInPixels();
        final int workingAreaX = xCoord - xOffset;
        final int workingAreaY = yCoord - yOffset;
        final int oldSelectedX = selectedX;
        final int oldSelectedY = selectedY;
        if (coordinateBounds(xCoord, yCoord)) {
            selectedX = (workingAreaX - (workingAreaX % tileSizeInPixels)) / tileSizeInPixels;
            selectedY = (workingAreaY - (workingAreaY % tileSizeInPixels)) / tileSizeInPixels;
            if (oldSelectedX != selectedX || oldSelectedY != selectedY) {
                repaintTile(selectedX, selectedY);
                repaintTile(oldSelectedX, oldSelectedY);
            }
        }
        else {
            selectedX = -1;
            selectedY = -1;
            if (oldSelectedX != selectedX || oldSelectedY != selectedY)
                repaintTile(oldSelectedX, oldSelectedY);
        }
    }

    private void repaintTile(int x, int y) {
        final int tileSizeInPixels = ModelManager.getInstance().getTileSizeInPixels();
        repaint(xOffset + x * tileSizeInPixels, yOffset + y * tileSizeInPixels, tileSizeInPixels, tileSizeInPixels);
    }

    private boolean coordinateBounds(int x, int y) {
        return x > xOffset && x < xOffset + necessaryWidth
                && y > yOffset && y < yOffset + necessaryHeight;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        handleMouseMoveAt(e.getX(), e.getY());
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        ModelManager modelManager = ModelManager.getInstance();
        if (modelManager.increaseTileSizeInPixels(e.getWheelRotation()))
            doUpdate();
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        lastMacroCommand = new MacroCommand();
        ModelManager.getInstance().performCommand(lastMacroCommand);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (lastMacroCommand.isEmpty())
            handleLeftMousePress();
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
        if (arg != null) {
            int [] args = (int[])arg;
            doUpdate(args[0], args[1]);
        }
        else
            doUpdate();
    }

    private void doUpdate() {
        resizePanel();
        repaint();
    }

    private void resizePanel() {
        computeNecessarySize();
        setNecessarySizeForComponent();
    }

    private void doUpdate(int x, int y) {
//        resizePanel();
        repaintTile(x, y);
    }
}