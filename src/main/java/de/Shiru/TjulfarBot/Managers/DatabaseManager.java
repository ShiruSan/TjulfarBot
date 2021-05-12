package de.Shiru.TjulfarBot.Managers;

import com.google.gson.Gson;
import com.mysql.cj.jdbc.MysqlDataSource;
import de.Shiru.TjulfarBot.BotMain;
import de.Shiru.TjulfarBot.Commands.Mute;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class DatabaseManager {
    public List<TempBan> tempBans = new ArrayList<>();
    public DataSource dataSource;
    private int tempBanId = 1;
    private int muteId = 1;
    public Timer managerScheduler = new Timer();

    public void init() {
        try {
            if(BotMain.getInstance().getSettings().sqLiteConfig.use) {
                File databaseFile = new File(BotMain.getInstance().getSettings().sqLiteConfig.file);
                if(!databaseFile.exists()) databaseFile.createNewFile();
                System.out.println("Init SQLite Manager...");
                SQLiteDataSource dataSource = new SQLiteDataSource();
                dataSource.setUrl("jdbc:sqlite:" + databaseFile.getAbsolutePath());
                this.dataSource = dataSource; 
            } else {
                MysqlDataSource mysqlDataSource = new MysqlDataSource();
                String ip = BotMain.getInstance().getSettings().mySQLConfig.ip;
                int port = BotMain.getInstance().getSettings().mySQLConfig.port;
                String username = BotMain.getInstance().getSettings().mySQLConfig.username;
                String password = BotMain.getInstance().getSettings().mySQLConfig.password;
                String database = BotMain.getInstance().getSettings().mySQLConfig.database;
                mysqlDataSource.setUrl("jdbc:mysql://" + ip +  ":" + port + "/" + database);
                mysqlDataSource.setUser(username);
                mysqlDataSource.setPassword(password);
                this.dataSource = mysqlDataSource;
            }
            PreparedStatement preparedStatement = dataSource.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS  `TempBans` (" +
                    "id integer not null," +
                    "userid bigint not null," +
                    "passing bigint not null," +
                    "passed boolean not null," +
                    "Primary Key(id));");
            preparedStatement.executeUpdate();
            preparedStatement.close();
            preparedStatement = dataSource.getConnection().prepareStatement("select * from TempBans");
            ResultSet set = preparedStatement.executeQuery();
            while (set.next()) tempBanId = set.getInt(1) + 1;
            set.close();
            preparedStatement.close();
            preparedStatement = dataSource.getConnection().prepareStatement("select * from TempBans where passed = ?");
            preparedStatement.setBoolean(1, false);
            set = preparedStatement.executeQuery();
            while (set.next()) {
                TempBan tempBan = new TempBan(set.getInt(1), set.getLong(2), set.getLong(3));
                tempBans.add(tempBan);
            }
            set.close();
            preparedStatement.close();
            preparedStatement = dataSource.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS  `Mutes` (" +
                    "`id` integer not null," +
                    "`userid` bigint not null," +
                    "`passing` bigint null," +
                    "`passed` boolean not null," +
                    "`type` integer not null," +
                    "`reason` varchar(1000) null," +
                    "`mutedChannels` varchar(10000) not null," +
                    "Primary Key(id));");
            preparedStatement.executeUpdate();
            preparedStatement.close();
            preparedStatement = dataSource.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS  `LevelConfig` (" +
                    "`id` bigint not null," +
                    "`type` varchar(9) not null," +
                    "Primary Key(id));");
            preparedStatement.executeUpdate();
            preparedStatement.close();
            preparedStatement = dataSource.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS  `Youtube` (" +
                    "`channelid` varchar(100) not null," +
                    "`channelname` varchar(100) not null," +
                    "Primary Key(channelid));");
            preparedStatement.executeUpdate();
            preparedStatement.close();
            preparedStatement = dataSource.getConnection().prepareStatement("select * from Mutes where passed = ?");
            preparedStatement.setBoolean(1, false);
            set = preparedStatement.executeQuery();
            JDA jda = BotMain.getInstance().getJda();
            Gson gson = new Gson();
            while (set.next()) {
                Mute.MutedMember mutedMember = new Mute.MutedMember();
                mutedMember.sqid = set.getInt(1);
                mutedMember.member = jda.getGuilds().get(0).getMemberById(set.getLong(2));
                mutedMember.expires = set.getLong(3);
                mutedMember.muteType = Mute.MutedMember.MuteType.getMuteTypeByID(set.getInt(5));
                mutedMember.reason = set.getString(6);
                List<Long> rawTextChannels = gson.fromJson(set.getString(7), ArrayList.class);
                List<TextChannel> currentMuted = new ArrayList<>();
                for(long id : rawTextChannels) currentMuted.add(mutedMember.member.getGuild().getTextChannelById(id));
                mutedMember.currentMuted = currentMuted;
                Mute.mutedMembers.add(mutedMember);
                muteId = set.getInt(1) + 1;
            }
            set.close();
            preparedStatement.close();
            managerScheduler.schedule(new TempBanListener(), 0, 60000);
            managerScheduler.schedule(new Mute.MuteTask(), 0, 1000);
            System.out.println("Database Manager ready !");
        } catch (SQLException | IOException throttles) {
            throttles.printStackTrace();
        }
    }

    public void uploadMute(Mute.MutedMember mutedMember) {
        try {
            PreparedStatement preparedStatement = dataSource.getConnection().prepareStatement("insert into `Mutes` values (?, ?, ?, ?, ?, ?, ?)");
            preparedStatement.setInt(1, muteId);
            preparedStatement.setLong(2, mutedMember.member.getIdLong());
            preparedStatement.setLong(3, mutedMember.expires);
            preparedStatement.setBoolean(4, false);
            preparedStatement.setInt(5, mutedMember.muteType.id);
            if(mutedMember.reason == null) preparedStatement.setNull(6, Types.VARCHAR);
            else preparedStatement.setString(6, mutedMember.reason);
            List<Long> channelIds = new ArrayList<>();
            for(TextChannel textChannel : mutedMember.currentMuted) channelIds.add(textChannel.getIdLong());
            preparedStatement.setString(7, new Gson().toJson(channelIds, ArrayList.class));
            preparedStatement.executeUpdate();
            preparedStatement.close();
            mutedMember.sqid = muteId;
            muteId++;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public TempBan tempBan(User user, int time, TimeUnit unit) {
        long expires = System.currentTimeMillis();
        if(unit == TimeUnit.HOURS) {
            expires += time * 60 * 60 * 1000;
        } else if(unit == TimeUnit.DAYS) {
            expires += time * 24 * 60 * 60 * 1000;
        }

        TempBan tempBan = new TempBan(tempBanId, user.getIdLong(), expires);
        tempBanId++;
        tempBan.createTableEntry();
        tempBans.add(tempBan);
        return tempBan;
    }

    public TempBan getTempBanByUserID(long id) {
        for(TempBan tempBan : tempBans) {
            if(tempBan.userid == id) return tempBan;
        }
        return null;
    }

    public boolean containsTempBan(long id) {
        for(TempBan tempBan : tempBans) {
            if(tempBan.userid == id) return true;
        }
        return false;
    }

    private class TempBanListener extends TimerTask {

        @Override
        public void run() {
            try {
                PreparedStatement preparedStatement = dataSource.getConnection().prepareStatement("select * from TempBans where passed = ?");
                preparedStatement.setBoolean(1, false);
                ResultSet set = preparedStatement.executeQuery();
                while(set.next()) {
                    if(set.getLong(3) < System.currentTimeMillis()) {
                        Guild guild = BotMain.getInstance().getJda().getGuilds().get(0);
                        guild.unban(String.valueOf(set.getLong(2))).complete();
                        getTempBanByUserID(set.getLong(2)).purge();
                    }
                }
                set.close();
                preparedStatement.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

    }

    public class TempBan {
        public int sqid;
        public long userid;
        public long expires;

        private TempBan(int sqid, long userid, long expires) {
            this.sqid = sqid;
            this.userid = userid;
            this.expires = expires;
        }

        public void createTableEntry() {
            try {
                PreparedStatement preparedStatement = dataSource.getConnection().prepareStatement("insert into TempBans values (?, ?, ?, ?)");
                preparedStatement.setInt(1, sqid);
                preparedStatement.setLong(2, userid);
                preparedStatement.setLong(3, expires);
                preparedStatement.setBoolean(4, false);
                preparedStatement.executeUpdate();
                preparedStatement.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

        }

        public void purge() {
            try {
                PreparedStatement preparedStatement = dataSource.getConnection().prepareStatement("update TempBans set passed = ? where id = ?");
                preparedStatement.setBoolean(1, true);
                preparedStatement.setInt(2, sqid);
                preparedStatement.executeUpdate();
                preparedStatement.close();
                tempBans.remove(this);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

    }

    public void close() {
        try {
            managerScheduler.cancel();
            dataSource.getConnection().close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

}
