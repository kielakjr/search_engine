package com.kielakjr.search_engine.search;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import com.kielakjr.search_engine.auth.User;
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

  public List<SearchHistoryResponse> getSearchHistoryForCurrentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
      User user = (User) auth.getPrincipal();
      return getSearchHistoryForUser(user.getId());
    }
    return List.of();
  }

  private SearchHistoryResponse toResponse(SearchHistory history) {
    return SearchHistoryResponse.builder()
      .query(history.getQuery())
      .domain(history.getDomain())
      .timestamp(history.getTimestamp().toString())
      .build();
  }
}
