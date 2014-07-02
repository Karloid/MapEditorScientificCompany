package editor.model;

import java.util.*;

public class TileType {

    private final int id;
    private final String name;
    private final String texture;
    private final Collection<String> tags;

    private Map<Side, String> neighbourMaterials;

    public TileType(int id, String name, String texture, Collection<String> tags) {
        this.id = id;
        this.name = name;
        this.texture = texture;
        this.tags = tags;

        neighbourMaterials = new EnumMap<Side, String>(Side.class);
    }

    public Collection<String> getTags() {
        return tags;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTexture() {
        return texture;
    }

    @Override
    public String toString() {
        return "[id: " + id + ", name: " + name + ", texture: " + texture + " ]" + tags.toString();
    }

    public String tooltipText() {
        return getName() + " (ID: " + getId() + "; " + getTexture() + ")";
    }

    public void setNeighbourMaterial(Side side, String newMaterial) {
        if (side != null)
            neighbourMaterials.put(side, newMaterial);
        else {
            setNeighbourMaterial(Side.WSW, newMaterial);
            setNeighbourMaterial(Side.WNW, newMaterial);
            setNeighbourMaterial(Side.NNW, newMaterial);
            setNeighbourMaterial(Side.NNE, newMaterial);
            setNeighbourMaterial(Side.ENE, newMaterial);
            setNeighbourMaterial(Side.ESE, newMaterial);
            setNeighbourMaterial(Side.SSE, newMaterial);
            setNeighbourMaterial(Side.SSW, newMaterial);
        }
    }

    public String getNeighbourMaterial(Side side) {
        return neighbourMaterials.get(side);
    }

    public Collection<TileType> getApplicableTileTypes(Collection<TileType> tileTypesToCheck, Side ... sides) {
        Collection<TileType> applicableTileTypes = new ArrayList<TileType>();
        for (TileType t : tileTypesToCheck) {
            if (isNeighbourTo(t, sides))
                applicableTileTypes.add(t);
        }
        return applicableTileTypes;
    }

    public boolean isNeighbourTo(TileType tileType, Side ... sides) {
        if (sides == null || sides.length == 0)
            return false;
        for (Side s : sides) {
            if (neighbourMaterials.get(s) == null || tileType.neighbourMaterials.get(s.getOppositeSide()) == null || !neighbourMaterials.get(s).equals(tileType.neighbourMaterials.get(s.getOppositeSide())))
                return false;
        }
        return true;
    }

    public static enum Side {
        WSW, WNW, NNW, NNE, ENE, ESE, SSE, SSW;

        public Side getOppositeSide() {
            switch (this) {
                case WSW:
                    return ESE;
                case WNW:
                    return ENE;
                case NNW:
                    return SSW;
                case NNE:
                    return SSE;
                case ENE:
                    return WNW;
                case ESE:
                    return WSW;
                case SSE:
                    return NNE;
                case SSW:
                    return NNW;
            }
            return null;
        }
    }
}