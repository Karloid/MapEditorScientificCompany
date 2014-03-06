package editor.model.command;

import java.util.ArrayList;
import java.util.List;

public class MacroCommand implements Command {
    private List<Command> commands;

    public MacroCommand() {
        commands = new ArrayList<Command>();
    }

    public void addCommand(Command c) {
        commands.add(c);
        c.perform();
    }

    public boolean isEmpty() {
        return commands.isEmpty();
    }

    @Override
    public void perform() {

    }

    @Override
    public void undo() {
        for (int i = commands.size() - 1; i > -1; i--) {
            commands.get(i).undo();
            commands.remove(i);
        }
    }
}
