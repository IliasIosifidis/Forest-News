package io.github.iliasiosifidis.articlestore.article;

import org.springframework.beans.BeanUtils;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

public final class TestArticles {
  private TestArticles(){}

  public static Article article(long id, String source, String externalId, String title){
    Article article = BeanUtils.instantiateClass(Article.class);
    ReflectionTestUtils.setField(article, "id", id);
    ReflectionTestUtils.setField(article, "source", source);
    ReflectionTestUtils.setField(article, "externalId", externalId);
    ReflectionTestUtils.setField(article, "title", title);
    ReflectionTestUtils.setField(article, "url", "https://example.com/" + externalId);
    ReflectionTestUtils.setField(article, "summary", "A summary");
    ReflectionTestUtils.setField(article, "publishedAt", Instant.parse("2026-11-12T10:00:00Z"));
    return article;
  }
}
