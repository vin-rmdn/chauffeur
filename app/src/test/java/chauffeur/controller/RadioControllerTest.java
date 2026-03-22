package chauffeur.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import chauffeur.discord.Discord;
import chauffeur.radio.RadioService;
import chauffeur.radio.external.OnlineRadioBox.Song;
import chauffeur.radio.external.OnlineRadioBox.SongRecord;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RadioController.class)
public class RadioControllerTest {
  @Autowired
  MockMvc mockMvc;

  @MockitoBean
  RadioService radioService;

  @MockitoBean
  Discord discord; // noop

  @Test
  void getRadioPlaylist_Successful() throws Exception {
    List<SongRecord> mockSongRecords = List
        .of(new SongRecord(new Song("Artist", "Title"), "09:00"));
    when(radioService.getPlaylists("test-id", 0)).thenReturn(mockSongRecords);

    String expectedContentTemplate = new String(Files.readAllBytes(Paths.get(
        "src/test/resources/chauffeur/controller/responses/radio-playlist-valid-response.json")));
    assertNotNull(expectedContentTemplate);

    mockMvc.perform(get("/radio/playlists/test-id").param("day_offsets", "0"))
        .andExpect(status().isOk())
        .andExpect(content().json(expectedContentTemplate.formatted(LocalDate.now().minusDays(0))));
  }
}
