package io.github.iliasiosifidis.digestservice.source;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
public class PromptBuilder {
  private static final Duration MAX_AGE = Duration.ofHours(36);
  private static final int MAX_SUMMARY_LENGTH = 200;

  public static final String SYSTEM_PROMPT =  """
          You are a news editor. You receive news articles, one per line, \\
          in the format: source | title | summary.
          Pick the 10 most important news stories and write 1-2 sentences about each.
          Write one story per line. No headlines, no numbering, no introduction.
          Use only the articles provided. Never add facts that are not in them.
          """;

  public List<ArticlePage.Item> select(List<ArticlePage.Item> articles, Instant windowEnd){
    Instant oldestAllowed = windowEnd.minus(MAX_AGE);
    return articles.stream()
            .filter(a -> a.publishedAt() != null && a.publishedAt().isAfter(oldestAllowed))
            .toList();
  }

  public String format(List<ArticlePage.Item> articles){
    StringBuilder prompt = new StringBuilder();
    for (ArticlePage.Item a : articles){
      prompt.append(a.source()).append(" | ")
            .append(a.title()).append(" | ")
            .append(clean(a.summary())).append("\n");
    }
    return prompt.toString();
  }

  private String clean(String summary){
    if (summary == null){
      return "";
    }
    String text = summary.replaceAll("<[^>]*>", "").replaceAll("\\s+", " ").strip();
    if (text.length() > MAX_SUMMARY_LENGTH){
      return text.substring(0, MAX_SUMMARY_LENGTH) + "...";
    }
    return text;
  }
}
