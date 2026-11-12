package io.github.iliasiosifidis.digestservice.source;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
public class PromptBuilder {
  private static final Duration MAX_AGE = Duration.ofHours(36);
  private static final int MAX_SUMMARY_LENGTH = 200;

  public static final String SYSTEM_PROMPT = "You are a news editor. Use only the articles provided. Never add facts that are not in them.";

  public List<ArticlePage.Item> select(List<ArticlePage.Item> articles, Instant windowEnd){
    Instant oldestAllowed = windowEnd.minus(MAX_AGE);
    return articles.stream()
            .filter(a -> a.publishedAt() != null && a.publishedAt().isAfter(oldestAllowed))
            .toList();
  }

  public String format(List<ArticlePage.Item> articles){
    StringBuilder prompt = new StringBuilder(
          """
               From the articles below, pick the 10 most important news stories.
               For each story write 1-2 sentences. No headlines.
               Return the stories as JSON array of strings.
          """);
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
