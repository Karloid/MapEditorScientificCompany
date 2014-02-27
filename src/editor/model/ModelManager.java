package editor.model;

import com.google.gson.Gson;
import editor.service.Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class ModelManager {
    private int tileSizeInPixels = 32;
    private int[][] tiles;
    private int currentSelectedMaterialID;
    private ArrayList<TileType> tileTypes;
    private int mapWidth;
    private int mapHeight;
    private List<Observer> observers;
    private List<Command> commands;

    private static ModelManager instance;

    public static final int MAP_DEFAULT_WIDTH_IN_TILES = 30;
    public static final int MAP_DEFAULT_HEIGHT_IN_TILES = 20;
    public static final String IMAGES_DIR = "images/";

    private ModelManager(String jsonFileName) {
        observers = new ArrayList<Observer>();
        commands = new ArrayList<Command>();
        prepareTiles();
        currentSelectedMaterialID = 0;
        loadTiles(jsonFileName);
    }

    public void registerObserver(Observer observer) {
        if (!observers.contains(observer))
            observers.add(observer);
    }

    private void fireModelChanged() {
        for (Observer o : observers)
            o.update(null, null);
    }

    private void fireModelChanged(int x, int y) {
        for (Observer o : observers)
            o.update(null, new int[] {x, y});
    }

    public static ModelManager createModelManagerTestInstance(String jsonFileName) {
        return new ModelManager(jsonFileName);
    }

    private void loadTiles(String jsonFileName) {
        tileTypes = new ArrayList<TileType>();
        String jsonString = Utils.readFile(jsonFileName);
        Map root = new Gson().fromJson(jsonString, Map.class);
        List<Map<String, Object>> tiles = (List<Map<String, Object>>) root.get("tiles");
        for (Map<String, Object> tile : tiles) {
            tileTypes.add(new TileType((int) Math.round((Double) tile.get("id")),
                    (String) tile.get("name"), (String) tile.get("texture"), (Collection<String>) tile.get("tags")));
        }
    }

    private void prepareTiles() {
        mapWidth = MAP_DEFAULT_WIDTH_IN_TILES;
        mapHeight = MAP_DEFAULT_HEIGHT_IN_TILES;
        tiles = new int[getMapWidth()][getMapHeight()];
        for (int i = 0; i < getMapWidth(); i++)
            for (int j = 0; j < getMapHeight(); j++)
                if (Math.random() > 0.5f)
                    tiles[i][j] = 1;//TileTypes.GRASS1;
                else
                    tiles[i][j] = 2;//TileTypes.GRASS2;
    }

    public static ModelManager getInstance() {
        if (instance == null)
            instance = new ModelManager(IMAGES_DIR + "tileTypes.json");
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
        if (currentSelectedMaterialID != 0) {
            tiles[x][y] = currentSelectedMaterialID;
            fireModelChanged(x, y);
        }
    }

    public ArrayList<TileType> getAllTileTypes() {
        return tileTypes;
    }

    public void setCurrentSelectedMaterialID(int newID) {
        currentSelectedMaterialID = newID;
    }

    public int getCurrentSelectedMaterialID() {
        return currentSelectedMaterialID;
    }

    public void saveMapAsJson(File file) {
        try {
            Writer w = new FileWriter(file);
            w.append('{');
            w.append('\n');
            w.append("\"width\" : " + getMapWidth());
            w.append(",\n");
            w.append("\"height\" : " + getMapHeight());
            w.append(",\n");
            w.append("\"tiles\" : " + new Gson().toJson(tiles));
            w.append('\n');
            w.append('}');
            w.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<TileType> getBasicTileTypes() {
        List<TileType> basicTileTypes = new ArrayList<TileType>();
        for (TileType t : getAllTileTypes()) {
            if (t.getTags().contains("COMMON"))
                basicTileTypes.add(t);
        }
        return basicTileTypes;
    }

    public List<TileType> getRelatedTileTypes(TileType tileType) {
        List<TileType> relatedTileTypes = new ArrayList<TileType>();
        for (TileType t : getAllTileTypes()) {
            for (String s : tileType.getTags()) {
                if (!s.equals("COMMON") && t.getTags().contains(s)) {
                    relatedTileTypes.add(t);
                    break;
                }
            }
        }
        return relatedTileTypes;
    }

    public List<TileType> getTileTypesWithTags(List<String> commonTags) {
        ArrayList<TileType> tileTypesWithTags = new ArrayList<TileType>();
        for (TileType tileType : getAllTileTypes())
            if (tileType.getTags().containsAll(commonTags))
                tileTypesWithTags.add(tileType);
        return tileTypesWithTags;
    }

    public void openMapFromJson(String fileName) {
        String jsonString = Utils.readFile(fileName);
        Map root = new Gson().fromJson(jsonString, Map.class);
        mapWidth = ((Double)root.get("width")).intValue();
        mapHeight = ((Double)root.get("height")).intValue();
        List<List<Double>> s = (List<List<Double>>)root.get("tiles");
        tiles = new int[mapWidth][mapHeight];
        for (int i = 0; i < s.size(); i++)
            for (int j = 0; j < s.get(i).size(); j++)
                tiles[i][j] = s.get(i).get(j).intValue();
        fireModelChanged();
    }

    public int getMapWidth() {
        return mapWidth;
    }

    public int getMapHeight() {
        return mapHeight;
    }

    public String getMapStringRepresentation() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < mapWidth; i++) {
            sb.append('[');
            for (int j = 0; j < mapHeight; j++) {
                sb.append(tiles[i][j]);
                if (j + 1 < mapHeight)
                    sb.append(',');
            }
            sb.append(']');
        }
        sb.append(']');
        return sb.toString();
    }

    public void performCommand(Command c) {
        c.perform();
        commands.add(c);
        fireModelChanged();
    }

    public void undoLastCommand() {
        if (commands.size() > 0) {
            commands.get(commands.size() - 1).undo();
            commands.remove(commands.size() - 1);
            fireModelChanged();
        }
    }
}
