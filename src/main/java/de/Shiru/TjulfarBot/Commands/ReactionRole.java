package de.Shiru.TjulfarBot.Commands;

import de.Shiru.TjulfarBot.Managers.ReactionManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;

public class ReactionRole extends Command {

    public ReactionRole(String name, Permission neededPermission) {
        super("reactionrole", Permission.MANAGE_CHANNEL);
    }

    @Override
    public void onCommand(Member author, Message message, String[] args) {
        if(args.length == 4) {
            if(message.getMentionedChannels().size() == 1) {
                TextChannel messageChannel = message.getMentionedChannels().get(0);
                try {
                    long messageid = Long.parseLong(args[1]);
                    messageChannel.retrieveMessageById(messageid).queue((reactionMessage) -> {
                        if(message.getMentionedRoles().size() == 1) {
                            Role role = message.getMentionedRoles().get(0);
                            String emoji = args[3];
                            if(!ReactionManager.get().containsReactionRole(messageid, emoji)) {
                                ReactionManager.ReactionRole reactionRole = ReactionManager.get().createReactionRole(reactionMessage, role, emoji);
                                reactionMessage.addReaction(emoji).queue((success) -> {
                                    ReactionManager.get().addReactionRole(reactionRole);
                                    message.getChannel().sendMessage(":white_check_mark: ReactionRole wurde erstellt !").queue();
                                });
                            } else message.getChannel().sendMessage(":x: Zu dieser Nachricht gibt es mit dem Emoji schon eine ReactionRole !").queue();
                        } else message.getChannel().sendMessage(":x: Du musst eine Role erw\u00E4hnen !").queue();
                    }, (fail) -> {
                        message.getChannel().sendMessage(":x: In diesem Channel gibt es keine Nachricht mit dieser ID !").queue();
                    });
                } catch (NumberFormatException e) {
                    message.getChannel().sendMessage(":x: `" + args[1] + "` ist keine valide Nachrichten ID !").queue();
                }
            } else message.getChannel().sendMessage(":x: Du musst einen Channel erw\u00E4hnen !").queue();
        } else sendTut(message);
    }

    private void sendTut(Message message) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.BLUE);
        builder.setTitle("reactionrole Command");
        builder.setDescription("Dieser Command ist daf\u00FCr da um ReactionRoles zu erstellen.");
        builder.addField("Nutzung des Command", "`+reactionrole #channel [Nachrichten ID] @Role [Reaction]`", false);
        builder.addField("Kleine Info", "Um eine Reaction Role zu entfernen, musst du die Reaction des Botes bei der jeweiligen Nachricht entfernen", false);
        message.getChannel().sendMessage(builder.build()).queue();
    }

}
