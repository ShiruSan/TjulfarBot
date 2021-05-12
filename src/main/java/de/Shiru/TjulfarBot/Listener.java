package de.Shiru.TjulfarBot;

import de.Shiru.TjulfarBot.Commands.Command;
import de.Shiru.TjulfarBot.Commands.Mute;
import de.Shiru.TjulfarBot.Commands.ReactionRole;
import de.Shiru.TjulfarBot.Managers.CommandManager;
import de.Shiru.TjulfarBot.Events.MuteEvent;
import de.Shiru.TjulfarBot.Events.UnmuteEvent;
import de.Shiru.TjulfarBot.Managers.ReactionManager;
import de.Shiru.TjulfarBot.Utils.ConsoleListener;
import de.Shiru.TjulfarBot.Managers.LevelManager;
import de.Shiru.TjulfarBot.Youtube.YoutubeManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.channel.text.TextChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.events.guild.member.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Listener extends ListenerAdapter {
    public static CommandManager manager = new CommandManager();
    public LevelManager levelManager = new LevelManager();

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        new ConsoleListener().start();
        BotMain.getInstance().getManager().init();
        levelManager.init();
        YoutubeManager.get();
        ReactionManager.get().init();
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if(!event.getAuthor().isBot()) {
            if(event.getMessage().getContentRaw().startsWith(BotMain.getInstance().getSettings().prefix)) {
                String cmd = event.getMessage().getContentRaw().substring(1);
                for(Command command : manager.commands) {
                    if(cmd.length() == command.name.length()) {
                        if(cmd.equals(command.name)) {
                            command.onCommand(event.getMember(), event.getMessage(), new String[0]);
                            break;
                        }
                    } else {
                        String[] cmd2 = cmd.split(" ");
                        if(cmd2[0].equals(command.name)) {
                            if(cmd2.length == 1) {
                                command.onCommand(event.getMember(), event.getMessage(), new String[0]);
                            } else {
                                command.onCommand(event.getMember(), event.getMessage(), Arrays.copyOfRange(cmd2, 1, cmd2.length));
                            }
                            break;
                        }
                    }
                }
            } else {
                if(!(levelManager.levelChannels.contains(event.getMessage().getChannel().getIdLong()))) return;
                if(levelManager.existsProfile(event.getAuthor().getIdLong())) {
                    levelManager.getProfileByID(event.getAuthor().getIdLong()).addMessage();
                } else levelManager.createProfile(event.getAuthor().getIdLong()).addMessage();
            }
        }
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        if(BotMain.getInstance().getSettings().welcomeChannel != -1) {
            TextChannel welcomeChannel = event.getGuild().getTextChannelById(BotMain.getInstance().getSettings().welcomeChannel);
            EmbedBuilder builder = new EmbedBuilder();
            builder.setAuthor("Neues Mitglied", event.getMember().getUser().getAvatarUrl(), event.getMember().getUser().getAvatarUrl());
            String regeln;
            builder.setThumbnail(event.getMember().getUser().getAvatarUrl());
            if(BotMain.getInstance().getSettings().ruleChannel != -1) regeln = event.getGuild().getTextChannelById(BotMain.getInstance().getSettings().ruleChannel).getAsMention();
            else regeln = "Regeln";
            builder.setDescription("Willkommen zur Tjulfars Bande, " + event.getMember().getAsMention() + " !\nLies dir doch als erstes die Regeln (" + regeln + ") durch !");
            welcomeChannel.sendMessage(builder.build()).queue();
        }
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        if(BotMain.getInstance().getSettings().serverLogChannel != -1) {
            TextChannel serverlog = event.getGuild().getTextChannelById(BotMain.getInstance().getSettings().serverLogChannel);
            event.getGuild().retrieveAuditLogs().queueAfter(1, TimeUnit.SECONDS, (Logs) -> {
                boolean kicked = false, banned = false;
                User mod = null;
                String reason = null;
                for(AuditLogEntry log : Logs) {
                    if (log.getTargetIdLong() == event.getUser().getIdLong()) {
                        banned = log.getType() == ActionType.BAN;
                        kicked = log.getType() == ActionType.KICK;
                        mod = log.getUser();
                        reason = log.getReason();
                        break;
                    }
                }
                EmbedBuilder builder = new EmbedBuilder();
                if(banned) {
                    builder.setAuthor("Mitglied gebannt !", event.getJDA().getSelfUser().getAvatarUrl(), event.getJDA().getSelfUser().getAvatarUrl());
                    builder.setColor(Color.RED);
                    builder.setThumbnail(event.getUser().getAvatarUrl());
                    builder.addField("Member", event.getUser().getAsMention(), true);
                    builder.addField("Mod", mod.getAsMention(), true);
                    if(reason != null) builder.addField("Grund:", reason, false);
                } else if(kicked) {
                    builder.setAuthor("Mitglied gekickt !", event.getJDA().getSelfUser().getAvatarUrl(), event.getJDA().getSelfUser().getAvatarUrl());
                    builder.setColor(Color.YELLOW);
                    builder.setThumbnail(event.getUser().getAvatarUrl());
                    builder.addField("Member", event.getUser().getAsMention(), true);
                    builder.addField("Mod", mod.getAsMention(), true);
                    if(reason != null) builder.addField("Grund:", reason, false);
                } else {
                    builder.setAuthor("Mitglied hat verlassen", event.getJDA().getSelfUser().getAvatarUrl(), event.getJDA().getSelfUser().getAvatarUrl());
                    builder.setColor(Color.BLUE);
                    builder.setThumbnail(event.getUser().getAvatarUrl());
                    builder.addField("Member", event.getUser().getAsMention(), true);
                }
                serverlog.sendMessage(builder.build()).queue();
            });
        }
    }

    @Override
    public void onGuildUnban(@NotNull GuildUnbanEvent event) {
        if(BotMain.getInstance().getSettings().serverLogChannel != -1) {
            event.getGuild().retrieveAuditLogs().queueAfter(1, TimeUnit.SECONDS, (logs) -> {
                TextChannel textChannel = event.getGuild().getTextChannelById(BotMain.getInstance().getSettings().serverLogChannel);
                User mod = null;
                for(AuditLogEntry log : logs) {
                    if(log.getTargetIdLong() == event.getUser().getIdLong() && log.getType() == ActionType.UNBAN) {
                        mod = log.getUser();
                        break;
                    }
                }
                EmbedBuilder builder = new EmbedBuilder();
                builder.setAuthor("Mitglied entbannt !", event.getJDA().getSelfUser().getAvatarUrl(), event.getJDA().getSelfUser().getAvatarUrl());
                builder.setColor(Color.RED);
                builder.setThumbnail(event.getUser().getAvatarUrl());
                builder.addField("Member", event.getUser().getAsMention(), true);
                builder.addField("Mod", mod.getAsMention(), true);
                textChannel.sendMessage(builder.build()).queue();
                if(BotMain.getInstance().getManager().containsTempBan(event.getUser().getIdLong())) {
                    BotMain.getInstance().getManager().getTempBanByUserID(event.getUser().getIdLong()).purge();
                }
            });
        }
    }

    @Override
    public void onGuildMemberRoleAdd(@NotNull GuildMemberRoleAddEvent event) {
        if(BotMain.getInstance().getSettings().serverLogChannel != -1) {
            TextChannel serverlog = event.getGuild().getTextChannelById(BotMain.getInstance().getSettings().serverLogChannel);
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.GREEN);
            builder.setAuthor("Rolen hinzugef\u0252gt", event.getMember().getUser().getAvatarUrl(), event.getMember().getUser().getAvatarUrl());
            List<String> mentions = new ArrayList<>();
            for(Role role : event.getRoles()) mentions.add(role.getAsMention());
            builder.addField("Member", event.getMember().getUser().getAsMention(), false);
            builder.addField(event.getRoles().size() + " Role(n) wurden hinzugef\u0252gt", String.join("\n", mentions), false);
            serverlog.sendMessage(builder.build()).queue();
        }
    }

    @Override
    public void onGuildMemberRoleRemove(@NotNull GuildMemberRoleRemoveEvent event) {
        if(BotMain.getInstance().getSettings().serverLogChannel != -1) {
            TextChannel serverlog = event.getGuild().getTextChannelById(BotMain.getInstance().getSettings().serverLogChannel);
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.RED);
            builder.setAuthor("Rolen entfernt", event.getMember().getUser().getAvatarUrl(), event.getMember().getUser().getAvatarUrl());
            List<String> mentions = new ArrayList<>();
            for(Role role : event.getRoles()) mentions.add(role.getAsMention());
            builder.addField("Member", event.getMember().getUser().getAsMention(), false);
            builder.addField(event.getRoles().size() + " Role(n) entfernt", String.join("\n", mentions), false);
            serverlog.sendMessage(builder.build()).queue();
        }
    }

    @Override
    public void onGenericEvent(@NotNull GenericEvent event) {
        if(event instanceof MuteEvent) {
            MuteEvent muteEvent = (MuteEvent) event;
            if(BotMain.getInstance().getSettings().serverLogChannel == -1) return;
            TextChannel serverlog = muteEvent.getGuild().getTextChannelById(BotMain.getInstance().getSettings().serverLogChannel);
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("Mitglied gemuted !");
            builder.setColor(Color.YELLOW);
            builder.setThumbnail(muteEvent.getEntity().getUser().getAvatarUrl());
            builder.addField("Mitglied", muteEvent.getEntity().getAsMention(), true);
            builder.addField("Mod", muteEvent.getMember().getAsMention(), false);
            if(muteEvent.getTimeUnit() != null) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("`" + muteEvent.getTime() + "` ");
                if(muteEvent.getTimeUnit() == TimeUnit.MINUTES) stringBuilder.append("Minute(n)");
                if(muteEvent.getTimeUnit() == TimeUnit.HOURS) stringBuilder.append("Stunde(n)");
                if(muteEvent.getTimeUnit() == TimeUnit.DAYS) stringBuilder.append("Tag(e)");
                builder.addField("Dauer des Mutes", stringBuilder.toString(), false);
            }
            if(muteEvent.getReason() != null) builder.addField("Grund", muteEvent.getReason(), false);
            serverlog.sendMessage(builder.build()).queue();
        } else if(event instanceof UnmuteEvent) {
            UnmuteEvent unmuteEvent = (UnmuteEvent) event;
            if(BotMain.getInstance().getSettings().serverLogChannel == -1) return;
            TextChannel serverlog = unmuteEvent.getGuild().getTextChannelById(BotMain.getInstance().getSettings().serverLogChannel);
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("Mitglied entmuted !");
            builder.setColor(Color.YELLOW);
            builder.setThumbnail(unmuteEvent.getEntity().getUser().getAvatarUrl());
            builder.addField("Mitglied", unmuteEvent.getEntity().getAsMention(), true);
            builder.addField("Mod", unmuteEvent.getMember().getAsMention(), false);
            serverlog.sendMessage(builder.build()).queue();
        }
    }

    @Override
    public void onTextChannelCreate(@NotNull TextChannelCreateEvent event) {
        for(Mute.MutedMember mutedMember : Mute.mutedMembers) {
            if(event.getChannel().canTalk(mutedMember.member)) {
                event.getChannel().createPermissionOverride(mutedMember.member).setDeny(Permission.MESSAGE_WRITE).complete();
                mutedMember.currentMuted.add(event.getChannel());
                mutedMember.update();
            }
        }
    }

    @Override
    public void onTextChannelDelete(@NotNull TextChannelDeleteEvent event) {
        for(Mute.MutedMember mutedMember : Mute.mutedMembers) {
            if(mutedMember.currentMuted.contains(event.getChannel().getIdLong())) {
                mutedMember.currentMuted.remove(event.getChannel());
                mutedMember.update();
            }
        }
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if(!event.getReactionEmote().isEmoji()) return;
        if(ReactionManager.get().containsReactionRole(event.getMessageIdLong(), event.getReactionEmote().getEmoji())) {
            ReactionManager.ReactionRole reactionRole = ReactionManager.get().getReactionRole(event.getMessageIdLong(), event.getReactionEmote().getEmoji());
            event.getGuild().addRoleToMember(event.getMember(), reactionRole.getRole()).queue();
        }
    }

    @Override
    public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event) {
        if(!event.getReactionEmote().isEmoji()) return;
        if(ReactionManager.get().containsReactionRole(event.getMessageIdLong(), event.getReactionEmote().getEmoji())) {
            if(event.getMember().getUser().isBot()) {
                if(event.getMember().getUser().getIdLong() == event.getJDA().getSelfUser().getIdLong()) {
                    ReactionManager.ReactionRole reactionRole = ReactionManager.get().getReactionRole(event.getMessageIdLong(), event.getReactionEmote().getEmoji());
                    ReactionManager.get().removeReactionRole(reactionRole);
                }
            } else {
                ReactionManager.ReactionRole reactionRole = ReactionManager.get().getReactionRole(event.getMessageIdLong(), event.getReactionEmote().getEmoji());
                event.getGuild().removeRoleFromMember(event.getMember(), reactionRole.getRole()).queue();
            }
        }
    }

    @Override
    public void onRoleDelete(@NotNull RoleDeleteEvent event) {
        List<ReactionManager.ReactionRole> reactionRoles = ReactionManager.get().getReactionRolesWithRole(event.getRole());
        if(reactionRoles.size() == 0) return;
        for(ReactionManager.ReactionRole reactionRole : reactionRoles) ReactionManager.get().removeReactionRole(reactionRole);
    }

}
