package io.github.iliasiosifidis.searchservice.backfill;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConfigurationProperties(prefix = "search.backfill")
public record BackfillProperties(
        @DefaultValue("http://localhost:8081") String articleStoreUrl,
        @DefaultValue("100") int pageSize,
        @DefaultValue("10")Duration retryDelay) {}
