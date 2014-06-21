package config_manager.model;

import com.google.gson.Gson;
import editor.model.*;
import editor.service.Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class ModelManager implements CommandHandler {
    private int tileSizeInPixels = 32;
    private int[][] tiles;
    private int mapWidth, mapHeight;
    private String mapAbsolutePath;
    private String currentConfig;

    private Map<Integer, TileType> tileIdToTileTypeMap;
    private List<TileType> tileTypes;
//    private Map<Integer, String> tileIdToTileTextureMap;
    private List<Observer> observers;
    private List<Command> commandHistory;
    private final String configurationDirectory;

    private static ModelManager instance;

    private static final int MAP_DEFAULT_WIDTH_IN_TILES = 30;

    private static final int MAP_DEFAULT_HEIGHT_IN_TILES = 20;
    private static final String STANDARD_CONFIGURATION_DIR = "configurations";
    public static final int TOOL_IMAGE_ICON_SIZE = 32;

    private ModelManager(String configurationDir, String configName, String tileTypeFileName) {
        mapAbsolutePath = "";
        configurationDirectory = configurationDir;
        mapWidth = MAP_DEFAULT_WIDTH_IN_TILES;
        mapHeight = MAP_DEFAULT_HEIGHT_IN_TILES;
        observers = new ArrayList<Observer>();
        commandHistory = new ArrayList<Command>();
        tileIdToTileTypeMap = new HashMap<Integer, TileType>();
//        tileIdToTileTextureMap = new HashMap<Integer, String>();
        tileTypes = getLoadedTileTypesFromJson(configurationDir + File.separator + currentConfig + File.separator + tileTypeFileName);
        tiles = createInitiatedByDefaultTiles(mapWidth, mapHeight);
    }

    public static ModelManager getInstance() {
        if (instance == null)
            instance = new ModelManager(STANDARD_CONFIGURATION_DIR, null, "tileTypes.json");
        return instance;
    }

    public String getImageDirectoryName() {
        return configurationDirectory + File.separator + currentConfig;
    }

    private List<String> readConfigurations() {
        List<String> configs = new ArrayList<String>();
        File root = new File(configurationDirectory);
        for (File f : root.listFiles()) {
            if (f.isDirectory())
                configs.add(f.getName());
        }
        return configs;
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

    private void fireCommandListChanged() {
        for (Observer o : observers)
            o.update(null, new ModelManagerUpdateInfo(ModelManagerUpdateType.COMMAND_LIST_CHANGED, null));
    }

    private void fireMaterialChanged() {
        for (Observer o : observers)
            o.update(null, new ModelManagerUpdateInfo(ModelManagerUpdateType.MATERIAL_UPDATE, null));
    }

    private void fireMapFileChanged() {
        for (Observer o : observers)
            o.update(null, new ModelManagerUpdateInfo(ModelManagerUpdateType.MAP_FILE_UPDATE, null));
    }

    public String getMapAbsolutePath() {
        return mapAbsolutePath;
    }

    public String getTextureForTileID(int tileID) {
        return tileIdToTileTypeMap.get(tileID).getTexture();
    }

    private List<TileType> getLoadedTileTypesFromJson(String jsonFileName) {
        List<TileType> loadedTileTypes = new ArrayList<TileType>();
        String jsonString = Utils.readFile(jsonFileName);
        Map root = new Gson().fromJson(jsonString, Map.class);
        List<Map<String, Object>> tiles = (List<Map<String, Object>>) root.get("tiles");
        for (Map<String, Object> tile : tiles) {
            TileType t = new TileType((int) Math.round((Double) tile.get("id")),
                    (String) tile.get("name"), (String) tile.get("texture"), (Collection<String>) tile.get("tags"));
            loadedTileTypes.add(t);
//            Collection<Double> neighbours = (Collection<Double>)tile.get("west_neighbours");
//            if (neighbours != null)
//                t.copyWestNeighbsFrom(neighbours);
//            neighbours = (Collection<Double>)tile.get("north_neighbours");
//            if (neighbours != null)
//                t.copyNorthNeighbsFrom(neighbours);
//            neighbours = (Collection<Double>)tile.get("east_neighbours");
//            if (neighbours != null)
//                t.copyEastNeighbsFrom(neighbours);
//            neighbours = (Collection<Double>)tile.get("south_neighbours");
//            if (neighbours != null)
//                t.copySouthNeighbsFrom(neighbours);
            tileIdToTileTypeMap.put(t.getId(), t);
        }
        if (loadedTileTypes.isEmpty())
            throw new RuntimeException("no tile types loaded :(");
        return loadedTileTypes;
    }

    private int[][] createInitiatedByDefaultTiles(int x, int y) {
        int[][] newTiles = new int[x][y];
        for (int i = 0; i < x; i++)
            for (int j = 0; j < y; j++)
                if (Math.random() > 0.5f)
                    newTiles[i][j] = tileTypes.get(0).getId();
                else
                    newTiles[i][j] = tileTypes.get(1).getId();
        return newTiles;
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
        } else if (tileSizeInPixels > 3) {
            tileSizeInPixels -= value;
            return true;
        }
        return false;
    }

    public int getTileAt(int x, int y) {
        return tiles[x][y];
    }

    public Iterator<TileType> getIteratorOfAllTileTypes() {
        return tileTypes.iterator();
    }

    public void saveMapAsJsonAtCurrentFile() {
        saveMapAsJson(new File(mapAbsolutePath));
    }

    public void saveMapAsJson(File file) {
        try {
            Writer w = new FileWriter(file);
            w.append('{');
            w.append('\n');
            w.append("\"width\" : ");
            w.append(String.valueOf(mapWidth));
            w.append(",\n");
            w.append("\"height\" : ");
            w.append(String.valueOf(mapHeight));
            w.append(",\n");
            w.append("\"tiles\" : ");
            w.append(new Gson().toJson(tiles).replace("],[", "],\n\t["));
            w.append('\n');
            w.append('}');
            w.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mapAbsolutePath = file.getAbsolutePath();
        fireMapFileChanged();
    }

    public List<TileType> getBasicTileTypes() {
        List<TileType> basicTileTypes = new ArrayList<TileType>();
        for (TileType t : tileTypes) {
            if (t.getTags().contains("COMMON"))
                basicTileTypes.add(t);
        }
        return basicTileTypes;
    }

    public List<TileType> getRelatedTileTypes(TileType tileType) {
        List<TileType> relatedTileTypes = new ArrayList<TileType>();
        for (TileType t : tileTypes) {
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
            for (TileType tileType : tileTypes)
                if (tileType.getTags().containsAll(commonTags))
                    tileTypesWithTags.add(tileType);
        }
        return tileTypesWithTags;
    }

    public void openMapFromJson(String fileName) {
        String jsonString = Utils.readFile(fileName);
        Map root = new Gson().fromJson(jsonString, Map.class);
        int newMapWidth = ((Double)root.get("width")).intValue();
        int newMapHeight = ((Double)root.get("height")).intValue();
        List<List<Double>> s = (List<List<Double>>)root.get("tiles");
        int[][] newTiles = new int[newMapWidth][newMapHeight];
        for (int i = 0; i < newMapWidth; i++) {
            for (int j = 0; j < newMapHeight; j++) {
                int tileTypeID = s.get(i).get(j).intValue();
                if (!tileIdToTileTypeMap.containsKey(tileTypeID))
                    throw new RuntimeException("No such tile type with ID=" + tileTypeID + " exists in currentConfig \"" + currentConfig + "\"!");
                newTiles[i][j] = tileTypeID;
            }
        }
        mapAbsolutePath = fileName;
        mapWidth = newMapWidth;
        mapHeight = newMapHeight;
        tiles = newTiles;
        fireAllMapChanged();
        fireMapFileChanged();
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

    @Override
    public void performCommand(Command command) {
        command.perform();
        commandHistory.add(command);
        fireCommandListChanged();
    }

    @Override
    public void undoLastCommand() {
        if (commandHistory.size() > 0) {
            commandHistory.get(commandHistory.size() - 1).undo();
            commandHistory.remove(commandHistory.size() - 1);
            fireCommandListChanged();
        }
    }

    @Override
    public void clearCommandHistory() {
        commandHistory.clear();
        fireCommandListChanged();
    }

    @Override
    public int getCommandHistorySize() {
        return commandHistory.size();
    }

    public static enum ModelManagerUpdateType {MATERIAL_UPDATE, TILE_UPDATE, TOTAL_MAP_UPDATE, MAP_FILE_UPDATE, NEW_MODEL, COMMAND_LIST_CHANGED;}

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
}