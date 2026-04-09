package com.kielakjr.search_engine.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import com.kielakjr.search_engine.auth.UserRepository;
import com.kielakjr.search_engine.auth.UserRequest;

@DisplayName("Auth Integration Tests")
public class AuthIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private UserRepository userRepository;

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();
  }

  @Nested
  @DisplayName("Registration → Login flow")
  class RegistrationLoginFlow {

    @Test
    void registerAndLoginSuccessfully() throws Exception {
      UserRequest request = new UserRequest("test@example.com", "password123");

      mockMvc.perform(post("/api/auth/register")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.email").value("test@example.com"))
          .andExpect(jsonPath("$.role").value("USER"))
          .andExpect(jsonPath("$.id").isNumber());

      mockMvc.perform(post("/api/auth/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.token").isNotEmpty())
          .andExpect(jsonPath("$.user.email").value("test@example.com"));
    }

    @Test
    void registerDuplicateEmailReturns400() throws Exception {
      UserRequest request = new UserRequest("dup@example.com", "password123");

      mockMvc.perform(post("/api/auth/register")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated());

      mockMvc.perform(post("/api/auth/register")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void loginWithWrongPasswordReturns400() throws Exception {
      UserRequest register = new UserRequest("wrong@example.com", "correctpass");
      UserRequest login = new UserRequest("wrong@example.com", "wrongpass");

      mockMvc.perform(post("/api/auth/register")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(register)))
          .andExpect(status().isCreated());

      mockMvc.perform(post("/api/auth/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(login)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void loginWithNonExistentEmailReturns400() throws Exception {
      UserRequest login = new UserRequest("ghost@example.com", "password123");

      mockMvc.perform(post("/api/auth/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(login)))
          .andExpect(status().isBadRequest());
    }
  }
}
