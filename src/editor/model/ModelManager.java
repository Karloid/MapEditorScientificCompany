package editor.model;

import com.google.gson.Gson;
import editor.service.Utils;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.List;

public class ModelManager implements Configurable, CommandHandler {
    private int tileSizeInPixels = 32;
    private int[][] tiles;
    private int primaryMaterialID, secondaryMaterialID;
    private int mapWidth, mapHeight;
    private String mapAbsolutePath;
    private String currentConfig;
    private String configFileName;

    private boolean isSmartModeOn = false;

    private List<String> configurations;

    private Map<Integer, TileType> tileIdToTileTypeMap;
    private List<TileType> tileTypes;
    private List<Observer> observers;
    private List<Command> commandHistory;
    private final String configurationDirectory;

    private static ModelManager instance;

    private static final int MAP_DEFAULT_WIDTH_IN_TILES = 30;
    private static final int MAP_DEFAULT_HEIGHT_IN_TILES = 20;
    private static final String STANDARD_CONFIGURATION_DIR = "configurations";

    public static final int TOOL_IMAGE_ICON_SIZE = 32;
    public static final String MAP_PATH = "maps";
    public static final String IMAGE_PATH = "images";

    private ModelManager(String configurationDir, String configName, String tileTypeFileName) {
        mapAbsolutePath = "";
        configurationDirectory = configurationDir;
        configFileName = tileTypeFileName;
        configurations = readConfigurations();
        if (configurations.isEmpty())
            throw new RuntimeException("no configurations exists in \"" + configurationDirectory + "\"!");
        if (configName != null) {
            if (!configurations.contains(configName))
                throw new RuntimeException("no currentConfig \"" + configName +"\" exists in \"" + configurationDirectory + "\"!");
            currentConfig = configName;
        }
        else
            currentConfig = configurations.get(0);
        mapWidth = MAP_DEFAULT_WIDTH_IN_TILES;
        mapHeight = MAP_DEFAULT_HEIGHT_IN_TILES;
        observers = new ArrayList<Observer>();
        commandHistory = new ArrayList<Command>();
        tileIdToTileTypeMap = new HashMap<Integer, TileType>();
        tileTypes = getLoadedTileTypesFromJson(configurationDir + File.separator + currentConfig + File.separator + tileTypeFileName);
        tiles = createInitiatedByDefaultTiles(mapWidth, mapHeight);
    }

    public static ModelManager getInstance() {
        if (instance == null)
            instance = new ModelManager(STANDARD_CONFIGURATION_DIR, null, "tileTypes.json");
        return instance;
    }

    public Dimension getMapSize() {
        return new Dimension(getMapWidth(), getMapHeight());
    }

    public static ModelManager createModelManagerTestInstance(String jsonFileName) {
        return new ModelManager("test_resources" + File.separator +  "configurations", "test_config", jsonFileName);
    }

    public String getImageDirectoryName() {
        return configurationDirectory + File.separator + currentConfig;
    }

    @Override
    public void applyNewConfig(String newConfig) {
        if (!configurations.contains(newConfig))
            throw new RuntimeException("no currentConfig \"" + newConfig +"\" exists in \"" + configurationDirectory + "\"!");
        ModelManager oldInstance = instance;
        instance = new ModelManager(configurationDirectory, newConfig, configFileName);
        for (Observer o : observers)
            o.update(null, new ModelManagerUpdateInfo(ModelManagerUpdateType.NEW_MODEL, oldInstance));
    }

    @Override
    public String getCurrentConfig() {
        return currentConfig;
    }

    @Override
    public int getConfigCount() {
        return configurations.size();
    }

    @Override
    public String getConfigAt(int index) {
        return configurations.get(index);
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
            if (tile.get("wsw") != null)
                t.setNeighbourMaterial(TileType.Side.WSW, (String)tile.get("wsw"));
            if (tile.get("wnw") != null)
                t.setNeighbourMaterial(TileType.Side.WNW, (String)tile.get("wnw"));
            if (tile.get("nnw") != null)
                t.setNeighbourMaterial(TileType.Side.NNW, (String)tile.get("nnw"));
            if (tile.get("nne") != null)
                t.setNeighbourMaterial(TileType.Side.NNE, (String)tile.get("nne"));
            if (tile.get("ene") != null)
                t.setNeighbourMaterial(TileType.Side.ENE, (String)tile.get("ene"));
            if (tile.get("ese") != null)
                t.setNeighbourMaterial(TileType.Side.ESE, (String)tile.get("ese"));
            if (tile.get("sse") != null)
                t.setNeighbourMaterial(TileType.Side.SSE, (String)tile.get("sse"));
            if (tile.get("ssw") != null)
                t.setNeighbourMaterial(TileType.Side.SSW, (String)tile.get("ssw"));
            loadedTileTypes.add(t);
            tileIdToTileTypeMap.put(t.getId(), t);
        }
        if (loadedTileTypes.isEmpty())
            throw new RuntimeException("no tile types loaded :(");
        primaryMaterialID = secondaryMaterialID = loadedTileTypes.get(0).getId();
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

    public boolean isSmartModeOn() {
        return isSmartModeOn;
    }

    public void setSmartMode(boolean smartModeNewValue) {
        isSmartModeOn = smartModeNewValue;
    }

    public int getSecondaryMaterialID() {
        return secondaryMaterialID;
    }

    public void swapMaterials() {
        int tmp = secondaryMaterialID;
        setSecondaryMaterialID(primaryMaterialID);
        setPrimaryMaterialID(tmp);
    }

    public static enum ModelManagerUpdateType {MATERIAL_UPDATE, TILE_UPDATE, TOTAL_MAP_UPDATE, MAP_FILE_UPDATE, NEW_MODEL, COMMAND_LIST_CHANGED}

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

    public class SmartUpdateTileAtCommand implements Command {
        private int x, y;
        private int newMaterial;
        private MacroCommand macroCommand;

        public SmartUpdateTileAtCommand(int _x, int _y, int _newMaterial) {
            x = _x;
            y = _y;
            newMaterial = _newMaterial;
            macroCommand = new MacroCommand();
        }

        private Collection<TileType> getApplicableTileTypes(int _x, int _y) {
            TileType northNeighbour = null;
            if (_y - 1 >= 0)
                northNeighbour = ModelManager.this.tileIdToTileTypeMap.get(ModelManager.this.tiles[_x][_y - 1]);
            TileType southNeighbour = null;
            if (_y + 1 < ModelManager.this.mapHeight)
                southNeighbour = ModelManager.this.tileIdToTileTypeMap.get(ModelManager.this.tiles[_x][_y + 1]);
            TileType westNeighbour = null;
            if (_x - 1 >= 0)
                westNeighbour = ModelManager.this.tileIdToTileTypeMap.get(ModelManager.this.tiles[_x - 1][_y]);
            TileType eastNeighbour = null;
            if (_x + 1 < ModelManager.this.mapWidth)
                eastNeighbour = ModelManager.this.tileIdToTileTypeMap.get(ModelManager.this.tiles[_x + 1][_y]);
            Collection<TileType> intersection = new ArrayList<TileType>();
            for (TileType t : tileTypes)
                intersection.add(t);
            if (northNeighbour != null)
                intersection = northNeighbour.getApplicableTileTypes(intersection, TileType.Side.SSE, TileType.Side.SSW);
            if (southNeighbour != null)
                intersection = southNeighbour.getApplicableTileTypes(intersection, TileType.Side.NNW, TileType.Side.NNE);
            if (westNeighbour != null)
                intersection = westNeighbour.getApplicableTileTypes(intersection, TileType.Side.ENE, TileType.Side.ESE);
            if (eastNeighbour != null)
                intersection = eastNeighbour.getApplicableTileTypes(intersection, TileType.Side.WSW, TileType.Side.WNW);
            return intersection;
        }

        @Override
        public void perform() {
            macroCommand.addCommand(ModelManager.this.new UpdateTileAtCommand(x, y, newMaterial));

            TileType center = ModelManager.this.tileIdToTileTypeMap.get(ModelManager.this.tiles[x][y]);
            Collection<TileType> westNeighbours = center.getApplicableTileTypes(ModelManager.this.tileTypes, TileType.Side.WSW, TileType.Side.WNW);
            Collection<TileType> northNeighbours = center.getApplicableTileTypes(ModelManager.this.tileTypes, TileType.Side.NNW, TileType.Side.NNE);
            Collection<TileType> eastNeighbours = center.getApplicableTileTypes(ModelManager.this.tileTypes, TileType.Side.ENE, TileType.Side.ESE);
            Collection<TileType> southNeighbours = center.getApplicableTileTypes(ModelManager.this.tileTypes, TileType.Side.SSE, TileType.Side.SSW);

            if (y - 1 >= 0) {
                TileType northNeighbour = ModelManager.this.tileIdToTileTypeMap.get(ModelManager.this.tiles[x][y - 1]);
                if (!northNeighbour.isNeighbourTo(center, TileType.Side.SSE, TileType.Side.SSW)) {
                    if (y - 2 >= 0) {
                        TileType northTileType = ModelManager.this.tileIdToTileTypeMap.get(ModelManager.this.tiles[x][y - 2]);
                        Collection<TileType> applicableTileTypes = northTileType.getApplicableTileTypes(northNeighbours, TileType.Side.SSE, TileType.Side.SSW);
                        if (!applicableTileTypes.isEmpty())
                            macroCommand.addCommand(ModelManager.this.new UpdateTileAtCommand(x, y - 1, applicableTileTypes.iterator().next().getId()));
                    } else if (!northNeighbours.isEmpty())
                        macroCommand.addCommand(ModelManager.this.new UpdateTileAtCommand(x, y - 1, northNeighbours.iterator().next().getId()));
                }
            }
            if (y + 1 < ModelManager.this.mapHeight) {
                TileType southNeighbour = ModelManager.this.tileIdToTileTypeMap.get(ModelManager.this.tiles[x][y + 1]);
                if (!southNeighbour.isNeighbourTo(center, TileType.Side.NNW, TileType.Side.NNE)) {
                    if (y + 2 < ModelManager.this.mapHeight) {
                        TileType southTileType = ModelManager.this.tileIdToTileTypeMap.get(ModelManager.this.tiles[x][y + 2]);
                        Collection<TileType> applicableTileTypes = southTileType.getApplicableTileTypes(southNeighbours, TileType.Side.NNW, TileType.Side.NNE);
                        if (!applicableTileTypes.isEmpty())
                            macroCommand.addCommand(ModelManager.this.new UpdateTileAtCommand(x, y + 1, applicableTileTypes.iterator().next().getId()));
                    } else if (!southNeighbours.isEmpty())
                        macroCommand.addCommand(ModelManager.this.new UpdateTileAtCommand(x, y + 1, southNeighbours.iterator().next().getId()));
                }
            }
            if (x - 1 >= 0) {
                TileType westNeighbour = ModelManager.this.tileIdToTileTypeMap.get(ModelManager.this.tiles[x - 1][y]);
                if (!westNeighbour.isNeighbourTo(center, TileType.Side.ENE, TileType.Side.ESE)) {
                    if (x - 2 >= 0) {
                        TileType westTileType = ModelManager.this.tileIdToTileTypeMap.get(ModelManager.this.tiles[x - 2][y]);
                        Collection<TileType> applicableTileTypes = westTileType.getApplicableTileTypes(westNeighbours, TileType.Side.ENE, TileType.Side.ESE);
                        if (!applicableTileTypes.isEmpty())
                            macroCommand.addCommand(ModelManager.this.new UpdateTileAtCommand(x - 1, y, applicableTileTypes.iterator().next().getId()));
                    } else if (!westNeighbours.isEmpty())
                        macroCommand.addCommand(ModelManager.this.new UpdateTileAtCommand(x - 1, y, westNeighbours.iterator().next().getId()));
                }
            }
            if (x + 1 < ModelManager.this.mapWidth) {
                TileType eastNeighbour = ModelManager.this.tileIdToTileTypeMap.get(ModelManager.this.tiles[x + 1][y]);
                if (!eastNeighbour.isNeighbourTo(center, TileType.Side.WSW, TileType.Side.WNW)) {
                    if (x + 2 < ModelManager.this.mapWidth) {
                        TileType eastTileType = ModelManager.this.tileIdToTileTypeMap.get(ModelManager.this.tiles[x + 2][y]);
                        Collection<TileType> applicableTileTypes = eastTileType.getApplicableTileTypes(eastNeighbours, TileType.Side.WSW, TileType.Side.WNW);
                        if (!applicableTileTypes.isEmpty())
                            macroCommand.addCommand(ModelManager.this.new UpdateTileAtCommand(x + 1, y, applicableTileTypes.iterator().next().getId()));
                    } else if (!eastNeighbours.isEmpty())
                        macroCommand.addCommand(ModelManager.this.new UpdateTileAtCommand(x + 1, y, eastNeighbours.iterator().next().getId()));
                }
            }
            if (x - 1 >= 0 && y - 1 >= 0) {
                Collection<TileType> intersection = getApplicableTileTypes(x - 1, y - 1);
                if (!intersection.isEmpty() && !intersection.contains(ModelManager.this.tileIdToTileTypeMap.get(ModelManager.this.tiles[x - 1][y - 1])))
                    macroCommand.addCommand(ModelManager.this.new UpdateTileAtCommand(x - 1, y - 1, intersection.iterator().next().getId()));
            }
            if (x - 1 >= 0 && y + 1 < ModelManager.this.mapHeight) {
                Collection<TileType> intersection = getApplicableTileTypes(x - 1, y + 1);
                if (!intersection.isEmpty() && !intersection.contains(ModelManager.this.tileIdToTileTypeMap.get(ModelManager.this.tiles[x - 1][y + 1])))
                    macroCommand.addCommand(ModelManager.this.new UpdateTileAtCommand(x - 1, y + 1, intersection.iterator().next().getId()));
            }
            if (x + 1 < ModelManager.this.mapWidth && y - 1 >= 0) {
                Collection<TileType> intersection = getApplicableTileTypes(x + 1, y - 1);
                if (!intersection.isEmpty() && !intersection.contains(ModelManager.this.tileIdToTileTypeMap.get(ModelManager.this.tiles[x + 1][y - 1])))
                    macroCommand.addCommand(ModelManager.this.new UpdateTileAtCommand(x + 1, y - 1, intersection.iterator().next().getId()));
            }
            if (x + 1 < ModelManager.this.mapWidth && y + 1 < ModelManager.this.mapHeight) {
                Collection<TileType> intersection = getApplicableTileTypes(x + 1, y + 1);
                if (!intersection.isEmpty() && !intersection.contains(ModelManager.this.tileIdToTileTypeMap.get(ModelManager.this.tiles[x + 1][y + 1])))
                    macroCommand.addCommand(ModelManager.this.new UpdateTileAtCommand(x + 1, y + 1, intersection.iterator().next().getId()));
            }
        }


        @Override
        public void undo() {
            macroCommand.undo();
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
            final ModelManager mgr = ModelManager.this;
            oldMapWidth = mgr.mapWidth;
            oldMapHeight = mgr.mapHeight;
            oldTiles = mgr.tiles;
            setMapSize(newMapWidth, newMapHeight);
            mgr.fireAllMapChanged();
        }

        @Override
        public void undo() {
            final ModelManager mgr = ModelManager.this;
            setMapSize(oldMapWidth, oldMapHeight);
            mgr.tiles = oldTiles;
            mgr.fireAllMapChanged();
        }

        private void setMapSize(int x, int y) {
            int[][] newTiles = ModelManager.this.createInitiatedByDefaultTiles(x, y);
            for (int i = 0; i < Math.min(x, mapWidth); i++)
                for (int j = 0; j < Math.min(y, mapHeight); j++)
                    newTiles[i][j] = ModelManager.this.tiles[i][j];
            mapWidth = x;
            mapHeight = y;
            ModelManager.this.tiles = newTiles;
        }
    }

    public class ClearMapCommand implements Command {
        private int[][] oldTiles;

        public ClearMapCommand() { }

        @Override
        public void perform() {
            final ModelManager mgr = ModelManager.this;
            oldTiles = mgr.tiles;
            mgr.tiles = ModelManager.this.createInitiatedByDefaultTiles(mgr.mapWidth, mgr.mapHeight);
            mgr.fireAllMapChanged();
        }

        @Override
        public void undo() {
            final ModelManager mgr = ModelManager.this;
            mgr.tiles = oldTiles;
            mgr.fireAllMapChanged();
        }
    }
}