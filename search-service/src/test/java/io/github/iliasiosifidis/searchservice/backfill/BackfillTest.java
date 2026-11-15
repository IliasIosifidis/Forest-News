package io.github.iliasiosifidis.searchservice.backfill;

import io.github.iliasiosifidis.searchservice.index.ArticleDocument;
import io.github.iliasiosifidis.searchservice.repository.ArticleSearchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class BackfillTest {

  private final ArticleSearchRepository repository = mock(ArticleSearchRepository.class);
  private MockRestServiceServer server;
  private Backfill backfill;

  @BeforeEach
  void setUp() {
    RestClient.Builder builder = RestClient.builder();
    server = MockRestServiceServer.bindTo(builder).build();
    backfill = new Backfill(builder, repository,
            new BackfillProperties("http://article-store", 100, Duration.ZERO));
  }

  @Test
  void doesNothingWhenTheIndexIsComplete() {
    server.expect(queryParam("page", "0"))
            .andRespond(withSuccess(page(2, 1, "a1", "a2"), MediaType.APPLICATION_JSON));
    given(repository.count()).willReturn(2L);

    backfill.run(null);

    verify(repository, never()).saveAll(any());
    server.verify();   // only page 0 was requested
  }

  @Test
  @SuppressWarnings("unchecked")
  void indexesEveryPageWhenTheIndexIsBehind() {
    server.expect(queryParam("page", "0"))
            .andRespond(withSuccess(page(3, 2, "a1", "a2"), MediaType.APPLICATION_JSON));
    server.expect(queryParam("page", "1"))
            .andRespond(withSuccess(page(3, 2, "a3"), MediaType.APPLICATION_JSON));
    given(repository.count()).willReturn(0L);

    backfill.run(null);

    ArgumentCaptor<Iterable<ArticleDocument>> saved = ArgumentCaptor.forClass(Iterable.class);
    verify(repository, times(2)).saveAll(saved.capture());
    List<String> ids = new ArrayList<>();
    saved.getAllValues().forEach(batch -> batch.forEach(doc -> ids.add(doc.id())));
    assertThat(ids).containsExactly("guardian:a1", "guardian:a2", "guardian:a3");
  }

  @Test
  void retriedWhenTheArticleStoreIsNotReadYet(){
    server.expect(queryParam("page", "0")).andRespond(withServerError());
    server.expect(queryParam("page","0"))
            .andRespond(withSuccess(page(1,1,"a1"),MediaType.APPLICATION_JSON));
    given(repository.count()).willReturn(1L);
    backfill.run(null);
    server.verify(); // both attempts happened
  }

  @Test
  void givesUpAfterFiveAttemptsWithoutCrashingTheApp(){
    server.expect(ExpectedCount.times(5), queryParam("page","0")).andRespond(withServerError());
    assertThatCode(()-> backfill.run(null)).doesNotThrowAnyException();
    server.verify();
  }

  private static String page(long totalElements, int totalPages, String... ids){
    List<String > items = new ArrayList<>();
    for (String id: ids){
      items.add("""
             {"source":"guardian","externalId":"%s","title":"Title %s","url":"https://example.com/%s","summary":"s","publishedAt":"2026-11-12T10:00:00Z"}"""
              .formatted(id, id, id));
    }
    return """
            {"content":[%s],"page":{"totalElements":%d,"totalPages":%d}}"""
            .formatted(String.join(",", items), totalElements, totalPages);
  }
}