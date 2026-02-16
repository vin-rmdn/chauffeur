package chauffeur.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import chauffeur.radio.RadioService;
import chauffeur.radio.RadioReviewService;
import chauffeur.radio.RadioReviewService.RadioPlaylistReviewResponse;
import chauffeur.radio.external.OnlineRadioBox.Song;
import chauffeur.radio.external.OnlineRadioBox.SongRecord;

@WebMvcTest(RadioController.class)
public class RadioTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    RadioService radioService;

    @MockitoBean
    RadioReviewService radioReviewService;

    @Test
    void getRadioPlaylist_Successful() throws Exception {
        List<SongRecord> mockSongRecords = List.of(new SongRecord(new Song("Artist", "Title"), "09:00"));
        when(radioService.GetPlaylists("test-id", 0)).thenReturn(mockSongRecords);

        String today = LocalDate.now().toString();
        String expectedContentTemplate = new String(
                Files.readAllBytes(
                        Paths.get(
                                "src/test/resources/chauffeur/controller/responses/radio-playlist-valid-response-template.json")));
        String expectedContent = String.format(expectedContentTemplate, today);

        mockMvc.perform(get("/radio/playlists/test-id")
                .param("day_offsets", "0"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedContent));
    }

    @Test
    void getRadioReview_Successful() throws Exception {
        HashMap<String, List<SongRecord>> playlists = new HashMap<>();
        playlists.put("2026-02-16", List.of(new SongRecord(new Song("Artist", "Title"), "09:00")));

        RadioPlaylistReviewResponse mockResponse = new RadioPlaylistReviewResponse("test-id", playlists,
                "The playlist has a strong pop focus.");

        when(radioReviewService.GetReview("test-id", List.of(0))).thenReturn(mockResponse);

        String expectedContent = new String(
                Files.readAllBytes(
                        Paths.get(
                                "src/test/resources/chauffeur/controller/responses/radio-review-valid-response.json")));

        mockMvc.perform(get("/radio/reviews/test-id")
                .param("day_offsets", "0"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedContent));
    }
}
