package com.kielakjr.search_engine.health;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import lombok.RequiredArgsConstructor;

import com.kielakjr.search_engine.health.HealthResponse.ComponentHealth;

@Service
@RequiredArgsConstructor
public class HealthService {
  private final DataSource dataSource;
  private final ElasticsearchClient elasticsearchClient;
  private final RedisConnectionFactory redisConnectionFactory;

  public HealthResponse checkHealth() {
    Map<String, ComponentHealth> components = new LinkedHashMap<>();

    components.put("postgres", checkPostgres());
    components.put("elasticsearch", checkElasticsearch());
    components.put("redis", checkRedis());

    boolean allUp = components.values().stream()
        .allMatch(c -> "UP".equals(c.getStatus()));

    return HealthResponse.builder()
        .status(allUp ? "UP" : "DOWN")
        .components(components)
        .build();
  }

  private ComponentHealth checkPostgres() {
    try (var connection = dataSource.getConnection()) {
      if (connection.isValid(3)) {
        var meta = connection.getMetaData();
        return ComponentHealth.builder()
            .status("UP")
            .details(meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion())
            .build();
      }
      return ComponentHealth.builder().status("DOWN").details("Connection not valid").build();
    } catch (Exception e) {
      return ComponentHealth.builder().status("DOWN").details(e.getMessage()).build();
    }
  }

  private ComponentHealth checkElasticsearch() {
    try {
      var response = elasticsearchClient.cluster().health();
      return ComponentHealth.builder()
          .status("UP")
          .details("cluster: " + response.clusterName() + ", status: " + response.status())
          .build();
    } catch (Exception e) {
      return ComponentHealth.builder().status("DOWN").details(e.getMessage()).build();
    }
  }

  private ComponentHealth checkRedis() {
    try (var connection = redisConnectionFactory.getConnection()) {
      String pong = connection.commands().ping();
      return ComponentHealth.builder().status("UP").details(pong).build();
    } catch (Exception e) {
      return ComponentHealth.builder().status("DOWN").details(e.getMessage()).build();
    }
  }
}
