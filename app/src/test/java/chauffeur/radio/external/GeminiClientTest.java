package chauffeur.radio.external;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpServerErrorException;

import chauffeur.radio.external.OnlineRadioBox.Song;
import chauffeur.radio.external.OnlineRadioBox.SongRecord;
import chauffeur.radio.review.LLMClient;

@ExtendWith(MockitoExtension.class)
public class GeminiClientTest {
    HttpClient mockHttpClient = mock(HttpClient.class);

    GeminiClient classUnderTest = new GeminiClient(
            "https://generativelanguage.googleapis.com",
            "test-api-key",
            "gemini-2.0-flash",
            mockHttpClient);

    @Mock
    HttpResponse<String> mockResponse;

    @Test
    void review_UsesAbstractLLMClientContract() {
        LLMClient client = classUnderTest;
        assertEquals(GeminiClient.class, client.getClass());
    }

    @Test
    void review_Successful() throws Exception {
        ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
        when(mockHttpClient.<String>send(captor.capture(), any())).thenReturn(mockResponse);

        InputStream responseStream = getClass().getClassLoader()
                .getResourceAsStream("responses/gemini-valid-response.json");
        if (responseStream == null) {
            throw new RuntimeException("Test response file not found");
        }

        try (Scanner scanner = new Scanner(responseStream, StandardCharsets.UTF_8.name())) {
            String response = scanner.useDelimiter("\\A").next();
            when(mockResponse.body()).thenReturn(response);
        }
        when(mockResponse.statusCode()).thenReturn(200);

        Map<String, List<SongRecord>> playlists = new HashMap<>();
        playlists.put("2026-02-16", List.of(new SongRecord(new Song("Artist", "Title"), "09:00")));

        String review = classUnderTest.Review(playlists);

        HttpRequest request = captor.getValue();
        assertEquals(
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=test-api-key",
                request.uri().toString());
        assertEquals("The station has a balanced upbeat mix.", review);
    }

    @Test
    void review_Non200_ThrowsException() throws Exception {
        when(mockHttpClient.<String>send(any(), any())).thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(500);

        Map<String, List<SongRecord>> playlists = new HashMap<>();
        playlists.put("2026-02-16", List.of(new SongRecord(new Song("Artist", "Title"), "09:00")));

        assertThrows(HttpServerErrorException.class, () -> classUnderTest.Review(playlists));
    }

    @Test
    void review_PromptContainsRequiredSections() throws Exception {
        Map<String, List<SongRecord>> playlists = new HashMap<>();
        playlists.put("2026-02-16", List.of(new SongRecord(new Song("Artist", "Title"), "09:00")));

        Method method = GeminiClient.class.getDeclaredMethod("buildPrompt", Map.class);
        method.setAccessible(true);
        String prompt = (String) method.invoke(classUnderTest, playlists);

        assertTrue(prompt.contains("Summary of the playlists"));
        assertTrue(prompt.contains("Most played genre"));
        assertTrue(prompt.contains("Standout tracks or artists"));
        assertTrue(prompt.contains("Noticeable patterns"));
    }
}