package chauffeur.controller;

import chauffeur.radio.RadioService;
import chauffeur.radio.external.OnlineRadioBox.SongRecord;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RadioController {
  @Autowired
  RadioService service;

  public static class RadioPlaylistResponse {
    public String id;
    public Map<String, List<SongRecord>> playlists;

    public RadioPlaylistResponse(String id) {
      this.id = id;
      this.playlists = new HashMap<>();
    }
  }

  @GetMapping("/radio/playlists/{id}")
  public RadioPlaylistResponse getRadioPlaylist(@PathVariable String id,
      @RequestParam(value = "day_offsets") List<Integer> dayOffsets) throws Exception {
    RadioPlaylistResponse response = new RadioPlaylistResponse(id);

    LocalDate today = LocalDate.now();
    for (Integer dayOffset : dayOffsets) {
      LocalDate playlistDate = today.minusDays((long) dayOffset);

      response.playlists.put(playlistDate.toString(), this.service.getPlaylists(id, dayOffset));
    }

    return response;
  }
}
