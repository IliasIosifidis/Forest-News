package io.github.iliasiosifidis.digestservice.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "digests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Digest {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private LocalDate day;

  @JdbcTypeCode(SqlTypes.JSON)
  private List<String> items;

  private String model;
  private int articleCount;
  private Instant createdAt;
}
