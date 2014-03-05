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

    public WorkingAreaPanel() {
        selectedX = selectedY = -1;
        ModelManager.getInstance().registerObserver(this);
        images = getImages();

        computeNecessarySize();
        setNecessarySizeForComponent();
        addListeners();

        setLayout(new BorderLayout());
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
    }

    private void drawWorkingArea(Graphics2D graphics2D) {
        ModelManager modelManager = ModelManager.getInstance();
        final int tileSizeInPixels = modelManager.getTileSizeInPixels();
        xOffset = (getWidth() - necessaryWidth) / 2;
        yOffset = (getHeight() - necessaryHeight) / 2;
        for (int i = 0; i < modelManager.getMapWidth(); i++) {
            for (int j = 0; j < modelManager.getMapHeight(); j++) {
                final int xToDraw = xOffset + i * tileSizeInPixels;
                final int yToDraw = yOffset + j * tileSizeInPixels;
                graphics2D.translate(xToDraw, yToDraw);
                final Image IMAGE_TO_DRAW = images[modelManager.getTileAt(i, j) - 1];
                final double xSizeMultiplier = (tileSizeInPixels + 0.0) / IMAGE_TO_DRAW.getWidth(this);
                final double ySizeMultiplier = (tileSizeInPixels + 0.0) / IMAGE_TO_DRAW.getHeight(this);
                graphics2D.scale(xSizeMultiplier, ySizeMultiplier);
                graphics2D.drawImage(images[modelManager.getTileAt(i, j) - 1], null, null);
                graphics2D.scale(1.0 / xSizeMultiplier, 1.0 / ySizeMultiplier);
                graphics2D.translate(-xToDraw, -yToDraw);
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
        final int modifiersEx = e.getModifiersEx();
        if ((modifiersEx & MouseEvent.ALT_DOWN_MASK) == 0)
            handleMouseDown(modifiersEx);
    }

    private void handleMouseDown(int modifiersEx) {
        ModelManager modelMgr = ModelManager.getInstance();
        int materialID;
        if ((modifiersEx & MouseEvent.BUTTON1_DOWN_MASK) != 0)
            materialID = modelMgr.getPrimaryMaterialID();
        else if ((modifiersEx & MouseEvent.BUTTON3_DOWN_MASK) != 0)
            materialID = modelMgr.getSecondaryMaterialID();
        else
            return;

        if (selectedX != -1 && selectedY != -1 && modelMgr.getTileAt(selectedX, selectedY) != materialID)
            lastMacroCommand.addCommand(new UpdateTileAtCommand(selectedX, selectedY, materialID));
    }

    private int getTileXByAbsolutePixelX(int absoluteXPixel, int tileSizeInPixels) {
        final int workingAreaX = absoluteXPixel - xOffset;
        return (workingAreaX - (workingAreaX % tileSizeInPixels)) / tileSizeInPixels;
    }

    private int getTileYByAbsolutePixelY(int absoluteYPixel, int tileSizeInPixels) {
        final int workingAreaY = absoluteYPixel - yOffset;
        return (workingAreaY - (workingAreaY % tileSizeInPixels)) / tileSizeInPixels;
    }

    private void handleMouseMoveAt(int xCoord, int yCoord) {
        final int tileSizeInPixels = ModelManager.getInstance().getTileSizeInPixels();
        final int oldSelectedX = selectedX;
        final int oldSelectedY = selectedY;
        if (coordinateBounds(xCoord, yCoord)) {
            selectedX = getTileXByAbsolutePixelX(xCoord, tileSizeInPixels);
            selectedY = getTileYByAbsolutePixelY(yCoord, tileSizeInPixels);
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

    private void handlePipetteTool(MouseEvent e) {
        if (!coordinateBounds(e.getX(), e.getY()))
            return;
        ModelManager modelManager = ModelManager.getInstance();
        final int tileSizeInPixels = modelManager.getTileSizeInPixels();
        final int tileX = getTileXByAbsolutePixelX(e.getX(), tileSizeInPixels);
        final int tileY = getTileYByAbsolutePixelY(e.getY(), tileSizeInPixels);
        final int materialToAssign = modelManager.getTileAt(tileX, tileY);
        final int button = e.getButton();
        if (button == MouseEvent.BUTTON1)
            modelManager.setPrimaryMaterialID(materialToAssign);
        else if (button == MouseEvent.BUTTON3)
            modelManager.setSecondaryMaterialID(materialToAssign);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if ((e.getModifiersEx() & MouseEvent.ALT_DOWN_MASK) != 0) {
            handlePipetteTool(e);
            return;
        }
        lastMacroCommand = new MacroCommand();
        handleMouseDown(e.getModifiersEx());
        ModelManager.getInstance().performCommand(lastMacroCommand);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if ((e.getModifiersEx() & MouseEvent.ALT_DOWN_MASK) != 0)
            return;
        if (lastMacroCommand.isEmpty())
            handleMouseDown(e.getModifiersEx());
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
            if (arg instanceof ModelManager.ModelManagerTileUpdateInfo) {
                ModelManager.ModelManagerTileUpdateInfo args = (ModelManager.ModelManagerTileUpdateInfo)arg;
                doUpdate(args.getTileX(), args.getTileY());
            }
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