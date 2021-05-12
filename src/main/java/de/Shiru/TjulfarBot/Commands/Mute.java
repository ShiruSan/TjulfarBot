package de.Shiru.TjulfarBot.Commands;

import com.google.gson.Gson;
import de.Shiru.TjulfarBot.BotMain;
import de.Shiru.TjulfarBot.Events.MuteEvent;
import de.Shiru.TjulfarBot.Events.UnmuteEvent;
import de.Shiru.TjulfarBot.Managers.DatabaseManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import javax.annotation.Nullable;
import java.awt.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class Mute extends Command {
    public static List<MutedMember> mutedMembers = new ArrayList<>();

    public Mute(String name, Permission neededPermission) {
        super("mute", Permission.VOICE_MUTE_OTHERS);
    }

    @Override
    public void onCommand(Member author, Message message, String[] args) {
        if(args.length == 1) {
            if(message.getMentionedMembers().size() == 1) {
                Member toMute = message.getMentionedMembers().get(0);
                if(!containsMute(toMute.getIdLong())) {
                    MutedMember mutedMember = MutedMember.createMute(toMute, MutedMember.MuteType.NONLIMIT, 0, null);
                    mutedMembers.add(mutedMember);
                    message.getChannel().sendMessage(":white_check_mark: " + toMute.getAsMention() + " wurde gemuted !").queue();
                    message.getJDA().getEventManager().handle(new MuteEvent(message.getMember(), toMute, mutedMember, 0, null, null));
                    BotMain.getInstance().getManager().uploadMute(mutedMember);
                } else message.getChannel().sendMessage(":x: Dieser Member ist akutell gemuted !").queue();
            } else sendTut(message);
        } else if(args.length == 2) {
            if(message.getMentionedMembers().size() == 1) {
                Member toMute = message.getMentionedMembers().get(0);
                if(!containsMute(toMute.getIdLong())) {
                    MutedMember mutedMember = null;
                    TimeUnit timeUnit = null;
                    String reason = null;
                    int eventtime = 0;
                    try {
                        int time = (eventtime = Integer.parseInt(args[1].substring(0, args[1].length() - 1)));
                        char timeunit = args[1].toCharArray()[args[1].toCharArray().length - 1];
                        if(timeunit == 'm') {
                            long expires = System.currentTimeMillis() + (time * 60 * 1000);
                            mutedMember = MutedMember.createMute(toMute, MutedMember.MuteType.LIMIT, expires, null);
                            mutedMembers.add(mutedMember);
                            message.getChannel().sendMessage(":white_check_mark: " + toMute.getAsMention() + " wurde für `" + time + "` Minute(n) gemuted !").queue();
                            timeUnit = TimeUnit.MINUTES;
                        } else if(timeunit == 'h') {
                            long expires = System.currentTimeMillis() + (time * 60 * 60 * 1000);
                            mutedMember = MutedMember.createMute(toMute, MutedMember.MuteType.LIMIT, expires, null);
                            mutedMembers.add(mutedMember);
                            message.getChannel().sendMessage(":white_check_mark: " + toMute.getAsMention() + " wurde für `" + time + "` Stunde(n) gemuted !").queue();
                            timeUnit = TimeUnit.HOURS;
                        } else if(timeunit == 'd') {
                            long expires = System.currentTimeMillis() + (time * 24 * 60 * 60 * 1000);
                            mutedMember = MutedMember.createMute(toMute, MutedMember.MuteType.LIMIT, expires, null);
                            mutedMembers.add(mutedMember);
                            message.getChannel().sendMessage(":white_check_mark: " + toMute.getAsMention() + " wurde für `" + time + "` Tag(e) gemuted !").queue();
                            timeUnit = TimeUnit.DAYS;
                        } else sendTut(message);
                    } catch (NumberFormatException e) {
                        reason = args[1];
                        mutedMember = MutedMember.createMute(toMute, MutedMember.MuteType.NONLIMIT, 0, args[1]);
                        mutedMembers.add(mutedMember);
                        message.getChannel().sendMessage(":white_check_mark: " + toMute.getAsMention() + " wurde gemuted !").queue();
                    }
                    BotMain.getInstance().getManager().uploadMute(mutedMember);
                    message.getJDA().getEventManager().handle(new MuteEvent(message.getMember(), toMute, mutedMember, eventtime, timeUnit, reason));
                } else message.getChannel().sendMessage(":x: Dieser Member ist akutell gemuted !").queue();
            } else sendTut(message);
        } else if(args.length > 2) {
            if(message.getMentionedMembers().size() == 1) {
                Member toMute = message.getMentionedMembers().get(0);
                if(!containsMute(toMute.getIdLong())) {
                    MutedMember mutedMember = null;
                    TimeUnit timeUnit = null;
                    String reason = null;
                    int eventtime = 0;
                    try {
                        int time = Integer.parseInt(args[1].substring(0, args[1].length() - 1));
                        char timeunit = args[1].toCharArray()[args[1].toCharArray().length - 1];
                        reason = "";
                        for(int i = 2; i < args.length; i++) {
                            if((i + 1) != args.length) reason += args[i] + " ";
                            else reason += args[i];
                        }
                        if(timeunit == 'm') {
                            timeUnit = TimeUnit.MINUTES;
                            long expires = System.currentTimeMillis() + (time * 60 * 1000);
                            mutedMember = MutedMember.createMute(toMute, MutedMember.MuteType.LIMIT, expires, reason);
                            mutedMembers.add(mutedMember);
                            message.getChannel().sendMessage(":white_check_mark: " + toMute.getAsMention() + " wurde für `" + time + "` Minute(n) gemuted !").queue();
                        } else if(timeunit == 'h') {
                            timeUnit = TimeUnit.HOURS;
                            long expires = System.currentTimeMillis() + (time * 60 * 60 * 1000);
                            mutedMember = MutedMember.createMute(toMute, MutedMember.MuteType.LIMIT, expires, reason);
                            mutedMembers.add(mutedMember);
                            message.getChannel().sendMessage(":white_check_mark: " + toMute.getAsMention() + " wurde für `" + time + "` Stunde(n) gemuted !").queue();
                        } else if(timeunit == 'd') {
                            timeUnit = TimeUnit.DAYS;
                            long expires = System.currentTimeMillis() + (time * 24 * 60 * 60 * 1000);
                            mutedMember = MutedMember.createMute(toMute, MutedMember.MuteType.LIMIT, expires, reason);
                            mutedMembers.add(mutedMember);
                            message.getChannel().sendMessage(":white_check_mark: " + toMute.getAsMention() + " wurde für `" + time + "` Tag(e) gemuted !").queue();
                        } else sendTut(message);
                    } catch (NumberFormatException e) {
                        reason = "";
                        for(int i = 1; i < args.length; i++) {
                            if((i + 1) != args.length) reason += args[i] + " ";
                            else reason += args[i];
                        }
                        mutedMember = MutedMember.createMute(toMute, MutedMember.MuteType.NONLIMIT, 0, reason);
                        mutedMembers.add(mutedMember);
                        message.getChannel().sendMessage(":white_check_mark: " + toMute.getAsMention() + " wurde gemuted !").queue();
                    }
                    BotMain.getInstance().getManager().uploadMute(mutedMember);
                    message.getJDA().getEventManager().handle(new MuteEvent(message.getMember(), toMute, mutedMember, eventtime, timeUnit, reason));
                } else message.getChannel().sendMessage(":x: Dieser Member ist akutell gemuted !").queue();
            } else sendTut(message);
        } else sendTut(message);
    }

    public static boolean containsMute(long id) {
        for(MutedMember mutedMember : mutedMembers) {
            if(mutedMember.member.getIdLong() == id) return true;
        }
        return false;
    }

    private void sendTut(Message message) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Mute Command");
        builder.setColor(Color.BLUE);
        builder.setDescription("Der Mute Command ist daf\u00FCr da um Member, die sich nicht an die Gespr\u00E4chsregeln halten, aus dem Chat auszuschließen.");
        builder.addField("Nutzung des Commands", "`+mute [Member] (Zeit) (Grund)`", false);
        builder.addField("Weitere Info", "Die Zeit kannst du wie folgt angeben: `1m, 1h oder 1d`.\nDu kannst Zeit und Grund auslassen.", false);
        message.getChannel().sendMessage(builder.build()).queue();
    }

    public static class MutedMember {
        private static Gson gson = new Gson();
        public int sqid;
        public Member member;
        public MuteType muteType;
        public List<TextChannel> currentMuted;
        public long expires;
        public String reason;

        public MutedMember() {}

        public static MutedMember createMute(Member member, MuteType type, long expires, @Nullable String reason) {
            MutedMember mutedMember = new MutedMember();
            mutedMember.member = member;
            mutedMember.muteType = type;
            mutedMember.expires = expires;
            mutedMember.currentMuted = new ArrayList<>();
            mutedMember.reason = reason;
            for(TextChannel textChannel : member.getGuild().getTextChannels()) {
                if(textChannel.canTalk(member)) {
                    if(textChannel.getPermissionOverride(member) == null) {
                        textChannel.createPermissionOverride(member).deny(Permission.MESSAGE_WRITE).complete();
                    } else {
                        textChannel.getPermissionOverride(member).getManager().deny(Permission.MESSAGE_WRITE).complete();
                    }
                    mutedMember.currentMuted.add(textChannel);
                }
            }
            return mutedMember;
        }

        public void purge() {
            for(TextChannel textChannel : currentMuted) {
                if(textChannel.getPermissionOverride(member) == null) {
                    textChannel.createPermissionOverride(member).setAllow(Permission.MESSAGE_WRITE).complete();
                } else {
                    textChannel.getPermissionOverride(member).getManager().setAllow(Permission.MESSAGE_WRITE).complete();
                }
            }
            DatabaseManager manager = BotMain.getInstance().getManager();
            try {
                PreparedStatement preparedStatement = manager.dataSource.getConnection().prepareStatement("update Mutes set passed = ? where id = ?");
                preparedStatement.setString(1, gson.toJson(currentMuted));
                preparedStatement.setInt(2, sqid);
                preparedStatement.executeUpdate();
                preparedStatement.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            mutedMembers.remove(this);
        }

        public void update() {
            DatabaseManager manager = BotMain.getInstance().getManager();
            try {
                PreparedStatement preparedStatement = manager.dataSource.getConnection().prepareStatement("update Mutes set mutedChannels = ? where id = ?");
                preparedStatement.setBoolean(1, true);
                preparedStatement.setInt(2, sqid);
                preparedStatement.executeUpdate();
                preparedStatement.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

        public enum MuteType {
            NONLIMIT(0),
            LIMIT(1);

            public final int id;
            private MuteType(int id) {
                this.id = id;
            }

            public static MuteType getMuteTypeByID(int id) {
                for(MuteType muteType : MuteType.values()) {
                    if(muteType.id == id) return muteType;
                }
                return null;
            }

        }

    }

    public static class MuteTask extends TimerTask {

        @Override
        public void run() {
            MutedMember mutedMember = null;
            for(MutedMember mutedMember2 : mutedMembers) {
                if(mutedMember2.muteType != MutedMember.MuteType.LIMIT) continue;
                if(mutedMember2.expires >= System.currentTimeMillis()) continue;
                mutedMember = mutedMember2;
                break;
            }
            if(mutedMember == null) return;
            mutedMember.purge();
            Member selfMember = mutedMember.member.getGuild().getSelfMember();
            BotMain.getInstance().getJda().getEventManager().handle(new UnmuteEvent(selfMember, mutedMember.member));
        }

    }

}
