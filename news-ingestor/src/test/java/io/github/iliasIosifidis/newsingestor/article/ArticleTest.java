package io.github.iliasIosifidis.newsingestor.article;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static org.assertj.core.api.AssertionsForClassTypes.*;

class ArticleTest {
  private static final Instant PUBLISHED = Instant.parse("2026-11-12T10:00:00Z");

  @Test
  void keyCombinesSourceAndExternalId(){
    Article article = new Article("guardian","world/123","Title", "https://example.com", null, PUBLISHED);
    assertThat(article.key()).isEqualTo("guardian:world/123");
    assertThat(Article.keyOf("guardian", "world/123")).isEqualTo(article.key());
  }

  @Test
  void summaryIsOptional(){
    assertThatCode(() -> new Article("hackernews", "1", "Title", "https://example.com", null, PUBLISHED))
            .doesNotThrowAnyException();
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   "})
  void rejectsAMissingTitle(String title){
    assertThatThrownBy(() -> new Article("guardian", "1", title,"https://example.com", null, PUBLISHED))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("title is required");
  }

  @Test
  void rejectsAMissingPublishDate(){
    assertThatThrownBy(() -> new Article("guardian", "1", "Title", "https://example.com", null,null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("publishedAt is required");
  }
}