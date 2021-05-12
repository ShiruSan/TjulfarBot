package de.Shiru.TjulfarBot.Youtube;

import java.net.URL;
import java.util.Date;

public class Item {
    private String kind;
    private String etag;
    private ID id;
    private Snippet snippet;

    public String getKind() {
        return kind;
    }

    public String getEtag() {
        return etag;
    }

    public ID getId() {
        return id;
    }

    public Snippet getSnippet() {
        return snippet;
    }

    public String getAsVideoURL() {
        return "https://www.youtube.com/watch?v=" + id.getVideoId();
    }

    public static class ID {
        private String kind;
        private String videoId;

        public String getKind() {
            return kind;
        }

        public String getVideoId() {
            return videoId;
        }

    }

    public static class Snippet {
        private Date publishedAt;
        private String channelId;
        private String title;
        private String description;
        private Thumbnail thumbnails;
        private String channelTitle;
        private String liveBroadcastContent;
        private Date publishTime;

        public Date getPublishedAt() {
            return publishedAt;
        }

        public String getChannelId() {
            return channelId;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public Thumbnail getThumbnails() {
            return thumbnails;
        }

        public String getChannelTitle() {
            return channelTitle;
        }

        public String getLiveBroadcastContent() {
            return liveBroadcastContent;
        }

        public Date getPublishTime() {
            return publishTime;
        }

    }

    public static class Thumbnail {
        private UrlPicture Default;
        private UrlPicture medium;
        private UrlPicture high;

        public UrlPicture getDefault() {
            return Default;
        }

        public UrlPicture getMedium() {
            return medium;
        }

        public UrlPicture getHigh() {
            return high;
        }

    }

    public static class UrlPicture {
        private URL url;
        private int height;
        private int width;

        public URL getUrl() {
            return url;
        }

        public int getHeight() {
            return height;
        }

        public int getWidth() {
            return width;
        }
    }

}
