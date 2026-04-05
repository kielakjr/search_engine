package com.kielakjr.search_engine.search;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.kielakjr.search_engine.auth.JwtService;
import com.kielakjr.search_engine.auth.UserRepository;

@WebMvcTest(SearchController.class)
@AutoConfigureMockMvc(addFilters = false)
public class SearchControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private SearchService searchService;

  @MockitoBean
  private SearchHistoryService searchHistoryService;

  @MockitoBean
  private JwtService jwtService;

  @MockitoBean
  private UserRepository userRepository;

  private SearchResponse searchResponse;
  private SearchHistoryResponse searchHistoryResponse;

  @BeforeEach
  void setUp() {
    searchResponse = SearchResponse.builder()
        .url("https://example.com")
        .title("Example Title")
        .snippet("Some content snippet")
        .build();

    searchHistoryResponse = SearchHistoryResponse.builder()
        .query("test query")
        .domain("https://example.com")
        .build();
  }

  @Nested
  @DisplayName("GET /search")
  class SearchTests {

    @Test
    void searchReturns200WithResults() throws Exception {
      when(searchService.search(anyString(), anyInt(), anyInt(), any()))
          .thenReturn(List.of(searchResponse));

      mockMvc.perform(get("/search")
              .param("query", "test"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$[0].url").value("https://example.com"))
          .andExpect(jsonPath("$[0].title").value("Example Title"))
          .andExpect(jsonPath("$[0].snippet").value("Some content snippet"));
    }

    @Test
    void searchReturns200WithEmptyResults() throws Exception {
      when(searchService.search(anyString(), anyInt(), anyInt(), any()))
          .thenReturn(List.of());

      mockMvc.perform(get("/search")
              .param("query", "test"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void searchReturns400WhenQueryIsMissing() throws Exception {
      mockMvc.perform(get("/search"))
          .andExpect(status().isBadRequest());
    }

    @Test
    void searchPassesDefaultPageAndSize() throws Exception {
      when(searchService.search(anyString(), anyInt(), anyInt(), any()))
          .thenReturn(List.of());

      mockMvc.perform(get("/search")
              .param("query", "test"))
          .andExpect(status().isOk());

      verify(searchService).search("test", 0, 10, null);
    }

    @Test
    void searchPassesDomainWhenProvided() throws Exception {
      when(searchService.search(anyString(), anyInt(), anyInt(), anyString()))
          .thenReturn(List.of(searchResponse));

      mockMvc.perform(get("/search")
              .param("query", "test")
              .param("domain", "https://example.com"))
          .andExpect(status().isOk());

      verify(searchService).search("test", 0, 10, "https://example.com");
    }
  }

  @Nested
  @DisplayName("GET /search/history")
  class SearchHistoryForCurrentUserTests {

    @Test
    void getSearchHistoryReturns200WithResults() throws Exception {
      when(searchHistoryService.getSearchHistoryForCurrentUser())
          .thenReturn(List.of(searchHistoryResponse));

      mockMvc.perform(get("/search/history"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$[0].query").value("test query"))
          .andExpect(jsonPath("$[0].domain").value("https://example.com"));
    }

    @Test
    void getSearchHistoryReturns200WithEmptyList() throws Exception {
      when(searchHistoryService.getSearchHistoryForCurrentUser())
          .thenReturn(List.of());

      mockMvc.perform(get("/search/history"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").isEmpty());
    }
  }

  @Nested
  @DisplayName("GET /search/history/{userId}")
  class SearchHistoryForUserTests {

    @Test
    void getSearchHistoryForUserReturns200WithResults() throws Exception {
      when(searchHistoryService.getSearchHistoryForUser(anyLong()))
          .thenReturn(List.of(searchHistoryResponse));

      mockMvc.perform(get("/search/history/1"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$[0].query").value("test query"))
          .andExpect(jsonPath("$[0].domain").value("https://example.com"));
    }

    @Test
    void getSearchHistoryForUserReturns200WithEmptyList() throws Exception {
      when(searchHistoryService.getSearchHistoryForUser(anyLong()))
          .thenReturn(List.of());

      mockMvc.perform(get("/search/history/1"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getSearchHistoryForUserReturns400WhenUserIdIsNotANumber() throws Exception {
      mockMvc.perform(get("/search/history/notanumber"))
          .andExpect(status().isBadRequest());
    }
  }
}
