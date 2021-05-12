package de.Shiru.TjulfarBot.Utils;

import de.Shiru.TjulfarBot.BotMain;
import de.Shiru.TjulfarBot.Youtube.YoutubeManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleListener extends Thread {

    @Override
    public void run() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while(true) {
            try {
                String line = reader.readLine();
                switch (line) {
                    case "shutdown":
                        JDA jda = BotMain.getInstance().getJda();
                        YoutubeManager.get().shutdown();
                        jda.getPresence().setStatus(OnlineStatus.OFFLINE);
                        System.out.println("[Console Listener] Shutting down JDA ...");
                        jda.shutdownNow();
                        BotMain.getInstance().close();
                        System.out.println("[Console Listener] JDA was closed successfully! Leaving application ...");
                        System.exit(0);
                    default:
                        System.out.println("[Console Listener] This Command isn't registered !");
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}
