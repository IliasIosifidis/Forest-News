package io.github.iliasiosifidis.articlestore.article;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name="articles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Article {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String source;
  private String externalId;
  private String title;
  private String url;
  private String summary;
  private Instant publishedAt;

  @Column(name = "received_at", insertable = false, updatable = false)
  private Instant receivedAt;
}

