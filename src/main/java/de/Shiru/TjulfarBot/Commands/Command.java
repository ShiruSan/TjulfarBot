package de.Shiru.TjulfarBot.Commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

public abstract class Command {
    public String name;
    public Permission neededPermission;

    public Command(String name, Permission neededPermission) {
        this.neededPermission = neededPermission;
        this.name = name;
    }

    public abstract void onCommand(Member author, Message message, String[] args);

}
