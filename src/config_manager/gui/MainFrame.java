package config_manager.gui;

import editor.gui.ToolboxPanel;
import editor.model.TileType;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class MainFrame extends JFrame {
    public final int SCREEN_WIDTH;
    public final int SCREEN_HEIGHT;
    private JScrollPane scrollPane;
    private ToolboxPanel toolboxPanel;

    private JProgressBar totalProgressBar;
    private JProgressBar dayProgressBar;
    private JProgressBar hourProgressBar;
    private JProgressBar minuteProgressBar;

    private final static String TITLE = "Configuration Editor";

    public static void main(String[] args) {
        Collection<String> tags = new ArrayList<String>();
        tags.add("GRASS");
        tags.add("COMMON");
        TileType t = new TileType(1, "GRASS1", "grass1.png", tags);
        t.setNeighbourMaterial(null, "bum");
        t.setNeighbourMaterial(null, "bum");

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

        JTable table = new MyTable();
        List<TileType> tileTypes = new ArrayList<TileType>();
        Collection<String> tags = new ArrayList<String>();
        tags.add("GRASS");
        tags.add("COMMON");
        tileTypes.add(new TileType(1, "GRASS1", "grass1.png", tags));
        tags = new ArrayList<String>();
        tags.add("GRASS");
        tileTypes.add(new TileType(2, "GRASS2", "grass2.png", tags));
        tags = new ArrayList<String>();
        tags.add("DIRT");
        tags.add("COMMON");
        tileTypes.add(new TileType(3, "DIRT1", "dirt1.png", tags));
        tags = new ArrayList<String>();
        tags.add("DIRT");
        tileTypes.add(new TileType(4, "DIRT2", "dirt2.png", tags));
        table.setModel(new MyTableModel(tileTypes));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
        table.getColumnModel().getColumn(0).setWidth(53);
        table.getColumnModel().getColumn(0).setMaxWidth(53);
        table.getColumnModel().getColumn(1).setWidth(10);
        table.getColumnModel().getColumn(1).setMaxWidth(10);

//        table = new JTable(new String[][]{{"1","2","3","4"}}, new String[] {"11","21","31","41"});

        Box b = Box.createVerticalBox();
        b.add(table);
        add(b, BorderLayout.CENTER);

        /*totalProgressBar = new JProgressBar(0, 100000);
        dayProgressBar = new JProgressBar(0, 100000);
        hourProgressBar = new JProgressBar(0, 100000);
        minuteProgressBar = new JProgressBar(0, 100000);

        Box b = Box.createVerticalBox();
        b.add(totalProgressBar);
        b.add(dayProgressBar);
        b.add(hourProgressBar);
        b.add(minuteProgressBar);



        add(b);
        Thread t = new Thread(this);

        t.start();*/
//        prepareDesktop();
        updateTitle();
    }



    private void prepareDesktop() {
//        WorkingAreaPanel mapPanel = new WorkingAreaPanel();
//        scrollPane = new JScrollPane(mapPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
//        mapPanel.setScrollPane(scrollPane);
        toolboxPanel = new ToolboxPanel();
        add(toolboxPanel, BorderLayout.EAST);
//        add(scrollPane);
    }


    private void updateTitle() {
        String mapAbsolutePath = "STUB";
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