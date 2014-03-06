package editor.model.command;

public interface Command {
    public void perform();
    public void undo();
}
