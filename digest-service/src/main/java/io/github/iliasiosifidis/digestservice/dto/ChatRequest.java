package io.github.iliasiosifidis.digestservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record ChatRequest(
        List<ChatMessage> messages,
        double temperature,

        @JsonProperty("response_format")
        Map<String, Object> responseFormat) {}


