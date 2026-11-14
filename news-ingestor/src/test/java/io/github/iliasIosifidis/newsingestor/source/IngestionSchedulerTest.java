package io.github.iliasIosifidis.newsingestor.source;   // same package as IngestionScheduler

import io.github.iliasIosifidis.newsingestor.article.Article;
import io.github.iliasIosifidis.newsingestor.dedup.DedupProperties;
import io.github.iliasIosifidis.newsingestor.dedup.SeenArticles;
import io.github.iliasIosifidis.newsingestor.publishing.ArticlePublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpException;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class IngestionSchedulerTest {

  private final NewsSource guardian = mock(NewsSource.class);
  private final NewsSource broken = mock(NewsSource.class);
  private final ArticlePublisher publisher = mock(ArticlePublisher.class);
  private final SeenArticles seenArticles = new SeenArticles(new DedupProperties(Duration.ofHours(48), 10_000));

  private final Article first = article("1");
  private final Article second = article("2");

  @BeforeEach
  void setUp() {
    given(guardian.name()).willReturn("guardian");
    given(broken.name()).willReturn("broken");
  }

  @Test
  void publishesEachNewArticleOnlyOnce() {
    given(guardian.fetchLatest()).willReturn(List.of(first, second));
    IngestionScheduler scheduler = schedulerWith(guardian);

    scheduler.pollAll();
    scheduler.pollAll();   // the next cycle returns the same two articles

    verify(publisher, times(1)).publish(first);
    verify(publisher, times(1)).publish(second);
  }

  @Test
  void aFailedPublishIsRetriedInTheNextCycle() {
    given(guardian.fetchLatest()).willReturn(List.of(first));
    willThrow(new AmqpException("broker down"))   // first call fails...
            .willDoNothing()                      // ...second call succeeds
            .given(publisher).publish(first);
    IngestionScheduler scheduler = schedulerWith(guardian);

    scheduler.pollAll();   // publish fails -> the article is forgotten
    scheduler.pollAll();   // so it counts as new again -> published

    verify(publisher, times(2)).publish(first);
  }

  @Test
  void aFailingSourceDoesNotStopTheOthers() {
    given(broken.fetchLatest()).willThrow(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE));
    given(guardian.fetchLatest()).willReturn(List.of(first));
    IngestionScheduler scheduler = schedulerWith(broken, guardian);   // the broken one goes first

    scheduler.pollAll();

    verify(publisher).publish(first);
  }

  @Test
  void aDisabledSourceIsNeverPolled() {
    given(broken.disabledReason()).willReturn(Optional.of("missing API key"));
    IngestionScheduler scheduler = schedulerWith(broken, guardian);

    scheduler.pollAll();

    verify(broken, never()).fetchLatest();
  }

  private IngestionScheduler schedulerWith(NewsSource... sources) {
    return new IngestionScheduler(List.of(sources), seenArticles, publisher);
  }

  private static Article article(String id) {
    return new Article("guardian", id, "Title " + id, "https://example.com/" + id, null,
            Instant.parse("2026-11-12T10:00:00Z"));
  }
}