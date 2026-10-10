package io.github.iliasIosifidis.newsingestor.source;

import io.github.iliasIosifidis.newsingestor.article.Article;
import io.github.iliasIosifidis.newsingestor.dedup.SeenArticles;
import io.github.iliasIosifidis.newsingestor.publishing.ArticlePublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.ArrayList;
import java.util.List;

@Component
public class IngestionScheduler {

  private static final Logger log = LoggerFactory.getLogger(IngestionScheduler.class);
  private static final Logger activity = LoggerFactory.getLogger("activity");
  private final List<NewsSource> sources;
  private final SeenArticles seenArticles;
  private final ArticlePublisher publisher;

  IngestionScheduler(List<NewsSource> sources, SeenArticles seenArticles, ArticlePublisher publisher) {
    this.sources = sources.stream()
            .filter(s -> s.disabledReason().isEmpty())
            .toList();
    this.seenArticles = seenArticles;
    this.publisher = publisher;
  }

  @Scheduled(initialDelayString = "PT5S", // Period of Time: 5 seconds
          fixedDelayString = "${news.ingestion.interval:PT15M}") // Default, when the property is missing

  void pollAll() {
    for (NewsSource source : sources) {
      try {
        fetchFreshArticles(source);
      } catch (HttpClientErrorException e) {
        log.error("{}: request rejected with {}, check this source's configuration",
                source.name(), e.getStatusCode());
      } catch (HttpServerErrorException e) {
        log.warn("{}: source returned {}, will retry next cycle",
                source.name(), e.getStatusCode());
      } catch (ResourceAccessException e) {
        log.warn("{}: unreachable: {}", source.name(),
                NestedExceptionUtils.getMostSpecificCause(e).toString());
      } catch (Exception e) {
        log.error("{}: unexpected failure: {}", source.name(),
                NestedExceptionUtils.getMostSpecificCause(e).toString());
      }
    }
  }

  private void fetchFreshArticles(NewsSource source) {
    // fetch
    List<Article> articles = source.fetchLatest();
    // keep only new ones
    List<Article> fresh = new ArrayList<>();

    for (Article article : articles) {
      if (seenArticles.markIfNew(article)) {
        fresh.add(article);
      }
    }
    log.info("{}: fetched {} articles, {} new", source.name(), articles.size(), fresh.size());
    // publish - on failure, forget the article so the next cycle retries it
    for (Article article : fresh) {
      try {
        publisher.publish(article);
      } catch (RuntimeException e) {
        seenArticles.forget(article);
        log.warn("{}: publish failed for {}, will retry next cycle: {}",
                source.name(), article.key(), e.getMessage());
      }
    }
    if (!fresh.isEmpty()){
      activity.info("Fetched {} new articles from {}", fresh.size(), source.name());
    }
  }
}
