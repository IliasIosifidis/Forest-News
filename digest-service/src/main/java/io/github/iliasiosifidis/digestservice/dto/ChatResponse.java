package io.github.iliasiosifidis.digestservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


public record ChatResponse(String model, List<Choice> choices) {
  public record Choice(
          @JsonProperty("finish_reason")
          String finishReason,
          ChatMessage message){}
}
