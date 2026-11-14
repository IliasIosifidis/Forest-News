package io.github.iliasIosifidis.newsingestor.publishing;

import io.github.iliasIosifidis.newsingestor.article.Article;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ArticlePublisherTest {

  private final RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class); //Fake template
  private final ArticlePublisher publisher = new ArticlePublisher(rabbitTemplate);

  @Test
  void publishesToTheExchangeWithTheSourceAsRoutingKey(){
    Article article = new Article("hackernews","1","Title","https://example.com",null, Instant.parse("2026-11-12T10:00:00Z"));
    publisher.publish(article);
    verify(rabbitTemplate).convertAndSend("news.articles", "article.hackernews", article); //Verifies the call with these arguments
  }

}