package chauffeur.llm;

import de.kherud.llama.Pair;
import java.util.List;

public class LargeLanguageModelResponse {
  public String response;
  public List<Pair<String, String>> chatHistory; // Updated with the new prompt

  public LargeLanguageModelResponse setResponse(String responseString) {
    response = responseString;

    return this;
  }

  public LargeLanguageModelResponse setChatHistory(List<Pair<String, String>> chatHistoryList) {
    chatHistory = chatHistoryList;

    return this;
  }
}
