package io.github.iliasIosifidis.newsingestor.source.freenews;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("news.free")
public record FreeNewsProperties(
        String baseUrl,
        String apiKey,
        @DefaultValue("us") String country,
        @DefaultValue("world") String topic) {}
