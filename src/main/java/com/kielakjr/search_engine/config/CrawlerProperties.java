package com.kielakjr.search_engine.config;

import org.springframework.context.annotation.Configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "app.crawler")
@Data
public class CrawlerProperties {
  private int maxDepth;
  private int maxPages;
  private long requestDelayMs;
}
