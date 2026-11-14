package io.github.iliasIosifidis.newsingestor.dedup;

import io.github.iliasIosifidis.newsingestor.article.Article;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
class SeenArticlesTest {

  private final SeenArticles seen = new SeenArticles(new DedupProperties(Duration.ofHours(48), 10_000));
  private final Article article = article("guardian", "1");

  @Test
  void anArticleIsNewTheFirstTimeOnly(){
    assertThat(seen.markIfNew(article)).isTrue();
    assertThat(seen.markIfNew(article)).isFalse();
  }

  @Test
  void isSeenOnlyLooksAndNeverMarks(){
    assertThat(seen.isSeen(article.key())).isFalse();
    assertThat(seen.markIfNew(article)).isTrue();
    assertThat(seen.isSeen(article.key())).isTrue();
  }

  @Test
  void theSameIdFromAnotherSourceIsADifferentArticle(){
    seen.markIfNew(article("guardian","42"));
    assertThat(seen.markIfNew(article("hackernews", "42"))).isTrue();
  }

  private static Article article(String source, String id){
    return new Article(source, id, "Title" + id, "https://example.com" + id, null, Instant.parse("2026-11-12T10:00:00Z"));
  }
}