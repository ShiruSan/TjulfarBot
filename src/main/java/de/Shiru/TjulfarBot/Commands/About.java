package de.Shiru.TjulfarBot.Commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import de.Shiru.TjulfarBot.Listener;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class About extends Command {

    public About(String name, Permission neededPermission) {
        super("about", null);
    }

    @Override
    public void onCommand(Member author, Message message, String[] args) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("\u00dcber den Tjulfar Bot");
        builder.setThumbnail(message.getJDA().getSelfUser().getAvatarUrl());
        StringBuilder builder1 = new StringBuilder();
        builder1.append("Dieser Discord Bot ist, wie der Name schon sagt, f\u00FCr Tjulfars Discord Server.\n");
        builder1.append("Der Bot wurde als kleines Fanprojekt von <@370553641263955970> gestartet und wird weiterentwickelt !\n");
        builder1.append("Der Geburtstag dieses Botes ist der 29.03.2021.\n");
        builder1.append("Au\u00DFerdem findest du hier eine Auflistung aller Commands.");
        builder.setDescription(builder1.toString());
        builder.setColor(Color.GREEN);
        builder.setFooter("Du m\u00F6chtest wissen wie ich funktioniere ? [ShiruSan/TjulfarBot](https://github.com/ShiruSan/TjulfarBot)");
        List<String> commands = new ArrayList<>();
        for(Command command : Listener.manager.commands) commands.add("  \u2022  " + command.name);
        builder.addField("Alle Commands", String.join("\n", commands), false);
        message.getChannel().sendMessage(builder.build()).queue();
    }

}
