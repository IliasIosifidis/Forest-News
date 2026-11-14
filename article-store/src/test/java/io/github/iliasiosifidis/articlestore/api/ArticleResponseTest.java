package io.github.iliasiosifidis.articlestore.api;

import io.github.iliasiosifidis.articlestore.article.TestArticles;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ArticleResponseTest {

  @Test
  void copiesEveryFieldFromTheEntity(){
    ArticleResponse response = ArticleResponse.from(TestArticles.article(7L, "guardian", "g-1", "Title"));

    assertThat(response.id()).isEqualTo(7L);
    assertThat(response.source()).isEqualTo("guardian");
    assertThat(response.externalId()).isEqualTo("g-1");
    assertThat(response.title()).isEqualTo("Title");
    assertThat(response.url()).isEqualTo("https://example.com/g-1");
    assertThat(response.summary()).isEqualTo("A summary");
    assertThat(response.publishedAt()).isEqualTo(Instant.parse("2026-11-12T10:00:00Z"));
  }

}