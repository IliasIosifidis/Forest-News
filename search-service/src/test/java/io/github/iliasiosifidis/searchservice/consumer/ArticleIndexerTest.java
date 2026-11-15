package io.github.iliasiosifidis.searchservice.consumer;

import io.github.iliasiosifidis.searchservice.index.ArticleDocument;
import io.github.iliasiosifidis.searchservice.repository.ArticleSearchRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ArticleIndexerTest {

  private static final Instant PUBLISHED = Instant.parse("2026-11-12T10:00:00Z");
  private final ArticleSearchRepository repository = mock(ArticleSearchRepository.class);
  private final ArticleIndexer indexer = new ArticleIndexer(repository);

  @Test
  void savesTheMessageAsDocument(){
    indexer.onArticle(new ArticleMessage("guardian", "g-1", "Title", "https://example.com/g-1","<p>Summary</p>", PUBLISHED));
    verify(repository).save(new ArticleDocument("guardian:g-1", "guardian", "g-1", "Title","Summary", "https://example.com/g-1", PUBLISHED));
  }

  @Test
  void aMessageWithoutExternalIdIsRejectedAndNotSaved(){
    ArticleMessage broken = new ArticleMessage("guardian", null, "Title", "https://example.com", null, PUBLISHED);
    assertThatThrownBy(() -> indexer.onArticle(broken)).isInstanceOf(IllegalArgumentException.class);
    verify(repository, never()).save(any());
  }
}