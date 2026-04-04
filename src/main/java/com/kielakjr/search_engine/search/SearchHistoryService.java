package com.kielakjr.search_engine.search;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchHistoryService {
  private final SearchHistoryRepository searchHistoryRepository;

  public List<SearchHistoryResponse> getSearchHistoryForUser(Long userId) {
    return searchHistoryRepository.findByUserIdOrderByTimestampDesc(userId).stream()
      .map(this::toResponse)
      .toList();
  }

  private SearchHistoryResponse toResponse(SearchHistory history) {
    return SearchHistoryResponse.builder()
      .query(history.getQuery())
      .domain(history.getDomain())
      .timestamp(history.getTimestamp().toString())
      .build();
  }
}
