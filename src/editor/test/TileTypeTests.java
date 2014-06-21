package editor.test;

import editor.model.TileType;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Before;

import java.util.*;

public class TileTypeTests extends TestCase {
    private TileType tileType1, tileType2, tileType3;

    @Before
    public void setUp() {
        Collection<String> tags1 = new ArrayList<String>();
        tags1.add("TAG1");
        tileType1 = new TileType(1, "TILE_TYPE_1", "tileType1.png", tags1);
        Collection<String> tags2 = new ArrayList<String>();
        tags1.add("TAG2");
        tileType2 = new TileType(2, "TILE_TYPE_2", "tileType2.png", tags2);
        Collection<String> tags3 = new ArrayList<String>();
        tags1.add("TAG3");
        tileType3 = new TileType(3, "TILE_TYPE_3", "tileType3.png", tags3);
    }

    public void test_setAndGetNeighbourMaterialForOneSide() {
        Assert.assertNull(tileType1.getNeighbourMaterial(TileType.Side.WSW));
        tileType1.setNeighbourMaterial(TileType.Side.WSW, "GRASS");
        Assert.assertEquals("GRASS", tileType1.getNeighbourMaterial(TileType.Side.WSW));
    }

    public void test_setAllNeighbourMaterials() {
        Assert.assertNull(tileType1.getNeighbourMaterial(TileType.Side.WSW));
        Assert.assertNull(tileType1.getNeighbourMaterial(TileType.Side.WNW));
        Assert.assertNull(tileType1.getNeighbourMaterial(TileType.Side.NNW));
        Assert.assertNull(tileType1.getNeighbourMaterial(TileType.Side.NNE));
        Assert.assertNull(tileType1.getNeighbourMaterial(TileType.Side.ENE));
        Assert.assertNull(tileType1.getNeighbourMaterial(TileType.Side.ESE));
        Assert.assertNull(tileType1.getNeighbourMaterial(TileType.Side.SSE));
        Assert.assertNull(tileType1.getNeighbourMaterial(TileType.Side.SSW));
        tileType1.setNeighbourMaterial(null, "GRASS");
        Assert.assertEquals("GRASS", tileType1.getNeighbourMaterial(TileType.Side.WSW));
        Assert.assertEquals("GRASS", tileType1.getNeighbourMaterial(TileType.Side.WNW));
        Assert.assertEquals("GRASS", tileType1.getNeighbourMaterial(TileType.Side.NNW));
        Assert.assertEquals("GRASS", tileType1.getNeighbourMaterial(TileType.Side.NNE));
        Assert.assertEquals("GRASS", tileType1.getNeighbourMaterial(TileType.Side.ENE));
        Assert.assertEquals("GRASS", tileType1.getNeighbourMaterial(TileType.Side.ESE));
        Assert.assertEquals("GRASS", tileType1.getNeighbourMaterial(TileType.Side.SSE));
        Assert.assertEquals("GRASS", tileType1.getNeighbourMaterial(TileType.Side.SSW));
    }

    public void test_getNeighbourTileTypes() {
        tileType1.setNeighbourMaterial(TileType.Side.WSW, "GRASS");
        tileType1.setNeighbourMaterial(TileType.Side.WNW, "GRASS");
        tileType2.setNeighbourMaterial(TileType.Side.ESE, "GRASS");
        tileType2.setNeighbourMaterial(TileType.Side.ENE, "GRASS");
        tileType3.setNeighbourMaterial(TileType.Side.ESE, "GRASS");
        tileType3.setNeighbourMaterial(TileType.Side.ENE, "DIRT");
        Collection<TileType> tileTypesToCheck = new ArrayList<TileType>();
        tileTypesToCheck.add(tileType2);
        tileTypesToCheck.add(tileType3);
        Collection<TileType> applicableTileTypes = tileType1.getApplicableTileTypes(tileTypesToCheck, TileType.Side.WSW, TileType.Side.WNW);
        Assert.assertEquals(1, applicableTileTypes.size());
        Assert.assertEquals(tileType2, applicableTileTypes.iterator().next());
    }

    public void test_isNeighbourTo() {
        tileType1.setNeighbourMaterial(TileType.Side.WSW, "GRASS");
        tileType1.setNeighbourMaterial(TileType.Side.WNW, "GRASS");
        tileType2.setNeighbourMaterial(TileType.Side.ESE, "GRASS");
        tileType2.setNeighbourMaterial(TileType.Side.ENE, "GRASS");
        tileType3.setNeighbourMaterial(TileType.Side.ESE, "GRASS");
        tileType3.setNeighbourMaterial(TileType.Side.ENE, "DIRT");
        Assert.assertTrue(tileType1.isNeighbourTo(tileType2, TileType.Side.WSW, TileType.Side.WNW));
        Assert.assertFalse(tileType1.isNeighbourTo(tileType3, TileType.Side.WSW, TileType.Side.WNW));
        Assert.assertTrue(tileType2.isNeighbourTo(tileType1, TileType.Side.ESE, TileType.Side.ENE));
        Assert.assertFalse(tileType3.isNeighbourTo(tileType1, TileType.Side.ESE, TileType.Side.ENE));
    }

//    public void test_twoTileTypeMatches() {
//        tileType1.setNeighbourMaterial(TileType.Side.NNW, "GRASS");
//        tileType1.setNeighbourMaterial(TileType.Side.NNE, "GRASS");
//        tileType1.setNeighbourMaterial(TileType.Side.EEN, "GRASS");
//        tileType1.setNeighbourMaterial(TileType.Side.EES, "DIRT");
//    }
}
