package editor.gui;

import editor.model.ModelManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class EditorMenuBar extends JMenuBar {
    private Component parent;

    public EditorMenuBar(Component _parent) {
        parent = _parent;
        JMenu fileMenu = new JMenu("Map");
        add(fileMenu);
        fileMenu.add(createSaveMenuItem());
        fileMenu.add(createOpenMenuItem());
        fileMenu.addSeparator();
        fileMenu.add(createQuitMenuItem());
        JMenu editMenu = new JMenu("Edit");
        add(editMenu);
        editMenu.add(createUndoMenuItem());
    }

    private JMenuItem createQuitMenuItem() {
        JMenuItem quitMenuItem = new JMenuItem("Quit");
        quitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));
        quitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        return quitMenuItem;
    }


    private JMenuItem createSaveMenuItem() {
        JMenuItem saveMenuItem = new JMenuItem("Save...");
        saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
        saveMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser("maps");
                fileChooser.setMultiSelectionEnabled(false);
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                if (fileChooser.showDialog(parent, "Save") == JFileChooser.APPROVE_OPTION) {
                    ModelManager.getInstance().saveMapAsJson(fileChooser.getSelectedFile());
                }
            }
        });
        return saveMenuItem;
    }

    private JMenuItem createOpenMenuItem() {
        JMenuItem openMenuItem = new JMenuItem("Load...");
        openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
        openMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser("maps");
                fileChooser.setMultiSelectionEnabled(false);
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                if (fileChooser.showDialog(parent, "Open") == JFileChooser.APPROVE_OPTION) {
                    ModelManager.getInstance().openMapFromJson(fileChooser.getSelectedFile().getAbsolutePath());
                }
            }
        });
        return openMenuItem;
    }

    private JMenuItem createUndoMenuItem() {
        JMenuItem undoMenuItem = new JMenuItem("Undo");
        undoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
        undoMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ModelManager.getInstance().redoLastCommand();
            }
        });
        return undoMenuItem;
    }
}
