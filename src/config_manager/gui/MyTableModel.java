package config_manager.gui;

import editor.model.TileType;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class MyTableModel extends AbstractTableModel {
    private List<TileType> tileTypes;

    public MyTableModel(List<TileType> _tileTypes) {
        tileTypes = _tileTypes;
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "Texture";
            case 1:
                return "ID";
            case 2:
                return "Name";
            case 3:
                return "Tags";
        }
        return null;
    }

    @Override
    public int getRowCount() {
        return tileTypes.size();
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                BufferedImage bi = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
//                bi.getGraphics().setXORMode(Color.RED);
//                bi.getGraphics().setColor(Color.blue);
//                bi.getGraphics().drawRect(1, 1, 40, 40);
//                bi.getGraphics().fillRect(1, 1, 40, 40);
                try {
                    bi.getGraphics().drawImage(ImageIO.read(new File("configurations" + File.separator + "config1" + File.separator + tileTypes.get(rowIndex).getTexture())), 0, 0, 50, 50, null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return new ImageIcon(bi);
            case 1:
                return tileTypes.get(rowIndex).getId();
            case 2:
                return tileTypes.get(rowIndex).getName();
            case 3:
                StringBuilder sb = new StringBuilder();
                sb.append("[");
                for (String s : tileTypes.get(rowIndex).getTags())
                    sb.append(s + ", ");
                if (sb.lastIndexOf(", ") == sb.length() - 2);
                    sb.delete(sb.length() - 2, sb.length());
                sb.append("]");
                return sb.toString();
        }
        return null;
    }
}
