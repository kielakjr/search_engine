package com.kielakjr.search_engine.crawler;

import com.kielakjr.search_engine.search.PageRepository;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.jsoup.HttpStatusException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.kielakjr.search_engine.config.CrawlerProperties;
import com.kielakjr.search_engine.search.PageDocument;
import com.kielakjr.search_engine.source.Source;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CrawlerService {
  private final PageRepository pageRepository;
  private final CrawlJobRepository crawlJobRepository;
  private final CrawlerProperties crawlerProperties;
  private final JsoupFetcher jsoupFetcher;

  @Async("crawlerTaskExecutor")
  public void startCrawl(Source source) {
    log.info("Starting crawl for source: " + source.getUrl());
    CrawlJob crawlJob = CrawlJob.builder()
      .source(source)
      .status(CrawlStatus.PENDING)
      .build();
    crawlJobRepository.save(crawlJob);
    crawlJob.setStatus(CrawlStatus.RUNNING);
    crawlJobRepository.save(crawlJob);
    try {
      int pagesFound = crawl(source.getUrl());
      crawlJob.setPagesFound(pagesFound);
      crawlJob.setStatus(CrawlStatus.COMPLETED);
    } catch (Exception e) {
      crawlJob.setStatus(CrawlStatus.FAILED);
      log.error("Crawl failed for source: " + source.getUrl(), e);
    } finally {
      crawlJobRepository.save(crawlJob);
    }
  }

  private int crawl(String seedUrl) {
    Queue<String> queue = new LinkedList<>();
    Set<String> visited = new HashSet<>();
    Map<String, Integer> depthMap = new HashMap<>();
    int pagesFound = 0;

    queue.add(seedUrl);
    visited.add(seedUrl);
    depthMap.put(seedUrl, 0);

    while (!queue.isEmpty()) {
      String url = queue.poll();
      int depth = depthMap.get(url);

      if (depth > crawlerProperties.getMaxDepth()) continue;
      if (pagesFound >= crawlerProperties.getMaxPages()) break;

      try {
        Document doc = jsoupFetcher.fetch(url);
        String title = doc.title();
        String body = doc.select("p, h1, h2, h3").text();
        if (body.isEmpty()) {
          body = doc.body().text();
        }
        if (body.length() > 5000) {
          body = body.substring(0, 5000);
        }
        pagesFound++;
        PageDocument pageDoc = PageDocument.builder()
          .url(url)
          .title(title)
          .content(body)
          .domain(seedUrl)
          .crawledAt(java.time.LocalDateTime.now())
          .build();

        pageRepository.save(pageDoc);

        Elements links = doc.select("a[href]");
        for (Element link : links) {
          String linkUrl = link.absUrl("href");

          if (linkUrl.matches(".*(\\.jpg|\\.png|\\.pdf|\\.zip).*")) continue;
          if (linkUrl.contains("mailto:")) continue;
          if (linkUrl.contains("javascript:")) continue;

          if (linkUrl.contains("#")) {
            linkUrl = linkUrl.substring(0, linkUrl.indexOf("#"));
          }

          if (linkUrl.isEmpty()) continue;

          if (!visited.contains(linkUrl) && isSameDomain(linkUrl, seedUrl)) {
            queue.add(linkUrl);
            visited.add(linkUrl);
            depthMap.put(linkUrl, depth + 1);
          }
        }
      } catch (HttpStatusException e) {

      } catch (Exception e) {
        log.error("Failed to crawl: " + url + " | " + e.getMessage(), e);
      }
    }
    return pagesFound;
  }

  private boolean isSameDomain(String url, String seedUrl) {
    try {
        String urlHost = new java.net.URI(url).getHost();
        String seedHost = new java.net.URI(seedUrl).getHost();
        return urlHost != null && urlHost.equals(seedHost);
    } catch (Exception e) {
        return false;
    }
  }

  public List<CrawlJobResponse> getAllJobs() {
    return crawlJobRepository.findAllByOrderByCreatedAtDesc().stream()
      .map(this::toResponse)
      .collect(Collectors.toList());
  }

  private CrawlJobResponse toResponse(CrawlJob job) {
    return CrawlJobResponse.builder()
      .id(job.getId())
      .sourceUrl(job.getSource().getUrl())
      .status(job.getStatus())
      .pagesFound(job.getPagesFound())
      .createdAt(job.getCreatedAt())
      .build();
  }
}

