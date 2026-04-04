package com.kielakjr.search_engine.search;

import lombok.Getter;
import lombok.Builder;

@Builder
@Getter
public class SearchResponse {
  private String url;
  private String title;
  private String snippet;
}
