package com.kielakjr.search_engine.health;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.kielakjr.search_engine.auth.JwtService;
import com.kielakjr.search_engine.auth.UserRepository;
import com.kielakjr.search_engine.health.HealthResponse.ComponentHealth;

@WebMvcTest(HealthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class HealthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private HealthService healthService;

  @MockitoBean
  private JwtService jwtService;

  @MockitoBean
  private UserRepository userRepository;

  @Nested
  @DisplayName("GET /api/health")
  class HealthTests {

    @Test
    void returnsUpWhenAllServicesHealthy() throws Exception {
      Map<String, ComponentHealth> components = new LinkedHashMap<>();
      components.put("postgres", ComponentHealth.builder().status("UP").details("PostgreSQL 17.0").build());
      components.put("elasticsearch", ComponentHealth.builder().status("UP").details("cluster: test, status: green").build());
      components.put("redis", ComponentHealth.builder().status("UP").details("PONG").build());

      when(healthService.checkHealth()).thenReturn(
          HealthResponse.builder().status("UP").components(components).build());

      mockMvc.perform(get("/api/health"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.status").value("UP"))
          .andExpect(jsonPath("$.components.postgres.status").value("UP"))
          .andExpect(jsonPath("$.components.elasticsearch.status").value("UP"))
          .andExpect(jsonPath("$.components.redis.status").value("UP"));
    }

    @Test
    void returns503WhenAnyServiceDown() throws Exception {
      Map<String, ComponentHealth> components = new LinkedHashMap<>();
      components.put("postgres", ComponentHealth.builder().status("UP").details("PostgreSQL 17.0").build());
      components.put("elasticsearch", ComponentHealth.builder().status("DOWN").details("Connection refused").build());
      components.put("redis", ComponentHealth.builder().status("UP").details("PONG").build());

      when(healthService.checkHealth()).thenReturn(
          HealthResponse.builder().status("DOWN").components(components).build());

      mockMvc.perform(get("/api/health"))
          .andExpect(status().isServiceUnavailable())
          .andExpect(jsonPath("$.status").value("DOWN"))
          .andExpect(jsonPath("$.components.elasticsearch.status").value("DOWN"));
    }
  }
}
