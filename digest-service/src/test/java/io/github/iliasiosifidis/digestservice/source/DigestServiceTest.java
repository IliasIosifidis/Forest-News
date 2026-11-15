package io.github.iliasiosifidis.digestservice.source;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@JdbcTest
@Testcontainers
class DigestServiceTest {

  @Container
  @ServiceConnection
  static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

  @Autowired
  private JdbcClient jdbc;

  private final ArticleStoreRestClient articleStore = mock(ArticleStoreRestClient.class);
  private final ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
  private DigestService digestService;

  @BeforeEach
  void setUp(){
    ChatClient.Builder builder = mock(ChatClient.Builder.class);
    given(builder.defaultSystem(anyString())).willReturn(builder);
    given(builder.build()).willReturn(chatClient);
    digestService = new DigestService(articleStore, new PromptBuilder(), builder, jdbc);
  }

  @Test
  void savesTheModelsAnswerWithoutDuplicateOrBlankLines(){
    given(articleStore.fetchArticles(any(), any())).willReturn(freshArticles());
    LocalDate day = LocalDate.of(2026,11,12);

    modelAnswers("First version");
    digestService.createFor(day);
    modelAnswers("Second version");
    digestService.createFor(day);

    long rows = jdbc.sql("SELECT count(*) FROM digests.digests").query(Long.class).single();
    assertThat(rows).isEqualTo(1);
    assertThat(digestService.latest()).hasValueSatisfying(digest ->
            assertThat(digest.content()).isEqualTo("Second version"));
  }

  @Test
  void withoutFreshArticlesTheModelIsNeverCalled(){
    given(articleStore.fetchArticles(any(), any())).willReturn(List.of());
    digestService.createFor(LocalDate.of(2026,11,12));
    verify(chatClient, never()).prompt();
    assertThat(digestService.latest()).isEmpty();
  }

  @Test
  void theWindowEndsAt21AthensTimeEvenOnTheDayTheClocksChange() {
    given(articleStore.fetchArticles(any(), any())).willReturn(List.of());

    // Sunday 25 October 2026: Greece goes from summer time (UTC+3) back to winter time (UTC+2)
    digestService.createFor(LocalDate.of(2026, 10, 25));

    verify(articleStore).fetchArticles(
            Instant.parse("2026-10-24T18:00:00Z"),    // 24 Oct 21:00 Athens, summer time
            Instant.parse("2026-10-25T19:00:00Z"));   // 25 Oct 21:00 Athens, winter time: a 25-hour window
  }

  private void modelAnswers(String text) {
    given(chatClient.prompt().user(anyString()).call().content()).willReturn(text);
  }

  private List<ArticlePage.Item> freshArticles() {
    return List.of(
            new ArticlePage.Item("guardian", "First", "Summary", "https://example.com/1",
                    Instant.parse("2026-11-12T10:00:00Z")),
            new ArticlePage.Item("currents", "Second", "Summary", "https://example.com/2",
                    Instant.parse("2026-11-12T11:00:00Z")));
  }
}