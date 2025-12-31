package chauffeur.radio;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import chauffeur.radio.external.OnlineRadioBox;
import chauffeur.radio.external.OnlineRadioBox.SongRecord;

@Controller
public class RadioController {
    OnlineRadioBox onlineRadioBox;

    public RadioController(OnlineRadioBox onlineRadioBox) {
        this.onlineRadioBox = onlineRadioBox;
    }

    public static class RadioPlaylistResponse {
        public String id;
        public List<SongRecord> playlist;

        public RadioPlaylistResponse(String id) {
            this.id = id;
        }
    }

    @GetMapping("/radio/status/{id}")
    public RadioPlaylistResponse GetRadioPlaylist(@PathVariable(name = "id") String id) {
        RadioPlaylistResponse response = new RadioPlaylistResponse(id);
        response.playlist = this.onlineRadioBox.getPlaylist(id);

        return response;
    }
}
