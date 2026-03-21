package chauffeur.radio.external;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import chauffeur.radio.external.Spotify.LoginException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SpotifyTest {
  HttpClient mockHttpClient = mock(HttpClient.class);
  Spotify classInTest = new Spotify("http://accountHost", "http://apiHost", "clientID",
      "clientSecret", mockHttpClient);

  @Mock
  HttpResponse<String> mockHttpResponse;

  @Test
  void TestSpotify_Login_InvalidResponse() throws Exception {
    when(mockHttpClient.<String>send(any(), any())).thenReturn(mockHttpResponse);

    InputStream responseStream = getClass().getClassLoader()
        .getResourceAsStream("responses/spotify-login-invalid-response-broken-json.json");

    try (Scanner scanner = new Scanner(responseStream, StandardCharsets.UTF_8.name())) {
      String response = scanner.useDelimiter("\\A").next();
      when(mockHttpResponse.body()).thenReturn(response);
    }

    assertThrows(LoginException.class, () -> classInTest.login());
  }

  @Test
  void TestSpotify_Login_Successful() throws Exception {
    when(mockHttpClient.<String>send(any(), any())).thenReturn(mockHttpResponse);

    InputStream responseStream = getClass().getClassLoader()
        .getResourceAsStream("responses/spotify-login-valid-response.json");

    try (Scanner scanner = new Scanner(responseStream, StandardCharsets.UTF_8.name())) {
      String response = scanner.useDelimiter("\\A").next();
      when(mockHttpResponse.body()).thenReturn(response);
    }

    assertDoesNotThrow(() -> classInTest.login());
  }
}
