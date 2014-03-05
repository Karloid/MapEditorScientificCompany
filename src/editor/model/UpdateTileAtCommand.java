package editor.model;

public class UpdateTileAtCommand implements Command{
    private int x;
    private int y;
    private int oldMaterial;
    private int newMaterial;

    public UpdateTileAtCommand(int _x, int _y, int _newMaterial) {
        x = _x;
        y = _y;
        newMaterial = _newMaterial;
    }

    @Override
    public void perform() {
        oldMaterial = ModelManager.getInstance().getTileAt(x, y);
        ModelManager.getInstance().updateTileAtWith(x, y, newMaterial);
    }

    @Override
    public void undo() {
        ModelManager.getInstance().updateTileAtWith(x, y, oldMaterial);
    }
}
