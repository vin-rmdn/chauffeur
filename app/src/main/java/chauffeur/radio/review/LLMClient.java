package chauffeur.radio.review;

import java.util.List;
import java.util.Map;

import chauffeur.radio.external.OnlineRadioBox.SongRecord;

public abstract class LLMClient {
    public abstract String Review(Map<String, List<SongRecord>> playlists) throws Exception;
}