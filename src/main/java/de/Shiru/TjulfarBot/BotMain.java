package de.Shiru.TjulfarBot;

import com.google.gson.Gson;
import de.Shiru.TjulfarBot.Managers.DatabaseManager;
import de.Shiru.TjulfarBot.Utils.Settings;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;
import java.io.*;
import java.util.Collections;

public class BotMain {
    private static BotMain instance;
    private JDA jda;
    private Settings settings;
    private DatabaseManager manager = new DatabaseManager();

    private BotMain() throws LoginException, IOException {
        if(!(new File("settings.json").exists())) {
            settings = Settings.DEFAULT;
            String json = new Gson().toJson(settings);
            File file = new File("settings.json");
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.write(json);
            writer.close();
        } else {
            BufferedReader reader = new BufferedReader(new FileReader(new File("settings.json")));
            String line;
            String result = "";
            while((line = reader.readLine()) != null) {
                result += line + "\n";
            }
            reader.close();
            settings = new Gson().fromJson(result, Settings.class);
        }
        JDABuilder builder = JDABuilder.createDefault(new BufferedReader(new FileReader("token.txt")).readLine());
        builder.setStatus(OnlineStatus.ONLINE);
        builder.setActivity(Activity.watching("Tjulfars Kanal"));
        builder.addEventListeners(new Listener());
        builder.setAutoReconnect(true);
        builder.enableIntents(Collections.singletonList(GatewayIntent.GUILD_MEMBERS));
        jda = builder.build();
    }

    public static void main(String[] args) throws LoginException, IOException {
        instance = new BotMain();
    }

    public static BotMain getInstance() {
        return instance;
    }

    public JDA getJda() {
        return jda;
    }

    public Settings getSettings() {
        return settings;
    }

    public DatabaseManager getManager() {
        return manager;
    }

    public void close() {
        try {
            manager.close();
            Gson gson = new Gson();
            File settings = new File("settings.json");
            if(settings.exists()) settings.createNewFile();
            else {
                settings.delete();
                settings.createNewFile();
            }
            String json = gson.toJson(getSettings(), Settings.class);
            FileWriter writer = new FileWriter(settings);
            writer.write(json);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
