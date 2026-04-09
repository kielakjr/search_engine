package com.kielakjr.search_engine.health;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class HealthController {
  private final HealthService healthService;

  @GetMapping("/api/health")
  public ResponseEntity<HealthResponse> health() {
    HealthResponse response = healthService.checkHealth();
    int status = "UP".equals(response.getStatus()) ? 200 : 503;
    return ResponseEntity.status(status).body(response);
  }
}
