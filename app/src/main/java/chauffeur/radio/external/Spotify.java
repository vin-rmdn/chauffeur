package chauffeur.radio.external;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

public class Spotify {
    private String accountHost;
    // private String apiHost;

    private String clientID;
    private String clientSecret;

    private HttpClient httpClient;

    private final ObjectMapper objectMapper;

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Token {
        public String accessToken;
        public int expiresInSecond;
    }

    private Token token;

    public Spotify(String accountHost, String apiHost, String clientID, String clientSecret, HttpClient httpClient) {
        this.accountHost = accountHost;
        // this.apiHost = apiHost;
        this.httpClient = httpClient;
        this.clientID = clientID;
        this.clientSecret = clientSecret;

        this.objectMapper = new ObjectMapper();
    }

    class LoginException extends Exception {
        public LoginException(String message) {
            super(message);
        }
    }

    public void Login() throws LoginException {
        Map<String, String> formData = new HashMap<>();
        formData.put("grant_type", "client_credentials");
        formData.put("client_id", clientID);
        formData.put("client_secret", clientSecret);

        String form = formData.entrySet().stream()
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        URI uri = URI.create(String.format("%s/api/login", this.accountHost));
        HttpRequest httpRequest = HttpRequest.newBuilder(uri)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form)).build();

        HttpResponse<String> response;
        try {
            response = this.httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new LoginException(e.getMessage());
        }

        Token token;
        try {
            token = this.objectMapper.readValue(response.body(), Token.class);
        } catch (Exception e) {
            throw new LoginException("Caught exception on mapping JSON to LoginResponse: " + e.getMessage());
        }

        this.token = token;
    }
}
