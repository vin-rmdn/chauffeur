package chauffeur.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
        }
    }

    @GetMapping("/radio/status/{id}")
    public RadioPlaylistResponse GetRadioPlaylist(@PathVariable(name = "id") String id) throws Exception {
        RadioPlaylistResponse response = new RadioPlaylistResponse(id);
        response.playlist = this.onlineRadioBox.getPlaylist(id, 0);

        return response;
    }
}
