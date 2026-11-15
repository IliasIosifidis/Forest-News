package io.github.iliasiosifidis.searchservice.backfill;

import io.github.iliasiosifidis.searchservice.index.ArticleDocument;
import io.github.iliasiosifidis.searchservice.repository.ArticleSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;

@Component
public class Backfill implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(Backfill.class);
  private final RestClient restClient;
  private final ArticleSearchRepository repository;
  private final BackfillProperties props;

  Backfill(RestClient.Builder builder, ArticleSearchRepository repository, BackfillProperties props) {
    this.restClient = builder.baseUrl(props.articleStoreUrl()).build();
    this.repository = repository;
    this.props = props;
  }

  @Override
  public void run(ApplicationArguments args){
    for (int attempt = 1; attempt <= 5; attempt++) {
      try {
        backfillIfIncomplete();
        return;
      } catch (RuntimeException e){
        log.error("Backfill failed, the index may be incomplete: {}", e.getMessage());
        sleepQuietly(props.retryDelay());
      }
    }
    log.error("Backfill gave up after 5 attempts, the index may be incomplete until the next restart");
  }

  private static void sleepQuietly(Duration duration){
    try {
      Thread.sleep(duration);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private void backfillIfIncomplete() {
    ArticlePage first = fetchPage(0);
    long total = first.page().totalElements();
    long indexed = repository.count();
    if (indexed >= total){
      log.info("Index complete ({} of {} articles), no backfill needed", indexed, total);
      return;
    }
    log.info("Backfilling: {} indexed, {} in the article store", indexed, total);
    index(first);
    for (int page = 1; page < first.page().totalPages(); page++) {
      index(fetchPage(page));
    }
  }

  private ArticlePage fetchPage(int page) {
    ArticlePage result = restClient.get()
            .uri(uri -> uri.path("/api/articles")
                    .queryParam("page", page)
                    .queryParam("size", props.pageSize())
                    .build())
            .retrieve()
            .body(ArticlePage.class);
    if (result == null || result.content() == null || result.page() == null){
      throw new IllegalStateException("unexpected response from the article store");
    }
    return result;
  }

  private void index(ArticlePage page){
    List<ArticleDocument> documents = page.content().stream()
            .map(i -> ArticleDocument.of(i.source(), i.externalId(), i.title(), i.url(), i.summary(), i.publishedAt()))
            .toList();
    repository.saveAll(documents);
  }
}
