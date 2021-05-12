package de.Shiru.TjulfarBot.Commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class Clear extends Command {

    public Clear(String name, Permission neededPermission) {
        super("clear", Permission.MESSAGE_MANAGE);
    }

    @Override
    public void onCommand(Member author, Message message, String[] args) {
        if (args.length == 1) {
            try {
                int count = Integer.parseInt(args[0]);
                if(count > 100) {
                    message.getChannel().sendMessage(":x: `" + count + "` ist eine zu hohe Zahl\nDer Limit liegt bei 100 !").queue();
                } else if (count <= 0) {
                    message.getChannel().sendMessage(":x: `" + count + "` ist eine zu geringe Zahl\nDas Minmum leigt bei 1 !").queue();
                } else {
                    List<Message> messages = message.getChannel().getHistory().retrievePast(count).complete();
                    new ClearThread(messages, message.getTextChannel()).start();
                }
            } catch (NumberFormatException e) {
                message.getChannel().sendMessage(":x: `" + args[0] + "` ist keine valide Zahl !").queue();
            }
        } else sendTut(message);
    }

    private void sendTut(Message message) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Clear Command");
        builder.setDescription("Der Clear Command ist dafür da um eine bestimmte Anzahl letzter Nachrichten zu l\u00F6schen.");
        builder.addField("Nutzung des Commands", "`+clear [Anzahl letzter Nachrichten]`", false);
        builder.addField("Kleiner Hinweis", "Das Limit an Nachrichten liegt bei 100!\nDie Nutzung diesen Commandes im selben Channel, bevor das vorherige Clearen zu Ende ist, kann Bot Fehler ausl\u00F6sen !", false);
        message.getChannel().sendMessage(builder.build()).queue();
    }

    private class ClearThread extends Thread {
        private List<Message> messagesToDelete;
        private TextChannel successfulMessageChannel;

        public ClearThread(List<Message> messages, TextChannel successfulMessageChannel) {
            messagesToDelete = messages;
            this.successfulMessageChannel = successfulMessageChannel;
        }

        @Override
        public void run() {
            if(messagesToDelete.size() == 1) messagesToDelete.get(0).delete().complete();
            else successfulMessageChannel.deleteMessages(messagesToDelete).complete();
            successfulMessageChannel.sendMessage(":white_check_mark: `" + messagesToDelete.size() + "` Nachricht(en) wurde gel\u00F6scht !").queue((message) -> {
                message.delete().queueAfter(3, TimeUnit.SECONDS);
            });
        }

    }

}