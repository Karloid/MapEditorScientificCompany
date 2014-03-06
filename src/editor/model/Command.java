package editor.model;

public interface Command {
    public void perform();
    public void undo();
}
