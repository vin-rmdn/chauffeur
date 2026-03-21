package chauffeur.llm;

import de.kherud.llama.InferenceParameters;
import de.kherud.llama.LlamaModel;
import de.kherud.llama.Pair;
import de.kherud.llama.args.MiroStat;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LargeLanguageModel {
  private LlamaModel model;
  // private final String systemPrompt = """
  // <<SYS>>
  // For this messaging session your name is Dono, an Artificial Intelligence
  // assistant assigned as a Discord bot to chat with users like how they would
  // chat a human. Use normal, short, non-formal English to respond, and answer
  // like usual.

  // You are an assistant, and you should only answer after "Assistant:".

  // Answer like how people chat, and chat in messaging lingo. Keep the answer
  // short.
  // <</SYS>>
  // """;

  private static final Logger logger = LoggerFactory.getLogger(LargeLanguageModel.class);

  public LargeLanguageModel(LlamaModel model) {
    this.model = model;
  }

  public LargeLanguageModelResponse converse(String message,
      List<Pair<String, String>> chatHistory) {
    final String template = """
        %s:
        %s

        """;

    if (chatHistory == null) {
      chatHistory = new ArrayList<Pair<String, String>>();
    }

    StringBuilder promptBuilder = new StringBuilder();
    for (Pair<String, String> chat : chatHistory) {
      promptBuilder.append(String.format(template, chat.getKey().toUpperCase(), chat.getValue()));
    }

    promptBuilder.append(String.format(template, "User", message));
    promptBuilder.append("Assistant:\n");

    logger.info("Built prompt: {}", promptBuilder.toString());

    String templatedMessage = promptBuilder.toString();

    InferenceParameters inferenceParameters = new InferenceParameters(templatedMessage)
        .setTemperature(0.7f).setPenalizeNl(true).setMiroStat(MiroStat.V2)
        .setStopStrings("User:", "[INST]").setTopK(40).setRepeatPenalty(1.1f).setTopP(0.95f)
        .setMinP(0.05f).setInputPrefix("User:");

    String response = model.complete(inferenceParameters);

    chatHistory.add(new Pair<String, String>("user", message));
    chatHistory.add(new Pair<String, String>("assistant", response));

    LargeLanguageModelResponse llmResponse = new LargeLanguageModelResponse()
        .setChatHistory(chatHistory).setResponse(response);

    return llmResponse;
  }
}
