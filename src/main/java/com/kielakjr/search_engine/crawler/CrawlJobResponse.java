package com.kielakjr.search_engine.crawler;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CrawlJobResponse {
  private Long id;
  private String sourceUrl;
  private CrawlStatus status;
  private int pagesFound;
  private LocalDate createdAt;
}
