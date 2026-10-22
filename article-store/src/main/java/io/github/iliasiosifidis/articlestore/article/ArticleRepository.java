package io.github.iliasiosifidis.articlestore.article;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article,Long> {

  @Modifying
  @Transactional
  @Query(value = """
            INSERT INTO articles (source, external_id, title, url, summary, published_at)
            VALUES (:source, :externalId, :title, :url, :summary, :publishedAt)
            ON CONFLICT (source, external_id) DO NOTHING
            """, nativeQuery = true)
  int insertIfAbsent (@Param("source") String source,
                      @Param("externalId") String externalId,
                      @Param("title") String title,
                      @Param("url") String url,
                      @Param("summary") String summary,
                      @Param("publishedAt")Instant publishedAt);

  Page<Article> findBySource(String source, Pageable pageable);

  @Query("select a from Article a where a.receivedAt >= :from and a.receivedAt < :to")
  Page<Article> findByReceivedBetween(Instant from, Instant to, Pageable pageable);

  Optional<Article> findBySourceAndExternalId(String source, String externalId);
}
