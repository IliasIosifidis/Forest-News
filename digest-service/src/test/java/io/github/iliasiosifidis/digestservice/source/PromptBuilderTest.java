package io.github.iliasiosifidis.digestservice.source;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class PromptBuilderTest {
  private static final Instant WINDOW_END = Instant.parse("2026-11-12T19:00:00Z");
  private final PromptBuilder promptBuilder = new PromptBuilder();

  @Test
  void select_dropsArticlesPublishedMoreThan36HoursBeforeTheWindowEnd(){
    ArticlePage.Item fresh = article("guardian", "Fresh", WINDOW_END.minus(Duration.ofHours(2)));
    ArticlePage.Item stale = article("guardian", "Stale", WINDOW_END.minus(Duration.ofHours(40)));
  }

  @Test
  void select_dropsArticlesWithoutAPublishedDate(){
    List<ArticlePage.Item> selected = promptBuilder.select(List.of(article("guardian", "No date", null)), WINDOW_END);
    assertThat(selected).isEmpty();
  }

  @Test
  void select_givesEverySourceAFairShareAndKeepsTheNewest(){
    List<ArticlePage.Item> articles = new ArrayList<>();
    for (int i = 0; i < 600; i++) {
      articles.add(article("currents", "Currents " + i, WINDOW_END.minusSeconds(i)));
    }
    for (int i = 0; i < 10; i++) {
      articles.add(article("guardian", "Guardian " + i, WINDOW_END.minusSeconds(i)));
    }
    List<ArticlePage.Item> selected = promptBuilder.select(articles, WINDOW_END);
    Map<String, Long> perSource = selected.stream()
            .collect(Collectors.groupingBy(ArticlePage.Item::source, Collectors.counting()));
    assertThat(perSource).containsEntry("guardian", 10L); //keeps everything from a small source
    assertThat(perSource).containsEntry("currents", 150L); //keeps 300 for 2 sources (currents is spamming a lot f articles)
    assertThat(selected).extracting(ArticlePage.Item::title)
            .contains("Currents 0")                       // the newest is in
            .doesNotContain("Currents 599");              // the oldest is not
  }

  @Test
  void format_stripsHtmlAndTruncatesLongSummaries(){
    ArticlePage.Item withHtml = new ArticlePage.Item("guardian", "Title", "<p>Hello <b>world</b></p>", "https://example.com/1", WINDOW_END);
    ArticlePage.Item longSummary = new ArticlePage.Item("currents", "Long", "a".repeat(500), "https://example.com/2", WINDOW_END);

    String prompt = promptBuilder.format(List.of(withHtml, longSummary));
    assertThat(prompt)
            .contains("guardian | Title | Hello world")
            .doesNotContain("<p>")
            .contains("a".repeat(200) + "...")
            .doesNotContain("a".repeat(201));
  }

  @Test
  void format_endsWithTheInstruction(){
    String prompt = promptBuilder.format(List.of(article("guardian", "Title", WINDOW_END)));
    assertThat(prompt).endsWith("No introduction, no repeats.");
  }

  private ArticlePage.Item article(String source, String title, Instant publishedAt) {
    return new ArticlePage.Item(source, title, "A summary", "https://example.com", publishedAt);
  }
}