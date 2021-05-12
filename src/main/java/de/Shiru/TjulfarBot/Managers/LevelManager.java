package de.Shiru.TjulfarBot.Managers;

import com.google.gson.Gson;
import de.Shiru.TjulfarBot.BotMain;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LevelManager {
    public ArrayList<Long> levelChannels = new ArrayList<>();
    public ArrayList<Long> blackListedMembers = new ArrayList<>();

    public void init() {
        try {
            PreparedStatement preparedStatement = BotMain.getInstance().getManager().dataSource.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS `Levels` (" +
                    "userid bigint not null," +
                    "level integer not null," +
                    "messages integer not null," +
                    "Primary Key(userid))");
            preparedStatement.executeUpdate();
            preparedStatement.close();
            preparedStatement = BotMain.getInstance().getManager().dataSource.getConnection().prepareStatement("select * from `LevelConfig`");
            ResultSet set = preparedStatement.executeQuery();
            while(set.next()) {
                if(set.getString(2).equals("channel")) {
                    levelChannels.add(set.getLong(1));
                } else blackListedMembers.add(set.getLong(1));
            }
            set.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void addLevelChannel(long id) {
        try {
            PreparedStatement preparedStatement = BotMain.getInstance().getManager().dataSource.getConnection().prepareStatement("insert into `LevelConfig` values (?, ?)");
            preparedStatement.setLong(1, id);
            preparedStatement.setString(2, "channel");
            preparedStatement.executeUpdate();
            preparedStatement.close();
            levelChannels.add(id);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void removeLevelChannel(long id) {
        try {
            PreparedStatement preparedStatement = BotMain.getInstance().getManager().dataSource.getConnection().prepareStatement("delete from `LevelConfig` where id = ?");
            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            levelChannels.remove(id);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void addBlacklist(long id) {
        try {
            PreparedStatement preparedStatement = BotMain.getInstance().getManager().dataSource.getConnection().prepareStatement("insert into `LevelConfig` values (?, ?)");
            preparedStatement.setLong(1, id);
            preparedStatement.setString(2, "blacklist");
            preparedStatement.executeUpdate();
            preparedStatement.close();
            blackListedMembers.add(id);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void removeBlacklist(long id) {
        try {
            PreparedStatement preparedStatement = BotMain.getInstance().getManager().dataSource.getConnection().prepareStatement("delete from `LevelConfig` where id = ?");
            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            blackListedMembers.remove(id);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public boolean existsProfile(long id) {
        try {
            PreparedStatement preparedStatement = BotMain.getInstance().getManager().dataSource.getConnection().prepareStatement("select * from Levels where userid = ?");
            preparedStatement.setLong(1, id);
            ResultSet set = preparedStatement.executeQuery();
            boolean exists = set.next();
            set.close();
            preparedStatement.close();
            return exists;
        } catch (SQLException e) {
            return false;
        }
    }

    public Profile createProfile(long id) {
        Profile profile = new Profile();
        profile.userid = id;
        profile.level = 0;
        profile.messages = 0;
        try {
            PreparedStatement preparedStatement = BotMain.getInstance().getManager().dataSource.getConnection().prepareStatement("insert into Levels values (?, ?, ?)");
            preparedStatement.setLong(1, id);
            preparedStatement.setInt(2, 0);
            preparedStatement.setInt(3, 0);
            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return profile;
    }

    public Profile getProfileByID(long id){
        try {
            PreparedStatement preparedStatement = BotMain.getInstance().getManager().dataSource.getConnection().prepareStatement("select * from Levels where userid = ?");
            preparedStatement.setLong(1, id);
            ResultSet set = preparedStatement.executeQuery();
            set.next();
            Profile profile = new Profile();
            profile.userid = id;
            profile.level = set.getInt(2);
            profile.messages = set.getInt(3);
            set.close();
            preparedStatement.close();
            return profile;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public MessageEmbed createEmbedProfile(LevelManager.Profile profile, Member member) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Level von " + member.getUser().getAsTag());
        builder.addField("Aktuelles Level:", String.valueOf(profile.getLevel()), false);
        builder.setThumbnail(member.getUser().getAvatarUrl());
        builder.setColor(Color.BLUE);
        int nextLevelMessages = 10;
        if(profile.getLevel() != 0) nextLevelMessages = (profile.getLevel() * 100);
        builder.addField("Nachrichten bis zum n\u00e4chsten Aufstieg", profile.getMessages() + "/" + nextLevelMessages, false);
        return builder.build();
    }

    public void onLevel(Profile leveledUp) {
        if(BotMain.getInstance().getSettings().levelupChannel == -1) return;
        TextChannel levelUp = BotMain.getInstance().getJda().getTextChannelById(BotMain.getInstance().getSettings().levelupChannel);
        Member member = levelUp.getGuild().retrieveMemberById(leveledUp.getUserid()).complete();
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.GREEN);
        builder.setTitle("Levelaufstieg !").setThumbnail(member.getUser().getAvatarUrl());
        builder.setDescription(member.getAsMention() + " stieg auf Level " + leveledUp.getLevel() + " !");
        builder.addField("Nachrichten bis n\u00e4chsten Aufstieg", String.valueOf(leveledUp.getLevel() * 100), false);
        levelUp.sendMessage(builder.build()).queue();
    }

    public class Profile {
        private long userid;
        private int level;
        private int messages;

        public void addMessage() {
            if(blackListedMembers.contains(userid)) return;
            messages++;
            updateEntry();
        }

        public void updateState() {
            if(level == 0) {
                if(messages != 10) return;
                level = 1;
                messages = 0;
                onLevel(this);
            } else {
                if(messages != (level * 100)) return;
                level++;
                messages = 0;
                onLevel(this);
            }
        }

        public void updateEntry() {
            updateState();
            try {
                PreparedStatement preparedStatement = BotMain.getInstance().getManager().dataSource.getConnection().prepareStatement("update Levels set level = ?, messages = ? where userid = ?");
                preparedStatement.setInt(1, level);
                preparedStatement.setInt(2, messages);
                preparedStatement.setLong(3, userid);
                preparedStatement.executeUpdate();
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public long getUserid() {
            return userid;
        }

        public int getLevel() {
            return level;
        }

        public int getMessages() {
            return messages;
        }

    }

}
