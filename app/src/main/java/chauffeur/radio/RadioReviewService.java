package chauffeur.radio;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import chauffeur.radio.external.OnlineRadioBox.SongRecord;
import chauffeur.radio.review.LLMClient;

@Service
public class RadioReviewService {
    private final RadioService radioService;
    private final LLMClient llmClient;

    @Autowired
    public RadioReviewService(RadioService radioService, LLMClient llmClient) {
        this.radioService = radioService;
        this.llmClient = llmClient;
    }

    public static class RadioPlaylistReviewResponse {
        public String id;
        public Map<String, List<SongRecord>> playlists;
        public String review;

        public RadioPlaylistReviewResponse(String id, Map<String, List<SongRecord>> playlists, String review) {
            this.id = id;
            this.playlists = playlists;
            this.review = review;
        }
    }

    public RadioPlaylistReviewResponse GetReview(String id, List<Integer> dayOffsets) throws Exception {
        Map<String, List<SongRecord>> playlists = new HashMap<>();
        LocalDate today = LocalDate.now();

        for (Integer dayOffset : dayOffsets) {
            LocalDate playlistDate = today.minusDays(dayOffset.longValue());
            playlists.put(playlistDate.toString(), this.radioService.GetPlaylists(id, dayOffset));
        }

        String review = this.llmClient.Review(playlists);
        return new RadioPlaylistReviewResponse(id, playlists, review);
    }
}
