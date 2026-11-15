package io.github.iliasiosifidis.digestservice.client;

// REPLACED WITH SPRING AI (chatClient)
/*
@Slf4j
@Component
public class LlmClient {

  public record ChatMessage(String role, String content){}
  public record ChatRequest(List<ChatMessage> messages,double temperature) {}
  public record ChatResponse(String model, List<Choice> choices) {
    public record Choice(@JsonProperty("finish_reason") String finishReason, ChatMessage message) {}
  }

  public record Stories(List<String> stories) {}

  public record Summary(String model, List<String> stories) {}

  private final RestClient restClient;

  public LlmClient(RestClient.Builder builder,
                   @Value("${digest.llm-url:http://localhost:8084}") String baseUrl) {
    var factory = new JdkClientHttpRequestFactory();
    factory.setReadTimeout(Duration.ofMinutes(20));
    this.restClient = builder.baseUrl(baseUrl).requestFactory(factory).build();
  }

  public String summarize(String systemPrompt, String userPrompt){
    ChatRequest request = new ChatRequest(
            List.of(new ChatMessage("system", systemPrompt), new ChatMessage("user", userPrompt)),
            0.2);
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
    log.info("Answer from the model {}", response.model());
    return choice.message().content().strip();
  }
}
 */
