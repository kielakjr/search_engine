package com.kielakjr.search_engine.crawler;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import com.kielakjr.search_engine.source.Source;

@Entity
@Builder
@Table(name = "crawl_jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrawlJob {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @ManyToOne
  private Source source;
  @Enumerated(EnumType.STRING)
  private CrawlStatus status;
  private int pagesFound;
  @CreationTimestamp
  private LocalDate createdAt;
}
