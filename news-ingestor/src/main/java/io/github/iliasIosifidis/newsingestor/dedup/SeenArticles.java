package io.github.iliasIosifidis.newsingestor.dedup;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.iliasIosifidis.newsingestor.article.Article;
import org.springframework.stereotype.Component;

@Component
public class SeenArticles {
  private final Cache<String, Boolean> seen;

  public SeenArticles(DedupProperties props){
    this.seen = Caffeine.newBuilder()
            .expireAfterWrite(props.retention())
            .maximumSize(props.maxSize())
            .build();
  }

  // Returns new if the article is new and remembers if from now on.
  public boolean markIfNew(Article article){
    return seen
            .asMap()
            .putIfAbsent(article.key(), Boolean.TRUE) == null; //putIfAbsent is atomic
  }

  public boolean isSeen(String key){
    return seen.getIfPresent(key) !=null;
  }

  public void forget(Article article){
    seen.invalidate(article.key());
  }
}
