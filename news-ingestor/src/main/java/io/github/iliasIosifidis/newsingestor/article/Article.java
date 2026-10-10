package io.github.iliasIosifidis.newsingestor.article;

import java.time.Instant;

public record Article(
        String source,
        String externalId,
        String title,
        String url,
        String summary,
        Instant publishedAt
) {
  // One bad article shouldn't destroy the bunch
  public Article{
    if (source == null || source.isBlank()){
      throw new IllegalArgumentException("source is required");
    }
    if (externalId == null || externalId.isBlank()){
      throw new IllegalArgumentException("externalId is required");
    }
    if (title == null || title.isBlank()){
      throw new IllegalArgumentException("url is required");
    }
    if (url == null || url.isBlank()){
      throw new IllegalArgumentException("url is required");
    }
    if (publishedAt == null){
      throw new IllegalArgumentException("publishedAt is required");
    }
  }

  public String key(){
    return source + ":" + externalId;
  }
}
