package chauffeur.radio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import chauffeur.radio.external.OnlineRadioBox.Song;
import chauffeur.radio.external.OnlineRadioBox.SongRecord;
import chauffeur.radio.review.LLMClient;

@ExtendWith(MockitoExtension.class)
public class RadioReviewServiceTest {
    @Mock
    RadioService radioService;

    @Mock
    LLMClient llmClient;

    @InjectMocks
    RadioReviewService classUnderTest;

    @Test
    void getReview_Successful() throws Exception {
        List<SongRecord> todayPlaylist = List.of(new SongRecord(new Song("Artist A", "Title A"), "09:00"));
        List<SongRecord> yesterdayPlaylist = List.of(new SongRecord(new Song("Artist B", "Title B"), "10:00"));

        when(radioService.GetPlaylists("station-1", 0)).thenReturn(todayPlaylist);
        when(radioService.GetPlaylists("station-1", 1)).thenReturn(yesterdayPlaylist);
        when(llmClient.Review(any())).thenReturn("Balanced pop and rock rotation");

        RadioReviewService.RadioPlaylistReviewResponse response = classUnderTest.GetReview("station-1", List.of(0, 1));

        assertEquals("station-1", response.id);
        assertEquals("Balanced pop and rock rotation", response.review);

        String today = LocalDate.now().toString();
        String yesterday = LocalDate.now().minusDays(1).toString();

        assertTrue(response.playlists.containsKey(today));
        assertTrue(response.playlists.containsKey(yesterday));
        assertEquals(todayPlaylist, response.playlists.get(today));
        assertEquals(yesterdayPlaylist, response.playlists.get(yesterday));

        verify(llmClient).Review(response.playlists);
    }
}
