package chauffeur.llm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.kherud.llama.InferenceParameters;
import de.kherud.llama.LlamaModel;
import de.kherud.llama.Pair;

public class TestLLM {
    @Mock
    private LlamaModel mockModel;

    @InjectMocks
    private LLM classInTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        classInTest = new LLM(mockModel);
    }

    @Test
    void TestLLM_Successful() {
        String mockResponse = "What's up? I'm a mocked response so test should work properly";
        when(mockModel.complete(any(InferenceParameters.class))).thenReturn(mockResponse);

        LLMResponse llmResponse = classInTest.converse("Hello there!", null);
        assertEquals(mockResponse, llmResponse.response);

        List<Pair<String,String>> expectedChatHistory = new ArrayList<>();
        expectedChatHistory.add(new Pair<>("user", "Hello there!"));
        expectedChatHistory.add(new Pair<>("assistant", mockResponse));
        assertEquals(expectedChatHistory, llmResponse.chatHistory);
    }

    @Test
    void TestLLM_Successful_WithChatHistory() {
        List<Pair<String, String>> chatHistory = new ArrayList<>();
        chatHistory.add(new Pair<String, String>("user", "Should I go to the malls?"));
        chatHistory.add(new Pair<String, String>("assistant", "You should. Malls are center for shopping places."));

        String mockResponse = "Yes, I remember. This is a mocked response of an LLM so test should work properly";
        when(mockModel.complete(any(InferenceParameters.class))).thenReturn(mockResponse);

        LLMResponse llmResponse = classInTest.converse("Remember what I was asking previously?", chatHistory);
        assertEquals(llmResponse.response, mockResponse);

        List<Pair<String, String>> expectedChatHistory = new ArrayList<>();
        expectedChatHistory.add(new Pair<>("user", "Should I go to the malls?"));
        expectedChatHistory.add(new Pair<>("assistant", "You should. Malls are center for shopping places."));
        expectedChatHistory.add(new Pair<>("user", "Remember what I was asking previously?"));
        expectedChatHistory.add(new Pair<>("assistant", llmResponse.response));
        assertEquals(expectedChatHistory, llmResponse.chatHistory);

        verify(mockModel, times(1)).complete(any(InferenceParameters.class));
    }
}
