package com.kielakjr.search_engine.crawler;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.kielakjr.search_engine.source.Source;
import com.kielakjr.search_engine.source.SourceRepository;
import com.kielakjr.search_engine.auth.JwtService;
import com.kielakjr.search_engine.auth.UserRepository;

@WebMvcTest(CrawlerController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CrawlerControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private CrawlerService crawlerService;

  @MockitoBean
  private SourceRepository sourceRepository;

  @MockitoBean
  private JwtService jwtService;

  @MockitoBean
  private UserRepository userRepository;

  private Source source;
  private CrawlJobResponse crawlJobResponse;

  @BeforeEach
  void setUp() {
    source = Source.builder()
        .id(1L)
        .url("https://example.com")
        .build();

    crawlJobResponse = CrawlJobResponse.builder()
        .id(1L)
        .sourceUrl("https://example.com")
        .status(CrawlStatus.COMPLETED)
        .pagesFound(5)
        .build();
  }

  @Nested
  @DisplayName("POST /api/crawler/start/{sourceId}")
  class StartCrawlTests {

    @Test
    void startCrawlReturns202WhenSourceExists() throws Exception {
      when(sourceRepository.findById(1L)).thenReturn(Optional.of(source));

      mockMvc.perform(post("/api/crawler/start/1"))
          .andExpect(status().isAccepted())
          .andExpect(jsonPath("$").value("Crawl started for source: https://example.com"));

      verify(crawlerService).startCrawl(source);
    }

    @Test
    void startCrawlReturns404WhenSourceNotFound() throws Exception {
      when(sourceRepository.findById(anyLong())).thenReturn(Optional.empty());

      mockMvc.perform(post("/api/crawler/start/99"))
          .andExpect(status().isNotFound());
    }

    @Test
    void startCrawlReturns400WhenSourceIdIsNotANumber() throws Exception {
      mockMvc.perform(post("/api/crawler/start/notanumber"))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("GET /api/crawler/jobs")
  class GetAllJobsTests {

    @Test
    void getAllJobsReturns200WithJobs() throws Exception {
      when(crawlerService.getAllJobs()).thenReturn(List.of(crawlJobResponse));

      mockMvc.perform(get("/api/crawler/jobs"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$[0].sourceUrl").value("https://example.com"))
          .andExpect(jsonPath("$[0].status").value("COMPLETED"))
          .andExpect(jsonPath("$[0].pagesFound").value(5));
    }

    @Test
    void getAllJobsReturns200WithEmptyList() throws Exception {
      when(crawlerService.getAllJobs()).thenReturn(List.of());

      mockMvc.perform(get("/api/crawler/jobs"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").isEmpty());
    }
  }
}
