package chauffeur.radio.external;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpServerErrorException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import chauffeur.radio.external.OnlineRadioBox.SongRecord;
import chauffeur.radio.review.LLMClient;

@Repository
public class GeminiClient extends LLMClient {
    private final String host;
    private final String apiKey;
    private final String model;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public GeminiClient(
            @Value("${gemini.host}") String host,
            @Value("${gemini.api_key}") String apiKey,
            @Value("${gemini.model}") String model) {
        this(host, apiKey, model, HttpClient.newHttpClient());
    }

    public GeminiClient(String host, String apiKey, String model, HttpClient httpClient) {
        this.host = host;
        this.apiKey = apiKey;
        this.model = model;
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper();
    }

    public String Review(Map<String, List<SongRecord>> playlists) throws Exception {
        String prompt = buildPrompt(playlists);
        GeminiGenerateContentRequest body = new GeminiGenerateContentRequest(prompt);

        URI uri = URI.create(String.format("%s/v1beta/models/%s:generateContent?key=%s", host, model, apiKey));

        HttpRequest request = HttpRequest.newBuilder(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .timeout(Duration.ofSeconds(30))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new HttpServerErrorException(HttpStatusCode.valueOf(response.statusCode()));
        }

        GeminiGenerateContentResponse parsedResponse = objectMapper.readValue(response.body(),
                GeminiGenerateContentResponse.class);

        if (parsedResponse.candidates == null || parsedResponse.candidates.isEmpty()) {
            return "";
        }

        Candidate candidate = parsedResponse.candidates.get(0);
        if (candidate.content == null || candidate.content.parts == null || candidate.content.parts.isEmpty()) {
            return "";
        }

        Part part = candidate.content.parts.get(0);
        return part.text == null ? "" : part.text;
    }

    private String buildPrompt(Map<String, List<SongRecord>> playlists) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Review the following radio playlists in 4 concise sections:\\n");
        prompt.append("1) Summary of the playlists.\\n");
        prompt.append("2) Most played genre(s).\\n");
        prompt.append("3) Standout tracks or artists.\\n");
        prompt.append("4) Noticeable patterns (e.g. repetition, time-of-day mood, era trends).\\n");

        playlists.forEach((date, records) -> {
            prompt.append("Date: ").append(date).append("\\n");
            for (SongRecord record : records) {
                prompt.append("- ")
                        .append(record.playedAt)
                        .append(" ")
                        .append(record.song.artist)
                        .append(" - ")
                        .append(record.song.title)
                        .append("\\n");
            }
        });

        return prompt.toString();
    }

    private static class GeminiGenerateContentRequest {
        public List<Content> contents;

        public GeminiGenerateContentRequest(String prompt) {
            this.contents = List.of(new Content(prompt));
        }
    }

    private static class Content {
        public List<Part> parts;

        public Content(String prompt) {
            this.parts = List.of(new Part(prompt));
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GeminiGenerateContentResponse {
        public List<Candidate> candidates = new ArrayList<>();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Candidate {
        public ContentResponse content;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ContentResponse {
        public List<Part> parts;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Part {
        public String text;

        public Part() {
        }

        public Part(String text) {
            this.text = text;
        }
    }
}