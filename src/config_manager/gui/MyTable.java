package config_manager.gui;

import javax.swing.*;

public class MyTable extends JTable {
    public MyTable() {

    }

    @Override
    public int getRowHeight() {
        return 53;
    }

    @Override
    public Class<?> getColumnClass(int column) {
        if (column == 0)
            return ImageIcon.class;
        return Object.class;
    }
}
