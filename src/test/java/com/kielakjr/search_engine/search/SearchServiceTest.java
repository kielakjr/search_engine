package com.kielakjr.search_engine.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.kielakjr.search_engine.auth.Role;
import com.kielakjr.search_engine.auth.User;

@ExtendWith(MockitoExtension.class)
public class SearchServiceTest {

  @Mock
  private ElasticsearchOperations elasticsearchOperations;
  @Mock
  private SearchHistoryRepository searchHistoryRepository;
  @Mock
  private SearchHits<PageDocument> searchHits;
  @Mock
  private SearchHit<PageDocument> searchHit;

  @InjectMocks
  private SearchService searchService;

  private PageDocument pageDocument;

  @BeforeEach
  void setUp() {
    pageDocument = PageDocument.builder()
        .url("https://example.com")
        .title("Example Title")
        .content("Some content about the query")
        .domain("https://example.com")
        .build();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void stubElasticsearch(List<SearchHit<PageDocument>> hits) {
    when(elasticsearchOperations.search(any(Query.class), eq(PageDocument.class))).thenReturn(searchHits);
    when(searchHits.getSearchHits()).thenReturn(hits);
  }

  private void stubSearchHit(PageDocument doc, List<String> snippets) {
    when(searchHit.getContent()).thenReturn(doc);
    when(searchHit.getHighlightField("content")).thenReturn(snippets);
  }

  private void authenticateAs(User user) {
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @Nested
  @DisplayName("search method")
  class SearchTests {

    @Test
    void searchReturnsEmptyListWhenNoHits() {
      stubElasticsearch(List.of());

      List<SearchResponse> result = searchService.search("query", 0, 10, null);

      assertThat(result).isEmpty();
    }

    @Test
    void searchReturnsMappedResults() {
      stubSearchHit(pageDocument, List.of("Some <em>content</em>"));
      stubElasticsearch(List.of(searchHit));

      List<SearchResponse> result = searchService.search("query", 0, 10, null);

      assertThat(result).hasSize(1);
      assertThat(result.get(0).getUrl()).isEqualTo("https://example.com");
      assertThat(result.get(0).getTitle()).isEqualTo("Example Title");
      assertThat(result.get(0).getSnippet()).isEqualTo("Some <em>content</em>");
    }

    @Test
    void searchReturnsEmptySnippetWhenNoHighlights() {
      stubSearchHit(pageDocument, List.of());
      stubElasticsearch(List.of(searchHit));

      List<SearchResponse> result = searchService.search("query", 0, 10, null);

      assertThat(result.get(0).getSnippet()).isEmpty();
    }

    @Test
    void searchWithDomainFilterPassesDomainCorrectly() {
      stubElasticsearch(List.of());

      List<SearchResponse> result = searchService.search("query", 0, 10, "https://example.com");

      assertThat(result).isEmpty();
      verify(elasticsearchOperations).search(any(Query.class), eq(PageDocument.class));
    }
  }

  @Nested
  @DisplayName("search history")
  class SearchHistoryTests {

    @Test
    void searchSavesHistoryForAuthenticatedUser() {
      User user = User.builder()
          .email("test@test.com")
          .password("encodedPassword")
          .role(Role.USER)
          .build();
      authenticateAs(user);
      stubElasticsearch(List.of());

      searchService.search("query", 0, 10, null);

      verify(searchHistoryRepository).save(any(SearchHistory.class));
    }

    @Test
    void searchDoesNotSaveHistoryForAnonymousUser() {
      SecurityContextHolder.clearContext();
      stubElasticsearch(List.of());

      searchService.search("query", 0, 10, null);

      verify(searchHistoryRepository, never()).save(any(SearchHistory.class));
    }

    @Test
    void searchSavesCorrectQueryAndDomainInHistory() {
      User user = User.builder()
          .email("test@test.com")
          .password("encodedPassword")
          .role(Role.USER)
          .build();
      authenticateAs(user);
      stubElasticsearch(List.of());

      searchService.search("my query", 0, 10, "https://example.com");

      verify(searchHistoryRepository).save(org.mockito.ArgumentMatchers.argThat(history ->
          history.getQuery().equals("my query") &&
          history.getDomain().equals("https://example.com") &&
          history.getUser().getEmail().equals("test@test.com")
      ));
    }
  }
}
