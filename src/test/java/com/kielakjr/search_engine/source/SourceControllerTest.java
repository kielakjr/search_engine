package com.kielakjr.search_engine.source;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(SourceController.class)
@AutoConfigureMockMvc(addFilters = false)
public class SourceControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private SourceService sourceService;

  @MockitoBean
  private com.kielakjr.search_engine.auth.JwtService jwtService;

  @MockitoBean
  private com.kielakjr.search_engine.auth.UserRepository userRepository;

  private final ObjectMapper objectMapper = new ObjectMapper();

  private SourceResponse sourceResponse;
  private SourceRequest sourceRequest;

  @BeforeEach
  void setUp() {
    sourceResponse = SourceResponse.builder()
        .id(1L)
        .url("https://example.com")
        .build();

    sourceRequest = new SourceRequest("example", "https://example.com");
  }

  @Nested
  @DisplayName("GET /api/sources")
  class GetAllSourcesTests {

    @Test
    void getAllSourcesReturns200WithSources() throws Exception {
      when(sourceService.getAllSources()).thenReturn(List.of(sourceResponse));

      mockMvc.perform(get("/api/sources"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$[0].id").value(1))
          .andExpect(jsonPath("$[0].url").value("https://example.com"));
    }

    @Test
    void getAllSourcesReturns200WithEmptyList() throws Exception {
      when(sourceService.getAllSources()).thenReturn(List.of());

      mockMvc.perform(get("/api/sources"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").isEmpty());
    }
  }

  @Nested
  @DisplayName("POST /api/sources")
  class CreateSourceTests {

    @Test
    void createSourceReturns201WithCreatedSource() throws Exception {
      when(sourceService.createSource(any(SourceRequest.class))).thenReturn(sourceResponse);

      mockMvc.perform(post("/api/sources")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(sourceRequest)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id").value(1))
          .andExpect(jsonPath("$.url").value("https://example.com"));
    }

    @Test
    void createSourceReturns400WhenBodyIsMissing() throws Exception {
      mockMvc.perform(post("/api/sources")
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isBadRequest());
    }

    @Test
    void createSourceReturns400WhenUrlIsInvalid() throws Exception {
      SourceRequest invalidRequest = new SourceRequest("example", "not-a-url");

      mockMvc.perform(post("/api/sources")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(invalidRequest)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void createSourceReturns400WhenSourceAlreadyExists() throws Exception {
      when(sourceService.createSource(any(SourceRequest.class)))
          .thenThrow(new IllegalArgumentException("Source already exists"));

      mockMvc.perform(post("/api/sources")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(sourceRequest)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void createSourceReturns400WhenNameIsMissing() throws Exception {
      SourceRequest invalidRequest = new SourceRequest("", "https://example.com");

      mockMvc.perform(post("/api/sources")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(invalidRequest)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void createSourceReturns400WhenUrlIsMissing() throws Exception {
      SourceRequest invalidRequest = new SourceRequest("example", "");

      mockMvc.perform(post("/api/sources")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(invalidRequest)))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("DELETE /api/sources/{id}")
  class DeleteSourceTests {

    @Test
    void deleteSourceReturns204WhenSourceExists() throws Exception {
      mockMvc.perform(delete("/api/sources/1"))
          .andExpect(status().isNoContent());

      verify(sourceService).deleteSource(1L);
    }

    @Test
    void deleteSourceReturns400WhenIdIsNotANumber() throws Exception {
      mockMvc.perform(delete("/api/sources/notanumber"))
          .andExpect(status().isBadRequest());
    }

    @Test
    void deleteSourceReturns400WhenSourceNotFound() throws Exception {
      doThrow(new IllegalArgumentException("Source not found"))
          .when(sourceService).deleteSource(anyLong());

      mockMvc.perform(delete("/api/sources/99"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.error").exists());
    }
  }
}
