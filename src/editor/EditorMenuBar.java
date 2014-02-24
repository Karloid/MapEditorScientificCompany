package editor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EditorMenuBar extends JMenuBar {
    private Component parent;

    public EditorMenuBar(Component _parent) {
        parent = _parent;
        JMenu fileMenu = new JMenu("Map");
        add(fileMenu);
        fileMenu.add(createSaveMenuItem());
        fileMenu.add(createLoadMenuItem());
    }

    private JMenuItem createSaveMenuItem() {
        JMenuItem saveMenuItem = new JMenuItem("save...");
        saveMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setMultiSelectionEnabled(false);
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                if (fileChooser.showDialog(parent, "Save") == JFileChooser.APPROVE_OPTION) {
                    ModelManager.getInstance().saveMapAsJson(fileChooser.getSelectedFile());
                }


            }
        });
        return saveMenuItem;
    }

    private JMenuItem createLoadMenuItem() {
        JMenuItem loadMenuItem = new JMenuItem("load...");
        return loadMenuItem;
    }
}
