package chauffeur.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import chauffeur.radio.external.OnlineRadioBox;
import chauffeur.radio.external.OnlineRadioBox.SongRecord;

@RestController
public class Radio {
    @Autowired
    OnlineRadioBox onlineRadioBox;

    public static class RadioPlaylistResponse {
        public String id;
        public List<SongRecord> playlist;

        public RadioPlaylistResponse(String id) {
            this.id = id;
            this.playlist = new ArrayList<>();
        }
    }

    @GetMapping("/radio/playlists/{id}")
    public RadioPlaylistResponse GetRadioPlaylist(
        @PathVariable(name = "id") String id,
        @RequestParam(value="day_offsets") List<Integer> dayOffsets
    ) throws Exception {
        RadioPlaylistResponse response = new RadioPlaylistResponse(id);

        for (Integer dayOffset : dayOffsets)
            response.playlist.addAll(this.onlineRadioBox.getPlaylist(id, dayOffset));

        return response;
    }
}
