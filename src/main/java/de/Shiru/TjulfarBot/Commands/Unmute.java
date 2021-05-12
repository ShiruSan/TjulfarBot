package de.Shiru.TjulfarBot.Commands;

import de.Shiru.TjulfarBot.Events.UnmuteEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

public class Unmute extends Command {

    public Unmute(String name, Permission neededPermission) {
        super("unmute", Permission.VOICE_MUTE_OTHERS);
    }

    @Override
    public void onCommand(Member author, Message message, String[] args) {
        if(args.length == 1) {
            if(message.getMentionedMembers().size() == 1) {
                Member toUnmute = message.getMentionedMembers().get(0);
                if(Mute.containsMute(toUnmute.getIdLong())) {
                    Mute.MutedMember mutedMember = null;
                    for(Mute.MutedMember mutedMember2 : Mute.mutedMembers) {
                        if(mutedMember2.member.getIdLong() == toUnmute.getIdLong()) {
                            mutedMember = mutedMember2;
                            break;
                        }
                    }
                    if(mutedMember != null) {
                        mutedMember.purge();
                        message.getChannel().sendMessage(":white_check_mark: " + toUnmute.getAsMention() + " wurde entmuted !").queue();
                        message.getJDA().getEventManager().handle(new UnmuteEvent(author, toUnmute));
                    } else message.getChannel().sendMessage(":x: Kritischer Fehler beim Mute Management !").queue();
                } else message.getChannel().sendMessage(":x: Dieser Member ist nicht gemuted !").queue();
            } else sendTut(message);
        } else sendTut(message);
    }

    private void sendTut(Message message) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Unmute Command");
        builder.setDescription("Der Unmute Command ist daf\u0252r gedacht um den Mute von Mitgliedern aufzuheben.");
        builder.addField("Nutzung des Commands", "`+unmute [Mitglied]`", false);
        message.getChannel().sendMessage(builder.build()).queue();
    }

}
