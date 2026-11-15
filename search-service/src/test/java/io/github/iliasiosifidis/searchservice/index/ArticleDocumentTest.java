package io.github.iliasiosifidis.searchservice.index;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class ArticleDocumentTest {
  private static final Instant PUBLISHED = Instant.parse("2026-11-12T10:00:00Z");

  @Test
  void theIdIsSourceAndExternalId(){
    ArticleDocument doc = ArticleDocument.of("guardian","g-1","Title","https://example.com", null, PUBLISHED);
    assertThat(doc.id()).isEqualTo("guardian:g-1");
  }

  @Test
  void stripsHtmlFromTheSummary(){
    ArticleDocument doc = ArticleDocument.of("guardian", "g-1", "Title", "https://example.com","<p>Hello <strong>world</strong></p> ", PUBLISHED);
    assertThat(doc.summary()).isEqualTo("Hello world");
  }

  @Test
  void aMissingSummaryStaysMissing(){
    ArticleDocument doc = ArticleDocument.of("hackernews", "1", "Title", "https://example.com", null, PUBLISHED);
    assertThat(doc.summary()).isNull();
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"  "})
  void rejectsAMissingExternalId(String externalId){
    assertThatThrownBy(() -> ArticleDocument.of("guardian", externalId, "Title", "https://example.com", null, PUBLISHED))
            .isInstanceOf(IllegalArgumentException.class);
  }
}