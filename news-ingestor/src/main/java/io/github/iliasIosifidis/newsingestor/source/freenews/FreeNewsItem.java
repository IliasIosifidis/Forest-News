package io.github.iliasIosifidis.newsingestor.source.freenews;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

record FreeNewsList(List<Item> data) {
  record Item(
          String uuid,
          String title,
          @JsonProperty("published_at") String publishedAt) {}
}

record FreeNewsDetails(Detail data){
  record Detail(
          @JsonProperty("original_url") String originalUrl,
          String incipit){}
}
