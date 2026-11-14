package io.github.iliasiosifidis.articlestore.consumer;

import io.github.iliasiosifidis.articlestore.ArticleMessage;
import io.github.iliasiosifidis.articlestore.article.ArticleRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ArticleListenerTest {
  private static final Instant PUBLISHED = Instant.parse("2026-11-12T10:00:00Z");
  private final ArticleRepository repository = mock(ArticleRepository.class);
  private final ArticleListener listener = new ArticleListener(repository);
  private final ArticleMessage message = new ArticleMessage("guardian", "g-1", "Title", "https://example.com/g-1", "A summary", PUBLISHED);

  @Test
  void passesEveryFieldToTheInsert(){
    given(repository.insertIfAbsent("guardian", "g-1", "Title", "https://example.com/g-1", "A summary", PUBLISHED))
            .willReturn(1);
    listener.onArticle(message);
    verify(repository).insertIfAbsent("guardian", "g-1", "Title", "https://example.com/g-1", "A summary", PUBLISHED);
  }

  @Test
  void aDuplicateIsNotError(){
    given(repository.insertIfAbsent("guardian", "g-1", "Title", "https://example.com/g-1", "A summary", PUBLISHED))
            .willReturn(0);   // ON CONFLICT DO NOTHING: zero rows inserted
    assertThatCode(() -> listener.onArticle(message)).doesNotThrowAnyException();
  }
}