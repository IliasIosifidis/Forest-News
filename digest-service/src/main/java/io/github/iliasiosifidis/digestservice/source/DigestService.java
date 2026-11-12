package io.github.iliasiosifidis.digestservice.source;

import io.github.iliasiosifidis.digestservice.client.LlmClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class DigestService {

  static final String  ZONE_ID = "Europe/Athens";
  static final int CUTOFF_HOUR = 21;
  static final ZoneId ZONE = ZoneId.of(ZONE_ID);

  public record DigestResponse(LocalDate day, String content, Instant createdAt){}

  private final ArticleStoreRestClient restClient;
  private final PromptBuilder promptBuilder;
  private final LlmClient llmClient;
  private final JdbcClient jdbc;

  public DigestService(ArticleStoreRestClient restClient, PromptBuilder promptBuilder, LlmClient llmClient, JdbcClient jdbc) {
    this.restClient = restClient;
    this.promptBuilder = promptBuilder;
    this.llmClient = llmClient;
    this.jdbc = jdbc;
  }

  public void createFor(LocalDate day) {
    ZonedDateTime to = day.atTime(CUTOFF_HOUR, 0).atZone(ZONE);
    ZonedDateTime from = to.minusDays(1);

    List<ArticlePage.Item> articles = restClient.fetchArticles(from.toInstant(), to.toInstant());
    List<ArticlePage.Item> selected = promptBuilder.select(articles, to.toInstant());
    log.info("Digest for {}: {} articles between {} and {}", day, articles.size(), from, to);

    if (selected.isEmpty()) {
      log.warn("Digest for {}: no fresh articles, skipping", day);
      return;
    }

    String digest = llmClient.summarize(PromptBuilder.SYSTEM_PROMPT, promptBuilder.format(selected));

    jdbc.sql("""
             INSERT INTO digests.digests (day, content) VALUES (:day, :content)
             ON CONFLICT (day) DO UPDATE SET content = EXCLUDED.content, created_at = now()
             """)
            .param("day", day)
            .param("content", digest)
            .update();
    log.info("Saved digest for {}", day);
  }
  public Optional<DigestResponse> latest(){
    return jdbc.sql("SELECT day, content, created_at FROM digests.digests ORDER BY day DESC LIMIT 1")
            .query(DigestResponse.class)
            .optional();
  }
}
