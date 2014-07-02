package editor.gui;

import editor.model.CommandHandler;
import editor.model.Configurable;
import editor.model.ModelManager;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;

public class EditorMenuBar extends JMenuBar {
    private JFileChooser fileChooser;
    private Component parent;
    private JMenuItem undoItem;
    private JCheckBoxMenuItem smartModeCheckBox;

    public EditorMenuBar(Component _parent) {
        parent = _parent;
        fileChooser = buildJFileChooser();
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
        undoItem = createUndoMenuItem();
        checkForCommandListChangesAndUpdate(ModelManager.getInstance());
        editMenu.add(undoItem);
        JMenu configMenu = new JMenu("Configuration");
        add(configMenu);
        smartModeCheckBox = createSmartModeCheckBoxItem();
        configMenu.add(smartModeCheckBox);
        configMenu.addSeparator();
        Configurable configurable = ModelManager.getInstance();
        ButtonGroup buttonGroup = new ButtonGroup();
        for (int i = 0; i < configurable.getConfigCount(); i++) {
            final String configurationName = configurable.getConfigAt(i);
            JRadioButtonMenuItem radioButtonMenuItem = new JRadioButtonMenuItem(configurationName);
            buttonGroup.add(radioButtonMenuItem);
            radioButtonMenuItem.setSelected(configurationName.equals(configurable.getCurrentConfig()));
            configMenu.add(radioButtonMenuItem);
            radioButtonMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Configurable newConfigurable = ModelManager.getInstance();
                    if (!newConfigurable.getCurrentConfig().equals(configurationName)) {
                        newConfigurable.applyNewConfig(configurationName);
                        parent.revalidate();
                        parent.repaint();
                    }
                }
            });
        }
    }

    private JFileChooser buildJFileChooser() {
        JFileChooser fc = new JFileChooser(ModelManager.MAP_PATH);
        fc.setMultiSelectionEnabled(false);
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setFileView(new FileView() {
            @Override
            public Icon getIcon(File f) {
                if (f.isDirectory())
                    return null;
                return new ImageIcon(ModelManager.IMAGE_PATH + File.separator + "map_example_icon.png");
            }
        });
        JSONFileFilter jsonFileFilter = new JSONFileFilter();
        fc.addChoosableFileFilter(jsonFileFilter);
        fc.setFileFilter(jsonFileFilter);
//        fc.setAcceptAllFileFilterUsed(false);
        return fc;
    }

    private JCheckBoxMenuItem createSmartModeCheckBoxItem() {
        final JCheckBoxMenuItem checkBox = new JCheckBoxMenuItem("Smart Mode", ModelManager.getInstance().isSmartModeOn());
        checkBox.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, 0));
        checkBox.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                ModelManager.getInstance().setSmartMode(checkBox.isSelected());
            }
        });
        return checkBox;
    }

    private JMenuItem createClearItem() {
        JMenuItem settingsMenuItem = new JMenuItem("Clear");
        settingsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
        settingsMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ModelManager modelMgr = ModelManager.getInstance();
                CommandHandler commandHandler = modelMgr;
                commandHandler.performCommand(modelMgr.new ClearMapCommand());
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
                if (mapSettingsPanel.showDialog(parent, "Settings")) {
                    CommandHandler commandHandler = modelMgr;
                    commandHandler.performCommand(modelMgr.new UpdateMapSizeCommand(mapSettingsPanel.getMapWidth(), mapSettingsPanel.getMapHeight()));
                }
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
                if (fileChooser.showDialog(parent, "Open") == JFileChooser.APPROVE_OPTION) {
                    try {
                        ModelManager.getInstance().openMapFromJson(fileChooser.getSelectedFile().getAbsolutePath());
                        CommandHandler commandHandler = ModelManager.getInstance();
                        commandHandler.clearCommandHistory();
                    }
                    catch (Exception exception) {
                        JOptionPane.showMessageDialog(null, exception.getMessage(), "File open error", JOptionPane.ERROR_MESSAGE);
                    }
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
                CommandHandler commandHandler = ModelManager.getInstance();
                commandHandler.undoLastCommand();
            }
        });
        return undoMenuItem;
    }

    private void saveMapAsNewJsonFile() {
        if (fileChooser.showDialog(parent, "Save As") == JFileChooser.APPROVE_OPTION) {
            File f = fileChooser.getSelectedFile();
            if (!f.getAbsolutePath().endsWith(".json"))
                f = new File(f.getAbsolutePath() + ".json");  //TODO - Разрулить! Здесь может быть косяк, т.к. может быть затерт существующий  ни в чем не виновный .json-файл
            ModelManager.getInstance().saveMapAsJson(f);
        }
    }

    public void checkForCommandListChangesAndUpdate(CommandHandler commandHandler) {
        undoItem.setEnabled(commandHandler.getCommandHistorySize() != 0);
    }

    public void checkForSmartModeChangesAndUpdate() {
        smartModeCheckBox.setSelected(ModelManager.getInstance().isSmartModeOn());
    }

    private static class JSONFileFilter extends FileFilter {
        @Override
        public String getDescription() {
            return "Map, saved with JSON-extension";
        }

        @Override
        public boolean accept(File f) {
            return f.isDirectory() || f.getPath().toLowerCase().endsWith(".json");
        }
    }
}
