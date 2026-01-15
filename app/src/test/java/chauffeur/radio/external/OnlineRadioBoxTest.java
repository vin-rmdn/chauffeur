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
import java.util.List;
import java.util.Scanner;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import chauffeur.radio.external.OnlineRadioBox.Song;
import chauffeur.radio.external.OnlineRadioBox.SongRecord;

@ExtendWith(MockitoExtension.class)
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

    @Mock
    HttpResponse<String> mockResponse;

    @Test
    void TestRepository_GetPlaylist_Successful() throws IOException, InterruptedException {
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

    @Test
    void TestGetPlaylist_Successful_Today() throws Exception {
        ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);

        try {
            when(mockHttpClient.<String>send(captor.capture(), any())).thenReturn(mockResponse);
        } catch (Exception e) {
            fail("Exception caught");
        }

        InputStream responseStream = getClass().getClassLoader()
                .getResourceAsStream("responses/online-radio-box-valid-response-ajax.json");
        if (responseStream == null)
            throw new RuntimeException("Test response file not found");

        try (Scanner scanner = new Scanner(responseStream, StandardCharsets.UTF_8.name())) {
            String response = scanner.useDelimiter("\\A").next();
            when(mockResponse.body()).thenReturn(response);
        }

        when(mockResponse.statusCode()).thenReturn(200);

        List<SongRecord> songRecords = classUnderTest.getPlaylist("playlist-id", 0);

        HttpRequest actualRequest = captor.getValue();
        assertEquals("http://fakehost/id/playlist-id/playlist/?ajax=1&tzLoc=Asia/Jakarta",
                actualRequest.uri().toString());

        assertNotNull(songRecords);

        songRecords.forEach(songRecord -> {
            System.out.printf("%s: %s - %s%n", songRecord.playedAt, songRecord.song.artist, songRecord.song.title);
        });
    }

    @Test
    void TestGetPlaylist_Successful_Yesterday() throws Exception {
        ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);

        try {
            when(mockHttpClient.<String>send(captor.capture(), any())).thenReturn(mockResponse);
        } catch (Exception e) {
            fail("Exception caught");
        }

        InputStream responseStream = getClass().getClassLoader()
                .getResourceAsStream("responses/online-radio-box-valid-response-ajax.json");
        if (responseStream == null)
            throw new RuntimeException("Test response file not found");

        try (Scanner scanner = new Scanner(responseStream, StandardCharsets.UTF_8.name())) {
            String response = scanner.useDelimiter("\\A").next();
            when(mockResponse.body()).thenReturn(response);
        }

        when(mockResponse.statusCode()).thenReturn(200);

        List<SongRecord> songRecords = classUnderTest.getPlaylist("playlist-id", 1);

        HttpRequest actualRequest = captor.getValue();
        assertEquals("http://fakehost/id/playlist-id/playlist/1?ajax=1&tzLoc=Asia/Jakarta",
                actualRequest.uri().toString());

        assertNotNull(songRecords);

        songRecords.forEach(songRecord -> {
            System.out.printf("%s: %s - %s%n", songRecord.playedAt, songRecord.song.artist, songRecord.song.title);
        });
    }
}
