package chauffeur.radio.external;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OnlineRadioBox {
    private String host;
    private HttpClient httpClient;

    private static final Logger logger = LoggerFactory.getLogger(OnlineRadioBox.class);

    public static class InvalidFormatException extends Exception {
        public InvalidFormatException(String message) {
            super(message);
        }
    }

    public static class SongRecord {
        Song song;
        String playedAt; // TODO: change to datetime

        public SongRecord(Song song, String playedAt) {
            this.song = song;
            this.playedAt = playedAt;
        }
    }

    public static class Song {
        String title;
        String artist;

        public Song(String artist, String title) {
            this.artist = artist;
            this.title = title;
        }

        public Song(String artistTitleEntry) throws InvalidFormatException {
            String[] entries = artistTitleEntry.split(" - ");
            if (entries.length != 2) {
                throw new InvalidFormatException(artistTitleEntry);
            }

            this.artist = entries[0];
            this.title = entries[1];
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Song))
                return false;

            Song obj = (Song) o;
            return (this.artist.equals(obj.artist) && this.title.equals(obj.title));
        }

        @Override
        public int hashCode() {
            return String.format("%s - %s", artist, title).hashCode();
        }
    }

    public OnlineRadioBox(String host) {
        this(host, HttpClient.newHttpClient());
    }

    public OnlineRadioBox(String host, HttpClient httpClient) {
        this.host = host;
        this.httpClient = httpClient;
    }

    public List<Song> GetPlaylist(String id) throws IOException, InterruptedException {
        URI uri = URI.create(String.format("%s/id/%s/playlist/", this.host, id));
        HttpRequest httpRequest = HttpRequest.newBuilder(uri).GET().build();

        // TODO: add ajax=1 to parameter and adjust respose parsing

        HttpResponse<String> response;

        try {
            response = this.httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            logger.atError().addKeyValue("exception", e).log("failed to execute http");

            throw e;
        }

        Document doc = Jsoup.parse(response.body());
        Elements elements = doc.select("td.track_history_item a");

        List<Song> trackLists = new ArrayList<>();

        for (Element element : elements) {
            try {
                Song song = new Song(element.text());

                trackLists.add(song);
            } catch (InvalidFormatException e) {
                logger.atWarn().addKeyValue("inputString", element.text()).log("unable to parse into Song");

                continue;
            }

        }

        return trackLists;
    }

    public List<SongRecord> getPlaylist(String id) {
        PlaylistAPIResponse response = callPlaylistAPI(id);

        Document doc = Jsoup.parse(response.data);
        Elements elements = doc.select("tr");

        List<SongRecord> trackLists = new ArrayList<>();
        elements.forEach(element -> {
            Song song;
            try {
                song = new Song(element.select("td.track_history_item").text());
            } catch (InvalidFormatException e) {
                logger.atWarn().addKeyValue("inputString", element.select("td.track_history_item").text())
                        .log("unable to parse into Song");

                return;
            }

            trackLists.add(new SongRecord(
                    song,
                    element.select("span.time--schedule").text()));
        });

        return trackLists;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class PlaylistAPIResponse {
        public String data;
    }

    private static final ObjectMapper mapper = new ObjectMapper();

    private PlaylistAPIResponse callPlaylistAPI(String id) {
        URI uri;
        try {
            uri = new URI(String.format("%s/id/%s/playlist/%s", this.host, id, "ajax=1&tzLoc=Asia/Jakarta"));
        } catch (Exception e) {
            logger.atError().addKeyValue("exception", e).log("failed to create uri");

            return null;
        }

        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().timeout(Duration.ofSeconds(10)).build();

        HttpResponse<String> httpResponse;
        try {
            httpResponse = this.httpClient.send(request, BodyHandlers.ofString());
        } catch (Exception e) {
            logger.atError().addKeyValue("exception", e).log("failed to execute http");

            return null;
        }

        PlaylistAPIResponse response;
        try {
            response = mapper.readValue(httpResponse.body(), PlaylistAPIResponse.class);
        } catch (Exception e) {
            logger.atError().addKeyValue("exception", e).log("failed to parse response body");

            return null;
        }

        return response;
    }
}
