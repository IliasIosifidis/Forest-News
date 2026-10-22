package io.github.iliasiosifidis.digestservice.source;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class ArticleStoreRestClient {

  private static final int PAGE_SIZE = 100;
  private final RestClient restClient;

  public ArticleStoreRestClient(RestClient.Builder builder,
                                @Value("${digest.article-store-url:http://localhost:8081}") String baseUrl) {
    this.restClient = builder.baseUrl(baseUrl).build();
  }

  public List<ArticlePage.Item> fetchArticles(Instant from, Instant to) {
    List<ArticlePage.Item> articles = new ArrayList<>();
    int page = 0;
    int totalPages;
    do {
      ArticlePage response = fetchPage(from, to, page);
      articles.addAll(response.content());
      totalPages = response.page().totalPages();
      page++;
    } while (page < totalPages);
    return articles;
  }

  private ArticlePage fetchPage(Instant from, Instant to, int page){
    ArticlePage response = restClient.get()
            .uri(uri -> uri.path("/api/articles")
                    .queryParam("from", from)
                    .queryParam("to",to)
                    .queryParam("page", page)
                    .queryParam("size", PAGE_SIZE)
                    .build())
            .retrieve()
            .body(ArticlePage.class);
    if (response == null){
      throw new IllegalStateException("Empty response from Article Store");
    }
    return response;
  }
}
