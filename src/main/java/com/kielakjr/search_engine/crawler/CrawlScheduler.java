package com.kielakjr.search_engine.crawler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.kielakjr.search_engine.source.SourceRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CrawlScheduler {
  private final CrawlerService crawlerService;
  private final SourceRepository sourceRepository;
  private final CrawlJobRepository crawlJobRepository;

  @Scheduled(
    fixedRateString = "${app.crawler.crawl-interval-ms}",
    initialDelayString = "${app.crawler.initial-delay-ms}"
  )
  public void scheduleCrawl() {
    sourceRepository.findAll().stream().filter(source -> {
      return source.isActive() && !crawlJobRepository.existsBySourceIdAndStatus(source.getId(), CrawlStatus.RUNNING);
    }).forEach(crawlerService::startCrawl);
  }
}
