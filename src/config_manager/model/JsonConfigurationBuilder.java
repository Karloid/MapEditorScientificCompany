package config_manager.model;

import editor.model.TileType;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class JsonConfigurationBuilder {
    private final Set<Integer> tileTypeIDs;
    private final String configuration;

    public JsonConfigurationBuilder(Collection<TileType> tileTypeCollection) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("\t\"tiles\" : [\n");
        tileTypeIDs = new HashSet<Integer>();
        for (TileType t : tileTypeCollection) {
            if (!tileTypeIDs.contains(t.getId()))
                tileTypeIDs.add(t.getId());
            else
                throw new RuntimeException("Tile type with not unique id=" + t.getId() + " fetched");
            sb.append(String.format("\t\t{\"id\" : %-4s \"name\" : %-33s \"texture\" : %-33s \"tags\" : [", t.getId() + ",", "\"" + t.getName() + "\",", "\"" + t.getTexture() + "\","));
            for (String tag : t.getTags()) {
                sb.append("\"");
                sb.append(tag);
                sb.append("\", ");
            }
            if (sb.lastIndexOf(", ") == sb.length() - 2)
                sb.delete(sb.length() - 2, sb.length());
            sb.append("]");
            sb.append("},\n");
        }
        if (sb.lastIndexOf(",") == sb.length() - 2)
            sb.deleteCharAt(sb.length() - 2);
        sb.append("\t]\n");
        sb.append("}");
        configuration = sb.toString();
    }

    public String getConfiguration() {
        return configuration;
    }
}
