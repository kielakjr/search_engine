package com.kielakjr.search_engine.search;

import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import com.kielakjr.search_engine.auth.User;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchService {
  private final ElasticsearchOperations elasticsearchOperations;
  private final SearchHistoryRepository searchHistoryRepository;

  public List<SearchResponse> search(String query, int page, int size, String domain) {
    Query searchQuery = NativeQuery.builder()
      .withQuery(q -> q
        .bool(b -> {
          b.must(must -> must
            .multiMatch(m -> m
              .query(query)
              .fields("title^3", "content")
              .fuzziness("1")
            )
          );
          if (domain != null && !domain.isEmpty()) {
            b.filter(f -> f
              .term(t -> t
                .field("domain")
                .value(domain)
              )
            );
          }
          return b;
        })
      )
      .withHighlightQuery(new HighlightQuery(new Highlight(List.of(new HighlightField("content"))), PageDocument.class))
      .withPageable(PageRequest.of(page, size))
      .build();

    List<SearchHit<PageDocument>> searchHits = elasticsearchOperations.search(searchQuery, PageDocument.class).getSearchHits();

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
      User user = (User) auth.getPrincipal();
      SearchHistory history = SearchHistory.builder()
        .query(query)
        .domain(domain)
        .user(user)
        .build();
      searchHistoryRepository.save(history);
    }

    return searchHits.stream()
      .map(hit -> {
        PageDocument doc = hit.getContent();
        List<String> snippets = hit.getHighlightField("content");
        String snippet = snippets.isEmpty() ? "" : snippets.get(0);
        return SearchResponse.builder()
          .url(doc.getUrl())
          .title(doc.getTitle())
          .snippet(snippet)
          .build();
      })
      .collect(Collectors.toList());
  }
}
