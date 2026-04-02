package com.kielakjr.search_engine.source;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SourceRepository extends JpaRepository<Source, Long> {
  public Optional<Source> findByUrl(String url);
}
