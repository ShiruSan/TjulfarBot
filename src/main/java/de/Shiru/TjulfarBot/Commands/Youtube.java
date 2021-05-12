package de.Shiru.TjulfarBot.Commands;

import de.Shiru.TjulfarBot.BotMain;
import de.Shiru.TjulfarBot.Youtube.YoutubeManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Youtube extends Command {

    public Youtube(String name, Permission neededPermission) {
        super("youtube", Permission.ADMINISTRATOR);
    }

    @Override
    public void onCommand(Member author, Message message, String[] args) {
        YoutubeManager manager = YoutubeManager.get();
        if(args.length == 1) {
            if(args[0].equals("channels")) {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setTitle("Alle Channels");
                builder.setColor(Color.BLUE);
                if(manager.getChannelIDs().size() != 0) {
                    List<String> lines = new ArrayList<>();
                    for(String id : manager.getChannelIDs()) {
                        lines.add("\u279C " + id + " (" + manager.getResponseItem(id).getSnippet().getChannelTitle() + ")");
                    }
                    builder.setDescription(String.join("\n", lines));
                } else builder.setDescription("Es sind noch keine Channels hinzugef\u0252gt worden !");
                message.getChannel().sendMessage(builder.build()).queue();
            } else sendTut(message);
        } else if(args.length == 3) {
            if(args[0].equals("channels")) {
                switch (args[1]) {
                    case "add":
                        if(!(manager.getChannelIDs().contains(args[2]))) {
                            if(manager.addChannelID(args[2])) {
                                message.getChannel().sendMessage(":white_check_mark: Die Channel ID wurde hinzugef\u0252gt !").queue();
                            } else message.getChannel().sendMessage(":x: Die Channel ID konnte aufgrund eines Fehlers nicht hinzugef\u0252gt werden !").queue();
                        } else message.getChannel().sendMessage(":x: Diese Channel ID wurde schon hinzugef\u0252gt !").queue();
                        break;
                    case "remove":
                        if(manager.getChannelIDs().contains(args[2])) {
                            if(manager.removeChannelID(args[2])) {
                                message.getChannel().sendMessage(":white_check_mark: Die Channel ID wurde entfernt !").queue();
                            } else message.getChannel().sendMessage(":x: Die Channel ID konnte aufgrund eines Fehlers nicht entfernt werden !").queue();
                        } else message.getChannel().sendMessage(":x: Diese Channel ID wurde nicht hinzugef\u0252gt !").queue();
                        break;
                    default:
                        sendTut(message);
                        break;
                }
            } else sendTut(message);
        } else sendTut(message);
    }

    private void sendTut(Message message) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Youtube Command");
        builder.setColor(Color.BLUE);
        builder.setDescription("Dieser Command ist daf\u0252r da um die Youtube Kanäle zu verwalten.");
        List<String> usements = Arrays.asList("+youtube channels", "+youtube channels add [Kanal-ID]", "+youtube channels remove [Kanal-ID]");
        builder.addField("Nutzung des Command", String.join("\n", usements), false);
        builder.addField("Weitere Infos", "Mit Kanal ID ist die von Youtube festgelegte ID eines Kanals, das man in der URL des Kanals vorfindet.", false);
        message.getChannel().sendMessage(builder.build()).queue();
    }

}
