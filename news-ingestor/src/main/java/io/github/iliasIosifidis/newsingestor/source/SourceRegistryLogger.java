package io.github.iliasIosifidis.newsingestor.source;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SourceRegistryLogger implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(SourceRegistryLogger.class);
  private final List<NewsSource> sources;

  public SourceRegistryLogger(List<NewsSource> sources) {
    this.sources = sources;
  }

  @Override
  public void run(ApplicationArguments args){
    for (NewsSource s: sources){
      s.disabledReason().ifPresentOrElse(
              reason -> log.warn("Source '{}' DISABLED: {}", s.name(), reason),
              () -> log.info("Source '{}' enabled", s.name()));
    }
    if (sources.stream().allMatch(s -> s.disabledReason().isPresent())){
      log.error("No enabled news sources: the ingestor will fetch nothing");
    }
  }
}
