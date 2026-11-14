package io.github.iliasIosifidis.newsingestor.source.hackernews;

import io.github.iliasIosifidis.newsingestor.article.Article;
import io.github.iliasIosifidis.newsingestor.source.NewsSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class HackerNewsSource implements NewsSource {

  private final HackerNewsProperties properties;
  private final RestClient restClient;
  private final String disabledReason;

  public HackerNewsSource(HackerNewsProperties properties, RestClient.Builder builder) {
    this.properties = properties;
    String reason = validate(properties);
    RestClient restClient = null;
    if (reason == null) {
      try {
        restClient = builder.baseUrl(properties.baseUrl()).build();
      } catch (RuntimeException e) {
        reason = "invalid base URL: " + e.getMessage();
      }
    }
    this.restClient = restClient;
    this.disabledReason = reason;
  }

  @Override
  public Optional<String> disabledReason() {
    return Optional.ofNullable(disabledReason);
  }

  private String validate(HackerNewsProperties properties) {
    if (properties.baseUrl() == null || properties.baseUrl().isBlank()) {
      return "missing base url";
    }
    if (properties.maxStories() <= 0) {
      return "page size must be positive";
    }
    return null;
  }

  @Override
  public String name() {
    return "hackernews";
  }


  @Override
  public List<Article> fetchLatest() {
    List<Article> articles = new ArrayList<>();
    for (Long id : fetchTopIds()) {
      try {
        HackerNewsItem item = fetchItem(id);
        if (isUsableStory(item)) {
          articles.add(toArticle(id, item));
        }
      } catch (RuntimeException e) {
        log.debug("hackernews: skipped item {}: {}", id, e.getMessage());
      }
    }
    return articles;
  }

  private List<Long> fetchTopIds() {
    Long[] ids = restClient.get()
            .uri("/topstories.json")
            .retrieve()
            .body(Long[].class);
    if (ids == null) {
      throw new IllegalStateException("unexpected response: no story IDs");
    }
    return Arrays.stream(ids)
            .limit(properties.maxStories())
            .toList();
  }

  private HackerNewsItem fetchItem(Long id) {
    return restClient.get()
            .uri("/item/{id}.json", id)
            .retrieve()
            .body(HackerNewsItem.class);
  }

  private boolean isUsableStory(HackerNewsItem item) {
    return item.url() != null
            && "story".equals(item.type())
            && !Boolean.TRUE.equals(item.deleted())
            && !Boolean.TRUE.equals(item.dead());
  }

  private Article toArticle(Long id, HackerNewsItem item) {
    if (item.time() == null) {
      throw new IllegalArgumentException("missing time");
    }
    return new Article(
            name(),
            String.valueOf(id),
            item.title(),
            item.url(),
            null,
            Instant.ofEpochSecond(item.time()));
  }
}
