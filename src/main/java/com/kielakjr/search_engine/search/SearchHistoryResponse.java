package com.kielakjr.search_engine.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SearchHistoryResponse {
  private String query;
  private String domain;
  private String timestamp;
}
