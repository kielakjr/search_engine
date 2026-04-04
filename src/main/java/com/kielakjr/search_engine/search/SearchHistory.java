package com.kielakjr.search_engine.search;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.kielakjr.search_engine.auth.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "search_history")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchHistory {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String query;
  private String domain;
  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;
  @CreationTimestamp
  private LocalDateTime timestamp;
}
