package io.github.iliasIosifidis.newsingestor.source;

import io.github.iliasIosifidis.newsingestor.article.Article;

import java.util.List;
import java.util.Optional;

public interface NewsSource {

  String name();

  List<Article> fetchLatest();

  default Optional<String> disabledReason(){
    return Optional.empty();
  }
}
