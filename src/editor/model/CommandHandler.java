package editor.model;

public interface CommandHandler {
    public void performCommand(Command command);
    public void undoLastCommand();
    public void clearCommandHistory();
    public int getCommandHistorySize();
}
