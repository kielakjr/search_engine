package com.kielakjr.search_engine.source;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import java.time.LocalDate;

@Entity
@Builder
@Table(name = "sources")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Source {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(unique = true, nullable = false)
  private String url;
  @Column(nullable = false)
  private String name;
  private boolean active;
  @CreationTimestamp
  private LocalDate createdAt;
}
