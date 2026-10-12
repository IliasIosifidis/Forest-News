package io.github.iliasIosifidis.newsingestor.source.freenews;

import io.github.iliasIosifidis.newsingestor.article.Article;
import io.github.iliasIosifidis.newsingestor.dedup.SeenArticles;
import io.github.iliasIosifidis.newsingestor.source.NewsSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class FreeNewsSource implements NewsSource {

  private static final String LANGUAGE = "en";
  private static final Duration PAUSE_BETWEEN_DETAILS = Duration.ofMillis(600);

  private final FreeNewsProperties properties;
  private final SeenArticles seenArticles;
  private final RestClient restClient;
  private final String disabledReason;

  public FreeNewsSource(FreeNewsProperties properties, SeenArticles seenArticles, RestClient.Builder builder, String disabledReason) {
    this.properties = properties;
    this.seenArticles = seenArticles;
    RestClient client = null;
    String reason = validate(properties);
    if (reason == null){
      try {
        client = builder
                .baseUrl(properties.baseUrl())
                .defaultHeader("x-api-key", properties.apiKey())
                .build();
      } catch (RuntimeException e){
        reason = "Invalid base URL: " + e.getMessage();
      }
    }
    this.restClient = client;
    this.disabledReason = reason;
  }

  private String validate(FreeNewsProperties properties) {
    if (properties.baseUrl() == null || properties.baseUrl().isBlank()){
      return "missing base URL";
    }
    if (properties.apiKey() == null || properties.apiKey().isBlank()){
      return "missing API key";
    }
    return null;
  }


  @Override
  public String name() {
    return "freenews";
  }

  @Override
  public Optional<String> disabledReason() {
    return Optional.ofNullable(disabledReason);
  }

  @Override
  public List<Article> fetchLatest() {
    if (disabledReason != null){
      throw new IllegalStateException(name() + " is disabled: " + disabledReason);
    }
    List<Article> articles = new ArrayList<>();
    for (FreeNewsList.Item item : fetchList()){
      if (item.uuid() == null || item.uuid().isBlank()){
        continue;
      }
      if (seenArticles.isSeen(Article.keyOf(name(), item.uuid()))){
        continue;
      }
      try {
        articles.add(toArticle(item, fetchDetails(item.uuid())));
      } catch (RuntimeException e){
        log.warn("{}: skipped item {}: {}", name(), item.uuid(), e.getMessage());
      }
      pause();
    }
    return articles;
  }

  private List<FreeNewsList.Item> fetchList() {
    FreeNewsList list = restClient.get()
            .uri(uri -> listUri(uri))
            .retrieve()
            .body(FreeNewsList.class);
    if (list == null || list.data() == null){
      throw new IllegalStateException("unexpected response shape");
    }
    return list.data();
  }

  private java.net.URI listUri(UriBuilder uri){
    UriBuilder builder = uri.path("/news")
            .queryParam("language", LANGUAGE)
            .queryParam("country", properties.country())
            .queryParam("order_by", properties.topic());
    if (!properties.topic().isBlank()){
      builder.queryParam("topic", properties.topic());
    }
    return builder.build();
  }

  private FreeNewsDetails.Detail fetchDetails(String uuid) {
    FreeNewsDetails details = restClient.get()
            .uri(uri -> uri.path("/details").queryParam("uuid", uuid).build())
            .retrieve()
            .body(FreeNewsDetails.class);
    if (details == null || details.data() == null){
      throw new IllegalStateException("no details returned");
    }
    return details.data();
  }

  private Article toArticle(FreeNewsList.Item item, FreeNewsDetails.Detail detail){
    return new Article(
            name(),
            item.uuid(),
            item.title(),
            detail.originalUrl(),
            detail.incipit(),
            Instant.parse(item.publishedAt()));
  }

  private static void pause(){ // their documentation suggests a delay after requests
    try {
      Thread.sleep(PAUSE_BETWEEN_DETAILS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
