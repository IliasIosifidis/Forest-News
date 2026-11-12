package io.github.iliasiosifidis.digestservice.source;

import java.time.Instant;
import java.util.List;

public record ArticlePage(List<Item> content, PageInfo page) {
  public record Item(String source, String title, String summary, String url, Instant publishedAt){}
  public record PageInfo(int totalPages){}
}
