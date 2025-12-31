package chauffeur.radio.external;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpServerErrorException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

@Repository
public class OnlineRadioBox {
    private String host;
    private HttpClient httpClient;

    private static final Logger logger = LoggerFactory.getLogger(OnlineRadioBox.class);
    private ObjectMapper mapper;

    public static class InvalidFormatException extends Exception {
        public InvalidFormatException(String message) {
            super(message);
        }
    }

    public static class SongRecord {
        public Song song;
        public String playedAt; // TODO: change to datetime

        public SongRecord(Song song, String playedAt) {
            this.song = song;
            this.playedAt = playedAt;
        }
    }

    public static class Song {
        public String title;
        public String artist;

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

    @Autowired
    public OnlineRadioBox(@Value("${online_radio_box.host}") String host) {
        this(host, HttpClient.newHttpClient());
    }

    public OnlineRadioBox(String host, HttpClient httpClient) {
        mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

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

    public List<SongRecord> getPlaylist(String id) throws IOException, InterruptedException, URISyntaxException {
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
                        .log("unable to parse into Song"); // TODO: iron out empty songs issue

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

    private PlaylistAPIResponse callPlaylistAPI(String id)
            throws IOException, InterruptedException, URISyntaxException {
        URI uri = new URI(String.format("%s/id/%s/playlist/?%s", this.host, id, "ajax=1&tzLoc=Asia/Jakarta"));

        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().timeout(Duration.ofSeconds(10)).build();
        HttpResponse<String> httpResponse = this.httpClient.send(request, BodyHandlers.ofString());

        if (httpResponse.statusCode() != 200) {
            int statusCode = httpResponse.statusCode();
            throw new HttpServerErrorException(HttpStatusCode.valueOf(statusCode));
        }

        PlaylistAPIResponse response = mapper.readValue(httpResponse.body(), PlaylistAPIResponse.class);

        return response;
    }
}
