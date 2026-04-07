package com.kielakjr.search_engine.crawler;

import com.kielakjr.search_engine.search.PageRepository;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.Set;

import org.jsoup.nodes.Document;
import org.jsoup.HttpStatusException;
import org.springframework.data.redis.core.RedisTemplate;
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
  private final RedisTemplate<String, String> redisTemplate;

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
    Map<String, Integer> depthMap = new HashMap<>();
    String redisKey = "crawled:" + seedUrl;
    int pagesFound = 0;

    queue.add(seedUrl);
    redisTemplate.expire(redisKey, 24, TimeUnit.HOURS);
    depthMap.put(seedUrl, 0);

    Set<String> disallowedPaths = getDisallowedPaths(seedUrl);

    while (!queue.isEmpty()) {
      String url = queue.poll();
      int depth = depthMap.get(url);

      if (depth > crawlerProperties.getMaxDepth()) continue;
      if (pagesFound >= crawlerProperties.getMaxPages()) break;

      if (pagesFound > 0 && crawlerProperties.getRequestDelayMs() > 0) {
        try {
          Thread.sleep(crawlerProperties.getRequestDelayMs());
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          break;
        }
      }

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

          if (isDisallowed(linkUrl, disallowedPaths)) continue;

          Boolean isMember = redisTemplate.opsForSet().isMember(redisKey, linkUrl);
          if (Boolean.FALSE.equals(isMember) && isSameDomain(linkUrl, seedUrl)) {
            queue.add(linkUrl);
            redisTemplate.opsForSet().add(redisKey, linkUrl);
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

  private boolean isDisallowed(String url, Set<String> disallowedPaths) {
    try {
      String path = new java.net.URI(url).getPath();
      if (path == null) return false;
      return disallowedPaths.stream().anyMatch(path::startsWith);
    } catch (Exception e) {
      return false;
    }
  }

  private Set<String> getDisallowedPaths(String seedUrl) {
    try {
      String robotsUrl = seedUrl + "/robots.txt";
      Document doc = jsoupFetcher.fetch(robotsUrl);
      String text = doc.body().wholeText();

      boolean relevantAgent = false;
      Set<String> disallowed = new HashSet<>();

      for (String line : text.split("\n")) {
        line = line.split("#")[0].trim();
        if (line.toLowerCase().startsWith("user-agent:")) {
          String agent = line.substring(11).trim();
          relevantAgent = agent.equals("*");
        } else if (relevantAgent && line.toLowerCase().startsWith("disallow:")) {
          String path = line.substring(9).trim();
          if (!path.isEmpty()) disallowed.add(path);
        }
      }
      return disallowed;
    } catch (Exception e) {
      return new HashSet<>();
    }
  }
}

