package io.github.iliasIosifidis.newsingestor.source.hackernews;

import io.github.iliasIosifidis.newsingestor.article.Article;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;


class HackerNewsSourceTest {
  private static final String BASE_URL = "https://hacker-news.firebaseio.com/v0";
  private static final long TIME = 1762941600L;

  private RestClient.Builder builder;
  private MockRestServiceServer server;

  @BeforeEach
  void setUp(){
    builder = RestClient.builder();
    server = MockRestServiceServer.bindTo(builder).build();
  }

  private HackerNewsSource sourceWith(String baseUrl, int maxStories){
    return new HackerNewsSource(new HackerNewsProperties(baseUrl, maxStories), builder);
  }

  @Test
  void fetchesAtMostMaxStoriesAndKeepsOnlyRealStories(){
    server.expect(requestTo(BASE_URL + "/topstories.json"))
            .andRespond(withSuccess("[101, 102, 103]", MediaType.APPLICATION_JSON));
    server.expect(requestTo(BASE_URL + "/item/101.json"))
            .andRespond(withSuccess(item(101, "story", "https://example.com/101"), MediaType.APPLICATION_JSON));
    server.expect(requestTo(BASE_URL + "/item/102.json"))
            .andRespond(withSuccess(item(102, "story", null), MediaType.APPLICATION_JSON));   // "Ask HN": no url
    // no expectation for 103: with maxStories = 2 it must never be requested
    List<Article> articles = sourceWith(BASE_URL, 2).fetchLatest();
    assertThat(articles).hasSize(1);
    Article article = articles.getFirst();
    assertThat(article.source()).isEqualTo("hackernews");
    assertThat(article.externalId()).isEqualTo("101");
    assertThat(article.url()).isEqualTo("https://example.com/101");
    assertThat(article.summary()).isNull();
    assertThat(article.publishedAt()).isEqualTo(Instant.ofEpochSecond(TIME));
    server.verify();
  }

  @Test
  void aFailingItemIsSkippedWithoutLosingTheOthers(){
    server.expect(requestTo(BASE_URL + "/topstories.json"))
            .andRespond(withSuccess("[201, 202]", MediaType.APPLICATION_JSON));
    server.expect(requestTo(BASE_URL + "/item/201.json"))
            .andRespond(withServerError());
    server.expect(requestTo(BASE_URL + "/item/202.json"))
            .andRespond(withSuccess(item(202, "story", "https://example.com/202"), MediaType.APPLICATION_JSON));
    List<Article> articles = sourceWith(BASE_URL, 20).fetchLatest();
    assertThat(articles).extracting(Article::externalId).containsExactly("202");
  }

  @Test
  void disablesItselfWithoutABaseUrl(){
    assertThat(sourceWith(null, 20).disabledReason()).hasValue("missing base url");
  }

  @Test
  void disablesItselfWithANonPositiveStoryCount(){
    assertThat(sourceWith(BASE_URL, 0).disabledReason()).isPresent();
  }

  private static String item(long id, String type, String url){
    String urlField = url == null ? "" : ",\"url\":\"" + url + "\"";
    return "{\"id\":%d,\"time\":%d,\"title\":\"Story %d\",\"type\":\"%s\"%s}"
            .formatted(id, TIME, id, type, urlField);
  }
}