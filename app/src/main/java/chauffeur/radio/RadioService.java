package chauffeur.radio;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import chauffeur.radio.external.OnlineRadioBox;
import chauffeur.radio.external.OnlineRadioBox.SongRecord;

@Service
public class RadioService {
    OnlineRadioBox radioClient;
    List<String> excludedArtists;
    List<String> excludedTitles;

    @Autowired
    public RadioService(
            OnlineRadioBox radioClient,
            @Value("${radio.excluded_artists}") List<String> excludedArtists,
            @Value("${radio.excluded_titles}") List<String> excludedTitles) {
        this.radioClient = radioClient;
        this.excludedArtists = excludedArtists;
        this.excludedTitles = excludedTitles;
    }

    public List<SongRecord> GetPlaylists(String id, int dayOffset) throws Exception {
        List<SongRecord> songRecords = this.radioClient.getPlaylist(id, dayOffset);

        songRecords.removeIf(rec -> this.excludedArtists.contains(rec.song.artist));
        songRecords.removeIf(rec -> this.excludedTitles.contains(rec.song.title));

        return songRecords;
    }
}
