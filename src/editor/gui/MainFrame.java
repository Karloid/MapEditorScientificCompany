package editor.gui;

import editor.model.ModelManager;

import javax.swing.*;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;

public class MainFrame extends JFrame implements Observer {
    public final int SCREEN_WIDTH;
    public final int SCREEN_HEIGHT;

    private final static String TITLE = "Super Map Editor";

    public static void main(String[] args) {
        MainFrame mainFrame = new MainFrame();
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
        setJMenuBar(new EditorMenuBar(this));
        prepareDesktop();
        ModelManager.getInstance().registerObserver(this);
        updateTitle();
    }

    private void prepareDesktop() {
        WorkingAreaPanel mapPanel = new WorkingAreaPanel();
        JScrollPane scrollPane = new JScrollPane(mapPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(new ToolboxPanel(), BorderLayout.WEST);
        add(scrollPane);
    }

    @Override
    public void update(Observable o, Object arg) {
        ModelManager.ModelManagerUpdateInfo updateInfo = (ModelManager.ModelManagerUpdateInfo)arg;
        if (updateInfo.getUpdateType().equals(ModelManager.ModelManagerUpdateType.MAP_FILE_UPDATE))
            updateTitle();
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