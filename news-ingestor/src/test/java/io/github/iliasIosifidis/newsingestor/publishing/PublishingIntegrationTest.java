package io.github.iliasIosifidis.newsingestor.publishing;

import io.github.iliasIosifidis.newsingestor.article.Article;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.rabbitmq.RabbitMQContainer;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public class PublishingIntegrationTest {

  @Container
  static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:4-management");
  private CachingConnectionFactory connectionFactory;
  private RabbitTemplate rabbitTemplate;
  private String queue;

  @BeforeEach
  void setUp() {
    connectionFactory = new CachingConnectionFactory(rabbit.getHost(), rabbit.getAmqpPort());
    connectionFactory.setUsername(rabbit.getAdminUsername());
    connectionFactory.setPassword(rabbit.getAdminPassword());
    // Set up the broker like a consumer would: exchange, queue, article.# binding
    RabbitAdmin admin = new RabbitAdmin(connectionFactory);
    TopicExchange exchange = new TopicExchange("news.articles", true, false);
    admin.declareExchange(exchange);
    Queue consumerQueue = new AnonymousQueue();
    admin.declareQueue(consumerQueue);
    admin.declareBinding(BindingBuilder.bind(consumerQueue).to(exchange).with("article.#"));
    queue = consumerQueue.getName();
    rabbitTemplate = new RabbitTemplate(connectionFactory);
    rabbitTemplate.setMessageConverter(new JacksonJsonMessageConverter(JsonMapper.builder().build()));
  }

  @AfterEach
  void tearDown() {
    connectionFactory.destroy();
  }

  @Test
  void aPublishedArticleArrivesAsJsonWithTheSourceAsRoutingKey() {
    Article article = new Article("guardian", "g-1", "Title", "http://example.com", "Summary",
            Instant.parse("2026-11-12T10:00:00Z"));

    new ArticlePublisher(rabbitTemplate).publish(article);
    Message message = rabbitTemplate.receive(queue, 5000);
    assertThat(message).isNotNull();
    assertThat(message.getMessageProperties().getReceivedRoutingKey()).isEqualTo("article.guardian");
    String json = new String(message.getBody(), StandardCharsets.UTF_8);
    assertThat(json).contains(
            "\"source\":\"guardian\"",
            "\"externalId\":\"g-1\"",
            "\"publishedAt\":\"2026-11-12T10:00:00Z\"");
  }
}
