package editor.gui;

import editor.model.ModelManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Observable;
import java.util.Observer;

public class MainFrame extends JFrame implements Observer {
    public final int SCREEN_WIDTH;
    public final int SCREEN_HEIGHT;
    private JScrollPane scrollPane;
    private ToolboxPanel toolboxPanel;
    private EditorMenuBar menuBar;

    private final static String TITLE = "Super Map Editor";

    public static void main(String[] args) {
        MainFrame mainFrame = new MainFrame();
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainFrame.setVisible(true);
    }

    public MainFrame() {
        Toolkit kit = Toolkit.getDefaultToolkit();
        Dimension screenSize = kit.getScreenSize();
        SCREEN_WIDTH = screenSize.width;
        SCREEN_HEIGHT = screenSize.height;
        setSize(6 * SCREEN_WIDTH / 8, 6 * SCREEN_HEIGHT / 8);
        setLocation(SCREEN_WIDTH / 8, SCREEN_HEIGHT / 8);
        setIconImage(kit.getImage("images/zbunny.png"));
        menuBar = new EditorMenuBar(this);
        setJMenuBar(menuBar);
        prepareDesktop();
        ModelManager.getInstance().registerObserver(this);
        updateTitle();
    }

    private void prepareDesktop() {
        WorkingAreaPanel mapPanel = new WorkingAreaPanel();
        scrollPane = new JScrollPane(mapPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        mapPanel.setScrollPane(scrollPane);
//        Point p = scrollPane.getViewport().getViewPosition();
//        scrollPane.getViewport().setViewPosition(p);
//        Dimension d1 = scrollPane.getViewport().getExtentSize();
//        System.out.println("extent size: " + d1.width + "x" + d1.height);
//        Dimension d = scrollPane.getViewport().getViewSize();
//        System.out.println("view size: " + d.width + "x" + d.height);
        toolboxPanel = new ToolboxPanel();
        add(toolboxPanel, BorderLayout.WEST);
        add(scrollPane);
    }

    @Override
    public void update(Observable o, Object arg) {
        ModelManager.ModelManagerUpdateInfo updateInfo = (ModelManager.ModelManagerUpdateInfo)arg;
        ModelManager.ModelManagerUpdateType updateType = updateInfo.getUpdateType();
        if (updateType.equals(ModelManager.ModelManagerUpdateType.MAP_FILE_UPDATE))
            updateTitle();
        else if (updateType.equals(ModelManager.ModelManagerUpdateType.NEW_MODEL)) {
            this.remove(scrollPane);
            remove(toolboxPanel);
            prepareDesktop();
            ModelManager.getInstance().registerObserver(this);
            updateTitle();
            menuBar.checkForCommandListChangesAndUpdate(ModelManager.getInstance());
            menuBar.checkForSmartModeChangesAndUpdate();
        }
        else if (updateType.equals(ModelManager.ModelManagerUpdateType.COMMAND_LIST_CHANGED)) {
            menuBar.checkForCommandListChangesAndUpdate(ModelManager.getInstance());
        }
    }

    private void updateTitle() {
        String mapAbsolutePath = ModelManager.getInstance().getMapAbsolutePath();
        StringBuilder sb = new StringBuilder();
        sb.append(TITLE);
        sb.append(" - [");
        if (mapAbsolutePath.isEmpty())
            sb.append("Unnamed");
        else
            sb.append(mapAbsolutePath);
        sb.append(']');
        setTitle(sb.toString());
    }
}