package com.kielakjr.search_engine.source;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SourceResponse {
  private Long id;
  private String url;
  private String name;
  private boolean active;
  private LocalDate createdAt;
}
