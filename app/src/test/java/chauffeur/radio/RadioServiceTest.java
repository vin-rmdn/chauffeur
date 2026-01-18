package chauffeur.radio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import chauffeur.radio.external.OnlineRadioBox;
import chauffeur.radio.external.OnlineRadioBox.Song;
import chauffeur.radio.external.OnlineRadioBox.SongRecord;

@ExtendWith(MockitoExtension.class)
public class RadioServiceTest {
    @Mock
    OnlineRadioBox mockRadioClient;

    @InjectMocks
    RadioService classInTest = new RadioService(mockRadioClient);

    @Test
    void TestRadioService_Successful() throws Exception {
        List<SongRecord> mockResponse = Arrays.asList(
                new SongRecord(
                        new Song("artist", "title"), "20:26"));
        when(mockRadioClient.getPlaylist("id", 1)).thenReturn(mockResponse);

        List<SongRecord> actual = classInTest.GetPlaylists("id", 1);
        assertEquals(mockResponse, actual);
    }
}
