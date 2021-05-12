package de.Shiru.TjulfarBot.Youtube;

public class SearchResponse {
    private String kind;
    private String etag;
    private String nextPageToken;
    private String regionCode;
    private Item[] items;

    public String getKind() {
        return kind;
    }

    public String getEtag() {
        return etag;
    }

    public String getNextPageToken() {
        return nextPageToken;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public Item[] getItems() {
        return items;
    }

    public static class PageInfo {
        private int totalResults;
        private int resultsPerPage;

        public int getTotalResults() {
            return totalResults;
        }

        public int getResultsPerPage() {
            return resultsPerPage;
        }

    }

}
