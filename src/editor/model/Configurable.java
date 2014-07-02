package editor.model;

public interface Configurable {
    public void applyNewConfig(String newConfig);
    public String getCurrentConfig();
    public int getConfigCount();
    public String getConfigAt(int index);
}
