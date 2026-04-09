package com.kielakjr.search_engine.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;
import com.kielakjr.search_engine.auth.Role;
import com.kielakjr.search_engine.auth.User;
import com.kielakjr.search_engine.auth.UserRepository;
import com.kielakjr.search_engine.auth.JwtService;
import com.kielakjr.search_engine.source.SourceRepository;
import com.kielakjr.search_engine.source.SourceRequest;

import org.springframework.security.crypto.password.PasswordEncoder;

@DisplayName("Source Integration Tests")
public class SourceIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private SourceRepository sourceRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private JwtService jwtService;

  @Autowired
  private PasswordEncoder passwordEncoder;

  private String adminToken;
  private String userToken;

  @BeforeEach
  void setUp() {
    sourceRepository.deleteAll();
    userRepository.deleteAll();

    User admin = userRepository.save(User.builder()
        .email("admin@test.com")
        .password(passwordEncoder.encode("password"))
        .role(Role.ADMIN)
        .build());
    adminToken = jwtService.generateToken(admin);

    User user = userRepository.save(User.builder()
        .email("user@test.com")
        .password(passwordEncoder.encode("password"))
        .role(Role.USER)
        .build());
    userToken = jwtService.generateToken(user);
  }

  @Nested
  @DisplayName("Source CRUD with auth")
  class SourceCrudTests {

    @Test
    void adminCanCreateAndListSources() throws Exception {
      SourceRequest request = new SourceRequest("Example", "https://example.com");

      mockMvc.perform(post("/api/sources")
              .header("Authorization", "Bearer " + adminToken)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.name").value("Example"))
          .andExpect(jsonPath("$.url").value("https://example.com"))
          .andExpect(jsonPath("$.active").value(true));

      mockMvc.perform(get("/api/sources")
              .header("Authorization", "Bearer " + userToken))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$[0].url").value("https://example.com"));
    }

    @Test
    void regularUserCannotCreateSource() throws Exception {
      SourceRequest request = new SourceRequest("Example", "https://example.com");

      mockMvc.perform(post("/api/sources")
              .header("Authorization", "Bearer " + userToken)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedCannotListSources() throws Exception {
      mockMvc.perform(get("/api/sources"))
          .andExpect(status().isForbidden());
    }

    @Test
    void adminCanDeleteSource() throws Exception {
      SourceRequest request = new SourceRequest("ToDelete", "https://delete.com");

      MvcResult result = mockMvc.perform(post("/api/sources")
              .header("Authorization", "Bearer " + adminToken)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated())
          .andReturn();

      Integer id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");

      mockMvc.perform(delete("/api/sources/" + id)
              .header("Authorization", "Bearer " + adminToken))
          .andExpect(status().isNoContent());

      mockMvc.perform(get("/api/sources")
              .header("Authorization", "Bearer " + adminToken))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void duplicateUrlReturns400() throws Exception {
      SourceRequest request = new SourceRequest("First", "https://duplicate.com");

      mockMvc.perform(post("/api/sources")
              .header("Authorization", "Bearer " + adminToken)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated());

      SourceRequest duplicate = new SourceRequest("Second", "https://duplicate.com");

      mockMvc.perform(post("/api/sources")
              .header("Authorization", "Bearer " + adminToken)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(duplicate)))
          .andExpect(status().isBadRequest());
    }
  }
}
