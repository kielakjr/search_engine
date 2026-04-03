package com.kielakjr.search_engine.source;

import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.InjectMocks;
import java.util.List;
import java.util.Optional;
import static org.mockito.Mockito.verify;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class SourceServiceTest {
  @Mock
  private SourceRepository sourceRepository;

  @InjectMocks
  private SourceService sourceService;

  @Nested
  @DisplayName("getAllSources method")
  class GetAllSourcesTests {

    @Test
    void getAllSourcesWithNoSources() {
      when(sourceRepository.findAll()).thenReturn(List.of());
      List<SourceResponse> result = sourceService.getAllSources();
      assertTrue(result.isEmpty());
    }

    @Test
    void getAllSourcesWithSources() {
      Source source1 = Source.builder()
          .id(1L)
          .url("http://example.com")
          .name("Example")
          .active(true)
          .build();
      Source source2 = Source.builder()
          .id(2L)
          .url("http://example.org")
          .name("Example Org")
          .active(true)
          .build();
      when(sourceRepository.findAll()).thenReturn(List.of(source1, source2));
      List<SourceResponse> result = sourceService.getAllSources();
      assertTrue(result.size() == 2);
    }
  }

  @Nested
  @DisplayName("createSource method")
  class CreateSourceTests {
    @Test
    void createSourceWithExistingUrl() {
      SourceRequest request = new SourceRequest("Example", "http://example.com");
      when(sourceRepository.findByUrl("http://example.com")).thenReturn(Optional.of(new Source()));
      assertThatThrownBy(() -> sourceService.createSource(request))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Source with the same URL already exists");
    }

    @Test
    void createSourceWithNewUrl() {
      SourceRequest request = new SourceRequest("Example", "http://example.com");
      when(sourceRepository.findByUrl("http://example.com")).thenReturn(Optional.empty());
      Source savedSource = Source.builder()
          .id(1L)
          .url("http://example.com")
          .name("Example")
          .active(true)
          .build();
      Source toSaveSource = Source.builder()
          .url("http://example.com")
          .name("Example")
          .active(true)
          .build();
      when(sourceRepository.save(toSaveSource)).thenReturn(savedSource);
      SourceResponse result = sourceService.createSource(request);
      assertTrue(result.getId() == 1L);
      assertTrue(result.getUrl().equals("http://example.com"));
      assertTrue(result.getName().equals("Example"));
      assertTrue(result.isActive());
    }

  }

  @Nested
  @DisplayName("deleteSource method")
  class DeleteSourceTests {
    @Test
    void deleteSourceWithNonExistingId() {
      when(sourceRepository.findById(1L)).thenReturn(Optional.empty());
      assertThatThrownBy(() -> sourceService.deleteSource(1L))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Source not found");
    }

    @Test
    void deleteSourceWithExistingId() {
      Source source = Source.builder()
          .id(1L)
          .url("http://example.com")
          .name("Example")
          .active(true)
          .build();
      when(sourceRepository.findById(1L)).thenReturn(Optional.of(source));
      sourceService.deleteSource(1L);
      verify(sourceRepository).delete(source);
    }
  }
}
