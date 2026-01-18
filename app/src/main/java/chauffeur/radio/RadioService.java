package chauffeur.radio;

import java.util.List;

import org.springframework.stereotype.Service;

import chauffeur.radio.external.OnlineRadioBox;
import chauffeur.radio.external.OnlineRadioBox.SongRecord;

@Service
public class RadioService {
    OnlineRadioBox radioClient;

    public RadioService(OnlineRadioBox radioClient) {
        this.radioClient = radioClient;
    }
    
    public List<SongRecord> GetPlaylists(String id, int dayOffset) throws Exception {
        List<SongRecord> songRecords = this.radioClient.getPlaylist(id, dayOffset);

        return songRecords;
    }
}
