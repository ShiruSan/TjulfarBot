package de.Shiru.TjulfarBot.Commands;

import de.Shiru.TjulfarBot.BotMain;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Tempban extends Command {

    public Tempban(String name, Permission neededPermission) {
        super("tempban", Permission.BAN_MEMBERS);
    }

    @Override
    public void onCommand(Member author, Message message, String[] args) {
        if(args.length == 2) {
            try {
                int time = Integer.parseInt(args[0].substring(0, args[0].length() - 1));
                char timeunit = args[0].toCharArray()[args[0].toCharArray().length - 1];
                if(message.getMentionedMembers().size() == 1) {
                    if(timeunit == 'h') {
                        Member toBan = message.getMentionedMembers().get(0);
                        toBan.ban(0).queue((success) -> {
                            BotMain.getInstance().getManager().tempBan(toBan.getUser(), time, TimeUnit.HOURS);
                            message.getChannel().sendMessage(":white_check_mark: " + toBan.getAsMention() + " wurde für `" + time + "` Stunde(n) gebannt !").queue();
                        });
                    } else if(timeunit == 'd') {
                        Member toBan = message.getMentionedMembers().get(0);
                        toBan.ban(0).queue((success) -> {
                            BotMain.getInstance().getManager().tempBan(toBan.getUser(), time, TimeUnit.DAYS);
                            message.getChannel().sendMessage(":white_check_mark: " + toBan.getAsMention() + " wurde für `" + time + "` Tag(n) gebannt !").queue();
                        });
                    } else if(timeunit == 'y') {
                        Member toBan = message.getMentionedMembers().get(0);
                        toBan.ban(0).queue((success) -> {
                            BotMain.getInstance().getManager().tempBan(toBan.getUser(), time * 365, TimeUnit.DAYS);
                            message.getChannel().sendMessage(":white_check_mark: " + toBan.getAsMention() + " wurde für `" + time + "` Jahr(en) gebannt !").queue();
                        });
                    }
                } else sendTut(message);
            } catch (NumberFormatException e) {
                message.getChannel().sendMessage(":x: `" + args[0].substring(0, args[0].length() - 1) + " ist keine valide Zahl !").queue();
            }
        } else if(args.length > 2) {
            try {
                int time = Integer.parseInt(args[0].substring(0, args[0].length() - 1));
                char timeunit = args[0].toCharArray()[args[0].toCharArray().length - 1];
                if(message.getMentionedMembers().size() == 1) {
                    String reason = "";
                    for(int i = 2; i < args.length; i++) {
                        if((i + 1) == args.length) reason += args[i];
                        else reason += args[i] + " ";
                    }
                    if(timeunit == 'h') {
                        Member toBan = message.getMentionedMembers().get(0);
                        toBan.ban(0, reason).queue((success) -> {
                            BotMain.getInstance().getManager().tempBan(toBan.getUser(), time, TimeUnit.HOURS);
                            message.getChannel().sendMessage(":white_check_mark: " + toBan.getAsMention() + " wurde für `" + time + "` Stunde(n) gebannt !").queue();
                        });
                    } else if(timeunit == 'd') {
                        Member toBan = message.getMentionedMembers().get(0);
                        toBan.ban(0, reason).queue((success) -> {
                            BotMain.getInstance().getManager().tempBan(toBan.getUser(), time, TimeUnit.DAYS);
                            message.getChannel().sendMessage(":white_check_mark: " + toBan.getAsMention() + " wurde für `" + time + "` Tag(n) gebannt !").queue();
                        });
                    } else if(timeunit == 'y') {
                        Member toBan = message.getMentionedMembers().get(0);
                        toBan.ban(0, reason).queue((success) -> {
                            BotMain.getInstance().getManager().tempBan(toBan.getUser(), time * 365, TimeUnit.DAYS);
                            message.getChannel().sendMessage(":white_check_mark: " + toBan.getAsMention() + " wurde für `" + time + "` Jahr(en) gebannt !").queue();
                        });
                    }
                } else sendTut(message);
            } catch (NumberFormatException e) {
                message.getChannel().sendMessage(":x: `" + args[0].substring(0, args[0].length() - 1) + " ist keine valide Zahl !").queue();
            }
        } else sendTut(message);
    }

    private void sendTut(Message message) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Tempban Command");
        builder.setColor(Color.BLUE);
        builder.setDescription("Der Tempban Command bannt tempor\u00E4r Mitglieder vom Discord Server.");
        builder.addField("Nutzung des Commands", "`+tempban [Zeit] [Member] (Grund)`", false);
        List<String> lines = Arrays.asList("Die Zeitangabe kann nur in Stunden, Tage oder Jahre stattfinden !", "Die Zeit gibt man in folgenden Schema an:", "`1h, 1d oder 1y`");
        builder.addField("Info zur Zeitangabe", String.join("\n", lines), false);
        message.getChannel().sendMessage(builder.build()).queue();
    }

}
