package io.github.iliasIosifidis.newsingestor.source.guardian;

import io.github.iliasIosifidis.newsingestor.article.Article;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GuardianSourceTest {
  private static final String BASE_URL = "https://content.guardianapis.com";

  private RestClient.Builder builder;
  private MockRestServiceServer server;

  @BeforeEach
  void setUp(){
    builder = RestClient.builder();
    server = MockRestServiceServer.bindTo(builder).build();
  }

  private GuardianSource sourceWith(String apiKey, String baseUrl){
    return new GuardianSource(builder, new GuardianProperties(baseUrl, apiKey, 20));
  }

  @Test
  void mapsGuardianResultsToArticles() {
    server.expect(requestTo(startsWith(BASE_URL + "/search")))
            .andExpect(queryParam("api-key", "test-key"))
            .andRespond(withSuccess("""
                  {"response": {"results": [{
                      "id": "world/2026/oct/03/example",
                      "webTitle": "Example headline",
                      "webUrl": "https://www.theguardian.com/world/2026/oct/03/example",
                      "webPublicationDate": "2026-10-03T10:00:00Z",
                      "fields": {"trailText": "Short summary"}
                  }]}}
                  """, MediaType.APPLICATION_JSON));

    List<Article> articles = sourceWith("test-key", BASE_URL).fetchLatest();

    assertThat(articles).hasSize(1);
    Article article = articles.getFirst();
    assertThat(article.source()).isEqualTo("guardian");
    assertThat(article.title()).isEqualTo("Example headline");
    assertThat(article.summary()).isEqualTo("Short summary");
    assertThat(article.publishedAt()).isEqualTo(Instant.parse("2026-10-03T10:00:00Z"));
    server.verify();
  }

  @Test
  void throwsClientErrorWhenKeyIsRejected(){
    server.expect(requestTo(startsWith(BASE_URL + "/search")))
            .andRespond(withStatus(HttpStatus.UNAUTHORIZED));
    GuardianSource source = sourceWith("wrong-key", BASE_URL);
    assertThatThrownBy(source::fetchLatest)
            .isInstanceOf(HttpClientErrorException.class);
  }

  @Test
  void throwsServerErrorWhenGuardianIsUnavailable(){
    server.expect(requestTo(startsWith(BASE_URL + "/search")))
            .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

    GuardianSource source = sourceWith("test-key", BASE_URL);
    assertThatThrownBy(source::fetchLatest)
            .isInstanceOf(HttpServerErrorException.ServiceUnavailable.class);
  }

  @Test
  void badArticleIsSkipped(){
    server.expect(requestTo(startsWith(BASE_URL + "/search")))
            .andExpect(queryParam("api-key", "test-key"))
            .andRespond(withSuccess("""
                  {"response": {"results": [
                  {
                  "id": "world/2026/oct/03/bad-example",
                      "webTitle": "Broken Article",
                      "webUrl": "https://www.theguardian.com/world/2026/oct/03/bad-example",
                      "webPublicationDate": "not-a-date",
                      "fields": {"trailText": "Short summary"}
                  },{
                      "id": "world/2026/oct/03/example",
                      "webTitle": "Example headline",
                      "webUrl": "https://www.theguardian.com/world/2026/oct/03/example",
                      "webPublicationDate": "2026-10-03T10:00:00Z",
                      "fields": {"trailText": "Short summary"}
                  }]}}
                  """, MediaType.APPLICATION_JSON));
    List<Article> articles = sourceWith("test-key", BASE_URL).fetchLatest();

    assertThat(articles).hasSize(1);
    Article article = articles.getFirst();
    assertThat(article.source()).isEqualTo("guardian");
    assertThat(article.title()).isEqualTo("Example headline");
    assertThat(article.summary()).isEqualTo("Short summary");
    assertThat(article.publishedAt()).isEqualTo(Instant.parse("2026-10-03T10:00:00Z"));
    server.verify();
  }

  @Test
  void disablesItselfWhenTheApiKeyIsMissing() {
    assertThat(sourceWith("", BASE_URL).disabledReason()).isPresent();
  }

  @Test
  void disablesItselfWhenTheBaseUrlIsMalformed() {
    assertThat(sourceWith("test-key", "http://[not-a-host").disabledReason()).isPresent();
  }

  @Test
  void aDisabledSourceRefusesToFetch() {
    GuardianSource source = sourceWith("", BASE_URL);

    assertThatThrownBy(source::fetchLatest).isInstanceOf(IllegalStateException.class);
  }
}