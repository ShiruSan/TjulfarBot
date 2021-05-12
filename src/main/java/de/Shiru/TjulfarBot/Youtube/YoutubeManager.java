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
            try {
                PreparedStatement preparedStatement = BotMain.getInstance().getManager().dataSource.getConnection().prepareStatement("select * from `Youtube`");
                ResultSet set = preparedStatement.executeQuery();
                while(set.next()) {
                    instance.channelIDs.add(set.getString(1));
                }
                set.close();
                preparedStatement.close();
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
        try {
            PreparedStatement preparedStatement = BotMain.getInstance().getManager().dataSource.getConnection().prepareStatement("insert into `Youtube` values (?, ?)");
            preparedStatement.setString(1, id);
            preparedStatement.setString(2, getResponseItem(id).getSnippet().getTitle());
            preparedStatement.executeUpdate();
            preparedStatement.close();
            channelIDs.add(id);
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public boolean removeChannelID(String id) {
        try {
            PreparedStatement preparedStatement = BotMain.getInstance().getManager().dataSource.getConnection().prepareStatement("delete from `Youtube` where channelid = ?");
            preparedStatement.setString(1, id);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            channelIDs.remove(id);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static class ChannelScheduler extends TimerTask {
        private HashMap<String, Item> lastUploads = new HashMap<>();

        @Override
        public void run() {
            System.out.println("Fired ChannelScheduler");
            TextChannel uploadChannel = null;
            if(BotMain.getInstance().getSettings().videoUploadChannel != -1) uploadChannel = BotMain.getInstance().getJda().getTextChannelById(BotMain.getInstance().getSettings().videoUploadChannel);
            for(String channelId : instance.channelIDs) {
                Item video = instance.getResponseItem(channelId);
                if(!lastUploads.containsKey(channelId)) {
                    lastUploads.put(channelId, video);
                    System.out.println("Added Video for " + video.getSnippet().getChannelTitle());
                    continue;
                }
                if(lastUploads.get(channelId).getId().getVideoId().equals(video.getId().getVideoId())) continue;
                if(uploadChannel == null) break;
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.RED);
                builder.setTitle("Neues Video von " + video.getSnippet().getChannelTitle());
                builder.setDescription(video.getSnippet().getChannelTitle() + " hat ein neues Video hochgeladen !\n" + video.getAsVideoURL());
                builder.addField(video.getSnippet().getTitle(), video.getSnippet().getDescription(), false);
                builder.setImage(video.getSnippet().getThumbnails().getHigh().getUrl().toString());
                builder.setFooter(video.getSnippet().getPublishedAt().toString());
                uploadChannel.sendMessage(uploadChannel.getGuild().getPublicRole().getAsMention()).embed(builder.build()).queue();
                System.out.println("Sent Message and updated Video for " + video.getSnippet().getChannelTitle());
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
