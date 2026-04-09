package com.kielakjr.search_engine.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI openAPI() {
    String schemeName = "Bearer Auth";

    return new OpenAPI()
        .info(new Info()
            .title("Search Engine API")
            .description("Full-text search engine with web crawling, JWT authentication, and Elasticsearch")
            .version("1.0"))
        .addSecurityItem(new SecurityRequirement().addList(schemeName))
        .schemaRequirement(schemeName, new SecurityScheme()
            .name(schemeName)
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT"));
  }
}
