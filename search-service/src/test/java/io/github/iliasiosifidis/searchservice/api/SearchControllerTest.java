package io.github.iliasiosifidis.searchservice.api;

import io.github.iliasiosifidis.searchservice.index.ArticleDocument;
import io.github.iliasiosifidis.searchservice.search.ArticleSearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@WebMvcTest(SearchController.class)
class SearchControllerTest {

  @Autowired
  private MockMvcTester mvc;

  @MockitoBean
  private ArticleSearchService searchService;

  private final ArticleDocument doc = ArticleDocument.of("guardian", "g-1", "Election results","https://example.com/g-1", "Summary", Instant.parse("2026-11-12T10:00:00Z"));

  @Test
  void returnsTheResultsAsJson(){
    given(searchService.search("elec",null,10)).willReturn(List.of(doc));
    assertThat(mvc.get().uri("/api/search?q=elec"))
            .hasStatusOk()
            .bodyJson()
            .extractingPath("$[0].externalId").isEqualTo("g-1");
  }

  @Test
  void passesSourceAndSizeThrough(){
    given(searchService.search("elec", "guardian", 5)).willReturn(List.of(doc));
    assertThat(mvc.get().uri("/api/search?q=elec&source=guardian&size=5")).hasStatusOk();
    verify(searchService).search("elec","guardian",5);
  }

  @Test
  void aMissingQueryIsABadRequest(){
    assertThat(mvc.get().uri("/api/search")).hasStatus(HttpStatus.BAD_REQUEST);
  }
}