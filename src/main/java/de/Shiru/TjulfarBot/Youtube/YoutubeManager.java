package de.Shiru.TjulfarBot.Youtube;

import com.google.gson.Gson;
import de.Shiru.TjulfarBot.BotMain;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

public class YoutubeManager {
    private static YoutubeManager instance;
    private List<String> channelIDs;
    private Gson gson;
    private Timer schedulder;
    private final String apiKey = "Key";

    private YoutubeManager() {}

    public static YoutubeManager get() {
        if(instance == null) {
            System.out.println("Init Youtube Manager");
            instance = new YoutubeManager();
            instance.gson = new Gson();
            instance.channelIDs = new ArrayList<>();
            try(Connection connection = BotMain.getInstance().getManager().dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("select * from `Youtube`");) {
                ResultSet set = preparedStatement.executeQuery();
                while(set.next()) {
                    instance.channelIDs.add(set.getString(1));
                }
                set.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            instance.schedulder = new Timer();
            instance.schedulder.schedule(new ChannelScheduler(), 0, (15 * 60 * 1000));
            System.out.println("Init Youtube Manager success");
        }
        return instance;
    }

    public boolean addChannelID(String id) {
        boolean success = false;
        try(Connection connection = BotMain.getInstance().getManager().dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("insert into `Youtube` values (?, ?)");) {
            preparedStatement.setString(1, id);
            preparedStatement.setString(2, getResponseItem(id).getSnippet().getTitle());
            preparedStatement.executeUpdate();
            channelIDs.add(id);
            success = true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return success;
    }

    public boolean removeChannelID(String id) {
        boolean success = false;
        try(Connection connection = BotMain.getInstance().getManager().dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("delete from `Youtube` where channelid = ?");) {
            preparedStatement.setString(1, id);
            preparedStatement.executeUpdate();
            channelIDs.remove(id);
            success = true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return success;
    }

    public Item getResponseItem(String channelid) {
        try {
            URL url = new URL("https://www.googleapis.com/youtube/v3/search?key=" + apiKey + "&channelId=" + channelid + "&part=snippet,id&order=date&maxResults=1");
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line, json = "";
            while((line = reader.readLine()) != null) json += line + "\n";
            reader.close();
            SearchResponse searchResponse = gson.fromJson(json, SearchResponse.class);
            if(searchResponse.getItems()[0].getId().getKind().equals("youtube#video")) return searchResponse.getItems()[0];
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static class ChannelScheduler extends TimerTask {
        private HashMap<String, Item> lastUploads = new HashMap<>();

        @Override
        public void run() {
            TextChannel uploadChannel = null;
            if(BotMain.getInstance().getSettings().videoUploadChannel != -1) uploadChannel = BotMain.getInstance().getJda().getTextChannelById(BotMain.getInstance().getSettings().videoUploadChannel);
            for(String channelId : instance.channelIDs) {
                Item item = instance.getResponseItem(channelId);
                if(item == null) continue;
                if(!lastUploads.containsKey(channelId)) {
                    lastUploads.put(channelId, item);
                    continue;
                }
                if(lastUploads.get(channelId).getId().getVideoId().equals(item.getId().getVideoId())) continue;
                if(uploadChannel == null) break;
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.RED);
                if(item.getSnippet().getLiveBroadcastContent().equals("live")) {
                    builder.setTitle(item.getSnippet().getChannelTitle() + " streamt nun !");
                    builder.setDescription(item.getSnippet().getChannelTitle() + " streamt jetzt auf Youtube !\nSchau jetzt zu: " + item.getAsVideoURL());
                } else {
                    builder.setTitle("Neues Video von " + item.getSnippet().getChannelTitle());
                    builder.setDescription(item.getSnippet().getChannelTitle() + " hat ein neues Video hochgeladen !\n" + item.getAsVideoURL());
                }
                builder.addField(item.getSnippet().getTitle(), item.getSnippet().getDescription(), false);
                builder.setImage(item.getSnippet().getThumbnails().getHigh().getUrl().toString());
                builder.setFooter(item.getSnippet().getPublishedAt().toString());
                uploadChannel.sendMessage(uploadChannel.getGuild().getPublicRole().getAsMention()).embed(builder.build()).queue();
                lastUploads.replace(channelId, item);
            }
        }

    }

    public List<String> getChannelIDs() {
        return channelIDs;
    }

    public void shutdown() {
        schedulder.cancel();
    }

}
