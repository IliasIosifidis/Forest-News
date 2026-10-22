package io.github.iliasiosifidis.digestservice.source;

import java.util.List;

public record ArticlePage(List<Item> content, PageInfo page) {
  record Item(String source, String title, String summary, String url){}
  record PageInfo(int totalPages){}
}
