package editor.gui;

import editor.model.ModelManager;
import editor.model.TileType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

public class ToolboxPanel extends JPanel {
    //constant collections
    private final List<TileTypeComponent> tileTypeGroups;

    //GUI
    private final TileTypeSelectorPanel tileTypeSelectorPanel;
    private final SelectedTileTypePanel selectedTileTypePanel;

    //static constants
    private static final int GRID_BAG_LAYOUT_WIDTH = 4;

    public ToolboxPanel() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Toolbox"));

        tileTypeGroups = new ArrayList<TileTypeComponent>();

        selectedTileTypePanel = new SelectedTileTypePanel();
        tileTypeSelectorPanel = new TileTypeSelectorPanel(6);
        ModelManager.getInstance().registerObserver(selectedTileTypePanel);

        loadTileTypeGroups();
        rebuildUI();
    }

    private void rebuildUI() {
        GridBagConstraints gbc = new GridBagConstraints();
        final int commonPadding = 2;
        final Insets commonInsets = new Insets(commonPadding, commonPadding, commonPadding, commonPadding);
        gbc.insets = commonInsets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weighty = 0.0;
        gbc.weightx = 0.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        int counter = 0;
        for (TileTypeComponent tileTypeComponent : tileTypeGroups) {
            int widthToCheck = GRID_BAG_LAYOUT_WIDTH;
            if (gbc.gridy < 2)
                widthToCheck = GRID_BAG_LAYOUT_WIDTH - 2;
            if (counter == widthToCheck) {
                gbc.gridx = 0;
                gbc.gridy++;
                counter = 0;
            }
            add(tileTypeComponent, gbc);
            gbc.gridx++;
            counter++;
        }

        final int gridYForTileTypeSelectorPanel = gbc.gridy + 1;

        gbc.insets = new Insets(commonPadding, 20, commonPadding, 20);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridx = GRID_BAG_LAYOUT_WIDTH - 2;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.gridheight = 2;
        add(selectedTileTypePanel, gbc);

        gbc.insets = commonInsets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridx = 0;
        gbc.gridy = gridYForTileTypeSelectorPanel;
        gbc.gridwidth = GRID_BAG_LAYOUT_WIDTH;
        gbc.gridheight = 1;
        gbc.weighty = 100.0;
        gbc.fill = GridBagConstraints.BOTH;
        JScrollPane scrollPane = new JScrollPane(tileTypeSelectorPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(1,1));
        add(scrollPane, gbc);

        updateUI();
    }

    private void loadTileTypeGroups() {
        final ModelManager modelMgr = ModelManager.getInstance();
        tileTypeGroups.clear();
        boolean isFirstCheckBoxSelected = false;
        for (TileType tileType : modelMgr.getBasicTileTypes()) {
            JCheckBox checkbox = new JCheckBox("");
            checkbox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    tileTypeSelectorPanel.loadTileTypes(modelMgr.getTileTypesWithTags(getCheckedTags()));
                }
            });
            tileTypeGroups.add(TileTypeComponent.createTileTypeComponentWithCheckBox(tileType, checkbox));
            if (!isFirstCheckBoxSelected) {
                isFirstCheckBoxSelected = true;
                checkbox.doClick();
            }
        }
    }

    private List<String> getCheckedTags() {
        List<String> selectedTags = new ArrayList<String>();
        for (TileTypeComponent c : tileTypeGroups)
            if (c.isSelected())
                selectedTags.add(c.getTileType().getTags().toArray()[0].toString());
        return selectedTags;
    }
}