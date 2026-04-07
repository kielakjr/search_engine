package com.kielakjr.search_engine.crawler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

import com.kielakjr.search_engine.config.CrawlerProperties;
import com.kielakjr.search_engine.search.PageDocument;
import com.kielakjr.search_engine.search.PageRepository;
import com.kielakjr.search_engine.source.Source;

@ExtendWith(MockitoExtension.class)
public class CrawlerServiceTest {

  @Mock
  private PageRepository pageRepository;
  @Mock
  private CrawlJobRepository crawlJobRepository;
  @Mock
  private CrawlerProperties crawlerProperties;
  @Mock
  private JsoupFetcher jsoupFetcher;
  @Mock
  private RedisTemplate<String, String> redisTemplate;
  @Mock
  private SetOperations<String, String> setOperations;

  @InjectMocks
  private CrawlerService crawlerService;

  private Source source;

  @BeforeEach
  void setUp() {
    source = Source.builder()
        .url("https://example.com")
        .build();
  }

  private Document buildRobotsDocument(String robotsContent) {
    String html = "<html><body>" + robotsContent.replace("\n", "\n") + "</body></html>";
    return Parser.htmlParser().parseInput(html, "https://example.com/robots.txt");
  }

  private Document buildDocument(String title, String body, List<String> links) {
    StringBuilder html = new StringBuilder();
    html.append("<html><head><title>").append(title).append("</title></head><body>");
    html.append("<p>").append(body).append("</p>");
    for (String link : links) {
      html.append("<a href=\"").append(link).append("\">link</a>");
    }
    html.append("</body></html>");
    return Parser.htmlParser().parseInput(html.toString(), "https://example.com");
  }

  @Nested
  @DisplayName("startCrawl method")
  class StartCrawlTests {

    @BeforeEach
    void setUp() {
      when(crawlJobRepository.save(any(CrawlJob.class))).thenAnswer(i -> i.getArgument(0));
      when(crawlerProperties.getMaxDepth()).thenReturn(2);
      when(crawlerProperties.getMaxPages()).thenReturn(10);
      when(redisTemplate.opsForSet()).thenReturn(setOperations);
    }

    @Test
    void startCrawlSavesJobAsCompletedOnSuccess() throws IOException {
      when(jsoupFetcher.fetch("https://example.com"))
          .thenReturn(buildDocument("Example", "Some content", List.of()));

      crawlerService.startCrawl(source);

      verify(crawlJobRepository, times(3)).save(any(CrawlJob.class));
    }

    @Test
    void startCrawlSavesJobAsFailedOnException() throws IOException {
      when(jsoupFetcher.fetch("https://example.com"))
          .thenThrow(new RuntimeException("Network error"));

      crawlerService.startCrawl(source);

      verify(crawlJobRepository, times(3)).save(any(CrawlJob.class));
    }
  }

  @Nested
  @DisplayName("crawl method (via startCrawl)")
  class CrawlTests {

    @BeforeEach
    void setUp() {
      when(crawlJobRepository.save(any(CrawlJob.class))).thenAnswer(i -> i.getArgument(0));
      when(crawlerProperties.getMaxDepth()).thenReturn(2);
      when(crawlerProperties.getMaxPages()).thenReturn(10);
      when(redisTemplate.opsForSet()).thenReturn(setOperations);
    }

    @Test
    void crawlSavesPageDocumentForEachPageVisited() throws IOException {
      when(jsoupFetcher.fetch("https://example.com"))
          .thenReturn(buildDocument("Example", "Some content", List.of()));

      crawlerService.startCrawl(source);

      verify(pageRepository, times(1)).save(any(PageDocument.class));
    }

    @Test
    void crawlFollowsLinksOnSameDomain() throws IOException {
      when(jsoupFetcher.fetch("https://example.com"))
          .thenReturn(buildDocument("Home", "content", List.of("https://example.com/page2")));
      when(jsoupFetcher.fetch("https://example.com/page2"))
          .thenReturn(buildDocument("Page 2", "more content", List.of()));
      when(setOperations.isMember(anyString(), eq("https://example.com/page2"))).thenReturn(false);

      crawlerService.startCrawl(source);

      verify(pageRepository, times(2)).save(any(PageDocument.class));
    }

    @Test
    void crawlDoesNotFollowExternalLinks() throws IOException {
      when(jsoupFetcher.fetch("https://example.com"))
          .thenReturn(buildDocument("Home", "content", List.of("https://other.com/page")));

      crawlerService.startCrawl(source);

      verify(jsoupFetcher, never()).fetch("https://other.com/page");
      verify(pageRepository, times(1)).save(any(PageDocument.class));
    }

    @Test
    void crawlRespectsMaxPagesLimit() throws IOException {
      when(crawlerProperties.getMaxPages()).thenReturn(1);
      when(jsoupFetcher.fetch("https://example.com"))
          .thenReturn(buildDocument("Home", "content", List.of("https://example.com/page2")));

      crawlerService.startCrawl(source);

      verify(pageRepository, times(1)).save(any(PageDocument.class));
    }

    @Test
    void crawlSkipsPageOnHttpStatusException() throws IOException {
      when(jsoupFetcher.fetch("https://example.com"))
          .thenThrow(new HttpStatusException("Not found", 404, "https://example.com"));

      crawlerService.startCrawl(source);

      verify(pageRepository, never()).save(any(PageDocument.class));
    }

    @Test
    void crawlSkipsLinksWithImageExtensions() throws IOException {
      when(jsoupFetcher.fetch("https://example.com"))
          .thenReturn(buildDocument("Home", "content", List.of(
              "https://example.com/image.jpg",
              "https://example.com/file.pdf"
          )));

      crawlerService.startCrawl(source);

      verify(jsoupFetcher, never()).fetch("https://example.com/image.jpg");
      verify(jsoupFetcher, never()).fetch("https://example.com/file.pdf");
    }
  }

  @Nested
  @DisplayName("robots.txt parsing")
  class RobotsTxtTests {

    @BeforeEach
    void setUp() {
      when(crawlJobRepository.save(any(CrawlJob.class))).thenAnswer(i -> i.getArgument(0));
      when(crawlerProperties.getMaxDepth()).thenReturn(2);
      when(crawlerProperties.getMaxPages()).thenReturn(10);
    }

    @Test
    void crawlRespectsDisallowedPaths() throws IOException {
      when(jsoupFetcher.fetch("https://example.com/robots.txt"))
          .thenReturn(buildRobotsDocument("User-agent: *\nDisallow: /private"));
      when(jsoupFetcher.fetch("https://example.com"))
          .thenReturn(buildDocument("Home", "content", List.of("https://example.com/private/page")));

      crawlerService.startCrawl(source);

      verify(jsoupFetcher, never()).fetch("https://example.com/private/page");
      verify(pageRepository, times(1)).save(any(PageDocument.class));
    }

    @Test
    void crawlAllowsPathsNotInRobotsTxt() throws IOException {
      when(redisTemplate.opsForSet()).thenReturn(setOperations);
      when(jsoupFetcher.fetch("https://example.com/robots.txt"))
          .thenReturn(buildRobotsDocument("User-agent: *\nDisallow: /private"));
      when(jsoupFetcher.fetch("https://example.com"))
          .thenReturn(buildDocument("Home", "content", List.of("https://example.com/public/page")));
      when(jsoupFetcher.fetch("https://example.com/public/page"))
          .thenReturn(buildDocument("Public", "public content", List.of()));
      when(setOperations.isMember(anyString(), eq("https://example.com/public/page"))).thenReturn(false);

      crawlerService.startCrawl(source);

      verify(pageRepository, times(2)).save(any(PageDocument.class));
    }

    @Test
    void crawlIgnoresRulesForOtherUserAgents() throws IOException {
      when(redisTemplate.opsForSet()).thenReturn(setOperations);
      when(jsoupFetcher.fetch("https://example.com/robots.txt"))
          .thenReturn(buildRobotsDocument("User-agent: Googlebot\nDisallow: /secret"));
      when(jsoupFetcher.fetch("https://example.com"))
          .thenReturn(buildDocument("Home", "content", List.of("https://example.com/secret/page")));
      when(jsoupFetcher.fetch("https://example.com/secret/page"))
          .thenReturn(buildDocument("Secret", "secret content", List.of()));
      when(setOperations.isMember(anyString(), eq("https://example.com/secret/page"))).thenReturn(false);

      crawlerService.startCrawl(source);

      verify(pageRepository, times(2)).save(any(PageDocument.class));
    }

    @Test
    void crawlHandlesMissingRobotsTxt() throws IOException {
      when(jsoupFetcher.fetch("https://example.com/robots.txt"))
          .thenThrow(new HttpStatusException("Not found", 404, "https://example.com/robots.txt"));
      when(jsoupFetcher.fetch("https://example.com"))
          .thenReturn(buildDocument("Home", "content", List.of()));

      crawlerService.startCrawl(source);

      verify(pageRepository, times(1)).save(any(PageDocument.class));
    }

    @Test
    void crawlHandlesCommentsInRobotsTxt() throws IOException {
      when(jsoupFetcher.fetch("https://example.com/robots.txt"))
          .thenReturn(buildRobotsDocument("User-agent: * # all bots\nDisallow: /admin # admin area"));
      when(jsoupFetcher.fetch("https://example.com"))
          .thenReturn(buildDocument("Home", "content", List.of("https://example.com/admin/dashboard")));

      crawlerService.startCrawl(source);

      verify(jsoupFetcher, never()).fetch("https://example.com/admin/dashboard");
    }
  }

  @Nested
  @DisplayName("getAllJobs method")
  class GetAllJobsTests {

    @Test
    void getAllJobsReturnsEmptyListWhenNoJobs() {
      when(crawlJobRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());

      List<CrawlJobResponse> result = crawlerService.getAllJobs();

      assertThat(result).isEmpty();
    }

    @Test
    void getAllJobsReturnsMappedResponses() {
      CrawlJob job = CrawlJob.builder()
          .source(source)
          .status(CrawlStatus.COMPLETED)
          .pagesFound(5)
          .build();
      when(crawlJobRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(job));

      List<CrawlJobResponse> result = crawlerService.getAllJobs();

      assertThat(result).hasSize(1);
      assertThat(result.get(0).getSourceUrl()).isEqualTo("https://example.com");
      assertThat(result.get(0).getStatus()).isEqualTo(CrawlStatus.COMPLETED);
      assertThat(result.get(0).getPagesFound()).isEqualTo(5);
    }
  }
}
