package io.github.iliasiosifidis.digestservice.source;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;



class ArticleStoreRestClientTest {
  private static final Instant FROM = Instant.parse("2026-11-11T19:00:00Z");
  private static final Instant TO = Instant.parse("2026-11-12T19:00:00Z");

  private MockRestServiceServer server;
  private ArticleStoreRestClient client;

  @BeforeEach
  void setUp() {
    RestClient.Builder builder = RestClient.builder();
    server = MockRestServiceServer.bindTo(builder).build();
    client = new ArticleStoreRestClient(builder, "http://article-store");
  }

  @Test
  void fetchArticles_sendTheWindowAndFollowsAllPages() {
    server.expect(requestTo(startsWith("http://article-store/api/articles")))
            .andExpect(method(HttpMethod.GET))
            .andExpect(queryParam("from", FROM.toString()))
            .andExpect(queryParam("to", TO.toString()))
            .andExpect(queryParam("page", "0"))
            .andRespond(withSuccess(page(2, "First", "Second"), MediaType.APPLICATION_JSON));
    server.expect(queryParam("page", "1"))
            .andRespond(withSuccess(page(2, "Third"), MediaType.APPLICATION_JSON));

    List<ArticlePage.Item> articles = client.fetchArticles(FROM, TO);
    assertThat(articles).extracting(ArticlePage.Item::title)
            .containsExactly("First", "Second", "Third");
    server.verify();
  }

  @Test
  void fetchArticles_makeOneRequestWhenThereAreNoArticles(){
    server.expect(queryParam("page", "0"))
            .andRespond(withSuccess(page(0), MediaType.APPLICATION_JSON));

    assertThat(client.fetchArticles(FROM, TO)).isEmpty();
    server.verify();
  }

  private static String page(int totalPages, String... titles) {
    String content = Arrays.stream(titles)
            .map("""
                    {"source":"guardian","title":"%s","summary":"s","url":"https://example.com","publishedAt":"2026-11-12T10:00:00Z"}"""::formatted)
            .collect(Collectors.joining(","));
    return """
            {"content":[%s],"page":{"totalPages":%d}}""".formatted(content, totalPages);
  }
}
