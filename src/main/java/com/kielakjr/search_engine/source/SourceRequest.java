package com.kielakjr.search_engine.source;

import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SourceRequest {
  @NotBlank(message = "Name is required")
  private String name;
  @NotBlank(message = "URL is required")
  @URL(message = "Invalid URL format")
  private String url;
}
