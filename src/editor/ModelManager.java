package editor;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ModelManager {
    private int tileSizeInPixels = 32;
    private int[][] tiles;
    private int currentSelectedMaterialID;
    private ArrayList<TileType> tileTypes;

    private static ModelManager instance;

    public static final int MAP_WIDTH_IN_TILES = 30;
    public static final int MAP_HEIGHT_IN_TILES = 20;
    public static final String IMAGES_DIR = "images/";

    private ModelManager() {
        prepareTiles();
        currentSelectedMaterialID = 1;
        loadTiles();
    }

    private void loadTiles() {
        tileTypes = new ArrayList<TileType>();
        String jsonString = Utils.readFile(IMAGES_DIR + "tileTypes.json");
        Map root = new Gson().fromJson(jsonString, Map.class);
        List<Map<String, Object>> tiles = (List<Map<String, Object>>) root.get("tiles");
        for (Map<String, Object> tile : tiles) {
            tileTypes.add(new TileType((int) Math.round((Double) tile.get("id")),
                    (String) tile.get("name"), (String) tile.get("texture"), (Collection<String>) tile.get("tags")));
        }
    }

    private void prepareTiles() {
        tiles = new int[MAP_WIDTH_IN_TILES][MAP_HEIGHT_IN_TILES];
        for (int i = 0; i < MAP_WIDTH_IN_TILES; i++)
            for (int j = 0; j < MAP_HEIGHT_IN_TILES; j++)
                if (Math.random() > 0.5f)
                    tiles[i][j] = TileTypes.GRASS1;
                else
                    tiles[i][j] = TileTypes.GRASS2;
    }

    public static ModelManager getInstance() {
        if (instance == null)
            instance = new ModelManager();
        return instance;
    }

    public int getTileSizeInPixels() {
        return tileSizeInPixels;
    }

    public boolean increaseTileSizeInPixels(int value) {
        if (value < 0) {
            if (tileSizeInPixels < 100) {
                tileSizeInPixels -= value;
                return true;
            }
        } else if (tileSizeInPixels > 10) {
            tileSizeInPixels -= value;
            return true;
        }
        return false;
    }

    public int getTileAt(int x, int y) {
        return tiles[x][y];
    }

    public void updateTileAt(int x, int y) {
        tiles[x][y] = currentSelectedMaterialID;
    }

    public ArrayList<TileType> getTileTypes() {
        return tileTypes;
    }

    public void setCurrentSelectedMaterialID(int newID) {
        currentSelectedMaterialID = newID;
    }

    public void saveMapAsJson(File file) {
        try {
            Writer w = new FileWriter(file);
            w.append('{');
            w.append('\n');
            w.append("\"width\" : " + MAP_WIDTH_IN_TILES);
            w.append(",\n");
            w.append("\"height\" : " + MAP_HEIGHT_IN_TILES);
            w.append(",\n");
            w.append("\"tiles\" : " + new Gson().toJson(tiles));
            w.append('\n');
            w.append('}');
            w.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadMapFromJson(File file) {

    }

//    public int getCurrentSelectedMaterialID() {
//        return currentSelectedMaterialID;
//    }

}
