package de.Shiru.TjulfarBot.Utils;

public class Settings {
    public String prefix;
    public long welcomeChannel;
    public long serverLogChannel;
    public long announcementChannel;
    public long ruleChannel;
    public long levelupChannel;
    public long videoUploadChannel;
    public MySQLConfig mySQLConfig;
    public SQLiteConfig sqLiteConfig;

    public final static Settings DEFAULT;
    static {
        DEFAULT = new Settings();
        DEFAULT.prefix = "+";
        DEFAULT.welcomeChannel = -1;
        DEFAULT.serverLogChannel = -1;
        DEFAULT.announcementChannel = -1;
        DEFAULT.ruleChannel = -1;
        DEFAULT.levelupChannel = -1;
        DEFAULT.videoUploadChannel = -1;
        DEFAULT.mySQLConfig = new MySQLConfig();
        DEFAULT.mySQLConfig.use = false;
        DEFAULT.mySQLConfig.ip = "";
        DEFAULT.mySQLConfig.port = 1111;
        DEFAULT.mySQLConfig.database = "";
        DEFAULT.mySQLConfig.username = "";
        DEFAULT.mySQLConfig.password = "";
        DEFAULT.sqLiteConfig = new SQLiteConfig();
        DEFAULT.sqLiteConfig.use = true;
        DEFAULT.sqLiteConfig.file = "data.db";
    }

    public static class MySQLConfig {
        public boolean use;
        public String ip;
        public int port;
        public String database;
        public String username;
        public String password;
    }

    public static class SQLiteConfig {
        public boolean use;
        public String file;
    }

}
