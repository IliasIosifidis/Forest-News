package io.github.iliasIosifidis.newsingestor.dedup;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConfigurationProperties(prefix = "news.dedup")
public record DedupProperties(
        @DefaultValue("48h") Duration retention,
        @DefaultValue("10000") long maxSize) {}
