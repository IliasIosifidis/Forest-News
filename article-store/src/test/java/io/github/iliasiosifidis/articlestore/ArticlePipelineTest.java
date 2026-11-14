package io.github.iliasiosifidis.articlestore;

import io.github.iliasiosifidis.articlestore.article.ArticleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@Testcontainers
class ArticlePipelineTest {

  @Container
  @ServiceConnection
  static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

  @Container
  @ServiceConnection
  static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:4-management");

  @Autowired
  private RabbitTemplate rabbitTemplate;

  @Autowired
  private ArticleRepository repository;

  @Test
  void anArticlePublishedToTheExchangeEndsUpInTheDatabase() {
    rabbitTemplate.convertAndSend("news.articles", "article.guardian", message("pipeline-1"));

    await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
            assertThat(repository.findBySourceAndExternalId("guardian", "pipeline-1")).isPresent());
  }

  @Test
  void theSameArticleDeliveredTwiceIsStoredOnce() {
    rabbitTemplate.convertAndSend("news.articles", "article.guardian", message("pipeline-dup"));
    rabbitTemplate.convertAndSend("news.articles", "article.guardian", message("pipeline-dup"));

    await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
            assertThat(repository.findBySourceAndExternalId("guardian", "pipeline-dup")).isPresent());
    await().during(Duration.ofSeconds(2)).atMost(Duration.ofSeconds(5)).untilAsserted(() ->
            assertThat(repository.findAll())
                    .filteredOn(a -> a.getExternalId().equals("pipeline-dup"))
                    .hasSize(1));
  }

  /** The same JSON the news-ingestor sends. */
  private static Map<String, Object> message(String externalId) {
    return Map.of(
            "source", "guardian",
            "externalId", externalId,
            "title", "The pipeline works",
            "url", "https://example.com/" + externalId,
            "summary", "Published to RabbitMQ, stored in Postgres.",
            "publishedAt", "2026-11-12T10:00:00Z");
  }
}