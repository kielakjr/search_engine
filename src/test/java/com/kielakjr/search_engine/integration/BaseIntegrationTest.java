package com.kielakjr.search_engine.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

import com.kielakjr.search_engine.search.PageRepository;
import com.kielakjr.search_engine.search.SearchService;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("resource")
public abstract class BaseIntegrationTest {

  static final PostgreSQLContainer<?> postgres;
  static final GenericContainer<?> redis;

  static {
    postgres = new PostgreSQLContainer<>("postgres:17-alpine");
    postgres.start();

    redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);
    redis.start();
  }

  @MockitoBean
  protected ElasticsearchClient elasticsearchClient;

  @MockitoBean
  protected ElasticsearchOperations elasticsearchOperations;

  @MockitoBean
  protected PageRepository pageRepository;

  @MockitoBean
  protected SearchService searchService;

  @Autowired
  protected MockMvc mockMvc;

  protected final ObjectMapper objectMapper = new ObjectMapper();

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.data.redis.host", redis::getHost);
    registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    registry.add("app.jwt.secret", () -> "dGVzdC1zZWNyZXQta2V5LXRoYXQtaXMtbG9uZy1lbm91Z2gtZm9yLWhzMjU2LWFsZ29yaXRobQ==");
    registry.add("app.jwt.expiration-ms", () -> "3600000");
    registry.add("spring.elasticsearch.uris", () -> "http://localhost:19200");
  }
}
