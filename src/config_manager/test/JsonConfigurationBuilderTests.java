package config_manager.test;

import config_manager.model.JsonConfigurationBuilder;
import editor.model.TileType;
import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collection;

public class JsonConfigurationBuilderTests extends TestCase {

    public void test_getConfiguration() {
        Collection<String> tags1 = new ArrayList<String>();
        tags1.add("GRASS");
        tags1.add("COMMON");
        TileType tileType1 = new TileType(1, "TILE_TYPE_1_NAME", "tileType1_texture.png", tags1);
        Collection<String> tags2 = new ArrayList<String>();
        tags2.add("DIRT");
        tags2.add("GRASS");
        tags2.add("COMMON");
        TileType tileType2 = new TileType(2, "TILE_TYPE_2", "tileType2.png", tags2);
        Collection<TileType> tileTypeCollection = new ArrayList<TileType>();
        tileTypeCollection.add(tileType1);
        tileTypeCollection.add(tileType2);
        JsonConfigurationBuilder jsonConfigurationBuilder = new JsonConfigurationBuilder(tileTypeCollection);
        String expectedValue = "{" + "\n" +
                "\t" + "\"tiles\" : [" + "\n" +
                "\t" + "\t" + "{\"id\" : 1,   \"name\" : \"TILE_TYPE_1_NAME\",               \"texture\" : \"tileType1_texture.png\",          \"tags\" : [\"GRASS\", \"COMMON\"]}," + "\n" +
                "\t" + "\t" + "{\"id\" : 2,   \"name\" : \"TILE_TYPE_2\",                    \"texture\" : \"tileType2.png\",                  \"tags\" : [\"DIRT\", \"GRASS\", \"COMMON\"]}" + "\n" +
                "\t" + "]" + "\n" +
                "}";
        Assert.assertEquals(expectedValue, jsonConfigurationBuilder.getConfiguration());
    }

    public void test_throwExceptionWhenIdAreNotUnique() {
        Collection<String> tags1 = new ArrayList<String>();
        tags1.add("GRASS");
        TileType tileType1 = new TileType(1, "TILE_TYPE_1", "tileType1.png", tags1);
        Collection<String> tags2 = new ArrayList<String>();
        tags2.add("DIRT");
        TileType tileType2 = new TileType(1, "TILE_TYPE_2", "tileType2.png", tags2);
        Collection<TileType> tileTypeCollection = new ArrayList<TileType>();
        tileTypeCollection.add(tileType1);
        tileTypeCollection.add(tileType2);
        try {
            new JsonConfigurationBuilder(tileTypeCollection);
            fail();
        }
        catch (RuntimeException e) {
            Assert.assertEquals("Tile type with not unique id=1 fetched", e.getMessage());
        }
    }

}
