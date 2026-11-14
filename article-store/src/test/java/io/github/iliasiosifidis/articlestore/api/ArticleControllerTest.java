package io.github.iliasiosifidis.articlestore.api;

import io.github.iliasiosifidis.articlestore.article.Article;
import io.github.iliasiosifidis.articlestore.article.ArticleRepository;
import io.github.iliasiosifidis.articlestore.article.TestArticles;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@WebMvcTest(ArticleController.class)
class ArticleControllerTest {

  @Autowired
  private MockMvcTester mvc;

  @MockitoBean
  private ArticleRepository repository;

  private final Article article = TestArticles.article(1L, "guardian", "g-1", "Title");

  @Test
  void list_withoutParameters_returnsAllArticlesNewestFirst() {
    given(repository.findAll(any(Pageable.class))).willReturn(page(article));

    assertThat(mvc.get().uri("/api/articles"))
            .hasStatusOk()
            .bodyJson()
            .extractingPath("$.content[0].externalId").isEqualTo("g-1");

    ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
    verify(repository).findAll(pageable.capture());
    assertThat(pageable.getValue().getPageSize()).isEqualTo(20);
    assertThat(pageable.getValue().getSort())
            .isEqualTo(Sort.by(Sort.Direction.DESC, "publishedAt", "id"));
  }

  @Test
  void list_withSource_filtersBySource() {
    given(repository.findBySource(eq("guardian"), any(Pageable.class))).willReturn(page(article));

    assertThat(mvc.get().uri("/api/articles?source=guardian")).hasStatusOk();

    verify(repository).findBySource(eq("guardian"), any(Pageable.class));
  }

  @Test
  void list_withFromAndTo_usesTheWindowEvenWithoutASource() {
    Instant from = Instant.parse("2026-11-11T19:00:00Z");
    Instant to = Instant.parse("2026-11-12T19:00:00Z");
    given(repository.findByReceivedBetween(eq(from), eq(to), any(Pageable.class))).willReturn(page(article));

    assertThat(mvc.get().uri("/api/articles?from={from}&to={to}", from, to)).hasStatusOk();

    verify(repository).findByReceivedBetween(eq(from), eq(to), any(Pageable.class));
    verify(repository, never()).findAll(any(Pageable.class));
  }

  @Test
  void list_capsThePageSizeAt100() {
    given(repository.findAll(any(Pageable.class))).willReturn(page(article));

    assertThat(mvc.get().uri("/api/articles?size=5000")).hasStatusOk();

    ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
    verify(repository).findAll(pageable.capture());
    assertThat(pageable.getValue().getPageSize()).isEqualTo(100);
  }

  @Test
  void byKey_returnsTheArticle() {
    given(repository.findBySourceAndExternalId("guardian", "g-1")).willReturn(Optional.of(article));

    assertThat(mvc.get().uri("/api/articles/by-key?source=guardian&externalId=g-1"))
            .hasStatusOk()
            .bodyJson()
            .extractingPath("$.title").isEqualTo("Title");
  }

  @Test
  void byKey_returns404ForAnUnknownArticle() {
    given(repository.findBySourceAndExternalId("guardian", "nope")).willReturn(Optional.empty());

    assertThat(mvc.get().uri("/api/articles/by-key?source=guardian&externalId=nope"))
            .hasStatus(HttpStatus.NOT_FOUND);
  }

  private static Page<Article> page(Article... articles) {
    return new PageImpl<>(List.of(articles), PageRequest.of(0, 20), articles.length);
  }
}