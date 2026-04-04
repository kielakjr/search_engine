package com.kielakjr.search_engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.kielakjr.search_engine.config.CrawlerProperties;
import com.kielakjr.search_engine.config.JwtProperties;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({CrawlerProperties.class, JwtProperties.class})
public class SearchEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(SearchEngineApplication.class, args);
	}

}
