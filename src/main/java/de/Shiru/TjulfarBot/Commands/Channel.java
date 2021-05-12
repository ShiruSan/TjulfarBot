package de.Shiru.TjulfarBot.Commands;

import de.Shiru.TjulfarBot.BotMain;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;

public class Channel extends Command {

    public Channel(String name, Permission neededPermission) {
        super("channel", Permission.MANAGE_CHANNEL);
    }

    @Override
    public void onCommand(Member author, Message message, String[] args) {
        if(args.length == 0) {
            sendTut(message);
        } else if(args.length == 2) {
            if(args[0].equals("remove")) {
                switch(args[1]) {
                    case "welcomeChannel":
                        BotMain.getInstance().getSettings().welcomeChannel = -1;
                        message.getChannel().sendMessage(":white_check_mark: Welcome Channel entfernt !").queue();
                        break;
                    case "serverLog":
                        BotMain.getInstance().getSettings().serverLogChannel = -1;
                        message.getChannel().sendMessage(":white_check_mark: Server Log entfernt !").queue();
                        break;
                    case "announcement":
                        BotMain.getInstance().getSettings().announcementChannel = -1;
                        message.getChannel().sendMessage(":white_check_mark: Announcement entfernt !").queue();
                        break;
                    case "ruleChannel":
                        BotMain.getInstance().getSettings().ruleChannel = -1;
                        message.getChannel().sendMessage(":white_check_mark: Rule Channel entfernt !").queue();
                        break;
                    case "levelup":
                        BotMain.getInstance().getSettings().levelupChannel = -1;
                        message.getChannel().sendMessage(":white_check_mark: Level Up Channel entfernt !").queue();
                        break;
                    case "videoUpload":
                        BotMain.getInstance().getSettings().videoUploadChannel = -1;
                        message.getChannel().sendMessage(":white_check_mark: Video Upload Channel entfernt !").queue();
                        break;
                    default:
                        message.getChannel().sendMessage(":x: Diese Funktion existiert nicht !").queue();
                        break;
                }
            } else sendTut(message);
        } else if(args.length > 2) {
            if(args[0].equals("set")) {
                if(message.getMentionedChannels().size() == 1) {
                    TextChannel channel = message.getMentionedChannels().get(0);
                    switch(args[1]) {
                        case "welcomeChannel":
                            BotMain.getInstance().getSettings().welcomeChannel = channel.getIdLong();
                            message.getChannel().sendMessage(":white_check_mark: Welcome Channel gesetzt !").queue();
                            break;
                        case "serverLog":
                            BotMain.getInstance().getSettings().serverLogChannel = channel.getIdLong();
                            message.getChannel().sendMessage(":white_check_mark: Server Log gesetzt !").queue();
                            break;
                        case "announcement":
                            BotMain.getInstance().getSettings().announcementChannel = channel.getIdLong();
                            message.getChannel().sendMessage(":white_check_mark: Announcement gesetzt !").queue();
                            break;
                        case "ruleChannel":
                            BotMain.getInstance().getSettings().ruleChannel = channel.getIdLong();
                            message.getChannel().sendMessage(":white_check_mark: Rule Channel gesetzt !").queue();
                            break;
                        case "levelup":
                            BotMain.getInstance().getSettings().levelupChannel = channel.getIdLong();
                            message.getChannel().sendMessage(":white_check_mark: Level Up Channel gesetzt !").queue();
                            break;
                        case "videoUpload":
                            BotMain.getInstance().getSettings().videoUploadChannel = channel.getIdLong();
                            message.getChannel().sendMessage(":white_check_mark: Video Upload Channel gesetzt !").queue();
                            break;
                        default:
                            message.getChannel().sendMessage(":x: Diese Funktion existiert nicht !").queue();
                            break;
                    }
                } else sendTut(message);
            } else sendTut(message);
        } else sendTut(message);
    }

    private void sendTut(Message message) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Channel Command");
        builder.setColor(Color.BLUE);
        builder.setDescription("Mit dem `+channel` Command kannst du elementaren Funktionen, die einen Channel ben\u00F6tigen, einen hinzuf\u00FCgen.");
        builder.addField("Nutzung des Commands", "`+channel [Aktion] [Funktion] (Channel)`", false);
        builder.addField("Aktionen", "set, remove", false);
        builder.addField("Funktionen", "welcomeChannel, serverLog, announcement, ruleChannel, levelup, videoUpload", true);
        message.getChannel().sendMessage(builder.build()).queue();
    }

}
