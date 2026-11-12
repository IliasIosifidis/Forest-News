package io.github.iliasiosifidis.digestservice.source;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@Service
public class DigestService {

  static final String  ZONE_ID = "Europe/Athens";
  static final int CUTOFF_HOUR = 21;
  static final ZoneId ZONE = ZoneId.of(ZONE_ID);

  private final ArticleStoreRestClient restClient;
  private final PromptBuilder promptBuilder;

  public DigestService(ArticleStoreRestClient restClient, PromptBuilder promptBuilder) {
    this.restClient = restClient;
    this.promptBuilder = promptBuilder;
  }

  public void createFor(LocalDate day){
    ZonedDateTime to = day.atTime(CUTOFF_HOUR, 0).atZone(ZONE);
    ZonedDateTime from = to.minusDays(1);
    List<ArticlePage.Item> articles = restClient.fetchArticles(from.toInstant(), to.toInstant());
    List<ArticlePage.Item> selected = promptBuilder.select(articles, to.toInstant());
    String prompt = promptBuilder.format(selected);
    log.info("Digest for {}: {} articles between {} and {}", day, articles.size(), from, to);
  }
}
