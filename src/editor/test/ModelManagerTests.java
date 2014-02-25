package editor.test;

import editor.model.ModelManager;
import editor.model.TileType;
import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.ArrayList;

public class ModelManagerTests extends TestCase {
    public static void test_getBasicTileTypes() {
        ModelManager modelManager = ModelManager.createModelManagerTestInstance("test_resources/tileTypes.json");
        ArrayList<TileType> basicTileTypes = modelManager.getBasicTileTypes();
        Assert.assertEquals(2, basicTileTypes.size());
        Assert.assertEquals("[id: 1, name: GRASS1, texture: grass1.png ][GRASS, COMMON]", basicTileTypes.get(0).toString());
        Assert.assertEquals("[id: 3, name: DIRT1, texture: dirt1.png ][DIRT, COMMON]", basicTileTypes.get(1).toString());
    }

    public static void test_getRelatedTileTypes() {
        ModelManager modelManager = ModelManager.createModelManagerTestInstance("test_resources/tileTypes.json");
        ArrayList<TileType> basicTileTypes = modelManager.getBasicTileTypes();
        ArrayList<TileType> relatedTileTypes = modelManager.getRelatedTileTypes(basicTileTypes.get(1));
        Assert.assertEquals(2, relatedTileTypes.size());
        Assert.assertEquals("[id: 3, name: DIRT1, texture: dirt1.png ][DIRT, COMMON]", relatedTileTypes.get(0).toString());
        Assert.assertEquals("[id: 4, name: DIRT2, texture: dirt2.png ][DIRT]", relatedTileTypes.get(1).toString());
    }

    public static void test_openMapFromJson() {
        ModelManager modelManager = ModelManager.getInstance();
        modelManager.openMapFromJson("test_resources/littleMap.json");
        Assert.assertEquals(3, modelManager.getMapWidth());
        Assert.assertEquals(2, modelManager.getMapHeight());
        Assert.assertEquals("[[1,2][8,10][9,3]]", modelManager.getMapStringRepresentation());
    }

}
