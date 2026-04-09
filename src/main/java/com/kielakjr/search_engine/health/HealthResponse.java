package com.kielakjr.search_engine.health;

import java.util.Map;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class HealthResponse {
  private String status;
  private Map<String, ComponentHealth> components;

  @Builder
  @Getter
  public static class ComponentHealth {
    private String status;
    private String details;
  }
}
