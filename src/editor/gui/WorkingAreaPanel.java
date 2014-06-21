package editor.gui;

import editor.model.CommandHandler;
import editor.model.MacroCommand;
import editor.model.ModelManager;
import editor.model.TileType;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class WorkingAreaPanel extends JPanel implements Observer {
    private int xOffset, yOffset;
    private int selectedX, selectedY;
    private int necessaryWidth, necessaryHeight;
    private Map<Integer, Image> images;
    private MacroCommand lastMacroCommand;
    private boolean isAllowedToRepaint = true;
    private int fromMouseX, fromMouseY;

    private JScrollPane scrollPane;

    public WorkingAreaPanel() {
        selectedX = selectedY = -1;
        ModelManager.getInstance().registerObserver(this);
        images = getLoadedFromFilesImages();

        computeNecessarySize();
        setNecessarySizeForComponent();
        addListeners();

        setLayout(new BorderLayout());
    }

    private void addListeners() {
        WorkingAreaPanelMouseHandler mouseHandler = new WorkingAreaPanelMouseHandler();
        addMouseMotionListener(mouseHandler);
        addMouseWheelListener(mouseHandler);
        addMouseListener(mouseHandler);
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
        if (isAllowedToRepaint) {
            Graphics2D graphics2D = (Graphics2D)g;
            drawBackground(graphics2D);
            drawWorkingArea(graphics2D);
        }
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
                final Image IMAGE_TO_DRAW = images.get(modelManager.getTileAt(i, j));
                final double xSizeMultiplier = (tileSizeInPixels + 0.0) / IMAGE_TO_DRAW.getWidth(this);
                final double ySizeMultiplier = (tileSizeInPixels + 0.0) / IMAGE_TO_DRAW.getHeight(this);
                graphics2D.scale(xSizeMultiplier, ySizeMultiplier);
                graphics2D.drawImage(images.get(modelManager.getTileAt(i, j)), null, null);
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
            graphics2D.drawRect(xOffset + selectedX * tileSizeInPixels,
                    yOffset + selectedY * tileSizeInPixels,
                    tileSizeInPixels - 1,
                    tileSizeInPixels - 1);
        }
    }

    private Map<Integer, Image> getLoadedFromFilesImages() {
        Map<Integer, Image> imagesMap = new HashMap<Integer, Image>();
        try {
            Iterator<TileType> iterator = ModelManager.getInstance().getIteratorOfAllTileTypes();
            while (iterator.hasNext()) {
                TileType t = iterator.next();
                imagesMap.put(t.getId(), ImageIO.read(new File(ModelManager.getInstance().getImageDirectoryName() + File.separator + t.getTexture())));
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return imagesMap;
    }

    private void drawBackground(Graphics2D graphics2D) {
        graphics2D.setColor(Color.DARK_GRAY);
        graphics2D.fillRect(0, 0, getWidth(), getHeight());
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

        if (selectedX != -1 && selectedY != -1 && modelMgr.getTileAt(selectedX, selectedY) != materialID) {
            if (!modelMgr.isSmartModeOn())
                lastMacroCommand.addCommand(modelMgr.new UpdateTileAtCommand(selectedX, selectedY, materialID));
            else
                lastMacroCommand.addCommand(modelMgr.new SmartUpdateTileAtCommand(selectedX, selectedY, materialID));

        }
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
    public void update(Observable o, Object arg) {
        ModelManager.ModelManagerUpdateInfo updateInfo = (ModelManager.ModelManagerUpdateInfo)arg;
        switch (updateInfo.getUpdateType()) {
            case TOTAL_MAP_UPDATE:
                doUpdate();
                break;
            case TILE_UPDATE:
                int[] args = (int[])updateInfo.getArguments();
                doUpdate(args[0], args[1]);
                break;
        }
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

    public void setScrollPane(JScrollPane pane) {
        scrollPane = pane;
    }

    private class WorkingAreaPanelMouseHandler extends MouseAdapter {
        @Override
        public void mouseDragged(MouseEvent e) {
            final int modifiersEx = e.getModifiersEx();
            if ((modifiersEx & MouseEvent.BUTTON2_DOWN_MASK) != 0) {
                int dx = WorkingAreaPanel.this.fromMouseX - e.getX();
                int dy = WorkingAreaPanel.this.fromMouseY - e.getY();
                Point viewPosition = WorkingAreaPanel.this.scrollPane.getViewport().getViewPosition();
                Dimension extentSize = WorkingAreaPanel.this.scrollPane.getViewport().getExtentSize();
                Dimension viewSize = WorkingAreaPanel.this.scrollPane.getViewport().getViewSize();
                int newViewX = viewPosition.x + dx;
                int newViewY = viewPosition.y + dy;
                if (newViewX < 0)
                    newViewX = 0;
                else if (newViewX > viewSize.width - extentSize.width)
                    newViewX = viewSize.width - extentSize.width - 1;
                if (newViewY < 0)
                    newViewY = 0;
                else if (newViewY > viewSize.height - extentSize.height)
                    newViewY = viewSize.height - extentSize.height - 1;
                WorkingAreaPanel.this.scrollPane.getViewport().setViewPosition(new Point(newViewX, newViewY));
                WorkingAreaPanel.this.fromMouseX = e.getX();
                WorkingAreaPanel.this.fromMouseY = e.getY();
                WorkingAreaPanel.this.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            }
            else {
                WorkingAreaPanel.this.handleMouseMoveAt(e.getX(), e.getY());
                if ((modifiersEx & MouseEvent.ALT_DOWN_MASK) == 0)
                    WorkingAreaPanel.this.handleMouseDown(modifiersEx);
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            WorkingAreaPanel.this.handleMouseMoveAt(e.getX(), e.getY());
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            if (ModelManager.getInstance().increaseTileSizeInPixels(e.getWheelRotation()))
                WorkingAreaPanel.this.doUpdate();
        }

        @Override
        public void mousePressed(MouseEvent e) {
            WorkingAreaPanel.this.fromMouseX = e.getX();
            WorkingAreaPanel.this.fromMouseY = e.getY();
            if ((e.getModifiersEx() & MouseEvent.ALT_DOWN_MASK) != 0) {
                WorkingAreaPanel.this.handlePipetteTool(e);
                return;
            }
            WorkingAreaPanel.this.lastMacroCommand = new MacroCommand();
            WorkingAreaPanel.this.handleMouseDown(e.getModifiersEx());
            CommandHandler commandHandler = ModelManager.getInstance();
            commandHandler.performCommand(lastMacroCommand);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            WorkingAreaPanel.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            if ((e.getModifiersEx() & MouseEvent.ALT_DOWN_MASK) != 0)
                return;
            if (lastMacroCommand.isEmpty())
                WorkingAreaPanel.this.handleMouseDown(e.getModifiersEx());
            WorkingAreaPanel.this.lastMacroCommand.perform();
        }
    }
}