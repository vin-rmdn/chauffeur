package chauffeur.radio;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
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
    RadioService classInTest = new RadioService(mockRadioClient, List.of("filtered-artist"), List.of("filtered-title"));

    @Test
    void TestRadioService_Successful() throws Exception {
        SongRecord actualRecord = new SongRecord(new Song("artist", "title"), "20:26");

        List<SongRecord> mockResponse = new ArrayList<SongRecord>();
        mockResponse.add(actualRecord);
        mockResponse.add(new SongRecord(new Song("filtered-artist", "title2"), "20:30"));
        mockResponse.add(new SongRecord(new Song("artist2", "filtered-title"), "20:35"));
        when(mockRadioClient.getPlaylist("id", 1)).thenReturn(mockResponse);

        List<SongRecord> actual = classInTest.GetPlaylists("id", 1);

        List<SongRecord> expected = Arrays.asList(actualRecord);
        Assertions.assertArrayEquals(expected.toArray(), actual.toArray());
    }
}
