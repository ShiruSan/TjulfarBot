package de.Shiru.TjulfarBot.Commands;

import de.Shiru.TjulfarBot.BotMain;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.util.Arrays;

public class Announcement extends Command {

    public Announcement(String name, Permission neededPermission) {
        super("announcement", Permission.MANAGE_SERVER);
    }

    @Override
    public void onCommand(Member author, Message message, String[] args) {
        if(args.length > 1) {
            try {
                int id = Integer.parseInt(args[0]);
                AnnouncementType type = AnnouncementType.getAnnouncementById(id);
                if(type != null) {
                    if(BotMain.getInstance().getSettings().announcementChannel != -1) {
                        TextChannel announcementChannel = message.getGuild().getTextChannelById(BotMain.getInstance().getSettings().announcementChannel);
                        StringBuilder builder = new StringBuilder();
                        for(int i = 1; i < args.length; i++) {
                            if((i + 1) != args.length) {
                                builder.append(args[i] + " ");
                            } else builder.append(args[i]);
                        }
                        String[] array = builder.toString().split("\u00A7");
                        String title = array[0];
                        String text = new String();
                        if(array.length > 2) text = String.join("\u00A7", Arrays.copyOfRange(array, 1, array.length));
                        else text = array[1];
                        EmbedBuilder builder1 = new EmbedBuilder();
                        builder1.setAuthor(type.title, message.getJDA().getSelfUser().getAvatarUrl(), message.getJDA().getSelfUser().getAvatarUrl());
                        builder1.setTitle(title);
                        builder1.setColor(type.announcementColor);
                        builder1.setDescription(text);
                        announcementChannel.sendMessage(message.getGuild().getPublicRole().getAsMention()).embed(builder1.build()).queue((success) -> {
                            message.getChannel().sendMessage(":white_check_mark: Neuigkeit veröffentlicht !").queue();
                        });
                    } else message.getChannel().sendMessage(":x: Der Announcement Channel wurde bisher noch nicht gesetzt !").queue();
                } else message.getChannel().sendMessage(":x: `" + args[0] + "` ist keine existierender Typ !").queue();
            } catch (NumberFormatException e) {
                message.getChannel().sendMessage(":x: `" + args[0] + "` ist keine valide Zahl !").queue();
            } catch (IndexOutOfBoundsException e) {
                message.getChannel().sendMessage(":x: Du hast die Titel/Nachricht Trennung nicht richtig gemacht !").queue();
            }
        } else sendTut(message);
    }

    private void sendTut(Message message) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Announcement Command");
        builder.setDescription("Dieser Command ist dazu da um eine sch\u00F6n aussehende Embed Nachricht in den gesetzten Announcement Channel zu schicken.");
        builder.addField("Nutzung des Command", "`+announcement [Announcement Typ] [Titel]\u00A7[Nachricht]`", false);
        builder.setColor(Color.BLUE);
        StringBuilder stringBuilder = new StringBuilder();
        for(AnnouncementType type : AnnouncementType.values()) {
            if(type.id != AnnouncementType.values().length) {
                stringBuilder.append("`" + type.id + "` \u279C " + type.title + " \n");
            } else stringBuilder.append("`" + type.id + "` \u279C " + type.title);
        }
        builder.addField("Announcement Typen", "Es gibt `" + AnnouncementType.values().length + "` verschiedene Arten an Typen.\nHier eine Auflistung:\n\n" + stringBuilder.toString(), true);
        message.getChannel().sendMessage(builder.build()).queue();
    }

    private enum AnnouncementType {

        INFO(1, "Info", Color.GREEN),
        INFO_FOR_DISCORD(2, "Info zum Discord Server", Color.getHSBColor(250.48f, 57.8f, 85.49f)),
        INFO_FOR_YOUTUBE(3, "Info zum Youtube Kanal", Color.RED),
        IMPORTANT_INFO(4,"Wichtige Info", Color.RED),
        IMPORTANT_INFO_FOR_DISCORD(5, "Wichtige Info zum Discord Server", Color.RED),
        IMPORTANT_INFO_FOR_YOUTUBE(6, "Wichtige Info zum Youtube Kanal", Color.RED);


        final int id;
        final String title;
        final Color announcementColor;
        private AnnouncementType(int id, String title, Color announcementColor) {
            this.id = id;
            this.title = title;
            this.announcementColor = announcementColor;
        }

        public static AnnouncementType getAnnouncementById(int id) {
            for (AnnouncementType type : AnnouncementType.values()) {
                if(type.id == id) return type;
            }
            return null;
        }

    }

}
