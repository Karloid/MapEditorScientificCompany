package editor.test;

import editor.model.ModelManager;
import editor.model.TileType;
import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

public class ModelManagerTests extends TestCase {
    public static void test_getBasicTileTypes() {
        ModelManager modelManager = ModelManager.createModelManagerTestInstance("tileTypes_small.json");
        List<TileType> basicTileTypes = modelManager.getBasicTileTypes();
        Assert.assertEquals(2, basicTileTypes.size());
        Assert.assertEquals("[id: 1, name: GRASS1, texture: grass1.png ][GRASS, COMMON]", basicTileTypes.get(0).toString());
        Assert.assertEquals("[id: 3, name: DIRT1, texture: dirt1.png ][DIRT, COMMON]", basicTileTypes.get(1).toString());
    }

    public static void test_getRelatedTileTypes() {
        ModelManager modelManager = ModelManager.createModelManagerTestInstance("tileTypes_small.json");
        List<TileType> basicTileTypes = modelManager.getBasicTileTypes();
        List<TileType> relatedTileTypes = modelManager.getRelatedTileTypes(basicTileTypes.get(1));
        Assert.assertEquals(2, relatedTileTypes.size());
        Assert.assertEquals("[id: 3, name: DIRT1, texture: dirt1.png ][DIRT, COMMON]", relatedTileTypes.get(0).toString());
        Assert.assertEquals("[id: 4, name: DIRT2, texture: dirt2.png ][DIRT]", relatedTileTypes.get(1).toString());
    }

    public static void test_getTileTypesWithTags_case1() {
        ModelManager modelManager = ModelManager.createModelManagerTestInstance("tileTypes_small.json");
        List<String> tags = new ArrayList<String>();
        tags.add("DIRT");
        List<TileType> relatedTileTypes = modelManager.getTileTypesWithTags(tags);
        Assert.assertEquals(2, relatedTileTypes.size());
        Assert.assertEquals("[id: 3, name: DIRT1, texture: dirt1.png ][DIRT, COMMON]", relatedTileTypes.get(0).toString());
        Assert.assertEquals("[id: 4, name: DIRT2, texture: dirt2.png ][DIRT]", relatedTileTypes.get(1).toString());
    }

    public static void test_getTileTypesWithTags_case2() {
        ModelManager modelManager = ModelManager.createModelManagerTestInstance("tileTypes_medium.json");
        List<String> tags = new ArrayList<String>();
        tags.add("GRASS");
        tags.add("DIRT");
        List<TileType> relatedTileTypes = modelManager.getTileTypesWithTags(tags);
        Assert.assertEquals(4, relatedTileTypes.size());
        Assert.assertEquals("[id: 5, name: DIRT_TO_GRASS_HOR1, texture: dirtToGrassHor1.png ][GRASS, DIRT]", relatedTileTypes.get(0).toString());
        Assert.assertEquals("[id: 6, name: DIRT_TO_GRASS_VER1, texture: dirtToGrassVert1.png ][GRASS, DIRT]", relatedTileTypes.get(1).toString());
        Assert.assertEquals("[id: 7, name: GRASS_TO_DIRT_HOR1, texture: grassToDirtHor1.png ][GRASS, DIRT]", relatedTileTypes.get(2).toString());
        Assert.assertEquals("[id: 8, name: GRASS_TO_DIRT_VER1, texture: grassToDirtVert1.png ][GRASS, DIRT]", relatedTileTypes.get(3).toString());
    }

    public static void test_openMapFromJson() {
        ModelManager modelManager = ModelManager.getInstance();
        modelManager.openMapFromJson("test_resources/littleMap.json");
        Assert.assertEquals(3, modelManager.getMapWidth());
        Assert.assertEquals(2, modelManager.getMapHeight());
        Assert.assertEquals("[[1,2][8,10][9,3]]", modelManager.getMapStringRepresentation());
    }

    public static void test_noTileTypesException() {
        try {
            ModelManager.createModelManagerTestInstance("tileTypes_empty.json");
            fail();
        } catch (RuntimeException e) { }
    }
}
