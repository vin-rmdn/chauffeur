package chauffeur.llm;

import java.util.List;

import de.kherud.llama.Pair;

public class LLMResponse {
    public String response;
    public List<Pair<String, String>> chatHistory; // Updated with the new prompt

    public LLMResponse setResponse(String responseString) {
        response = responseString;

        return this;
    }

    public LLMResponse setChatHistory(List<Pair<String, String>> chatHistoryList) {
        chatHistory = chatHistoryList;

        return this;
    }
}
