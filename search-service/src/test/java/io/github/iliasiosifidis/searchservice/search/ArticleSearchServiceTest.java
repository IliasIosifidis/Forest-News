package io.github.iliasiosifidis.searchservice.search;

import io.github.iliasiosifidis.searchservice.index.ArticleDocument;
import io.github.iliasiosifidis.searchservice.repository.ArticleSearchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.elasticsearch.test.autoconfigure.DataElasticsearchTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataElasticsearchTest
@Import(ArticleSearchService.class)
@Testcontainers
class ArticleSearchServiceTest {

  @Container
  @ServiceConnection
  static ElasticsearchContainer elasticSearch =
          new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:9.3.4")
                  .withEnv("xpack.security.enabled", "false")
                  .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m");

  @Autowired
  private ArticleSearchService searchService;

  @Autowired
  private ArticleSearchRepository repository;

  @Autowired
  private ElasticsearchOperations operations;

  @BeforeEach
  void emptyTheIndex() {
    repository.deleteAll();
    refresh();
  }

  @Test
  void findsATitleFromTheBeginningOfAWord() {
    index(doc("guardian", "1", "Election results announced", "Votes were counted overnight."));
    assertThat(titles(searchService.search("elec", null, 10)));
  }

  @Test
  void forgivesATypoInAnEarlierWord() {
    index(doc("guardian", "1", "Election results announced", "Votes were counted overnight."));

    // "electoin" only matches through fuzziness; "zzz" matches nothing
    assertThat(titles(searchService.search("electoin zzz", null, 10)))
            .containsExactly("Election results announced");
  }

  @Test
  void doesNotForgiveATypoInTheLastWord() {
    index(doc("guardian", "1", "Election results announced", "Votes were counted overnight."));

    // the last word is treated as a prefix, and prefixes are never fuzzy
    assertThat(searchService.search("electoin", null, 10)).isEmpty();
  }

  @Test
  void ranksATitleMatchAboveASummaryMatch() {
    index(doc("guardian", "1", "Markets close higher", "Investors shrugged off climate worries."),
            doc("guardian", "2", "Climate summit opens", "Leaders meet in Belem."));

    assertThat(titles(searchService.search("climate", null, 10)))
            .first().isEqualTo("Climate summit opens");
  }

  @Test
  void filtersBySource() {
    index(doc("guardian", "1", "Climate summit opens", "Guardian coverage."),
            doc("currents", "2", "Climate summit opens", "Currents coverage."));

    assertThat(searchService.search("climate", "guardian", 10))
            .extracting(ArticleDocument::source)
            .containsOnly("guardian");
  }

  @Test
  void neverReturnsMoreThan20Results() {
    List<ArticleDocument> many = new ArrayList<>();
    for (int i = 0; i < 25; i++) {
      many.add(doc("guardian", "c" + i, "Climate story " + i, "Summary"));
    }
    index(many.toArray(ArticleDocument[]::new));

    assertThat(searchService.search("climate", null, 100)).hasSize(20);
  }

  private void index(ArticleDocument... documents) {
    repository.saveAll(List.of(documents));
    refresh();
  }

  private void refresh() {
    operations.indexOps(ArticleDocument.class).refresh();
  }

  private static ArticleDocument doc(String source, String id, String title, String summary) {
    return ArticleDocument.of(source, id, title, "https://example.com/" + id, summary, Instant.parse("2026-11-12T10:00:00Z"));
  }

  private static List<String> titles(List<ArticleDocument> documents) {
    return documents.stream().map(ArticleDocument::title).toList();
  }
}