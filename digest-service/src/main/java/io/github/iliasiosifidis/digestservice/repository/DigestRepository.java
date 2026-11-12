package io.github.iliasiosifidis.digestservice.repository;

import io.github.iliasiosifidis.digestservice.entities.Digest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

public interface DigestRepository extends JpaRepository<Digest, Long> {

  @Modifying
  @Transactional
  @Query(value = """
          INSERT INTO digests.digests (day, items, model, article_count)
          VALUES (:day, CAST(:items AS jsonb), :model, :articleCount)
          ON CONFLICT (day) DO UPDATE
          SET items         = EXCLUDED.items,
              model         = EXCLUDED.model,
              article_count = EXCLUDED.article_count,
              created_at    = now()
          """, nativeQuery = true)
  void upsert(LocalDate day, String items, String model, int articleCount);

  Optional<Digest> findTopByOrderByDayDesc();
}
