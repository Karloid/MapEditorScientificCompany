package editor.model;

import com.google.gson.Gson;
import editor.model.command.Command;
import editor.service.Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class ModelManager {
    private int tileSizeInPixels = 32;
    private int[][] tiles;
    private int primaryMaterialID, secondaryMaterialID;
    private ArrayList<TileType> tileTypes;
    private Map<Integer, String> tileIdToTileTextureMap;
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
        tileIdToTileTextureMap = new HashMap<Integer, String>();
        loadTileTypes(jsonFileName);
        initTilesWithDefaultValues();
    }

    public void setMapSize(int x, int y) {
        int[][] newTiles = createInitiatedByDefaultTiles(x, y);
        for (int i = 0; i < Math.min(x, getMapWidth()); i++)
            for (int j = 0; j < Math.min(y, getMapHeight()); j++)
                newTiles[i][j] = tiles[i][j];
        mapWidth = x;
        mapHeight = y;
        tiles = newTiles;
        fireAllMapChanged();
    }

    public void registerObserver(Observer observer) {
        if (!observers.contains(observer))
            observers.add(observer);
    }

    private void fireAllMapChanged() {
        for (Observer o : observers)
            o.update(null, new ModelManagerUpdateInfo(ModelManagerUpdateType.TOTAL_MAP_UPDATE, null));
    }

    private void fireOneTileChanged(int x, int y) {
        for (Observer o : observers)
            o.update(null, new ModelManagerUpdateInfo(ModelManagerUpdateType.TILE_UPDATE, new int[] {x, y}));
    }

    private void fireMaterialChanged() {
        for (Observer o : observers)
            o.update(null, new ModelManagerUpdateInfo(ModelManagerUpdateType.MATERIAL_UPDATE, null));
    }

    public static ModelManager createModelManagerTestInstance(String jsonFileName) {
        return new ModelManager(jsonFileName);
    }

    public String getTextureForTileID(int tileID) {
        return tileIdToTileTextureMap.get(tileID);
    }

    private void loadTileTypes(String jsonFileName) {
        tileTypes = new ArrayList<TileType>();
        String jsonString = Utils.readFile(jsonFileName);
        Map root = new Gson().fromJson(jsonString, Map.class);
        List<Map<String, Object>> tiles = (List<Map<String, Object>>) root.get("tiles");
        for (Map<String, Object> tile : tiles) {
            TileType t = new TileType((int) Math.round((Double) tile.get("id")),
                    (String) tile.get("name"), (String) tile.get("texture"), (Collection<String>) tile.get("tags"));
            tileTypes.add(t);
            tileIdToTileTextureMap.put(t.getId(), t.getTexture());
        }
        if (tileTypes.isEmpty())
            throw new RuntimeException();
        primaryMaterialID = secondaryMaterialID = tileTypes.get(0).getId();
    }

    private static int[][] createInitiatedByDefaultTiles(int x, int y) {
        int[][] newTiles = new int[x][y];
        for (int i = 0; i < x; i++)
            for (int j = 0; j < y; j++)
                if (Math.random() > 0.5f)
                    newTiles[i][j] = 1;//TileTypes.GRASS1;
                else
                    newTiles[i][j] = 2;//TileTypes.GRASS2;
        return newTiles;
    }

    private void initTilesWithDefaultValues() {
        mapWidth = MAP_DEFAULT_WIDTH_IN_TILES;
        mapHeight = MAP_DEFAULT_HEIGHT_IN_TILES;
        tiles = createInitiatedByDefaultTiles(getMapWidth(), getMapHeight());
//        tiles = new int[getMapWidth()][getMapHeight()];
//        for (int i = 0; i < getMapWidth(); i++)
//            for (int j = 0; j < getMapHeight(); j++)
//                if (Math.random() > 0.5f)
//                    tiles[i][j] = 1;//TileTypes.GRASS1;
//                else
//                    tiles[i][j] = 2;//TileTypes.GRASS2;
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

    public ArrayList<TileType> getAllTileTypes() {
        return tileTypes;
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
        if (!commonTags.isEmpty()) {
            for (TileType tileType : getAllTileTypes())
                if (tileType.getTags().containsAll(commonTags))
                    tileTypesWithTags.add(tileType);
        }
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
        fireAllMapChanged();
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
        fireAllMapChanged();
    }

    public void undoLastCommand() {
        if (commands.size() > 0) {
            commands.get(commands.size() - 1).undo();
            commands.remove(commands.size() - 1);
            fireAllMapChanged();
        }
    }

    public void setPrimaryMaterialID(int _primaryMaterialID) {
        primaryMaterialID = _primaryMaterialID;
        fireMaterialChanged();
    }

    public void setSecondaryMaterialID(int _secondaryMaterialID) {
        secondaryMaterialID = _secondaryMaterialID;
        fireMaterialChanged();
    }

    public int getPrimaryMaterialID() {
        return primaryMaterialID;
    }

    public int getSecondaryMaterialID() {
        return secondaryMaterialID;
    }

    public void swapMaterials() {
        int tmp = secondaryMaterialID;
        setSecondaryMaterialID(primaryMaterialID);
        setPrimaryMaterialID(tmp);
    }

    public static enum ModelManagerUpdateType {MATERIAL_UPDATE, TILE_UPDATE, TOTAL_MAP_UPDATE};

    public static class ModelManagerUpdateInfo {
        private ModelManagerUpdateType updateType;
        private Object arguments;

        public ModelManagerUpdateInfo(ModelManagerUpdateType _updateType, Object _args) {
            updateType = _updateType;
            arguments = _args;
        }

        public ModelManagerUpdateType getUpdateType() {
            return updateType;
        }

        public Object getArguments() {
            return arguments;
        }
    }

    public class UpdateTileAtCommand implements Command {

        private int x, y;
        private int oldMaterial, newMaterial;
        public UpdateTileAtCommand(int _x, int _y, int _newMaterial) {
            x = _x;
            y = _y;
            newMaterial = _newMaterial;
        }

        @Override
        public void perform() {
            oldMaterial = ModelManager.this.tiles[x][y];
            updateTileAtWith(x, y, newMaterial);
        }

        @Override
        public void undo() {
            updateTileAtWith(x, y, oldMaterial);
        }

        private void updateTileAtWith(int x, int y, int materialID) {
            if (materialID != 0) {
                ModelManager.this.tiles[x][y] = materialID;
                ModelManager.this.fireOneTileChanged(x, y);
            }
        }

    }
    public class UpdateMapSizeCommand implements Command {
        private int newMapWidth, newMapHeight;
        private int oldMapWidth, oldMapHeight;
        private int[][] oldTiles;

        public UpdateMapSizeCommand(int _width, int _height) {
            newMapWidth = _width;
            newMapHeight = _height;
        }

        @Override
        public void perform() {
            oldMapWidth = ModelManager.this.mapWidth;
            oldMapHeight = ModelManager.this.mapHeight;
            oldTiles = ModelManager.this.tiles;
            ModelManager.this.setMapSize(newMapWidth, newMapHeight);
        }

        @Override
        public void undo() {
            ModelManager.this.setMapSize(oldMapWidth, oldMapHeight);
            ModelManager.this.tiles = oldTiles;
            ModelManager.this.fireAllMapChanged();
        }

    }

    public class ClearMapCommand implements Command {
        private int[][] oldTiles;

        public ClearMapCommand() { }

        @Override
        public void perform() {
            oldTiles = ModelManager.this.tiles;
            instance.tiles = ModelManager.createInitiatedByDefaultTiles(instance.mapWidth, instance.mapHeight);
            instance.fireAllMapChanged();
        }

        @Override
        public void undo() {
            instance.tiles = oldTiles;
            instance.fireAllMapChanged();
        }
    }
}
