
package editor.gui;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyVetoException;

public class MainFrame extends JFrame {
    private JDesktopPane desktop;

    public final int SCREEN_WIDTH;
    public final int SCREEN_HEIGHT;

    public static void main(String[] args) {
        MainFrameVer2 mainFrame = new MainFrameVer2();
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

    private void prepareToolkitInternalFrame() {
        JInternalFrame toolkitInternalFrame = new ToolkitInternalFrame();
        toolkitInternalFrame.setLocation(1, 30);
//        toolkitInternalFrame.setSize(new Dimension(50, 50));
//        toolkitInternalFrame.reshape(1, 30, 170, 400);
        toolkitInternalFrame.setLayer(2);
        toolkitInternalFrame.setVisible(true);
//        toolkitInternalFrame.pack();
        desktop.add(toolkitInternalFrame);
    }

    private void prepareDesktop() {
        desktop = new JDesktopPane();
        setContentPane(desktop);
        prepareWorkingAreaInternalFrame();
        prepareToolkitInternalFrame();
    }

    private void prepareWorkingAreaInternalFrame() {
        JInternalFrame workingAreaInternalFrame = new JInternalFrame(null, false, false, false, false);
        WorkingAreaPanel mapPanel = new WorkingAreaPanel();
        JScrollPane scrollPane = new JScrollPane(mapPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        workingAreaInternalFrame.setFrameIcon(null);
        workingAreaInternalFrame.setLayer(1);
        workingAreaInternalFrame.add(scrollPane);
        workingAreaInternalFrame.setVisible(true);
        desktop.add(workingAreaInternalFrame);
        try {
            workingAreaInternalFrame.setMaximum(true);
        }
        catch (PropertyVetoException e) {
            e.printStackTrace();
        }
    }
}