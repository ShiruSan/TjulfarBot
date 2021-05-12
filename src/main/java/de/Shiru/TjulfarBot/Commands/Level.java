package de.Shiru.TjulfarBot.Commands;

import de.Shiru.TjulfarBot.Listener;
import de.Shiru.TjulfarBot.Managers.LevelManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;

public class Level extends Command {

    public Level(String name, Permission neededPermission) {
        super("level", null);
    }

    @Override
    public void onCommand(Member author, Message message, String[] args) {
        LevelManager levelManager = ((Listener) author.getJDA().getEventManager().getRegisteredListeners().get(0)).levelManager;
        if(args.length == 0) {
            if(levelManager.existsProfile(author.getIdLong())) {
                message.getChannel().sendMessage(levelManager.createEmbedProfile(levelManager.getProfileByID(author.getIdLong()), author)).queue();
            } else message.getChannel().sendMessage(levelManager.createEmbedProfile(levelManager.createProfile(author.getIdLong()), author)).queue();
        } else if(args.length == 1) {
            if(args[0].equals("help")) { sendTut(message); return; }
            if(message.getMentionedMembers().size() != 1) { sendTut(message); return; }
            Member member = message.getMentionedMembers().get(0);
            if(levelManager.existsProfile(member.getIdLong())) {
                message.getChannel().sendMessage(levelManager.createEmbedProfile(levelManager.getProfileByID(member.getIdLong()), member)).queue();
            } else message.getChannel().sendMessage(levelManager.createEmbedProfile(levelManager.createProfile(member.getIdLong()), member)).queue();
        } else if(args.length == 2) {
            if(!(args[0].equals("blacklist"))) { sendTut(message); return; }
            if(author.hasPermission(Permission.VOICE_MUTE_OTHERS)) {
                if(message.getMentionedMembers().size() != 1) { sendTut(message); return; }
                Member member = message.getMentionedMembers().get(0);
                if(levelManager.blackListedMembers.contains(member.getIdLong())) {
                    levelManager.removeBlacklist(member.getIdLong());
                    message.getChannel().sendMessage(":white_check_mark: " + member.getAsMention() + " wurde von der Blacklist entfernt und kann nun wieder neue Levels erreichen !").queue();
                } else {
                    levelManager.addBlacklist(member.getIdLong());
                    message.getChannel().sendMessage(":white_check_mark: " + member.getAsMention() + " wurde geblacklisted und kriegt kein Levelaufstieg !").queue();
                }
            } else message.getChannel().sendMessage(":x: Dazu hast du nicht die Rechte !").queue();
        } else if(args.length == 3) {
            if(!args[0].equals("channels")) { sendTut(message); return; }
            if(message.getMentionedChannels().size() != 1) { sendTut(message); return; }
            TextChannel forAction = message.getMentionedChannels().get(0);
            if(args[1].equals("add")) {
                if(!levelManager.levelChannels.contains(forAction.getIdLong())) {
                    levelManager.addLevelChannel(forAction.getIdLong());
                    message.getChannel().sendMessage(":white_check_mark: In" + forAction.getAsMention() + " kann man nun im Level aufsteigen !").queue();
                } else message.getChannel().sendMessage(":x: In" + forAction.getAsMention() + " kann man schon im Level aufsteigen !").queue();
            } else if(args[1].equals("remove")) {
                if(levelManager.levelChannels.contains(forAction.getIdLong())) {
                    levelManager.removeLevelChannel(forAction.getIdLong());
                    message.getChannel().sendMessage(":white_check_mark: In" + forAction.getAsMention() + " kann man nun nicht mehr im Level aufsteigen !").queue();
                } else message.getChannel().sendMessage(":x: In" + forAction.getAsMention() + " kann man gar nicht im Level aufsteigen !").queue();
            } else sendTut(message);
        } else sendTut(message);
    }

    private void sendTut(Message message) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Level Command");
        builder.setColor(Color.BLUE);
        builder.setDescription("Der Level Command ist daf\u0252r da um seinen Level aufzurufen oder mit den n\u00F6tigen Rechten, die Channel in denen man seinen Level steigern kann zu \u00E4ndern oder Mitglieder zu blacklisten");
        builder.addField("Nutzung des Command", "`+level`\n`+level @Mitglied`\n`+level channels add/remove #channel`\n`+level blacklist @Member`", false);
        message.getChannel().sendMessage(builder.build()).queue();
    }

}
