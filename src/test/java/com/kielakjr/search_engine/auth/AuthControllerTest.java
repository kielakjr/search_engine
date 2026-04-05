package com.kielakjr.search_engine.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @MockitoBean
  private AuthService authService;

  @MockitoBean
  private JwtService jwtService;

  @MockitoBean
  private UserRepository userRepository;

  private UserRequest userRequest;

  @BeforeEach
  void setUp() {
    userRequest = new UserRequest("test@test.com", "password");
  }

  @Nested
  @DisplayName("POST /api/auth/register")
  class RegisterTests {

    @Test
    void registerReturns201WithUserResponse() throws Exception {
      UserResponse userResponse = UserResponse.builder()
          .email("test@test.com")
          .build();
      when(authService.register(any(UserRequest.class))).thenReturn(userResponse);

      mockMvc.perform(post("/api/auth/register")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(userRequest)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    void registerReturns400WhenEmailIsInvalid() throws Exception {
      UserRequest invalidRequest = new UserRequest("not-an-email", "password");

      mockMvc.perform(post("/api/auth/register")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(invalidRequest)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void registerReturns400WhenBodyIsMissing() throws Exception {
      mockMvc.perform(post("/api/auth/register")
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isBadRequest());
    }

    @Test
    void registerReturns400WhenEmailAlreadyExists() throws Exception {
      when(authService.register(any(UserRequest.class)))
          .thenThrow(new IllegalArgumentException("Email already exists"));

      mockMvc.perform(post("/api/auth/register")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(userRequest)))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("POST /api/auth/login")
  class LoginTests {

    @Test
    void loginReturns200WithToken() throws Exception {
      UserResponse userResponse = UserResponse.builder()
          .email("test@test.com")
          .build();
      AuthTokenResponse tokenResponse = AuthTokenResponse.builder()
          .token("jwtToken")
          .user(userResponse)
          .build();
      when(authService.login(any(UserRequest.class))).thenReturn(tokenResponse);

      mockMvc.perform(post("/api/auth/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(userRequest)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.token").value("jwtToken"))
          .andExpect(jsonPath("$.user.email").value("test@test.com"));
    }

    @Test
    void loginReturns400WhenEmailIsInvalid() throws Exception {
      UserRequest invalidRequest = new UserRequest("not-an-email", "password");

      mockMvc.perform(post("/api/auth/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(invalidRequest)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void loginReturns400WhenBodyIsMissing() throws Exception {
      mockMvc.perform(post("/api/auth/login")
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isBadRequest());
    }

    @Test
    void loginReturns400WhenCredentialsAreInvalid() throws Exception {
      when(authService.login(any(UserRequest.class)))
          .thenThrow(new IllegalArgumentException("Invalid email or password"));

      mockMvc.perform(post("/api/auth/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(userRequest)))
          .andExpect(status().isBadRequest());
    }
  }
}
