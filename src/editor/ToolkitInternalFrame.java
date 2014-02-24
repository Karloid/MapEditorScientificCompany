package editor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class ToolkitInternalFrame extends JInternalFrame {
    public ToolkitInternalFrame() {
        super("Toolkit", true, true, false, false);
        JPanel panel = new JPanel(new FlowLayout());

        ArrayList<TileType> tileTypes = ModelManager.getInstance().getTileTypes();
        for (TileType t : tileTypes) {
            final TileType tileType = t;
            JButton button = new JButton();
            button.setIcon(new ImageIcon(ModelManager.IMAGES_DIR + tileType.getTexture()));
            button.setBorderPainted(false);
            button.setContentAreaFilled(false);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ModelManager.getInstance().setCurrentSelectedMaterialID(tileType.getId());
                }
            });
            panel.add(button);
        }

        add(panel);
    }
}
