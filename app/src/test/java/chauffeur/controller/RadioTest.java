package chauffeur.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import chauffeur.radio.RadioService;
import chauffeur.radio.external.OnlineRadioBox.Song;
import chauffeur.radio.external.OnlineRadioBox.SongRecord;

@WebMvcTest(RadioController.class)
public class RadioTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    RadioService radioService;

    @Test
    void getRadioPlaylist_Successful() throws Exception {
        List<SongRecord> mockSongRecords = List.of(new SongRecord(new Song("Artist", "Title"), "09:00"));
        when(radioService.GetPlaylists("test-id", 0)).thenReturn(mockSongRecords);

        mockMvc.perform(get("/radio/playlists/test-id")
                .param("day_offsets", "0"))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String content = result.getResponse().getContentAsString();
                    assertNotNull(content);
                });
    }
}
