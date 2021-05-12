package de.Shiru.TjulfarBot.Managers;

import de.Shiru.TjulfarBot.Commands.*;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.util.ArrayList;
import java.util.List;

public class CommandManager {
    public List<Command> commands = new ArrayList<>();

    public CommandManager() {
        commands.add(new About(null,null));
        commands.add(new Announcement(null, null));
        commands.add(new Channel(null, null));
        commands.add(new Clear(null, null));
        commands.add(new Level(null, null));
        commands.add(new Mute(null, null));
        commands.add(new ReactionRole(null, null));
        commands.add(new Tempban(null, null));
        commands.add(new Unmute(null, null));
        commands.add(new Youtube(null, null));
    }

}
