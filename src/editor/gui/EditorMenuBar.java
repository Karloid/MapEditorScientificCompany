package editor.gui;

import editor.model.ModelManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;

public class EditorMenuBar extends JMenuBar {
    private Component parent;

    public EditorMenuBar(Component _parent) {
        parent = _parent;
        JMenu mapMenu = new JMenu("Map");
        add(mapMenu);
        mapMenu.add(createClearItem());
        mapMenu.addSeparator();
        mapMenu.add(createSettingsItem());
        mapMenu.addSeparator();
        mapMenu.add(createSaveMenuItem());
        mapMenu.add(createSaveAsMenuItem());
        mapMenu.add(createOpenMenuItem());
        mapMenu.addSeparator();
        mapMenu.add(createQuitMenuItem());
        JMenu editMenu = new JMenu("Edit");
        add(editMenu);
        editMenu.add(createUndoMenuItem());
    }

    private JMenuItem createClearItem() {
        JMenuItem settingsMenuItem = new JMenuItem("Clear");
        settingsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
        settingsMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ModelManager modelMgr = ModelManager.getInstance();
                modelMgr.performCommand(modelMgr.new ClearMapCommand());
            }
        });
        return settingsMenuItem;
    }

    private JMenuItem createSettingsItem() {
        JMenuItem settingsMenuItem = new JMenuItem("Settings...");
        settingsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
        settingsMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ModelManager modelMgr = ModelManager.getInstance();
                MapSettingsPanel mapSettingsPanel = new MapSettingsPanel(modelMgr.getMapWidth(), modelMgr.getMapHeight());
                if (mapSettingsPanel.showDialog(parent, "Settings"))
                    modelMgr.performCommand(modelMgr.new UpdateMapSizeCommand(mapSettingsPanel.getMapWidth(), mapSettingsPanel.getMapHeight()));
            }
        });
        return settingsMenuItem;
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
        JMenuItem saveMenuItem = new JMenuItem("Save");
        saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
        saveMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ModelManager mgr = ModelManager.getInstance();
                if (!mgr.getMapAbsolutePath().isEmpty())
                    mgr.saveMapAsJsonAtCurrentFile();
                else
                    saveMapAsNewJsonFile();
            }
        });
        return saveMenuItem;
    }

    private JMenuItem createSaveAsMenuItem() {
        JMenuItem saveAsMenuItem = new JMenuItem("Save As...");
        saveAsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
        saveAsMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveMapAsNewJsonFile();
            }
        });
        return saveAsMenuItem;
    }

    private JMenuItem createOpenMenuItem() {
        JMenuItem openMenuItem = new JMenuItem("Open...");
        openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
        openMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser("maps");
                fileChooser.setMultiSelectionEnabled(false);
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                if (fileChooser.showDialog(parent, "Open") == JFileChooser.APPROVE_OPTION) {
                    ModelManager.getInstance().openMapFromJson(fileChooser.getSelectedFile().getAbsolutePath());
                    ModelManager.getInstance().clearCommandHistory();
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
                ModelManager.getInstance().undoLastCommand();
            }
        });
        return undoMenuItem;
    }

    private void saveMapAsNewJsonFile() {
        JFileChooser fileChooser = new JFileChooser("maps");
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (fileChooser.showDialog(parent, "Save As") == JFileChooser.APPROVE_OPTION) {
            File f = fileChooser.getSelectedFile();
            if (!f.getAbsolutePath().endsWith(".json"))
                f = new File(f.getAbsolutePath() + ".json");  //TODO - Разрулить! Здесь может быть косяк, т.к. может быть затерт существующий  ни в чем не виновный .json-файл
            ModelManager.getInstance().saveMapAsJson(f);
        }
    }
}
