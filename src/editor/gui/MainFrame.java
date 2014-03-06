package editor.gui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    public final int SCREEN_WIDTH;
    public final int SCREEN_HEIGHT;

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
        prepareDesktop();
        setJMenuBar(new EditorMenuBar(this));
    }

    private void prepareDesktop() {
        WorkingAreaPanel mapPanel = new WorkingAreaPanel();
        JScrollPane scrollPane = new JScrollPane(mapPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(new ToolboxPanel(), BorderLayout.WEST);
        add(scrollPane);
    }
}
