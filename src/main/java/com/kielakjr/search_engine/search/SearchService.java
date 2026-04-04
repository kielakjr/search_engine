package com.kielakjr.search_engine.search;

import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchService {
  private final ElasticsearchOperations elasticsearchOperations;

  public List<SearchResponse> search(String query, int page, int size) {
    Query searchQuery = NativeQuery.builder()
      .withQuery(q -> q
        .multiMatch(m -> m
          .query(query)
          .fields("title^3", "content")
        )
      )
      .withHighlightQuery(new HighlightQuery(new Highlight(List.of(new HighlightField("content"))), PageDocument.class))
      .withPageable(PageRequest.of(page, size))
      .build();

    List<SearchHit<PageDocument>> searchHits = elasticsearchOperations.search(searchQuery, PageDocument.class).getSearchHits();
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
