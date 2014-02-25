package editor.model;

/**
 * Created by NOTADMIN on 25.02.14.
 */
public class UpdateTileAtCommand implements Command{
    private int x;
    private int y;
    private int oldMaterial;

    public UpdateTileAtCommand(int _x, int _y) {
        x = _x;
        y = _y;
    }

    @Override
    public void perform() {
        oldMaterial = ModelManager.getInstance().getTileAt(x, y);
        ModelManager.getInstance().updateTileAt(x, y);
    }

    @Override
    public void undo() {
        ModelManager modelManager = ModelManager.getInstance();
        int lastMaterial = modelManager.getCurrentSelectedMaterialID();
        modelManager.setCurrentSelectedMaterialID(oldMaterial);
        modelManager.updateTileAt(x, y);
        modelManager.setCurrentSelectedMaterialID(lastMaterial);
    }
}
