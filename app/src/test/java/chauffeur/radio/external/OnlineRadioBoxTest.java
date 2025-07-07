package chauffeur.radio.external;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;

import org.junit.jupiter.api.Test;

import chauffeur.radio.external.OnlineRadioBox;
import chauffeur.radio.external.OnlineRadioBox.Song;

public class OnlineRadioBoxTest {
    HttpClient mockHttpClient = mock(HttpClient.class);

    final String host = "http://fakehost"; // TODO: use fake host instead

    OnlineRadioBox classUnderTest = new OnlineRadioBox(host, mockHttpClient);

    @SuppressWarnings("unlikely-arg-type")
    @Test
    void TestSongEquals_DifferentClass() {
        Song a = new Song("Rick Astley", "Never Gonna Give You Up");

        assertFalse(a.equals("this is not a song"));
    }

    @Test
    void TestSong_hashCode() {
        Song a = new Song("Rick Astley", "Never Gonna Give You Up");

        assertEquals(221180796, a.hashCode());
    }

    @Test
    void TestRepository_GetPlaylist_Failure_HTTPError() throws IOException, InterruptedException {
        when(mockHttpClient.<String>send(any(), any())).thenThrow(new IOException("this is a test exception"));

        assertThrows(IOException.class, () -> classUnderTest.GetPlaylist("most1058"));
    }

    @Test
    void TestRepository_GetPlaylist_Success_WithOneCorruptedSongFormat() throws IOException, InterruptedException {
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockHttpClient.<String>send(any(), any())).thenReturn(mockResponse);

        InputStream responseStream = getClass().getClassLoader()
                .getResourceAsStream("responses/online-radio-box-invalid-response-corrupted.html");
        if (responseStream == null)
            throw new RuntimeException("Test response file not found");

        try (Scanner scanner = new Scanner(responseStream, StandardCharsets.UTF_8.name())) {
            String response = scanner.useDelimiter("\\A").next();
            when(mockResponse.body()).thenReturn(response);
        }

        var trackLists = classUnderTest.GetPlaylist("most1058");
        assertNotNull(trackLists);

        HttpRequest expectedHttpRequest = HttpRequest
                .newBuilder(URI.create(String.format("%s/id/most1058/playlist/", host))).GET().build();
        try {
            verify(mockHttpClient).send(expectedHttpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            fail("Exception caught");
        }

        assertEquals(
                Arrays.asList(new Song("TINA TURNER", "BETTER BE GOOD TO ME")),
                trackLists);
    }

    @Test
    void TestRepository_GetPlaylist_Successful() throws IOException, InterruptedException {
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockHttpClient.<String>send(any(), any())).thenReturn(mockResponse);

        InputStream responseStream = getClass().getClassLoader()
                .getResourceAsStream("responses/online-radio-box-valid-response.html");
        if (responseStream == null)
            throw new RuntimeException("Test response file not found");

        try (Scanner scanner = new Scanner(responseStream, StandardCharsets.UTF_8.name())) {
            String response = scanner.useDelimiter("\\A").next();
            when(mockResponse.body()).thenReturn(response);
        }

        var trackLists = classUnderTest.GetPlaylist("most1058");
        assertNotNull(trackLists);

        HttpRequest expectedHttpRequest = HttpRequest
                .newBuilder(URI.create(String.format("%s/id/most1058/playlist/", host))).GET().build();
        try {
            verify(mockHttpClient).send(expectedHttpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            fail("Exception caught");
        }

        assertEquals(
                Arrays.asList(
                        new Song("SAVAGE GARDEN", "TO THE MOON & BACK"),
                        new Song("TINA TURNER", "BETTER BE GOOD TO ME")),
                trackLists);
    }
}
