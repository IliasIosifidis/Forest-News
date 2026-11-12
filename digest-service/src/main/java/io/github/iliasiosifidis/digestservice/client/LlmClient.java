package io.github.iliasiosifidis.digestservice.client;

import io.github.iliasiosifidis.digestservice.dto.ChatMessage;
import io.github.iliasiosifidis.digestservice.dto.ChatRequest;
import io.github.iliasiosifidis.digestservice.dto.ChatResponse;
import io.github.iliasiosifidis.digestservice.dto.Stories;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class LlmClient {

  private final RestClient restClient;
  private final JsonMapper jsonMapper;
  private static final Map<String, Object> RESPONSE_FORMAT = Map.of(
          "type", "json_schema",
          "json_schema", Map.of(
                  "name", "digest",
                  "strict", true,
                  "schema", Map.of(
                          "type", "object",
                          "properties", Map.of(
                                  "stories", Map.of(
                                          "type", "array",
                                          "items", Map.of("type", "string"),
                                          "maxItems", 10)),
                          "required", List.of("stories"))));

  public LlmClient(RestClient.Builder builder, JsonMapper jsonMapper,
                   @Value("${digest.llm-url:http://localhost:8084}") String baseUrl) {
    var factory = new JdkClientHttpRequestFactory();
    factory.setReadTimeout(Duration.ofMinutes(20));
    this.restClient = builder.baseUrl(baseUrl).requestFactory(factory).build();
    this.jsonMapper = jsonMapper;
  }

  public List<String > summarize(String systemPrompt, String userPrompt){
    ChatRequest request = new ChatRequest(
            List.of(new ChatMessage("system", systemPrompt), new ChatMessage("user", userPrompt)),
            0.2,
            RESPONSE_FORMAT);
    ChatResponse response = restClient.post()
            .uri("/v1/chat/completions")
            .body(request)
            .retrieve()
            .body(ChatResponse.class);

    if (response == null || response.choices().isEmpty()){
      throw new IllegalStateException("Empty response from the model");
    }
    ChatResponse.Choice choice = response.choices().getFirst();
    if (!"stop".equals(choice.finishReason())){
      throw new IllegalStateException("Model stopped early: " + choice.finishReason());
    }
    return jsonMapper.readValue(choice.message().content(), Stories.class).stories();

  }
}
