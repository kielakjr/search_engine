package com.kielakjr.search_engine.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {
  private String url;
  private String title;
  private String snippet;
}
