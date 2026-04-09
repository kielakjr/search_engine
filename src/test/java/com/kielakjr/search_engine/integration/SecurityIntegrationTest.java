package com.kielakjr.search_engine.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import com.kielakjr.search_engine.auth.JwtService;
import com.kielakjr.search_engine.auth.Role;
import com.kielakjr.search_engine.auth.User;
import com.kielakjr.search_engine.auth.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;

@DisplayName("Security Integration Tests")
public class SecurityIntegrationTest extends BaseIntegrationTest {

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
  @DisplayName("Public endpoints")
  class PublicEndpoints {

    @Test
    void searchIsPublic() throws Exception {
      mockMvc.perform(get("/search").param("query", "test"))
          .andExpect(status().isOk());
    }

    @Test
    void healthIsPublic() throws Exception {
      mockMvc.perform(get("/api/health"))
          .andExpect(result -> {
            int code = result.getResponse().getStatus();
            assert code == 200 || code == 503 : "Expected 200 or 503 but got " + code;
          });
    }

    @Test
    void registerIsPublic() throws Exception {
      mockMvc.perform(post("/api/auth/register")
              .contentType(MediaType.APPLICATION_JSON)
              .content("{\"email\":\"new@test.com\",\"password\":\"pass\"}"))
          .andExpect(status().isCreated());
    }
  }

  @Nested
  @DisplayName("Protected endpoints")
  class ProtectedEndpoints {

    @Test
    void sourcesRequireAuth() throws Exception {
      mockMvc.perform(get("/api/sources"))
          .andExpect(status().isForbidden());
    }

    @Test
    void sourcesAccessibleWithToken() throws Exception {
      mockMvc.perform(get("/api/sources")
              .header("Authorization", "Bearer " + userToken))
          .andExpect(status().isOk());
    }

    @Test
    void noTokenReturnsForbidden() throws Exception {
      mockMvc.perform(get("/api/sources"))
          .andExpect(status().isForbidden());
    }
  }

  @Nested
  @DisplayName("Admin-only endpoints")
  class AdminEndpoints {

    @Test
    void userCannotCreateSource() throws Exception {
      mockMvc.perform(post("/api/sources")
              .header("Authorization", "Bearer " + userToken)
              .contentType(MediaType.APPLICATION_JSON)
              .content("{\"name\":\"Test\",\"url\":\"https://example.com\"}"))
          .andExpect(status().isForbidden());
    }

    @Test
    void adminCanCreateSource() throws Exception {
      mockMvc.perform(post("/api/sources")
              .header("Authorization", "Bearer " + adminToken)
              .contentType(MediaType.APPLICATION_JSON)
              .content("{\"name\":\"Test\",\"url\":\"https://example.com\"}"))
          .andExpect(status().isCreated());
    }

    @Test
    void userCannotStartCrawl() throws Exception {
      mockMvc.perform(post("/api/crawler/start/1")
              .header("Authorization", "Bearer " + userToken))
          .andExpect(status().isForbidden());
    }
  }
}
